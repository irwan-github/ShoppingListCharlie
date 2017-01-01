package com.mirzairwan.shopping.data;


import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.mirzairwan.shopping.PictureMgr;
import com.mirzairwan.shopping.R;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ToBuyItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mirza Irwan on 18/12/16.
 */

public class DaoContentProv implements DaoManager
{
    private static final String LOG_TAG = DaoContentProv.class.getSimpleName();
    private static final String FILE_PROVIDER = "fileprovider";

    private Context mContext;

    public DaoContentProv(Context context)
    {
        mContext = context;
    }

    @Override
    public String update(long buyItemId, boolean isChecked)
    {
        ContentValues values = new ContentValues();
        values.put(ToBuyItemsEntry.COLUMN_IS_CHECKED, isChecked ? 1 : 0);
        Uri updateBuyItemUri = ContentUris.withAppendedId(ToBuyItemsEntry.CONTENT_URI, buyItemId);
        int updated = mContext.getContentResolver().update(updateBuyItemUri, values, null, null);
        if (updated == 1)
            return mContext.getString(R.string.database_success);
        else
            return mContext.getString(R.string.database_failed);
    }

    @Override
    public long insert(ToBuyItem buyItem)
    {
        ContentValues values = getBuyItemContentValues(buyItem, new Date());
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri result = contentResolver.insert(ToBuyItemsEntry.CONTENT_URI, values);
        return ContentUris.parseId(result);
    }

    @Override
    public String insert(ToBuyItem buyItem, Item item, List<Price> itemPrices, PictureMgr pictureMgr)
    {
        Log.d(LOG_TAG, "Save domain object graph");
        String msg = "";
        ContentProviderResult[] results = null;
        Date updateTime = new Date();

        ContentValues itemValues = new ContentValues();
        itemValues = getItemContentValues(item, updateTime, itemValues);

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder itemBuilder =
                ContentProviderOperation.newInsert(ItemsEntry.CONTENT_URI);

        ContentProviderOperation itemInsertOp = itemBuilder.withValues(itemValues).build();

        ops.add(itemInsertOp);

        //insert picture paths
        int opSavePictureIdx = -1;
        for (Picture picture : pictureMgr.getPictureForSaving()) {
            ContentProviderOperation.Builder insertPicPathBuilder = ContentProviderOperation.newInsert(PicturesEntry.CONTENT_URI);
            insertPicPathBuilder.withValueBackReference(PicturesEntry.COLUMN_ITEM_ID, 0);
            insertPicPathBuilder.withValue(PicturesEntry.COLUMN_FILE_PATH, picture.getPicturePath());
            insertPicPathBuilder.withValue(PicturesEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());
            ops.add(insertPicPathBuilder.build());
            opSavePictureIdx = ops.size() - 1;
        }

        for (int j = 0; j < itemPrices.size(); ++j) {
            Price price = itemPrices.get(j);
            ContentProviderOperation.Builder priceBuilder =
                    ContentProviderOperation.newInsert(PricesEntry.CONTENT_URI);

            long itemId = -1; //The item id does not exist at this point.
            ContentValues priceContentValues = getPriceContentValues(price, itemId, updateTime, null);

            priceBuilder = priceBuilder.withValues(priceContentValues).
                    withValueBackReference(PricesEntry.COLUMN_ITEM_ID, 0);

            ops.add(priceBuilder.build());

            if (buyItem.getSelectedPriceType() == price.getPriceType()) {
                ContentProviderOperation.Builder buyItemBuilder =
                        ContentProviderOperation.newInsert(ToBuyItemsEntry.CONTENT_URI);

                buyItemBuilder = buyItemBuilder.withValues(getBuyItemContentValues(buyItem, updateTime))
                        .withValueBackReference(ToBuyItemsEntry.COLUMN_ITEM_ID, 0);

                buyItemBuilder = buyItemBuilder
                        .withValueBackReference(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, ops.size() - 1);

                ContentProviderOperation opBuyItem = buyItemBuilder.build();
                ops.add(opBuyItem);

            }
        }

        try {
            results = mContext.getContentResolver()
                    .applyBatch(Contract.CONTENT_AUTHORITY, ops);
            msg = mContext.getString(R.string.database_success);
        } catch (RemoteException e) {
            msg = mContext.getString(R.string.database_failed);
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            msg = mContext.getString(R.string.database_failed);
            e.printStackTrace();
        }

        logDbOperation(results);

        if (opSavePictureIdx > -1) {
            if (results[opSavePictureIdx].uri != null || results[opSavePictureIdx].count == 1) {
                String deletePicMsg = cleanUpDiscardedPictures(results[opSavePictureIdx], pictureMgr);
                if (deletePicMsg != null)
                    msg += "\n" + deletePicMsg;
            } else //Original picture failed to be removed
            {
                pictureMgr.setViewOriginalPicture(); //Set PictureMgr to show original picture for viewing
            }
        }

        return msg;
    }

    @Override
    public String update(ToBuyItem buyItem, Item item, List<Price> itemPrices, PictureMgr pictureMgr)
    {
        Log.d(LOG_TAG, "Save domain object graph");
        String msg = "";
        ContentProviderResult[] results = null;
        Date updateTime = new Date();

        ContentValues itemValues = getItemContentValues(item, updateTime, null);

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();

        Uri updateItemUri = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, item.getId());
        ContentProviderOperation.Builder itemBuilder =
                ContentProviderOperation.newUpdate(updateItemUri);

        ContentProviderOperation itemUpdateOp = itemBuilder.withValues(itemValues).build();

        ops.add(itemUpdateOp);

        int opSavePictureIdx = -1;
        opSavePictureIdx = preparePictureForItemUpdateOps(pictureMgr, ops, updateTime);

        for (int j = 0; j < itemPrices.size(); ++j) {
            Price price = itemPrices.get(j);
            Uri updatePriceUri = ContentUris.withAppendedId(PricesEntry.CONTENT_URI, price.getId());
            ContentProviderOperation.Builder priceBuilder =
                    ContentProviderOperation.newUpdate(updatePriceUri);

            ContentValues priceContentValues = getPriceContentValues(price, item.getId(), updateTime, null);

            priceBuilder = priceBuilder.withValues(priceContentValues);

            ops.add(priceBuilder.build());
        }

        Uri updateBuyItemUri = ContentUris.withAppendedId(ToBuyItemsEntry.CONTENT_URI, buyItem.getId());
        ContentProviderOperation.Builder buyItemBuilder =
                ContentProviderOperation.newUpdate(updateBuyItemUri);

        buyItemBuilder = buyItemBuilder.withValues(getBuyItemContentValues(buyItem, updateTime));

        ContentProviderOperation opBuyItem = buyItemBuilder.build();
        ops.add(opBuyItem);

        try {
            results = mContext.getContentResolver()
                    .applyBatch(Contract.CONTENT_AUTHORITY, ops);
            msg = mContext.getString(R.string.database_success);
        } catch (RemoteException e) {
            e.printStackTrace();
            msg = mContext.getString(R.string.database_failed);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            msg = mContext.getString(R.string.database_failed);
        }

        logDbOperation(results);

        if (opSavePictureIdx > -1) {

            String discardFileMsg = cleanUpDiscardedPictures(results[opSavePictureIdx], pictureMgr);
            if (discardFileMsg != null)
                msg += "\n" + discardFileMsg;

        }

        return msg;
    }

    public int deleteFileFromFilesystem(File file)
    {
        String authority = mContext.getApplicationInfo().packageName + "." + FILE_PROVIDER;
        Uri uriFile = FileProvider.getUriForFile(mContext, authority, file);
        int deletePictureFile = mContext.getContentResolver().delete(uriFile, null, null);
//        Log.d(LOG_TAG, "Delete picture " + uriFile.toString() + " : " +
//                (deletePictureFile > 0 ? "ok" : "failed"));
        return deletePictureFile;
    }

    public void logDeleteDiscardedFileFromFilesystem(int deletedFiles, String prefix)
    {
        String msg = prefix + " : " + (deletedFiles > 0 ? "OK" : "Failed");
        Log.d(LOG_TAG, msg);

    }

    @Override
    public String cleanUpDiscardedPictures(PictureMgr pictureMgr)
    {
        String msg = null;
        int deleteDiscardedFile = 0;
        int discardPicturesSize = pictureMgr.getDiscardedPictures().size();

        Iterator<Picture> iteratorDiscardPics = pictureMgr.getDiscardedPictures().iterator();
        int externalFileCount = 0;
        while (iteratorDiscardPics.hasNext()) {

            Picture discardedPicture = iteratorDiscardPics.next();

            if (!pictureMgr.isExternalFile(discardedPicture) && discardedPicture.getFile() != null) //Do not delete file NOT owned by app
                deleteDiscardedFile += deleteFileFromFilesystem(discardedPicture.getFile());
            else
                ++externalFileCount; //Keep count of external file to give the correct deletion report

            if (deleteDiscardedFile == 1) {
                iteratorDiscardPics.remove(); //remove external and internal pictures from discarded list
            }

            logDeleteDiscardedFileFromFilesystem(deleteDiscardedFile, "Delete discarded file");

        }

        if (discardPicturesSize - externalFileCount > 0) //Do not count external file because they are not deleted
            msg = (deleteDiscardedFile == discardPicturesSize - externalFileCount ?
                    mContext.getString(R.string.filesystem_delete_discarded_ok) :
                    mContext.getString(R.string.filesystem_delete_discarded_failed));

        return msg;

    }

    protected String cleanUpDiscardedPictures(ContentProviderResult result, PictureMgr pictureMgr)
    {
        if (result.uri == null && result.count == 0) {
            pictureMgr.setViewOriginalPicture();
        }

        return cleanUpDiscardedPictures(pictureMgr);
    }

    /**
     * It will not do database operation if picture last viewed is same as picture in database
     *
     * @param pictureMgr
     * @param ops
     * @param updateTime
     * @return
     */
    protected int preparePictureForItemUpdateOps(PictureMgr pictureMgr, ArrayList<ContentProviderOperation> ops, Date updateTime)
    {
        int opSavePictureIdx = -1; //Start with picture to make it easier to do FileProvider operation

        Picture pictureLastViewed = pictureMgr.getPictureForViewing();
        Picture originalPicture = pictureMgr.getOriginalPicture();
        if (originalPicture != null && pictureLastViewed != originalPicture) { //Update item's picture operation
            Uri updatePictureUri = ContentUris.withAppendedId(PicturesEntry.CONTENT_URI, originalPicture.getId());
            ContentProviderOperation.Builder updatePictureBuilder = ContentProviderOperation.newUpdate(updatePictureUri);
            updatePictureBuilder.withValue(PicturesEntry.COLUMN_FILE_PATH, pictureLastViewed.getPicturePath());
            updatePictureBuilder.withValue(PicturesEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());
            ops.add(updatePictureBuilder.build());
            opSavePictureIdx = ops.size() - 1;
        } else if (originalPicture == null && pictureLastViewed != null) { //Insert item's picture operation
            Uri insertPictureUri = PicturesEntry.CONTENT_URI;
            ContentProviderOperation.Builder insertPictureBuilder = ContentProviderOperation.newInsert(insertPictureUri);
            insertPictureBuilder.withValue(PicturesEntry.COLUMN_FILE_PATH, pictureLastViewed.getPicturePath());

            if (pictureMgr.getItemId() < 1) {
                throw new IllegalArgumentException("Picture with item id " + pictureMgr.getItemId());
            }

            insertPictureBuilder.withValue(PicturesEntry.COLUMN_ITEM_ID, pictureMgr.getItemId());
            insertPictureBuilder.withValue(PicturesEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());
            ops.add(insertPictureBuilder.build());
            opSavePictureIdx = ops.size() - 1;
        }

        return opSavePictureIdx;
    }

    @Override
    public String update(Item item, List<Price> prices, PictureMgr pictureMgr)
    {
        String msg;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        Date updateTime = new Date();
        int opSavePictureIdx = -1; //Start with picture to make it easier to do FileProvider operation

        opSavePictureIdx = preparePictureForItemUpdateOps(pictureMgr, ops, updateTime);

        Uri updateItemUri = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, item.getId());
        ContentProviderOperation.Builder updateItemBuilder = ContentProviderOperation.newUpdate(updateItemUri);
        updateItemBuilder.withValues(getItemContentValues(item, updateTime, null));
        ops.add(updateItemBuilder.build());

        for (Price price : prices) {
            Uri updatePriceUri = ContentUris.withAppendedId(PricesEntry.CONTENT_URI, price.getId());
            ContentProviderOperation.Builder updatePriceBuilder = ContentProviderOperation.newUpdate(updatePriceUri);
            updatePriceBuilder.withValues(getPriceContentValues(price, item.getId(), updateTime, null));
            ops.add(updatePriceBuilder.build());
        }

        ContentProviderResult[] results = null;
        try {
            results = mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, ops);
            msg = mContext.getString(R.string.database_success);
        } catch (RemoteException e) {
            msg = mContext.getString(R.string.database_failed);
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            msg = mContext.getString(R.string.database_failed);
            e.printStackTrace();
        }

        logDbOperation(results);

        if (opSavePictureIdx > -1) {
            String msgDiscardPics = cleanUpDiscardedPictures(results[opSavePictureIdx], pictureMgr);
            if (msgDiscardPics != null)
                msg += "\n" + msgDiscardPics;
        }

        return msg;
    }

    protected void logDbOperation(ContentProviderResult[] results)
    {
        if (results == null) {
            Log.d(LOG_TAG, "Database return results is NULL.");
            return;
        }

        String dbLog = "";
        for (int idx = 0; idx < results.length; ++idx) {
            if (idx == 0)
                dbLog = "Saved : " + (results[idx].count != null ? results[idx].count : results[idx].uri.toString());
            else
                dbLog += "\nSaved : " + ((results[idx].count != null) ? results[idx].count : results[idx].uri.toString());
        }

        Log.d(LOG_TAG, dbLog);

    }

    @Override
    public int delete(ToBuyItem buyItem)
    {
        Uri uriDeleteBuyItem = ContentUris.withAppendedId(ToBuyItemsEntry.CONTENT_URI,
                buyItem.getId());
        return mContext.getContentResolver().delete(uriDeleteBuyItem, null, null);
    }

    /**
     * Delete records in the following sequence:
     * 1. The Item's picture
     * 2. The Item's prices
     * 3. The Item,
     *
     * @param item
     * @return
     */
    @Override
    public String delete(Item item, PictureMgr pictureMgr)
    {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String msg = null;

        //Delete picture(s) Make it the first so that it is easier to track for FileProvider
        int deletePictureOpIndex = 0;
        Uri uriDeletePicture = PicturesEntry.CONTENT_URI;
        ContentProviderOperation.Builder pictureDeleteBuilder = ContentProviderOperation
                .newDelete(uriDeletePicture);
        pictureDeleteBuilder.withSelection(PicturesEntry.COLUMN_ITEM_ID + "=?",
                new String[]{String.valueOf(item.getId())});
        ops.add(pictureDeleteBuilder.build());

        //Delete prices
        Uri uriDeletePrice = PricesEntry.CONTENT_URI;
        ContentProviderOperation.Builder deletePriceBuilder =
                ContentProviderOperation.newDelete(uriDeletePrice);
        deletePriceBuilder.withSelection(PricesEntry.COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(item.getId())});
        ops.add(deletePriceBuilder.build());


        //Delete item
        Uri uriDeleteItem = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, item.getId());
        ContentProviderOperation.Builder itemDeleteBuilder = ContentProviderOperation.newDelete(uriDeleteItem);
        ops.add(itemDeleteBuilder.build());

        ContentProviderResult[] contentProviderResults = null;
        try {
            contentProviderResults = mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, ops);
            msg = mContext.getString(R.string.database_success);
        } catch (RemoteException e) {
            msg = mContext.getString(R.string.database_failed);
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            msg = mContext.getString(R.string.database_failed);
            e.printStackTrace();
        }

        pictureMgr.discardOriginalPicture(); // Without this, the original file in the filesystem will NOT be deleted.

        String msgCleanUp = cleanUpDiscardedPictures(contentProviderResults[deletePictureOpIndex], pictureMgr);

        if (msgCleanUp != null)
            msg += "\n" + msgCleanUp;

        return msg;
    }


    private ContentValues getItemContentValues(Item item, Date updateTime, ContentValues values)
    {
        if (values == null)
            values = new ContentValues();
        values.put(ItemsEntry.COLUMN_NAME, item.getName());
        values.put(ItemsEntry.COLUMN_BRAND, item.getBrand());
        values.put(ItemsEntry.COLUMN_COUNTRY_ORIGIN, item.getCountryOrigin());
        values.put(ItemsEntry.COLUMN_DESCRIPTION, item.getDescription());
        values.put(ItemsEntry.COLUMN_COUNTRY_ORIGIN, item.getCountryOrigin());

        if (updateTime != null)
            values.put(ItemsEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());

        return values;
    }

    private ContentValues getPriceContentValues(Price price, long itemId, Date updateTime, ContentValues values)
    {
        ContentValues priceValues = values;
        if (priceValues == null)
            priceValues = new ContentValues();


        if (price.getPriceType() == Price.Type.UNIT_PRICE) {
            priceValues.put(PricesEntry.COLUMN_PRICE, (long) (price.getUnitPrice() * 100));
            priceValues.put(PricesEntry.COLUMN_PRICE_TYPE_ID, Price.Type.UNIT_PRICE.getType());
        } else {
            priceValues.put(PricesEntry.COLUMN_PRICE, (long) (price.getBundlePrice() * 100));
            priceValues.put(PricesEntry.COLUMN_BUNDLE_QTY,
                    (long) (price.getBundleQuantity() * 100));
            priceValues.put(PricesEntry.COLUMN_PRICE_TYPE_ID, Price.Type.BUNDLE_PRICE.getType());
        }

        int shopId = 1;
        priceValues.put(PricesEntry.COLUMN_SHOP_ID, shopId);

        if (itemId > 0)
            priceValues.put(PricesEntry.COLUMN_ITEM_ID, itemId);

        if (updateTime != null)
            priceValues.put(PricesEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());

        priceValues.put(PricesEntry.COLUMN_CURRENCY_CODE, price.getCurrencyCode());
        return priceValues;
    }

    private ContentValues getBuyItemContentValues(ToBuyItem buyItem, Date updateTime)
    {
        ContentValues buyItemValues = new ContentValues();
        if (buyItem.getItem().getId() > 0)
            buyItemValues.put(ToBuyItemsEntry.COLUMN_ITEM_ID, buyItem.getItem().getId());
        buyItemValues.put(ToBuyItemsEntry.COLUMN_QUANTITY, buyItem.getQuantity());
        if (buyItem.getSelectedPrice().getId() > 0)
            buyItemValues.put(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID,
                    buyItem.getSelectedPrice().getId());
        buyItemValues.put(ToBuyItemsEntry.COLUMN_IS_CHECKED, buyItem.isChecked());
        buyItemValues.put(ToBuyItemsEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());
        return buyItemValues;
    }
}
