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
import android.util.Log;

import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ToBuyItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mirza Irwan on 18/12/16.
 */

public class DaoContentProv implements DaoManager
{
    private static final String LOG_TAG = DaoContentProv.class.getSimpleName();
    private Context mContext;

    public DaoContentProv(Context context)
    {
        mContext = context;
    }

    @Override
    public int update(long buyItemId, boolean isChecked)
    {
        ContentValues values = new ContentValues();
        values.put(ToBuyItemsEntry.COLUMN_IS_CHECKED, isChecked ? 1 : 0);
        Uri updateBuyItemUri = ContentUris.withAppendedId(ToBuyItemsEntry.CONTENT_URI, buyItemId);
        return mContext.getContentResolver().update(updateBuyItemUri, values, null, null);
    }

    @Override
    public String insert(ToBuyItem buyItem)
    {
        ContentValues values = getBuyItemContentValues(buyItem, new Date());
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri result = contentResolver.insert(ToBuyItemsEntry.CONTENT_URI, values);
        return result.toString();
    }

    @Override
    public String insert(ToBuyItem buyItem, Item item, List<Price> itemPrices, List<Picture> picturesPath)
    {
        Log.d(LOG_TAG, "Save domain object graph");
        String msg = "";
        ContentProviderResult[] result;
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
        for(Picture path : picturesPath) {
            ContentProviderOperation.Builder insertPicPathBuilder = ContentProviderOperation.newInsert(PicturesEntry.CONTENT_URI);
            insertPicPathBuilder.withValueBackReference(PicturesEntry.COLUMN_ITEM_ID, 0);
            insertPicPathBuilder.withValue(PicturesEntry.COLUMN_FILE_PATH, path.getPicturePath());
            insertPicPathBuilder.withValue(PicturesEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());
            ops.add(insertPicPathBuilder.build());
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
        }

        ContentProviderOperation.Builder buyItemBuilder =
                ContentProviderOperation.newInsert(ToBuyItemsEntry.CONTENT_URI);

        buyItemBuilder = buyItemBuilder.withValues(getBuyItemContentValues(buyItem, updateTime))
                .withValueBackReference(ToBuyItemsEntry.COLUMN_ITEM_ID, 0);

        for (int k = 0; k < itemPrices.size(); ++k) {
            Price price = itemPrices.get(k);
            Price.Type selectedPriceType = buyItem.getSelectedPriceType();
            if (selectedPriceType == price.getPriceType())
                buyItemBuilder = buyItemBuilder
                        .withValueBackReference(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, k + 1);
        }

        ContentProviderOperation opBuyItem = buyItemBuilder.build();
        ops.add(opBuyItem);

        try {
            result = mContext.getContentResolver()
                    .applyBatch(Contract.CONTENT_AUTHORITY, ops);
            msg = result.toString();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        return msg;
    }


    @Override
    public String insert(ToBuyItem buyItem, Item item, List<Price> itemPrices)
    {
        Log.d(LOG_TAG, "Save domain object graph");
        String msg = "";
        ContentProviderResult[] result;
        Date updateTime = new Date();

        ContentValues itemValues = new ContentValues();
        itemValues = getItemContentValues(item, updateTime, itemValues);

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder itemBuilder =
                ContentProviderOperation.newInsert(ItemsEntry.CONTENT_URI);

        ContentProviderOperation itemInsertOp = itemBuilder.withValues(itemValues).build();

        ops.add(itemInsertOp);

        for (int j = 0; j < itemPrices.size(); ++j) {
            Price price = itemPrices.get(j);
            ContentProviderOperation.Builder priceBuilder =
                    ContentProviderOperation.newInsert(PricesEntry.CONTENT_URI);

            long itemId = -1; //The item id does not exist at this point.
            ContentValues priceContentValues = getPriceContentValues(price, itemId, updateTime, null);

            priceBuilder = priceBuilder.withValues(priceContentValues).
                    withValueBackReference(PricesEntry.COLUMN_ITEM_ID, 0);

            ops.add(priceBuilder.build());
        }

        ContentProviderOperation.Builder buyItemBuilder =
                ContentProviderOperation.newInsert(ToBuyItemsEntry.CONTENT_URI);

        buyItemBuilder = buyItemBuilder.withValues(getBuyItemContentValues(buyItem, updateTime))
                .withValueBackReference(ToBuyItemsEntry.COLUMN_ITEM_ID, 0);

        for (int k = 0; k < itemPrices.size(); ++k) {
            Price price = itemPrices.get(k);
            Price.Type selectedPriceType = buyItem.getSelectedPriceType();
            if (selectedPriceType == price.getPriceType())
                buyItemBuilder = buyItemBuilder
                        .withValueBackReference(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, k + 1);
        }

        ContentProviderOperation opBuyItem = buyItemBuilder.build();
        ops.add(opBuyItem);

        try {
            result = mContext.getContentResolver()
                    .applyBatch(Contract.CONTENT_AUTHORITY, ops);
            msg = result.toString();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        return msg;
    }

    @Override
    public String update(Item item, List<Price> prices)
    {
        String msg = null;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        Date updateTime = new Date();

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
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        msg = String.valueOf(results.length + " updated");
        return msg;
    }

    @Override
    public ContentProviderResult[] update(Item item, List<Price> prices, List<Picture> pictures)
    {
        String msg = null;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        Date updateTime = new Date();

        Uri updateItemUri = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, item.getId());
        ContentProviderOperation.Builder updateItemBuilder = ContentProviderOperation.newUpdate(updateItemUri);
        updateItemBuilder.withValues(getItemContentValues(item, updateTime, null));
        ops.add(updateItemBuilder.build());

        for(Picture itemPic : pictures)
        {
            if(itemPic.getId() > 0)
            {
                Uri updatePictureUri = ContentUris.withAppendedId(PicturesEntry.CONTENT_URI, itemPic.getId());
                ContentProviderOperation.Builder updatePictureBuilder = ContentProviderOperation.newUpdate(updatePictureUri);
                updatePictureBuilder.withValue(PicturesEntry.COLUMN_FILE_PATH, itemPic.getPicturePath());
                updatePictureBuilder.withValue(PicturesEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());
                ops.add(updatePictureBuilder.build());
            }
            else
            {
                Uri insertPictureUri = PicturesEntry.CONTENT_URI;
                ContentProviderOperation.Builder insertPictureBuilder = ContentProviderOperation.newInsert(insertPictureUri);
                insertPictureBuilder.withValue(PicturesEntry.COLUMN_FILE_PATH, itemPic.getPicturePath());
                insertPictureBuilder.withValue(PicturesEntry.COLUMN_ITEM_ID, item.getId());
                insertPictureBuilder.withValue(PicturesEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());
                ops.add(insertPictureBuilder.build());

            }

        }

        for (Price price : prices) {
            Uri updatePriceUri = ContentUris.withAppendedId(PricesEntry.CONTENT_URI, price.getId());
            ContentProviderOperation.Builder updatePriceBuilder = ContentProviderOperation.newUpdate(updatePriceUri);
            updatePriceBuilder.withValues(getPriceContentValues(price, item.getId(), updateTime, null));
            ops.add(updatePriceBuilder.build());
        }

        ContentProviderResult[] results = null;
        try {
            results = mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        msg = String.valueOf(results.length + " updated");
        return results;
    }

    @Override
    public String update(ToBuyItem buyItem, Item item, List<Price> itemPrices)
    {
        Log.d(LOG_TAG, "Save domain object graph");
        String msg = "";
        ContentProviderResult[] result;
        Date updateTime = new Date();

        ContentValues itemValues = getItemContentValues(item, updateTime, null);

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        Uri updateItemUri = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, item.getId());
        ContentProviderOperation.Builder itemBuilder =
                ContentProviderOperation.newUpdate(updateItemUri);

        ContentProviderOperation itemUpdateOp = itemBuilder.withValues(itemValues).build();

        ops.add(itemUpdateOp);

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
            result = mContext.getContentResolver()
                    .applyBatch(Contract.CONTENT_AUTHORITY, ops);
            msg = result.toString();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        return msg;
    }




    @Override
    public int delete(ToBuyItem buyItem)
    {
        Uri uriDeleteBuyItem = ContentUris.withAppendedId(ToBuyItemsEntry.CONTENT_URI,
                buyItem.getId());
        return mContext.getContentResolver().delete(uriDeleteBuyItem, null, null);

    }

    @Override
    public int delete(Item item)
    {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        Uri uriDeleteItem = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, item.getId());
        ContentProviderOperation.Builder itemDeleteBuilder = ContentProviderOperation.newDelete(uriDeleteItem);
        ops.add(itemDeleteBuilder.build());

        Uri uriDeletePrice = PricesEntry.CONTENT_URI;
        ContentProviderOperation.Builder deletePriceBuilder =
                ContentProviderOperation.newDelete(uriDeletePrice);
        deletePriceBuilder.withSelection(PricesEntry.COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(item.getId())});
        ops.add(deletePriceBuilder.build());

        ContentProviderResult[] contentProviderResults = null;
        try {
            contentProviderResults = mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        return contentProviderResults.length;
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
