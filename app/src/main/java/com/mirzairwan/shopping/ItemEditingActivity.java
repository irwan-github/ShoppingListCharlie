package com.mirzairwan.shopping;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.Price;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;

/**
 * Display the item details in a screen
 * Update item details
 * Delete existing items
 */
public class ItemEditingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{

    protected static final String ITEM_URI = "ITEM_URI";
    private static final int ITEM_LOADER_ID = 20;
    protected static final int ITEM_PRICE_LOADER_ID = 21;
    protected static final int ITEM_PICTURE_LOADER_ID = 22;
    private static final int CAMERA_MENU_ITEM_ID = 99;
    private static final String SHOPPING_LIST_PICS = "Item_";
    private static final int REQUEST_SNAP_PICTURE = 15;
    private static final String LOG_TAG = ItemEditingActivity.class.getSimpleName();


    protected EditText etName;
    protected EditText etBrand;
    protected EditText etDescription;
    protected EditText etCountryOrigin;

    protected EditText etUnitPrice;
    protected EditText etBundlePrice;
    protected EditText etBundleQty;

    protected View.OnTouchListener mOnTouchListener;
    protected boolean mItemHaveChanged = false;
    private DaoManager daoManager;

    protected Item item;
    protected List<Price> mPrices;
    private ImageView mImgItemPic;
    //private String mCurrentPicturePath;
    protected List<Picture> mPictures = new ArrayList<>();
    protected List<File> mPictureFilesTemp = new ArrayList<>();
    protected Picture pictureInProcessToBeDeleted;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editing2);

        initLoaders();
        setTitle(R.string.view_buy_item_details);

        daoManager = Builder.getDaoManager(this);

    }

    protected void setCurrencySymbol(EditText et, String currencyCode)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String countryCode = sharedPrefs.getString(getString(R.string.user_country_pref), null);
        String currencySymbol = NumberFormatter.getCurrencySymbol(countryCode, currencyCode);

        ViewParent viewParent = et.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        String hint = etLayout.getHint().toString();
        etLayout.setHint(hint + " (" + currencySymbol + ")");
    }

    protected void initLoaders()
    {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        Bundle arg = new Bundle();
        arg.putParcelable(ITEM_URI, uri);
        getLoaderManager().initLoader(ITEM_LOADER_ID, arg, this);
        getLoaderManager().initLoader(ITEM_PRICE_LOADER_ID, arg, this);
        getLoaderManager().initLoader(ITEM_PICTURE_LOADER_ID, arg, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.item_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {

        switch (menuItem.getItemId()) {
            case R.id.save_item_details:
                save();
                return true;
            case R.id.menu_camera:
                startSnapShotActivity();
                return true;
            case R.id.menu_remove_item_from_list:
                delete();
                return true;
            case android.R.id.home:
                if (mItemHaveChanged)
                    showUnsavedDialog(new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            NavUtils.navigateUpFromSameTask(ItemEditingActivity.this);
                        }
                    });
                else
                    NavUtils.navigateUpFromSameTask(ItemEditingActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    /**
     * Current implemetation supports only 1 picture. Therefore when user make subsequent snapshots,
     * the previous photos must be deleted from filesystem. The original photo is deleted when acamera activity returns status OK
     */
    protected void startSnapShotActivity()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File itemPicFile = null;
            try {
                itemPicFile = createFileHandle();
                mPictureFilesTemp.add(itemPicFile); //At this point, maximum temp files is 2
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Photo file cannot be created. Aborting camera operation", Toast.LENGTH_SHORT).show();
                return;
            }
            if (itemPicFile != null) {
                Uri itemPicUri = FileProvider.getUriForFile(this, "com.mirzairwan.shopping.fileprovider", itemPicFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, itemPicUri);
                startActivityForResult(cameraIntent, REQUEST_SNAP_PICTURE);
            }
        }
    }

    protected File createFileHandle() throws IOException
    {
        //Get directory handle where picture is stored
        File dirPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String picFilename = SHOPPING_LIST_PICS + "_" + timeStamp + "_";
        //Get file handle
        File filePicture = File.createTempFile(picFilename, ".jpg", dirPictures);

        //mCurrentPicturePath = filePicture.getAbsolutePath();
        return filePicture;
    }

    @Override
    /**
     * Current implemetation supports only 1 picture. Therefore when user make subsequent snapshots,
     * the previous photos MUST be deleted from filesystem. The original photo is deleted when
     * camera activity returns status OK
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (resultCode) {
            case Activity.RESULT_OK:
                switch (requestCode) {
                    case REQUEST_SNAP_PICTURE:
//                        int result = 0;
//                        boolean existPreviousPicture = mPictureFilesTemp.size() == 1;
//                        if(existPreviousPicture)
//                            result = deletePictureFromFilesystem(mPicturesTemp.get(0)); //Delete previous picture
//                        boolean isDeleteSuccess = result == 1;
                        //preparePicturePathForSaving(mPictureFilesTemp.get(0));
                        removePreviousTempFiles();
                        setPictureView(mPictureFilesTemp.get(0));
                        break;
                }

                break;

            default: //Assume the worst. No picture from camera. Delete the useless file.
                removeCurrentTempFiles();
                ;

        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * Called when camera activity failed. Delete the file cereated for the failed attempt.
     */
    private void removeCurrentTempFiles()
    {
        File fileToBeDeleted = mPictureFilesTemp.remove(mPictureFilesTemp.size() - 1); //First picture
        deleteFileFromFilesystem(fileToBeDeleted);
    }

    /**
     * Called when camera activity succeed. Delete the file cereated for the previous successful attempt.
     */

    private void removePreviousTempFiles()
    {
        if (mPictureFilesTemp.size() == 1) //No previous file
            return;

        File fileToBeDeleted = mPictureFilesTemp.remove(0); //First picture
        deleteFileFromFilesystem(fileToBeDeleted);
    }

    protected int deleteFileFromFilesystem(File file)
    {
        String authority = getClass().getPackage().getName() + ".fileprovider";
        Uri uri = FileProvider.getUriForFile(this, authority, file);
        int result = getContentResolver().delete(uri, null, null);
        Log.d(LOG_TAG, ">>>deletePicture " + result);
        return result;

    }

//    /**
//     * Update item's picture path
//     *
//     * @param picturePath
//     */
//    protected void preparePicturePathForSaving(String picturePath)
//    {
//        //Currenctly, only 1 picture is supported
//        if (mPictures.size() == 1) {
//            Picture picture = mPictures.get(0);
//            picture.setPicturePath(picturePath);
//        } else {
//            Picture newPicture = new Picture(picturePath);
//            mPictures.clear();
//            mPictures.add(newPicture);
//        }
//
//    }

    protected void preparePictureForSaving()
    {
        //Currently, only 1 picture is supported
        if (mPictureFilesTemp.size() == 1) {
            pictureInProcessToBeDeleted = mPictures.get(0);
            File pictureFile = mPictureFilesTemp.get(0);
            mPictures.get(0).setFile(pictureFile);
        }
// else {
//            Picture newPicture = new Picture(mPictureFilesTemp.get(0));
//            mPictures.clear();
//            mPictures.add(newPicture);
//        }

    }


    protected void setPictureView(List<Picture> pictures)
    {
        //Currently only 1 picture is supported
        setPictureView(pictures.get(0));
    }

    protected void setPictureView(Picture picture)
    {
        setPictureView(picture.getFile());
    }

    protected void setPictureView(File pictureFile)
    {
        // Get the dimensions of the View
        int targetW = mImgItemPic.getWidth();
        int targetH = mImgItemPic.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pictureFile.getPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getPath(), bmOptions);
        mImgItemPic.setImageBitmap(bitmap);

    }

    @Override
    protected void onStart()
    {
        mOnTouchListener = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                mItemHaveChanged = true;
                return false;
            }
        };

        etName = (EditText) findViewById(R.id.et_item_name);
        etBrand = (EditText) findViewById(R.id.et_item_brand);
        etDescription = (EditText) findViewById(R.id.et_item_description);
        etCountryOrigin = (EditText) findViewById(R.id.et_item_country_origin);
        mImgItemPic = (ImageView) findViewById(R.id.img_item);

        etName.setOnTouchListener(mOnTouchListener);
        etBrand.setOnTouchListener(mOnTouchListener);
        etDescription.setOnTouchListener(mOnTouchListener);
        etCountryOrigin.setOnTouchListener(mOnTouchListener);
        ;

        etUnitPrice = (EditText) findViewById(R.id.et_unit_price);
        etBundlePrice = (EditText) findViewById(R.id.et_bundle_price);
        etBundleQty = (EditText) findViewById(R.id.et_bundle_qty);

        etUnitPrice.setOnTouchListener(mOnTouchListener);
        etBundlePrice.setOnTouchListener(mOnTouchListener);
        etBundleQty.setOnTouchListener(mOnTouchListener);
        super.onStart();
    }

    @Override
    public void onBackPressed()
    {
        if (mItemHaveChanged)
            showUnsavedDialog(new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            });
        else
            super.onBackPressed();
    }

    private void showUnsavedDialog(DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, onClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    protected void alertRequiredField(int messageId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mandatory field(s)");
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     *
     */
    protected void delete()
    {
        daoManager.delete(item);
        finish();
    }

    protected Item getItemFromInputField()
    {
        String itemName;
        if (TextUtils.isEmpty(etName.getText())) {
            alertRequiredField(R.string.mandatory_name);
            etName.requestFocus();
            return null;
        } else {
            itemName = etName.getText().toString();
        }

        String itemBrand = etBrand.getText().toString();
        String countryOrigin = etCountryOrigin.getText().toString();
        String itemDescription = etDescription.getText().toString();

        if (item == null)
            item = new Item(itemName);
        else
            item.setName(itemName);

        item.setBrand(itemBrand);
        item.setCountryOrigin(countryOrigin);
        item.setDescription(itemDescription);
        return item;
    }

    protected String getBundleQtyFromInputField()
    {
        String bundleQty;
        bundleQty = "0.00";
        if (etBundleQty != null && !TextUtils.isEmpty(etBundleQty.getText()))
            bundleQty = etBundleQty.getText().toString();
        return bundleQty;
    }

    protected String getBundlePriceFromInputField()
    {
        String bundlePrice;
        bundlePrice = "0.00";
        if (etBundlePrice != null && !TextUtils.isEmpty(etBundlePrice.getText()))
            bundlePrice = etBundlePrice.getText().toString();
        return bundlePrice;
    }

    protected String getUnitPriceFromInputField()
    {
        String unitPrice;
        unitPrice = "0.00";
        if (etUnitPrice != null && !TextUtils.isEmpty(etUnitPrice.getText()))
            unitPrice = etUnitPrice.getText().toString();
        return unitPrice;
    }

    /**
     * Update prices of existing item. Existing item have existing prices. The currency code will
     * not be changed
     *
     * @param item
     * @param unitPrice
     * @param bundlePrice
     * @param bundleQty
     */
    protected void preparePricesForSaving(Item item, String unitPrice, String bundlePrice, String bundleQty)
    {
        for (Price price : mPrices) {
            if (price.getPriceType() == BUNDLE_PRICE) {
                price.setBundlePrice(Double.parseDouble(bundlePrice), Double.parseDouble(bundleQty));
            } else {
                price.setUnitPrice(Double.parseDouble(unitPrice));
            }
            item.addPrice(price);
        }
    }

    protected void save()
    {
        Item item = getItemFromInputField();

        preparePricesForSaving(item, getUnitPriceFromInputField(), getBundlePriceFromInputField(), getBundleQtyFromInputField());

        preparePictureForSaving();

        ContentProviderResult[] msg;
        msg = daoManager.update(item, item.getPrices(), mPictures);

        //Check that picture is updated. If updated, delete the previous saved file from file system
        boolean isItemSaved = false;
        boolean isPictureSaved = false;
        isPictureSaved = msg[1].count == 1;
        isItemSaved = msg[0].count == 1;

        if (!isItemSaved)
            Toast.makeText(this, "Update not successful", Toast.LENGTH_SHORT).show();
        else {

            if (isPictureSaved && pictureInProcessToBeDeleted != null) {
                deleteFileFromFilesystem(pictureInProcessToBeDeleted.getFile());
            }

            finish();
        }
    }


    /**
     * Populate Item object
     *
     * @param cursor
     */
    protected Item createItem(String idColumnName, String nameColumnName, String brandColumnName,
                              String descriptionColumnName, String countryOriginColumnName,
                              Cursor cursor)
    {
        if (cursor == null)
            throw new IllegalArgumentException("Cursor cannot be null");

        long itemId;
        String itemName, itemBrand, itemDescription, countryOrigin;

        itemId = cursor.getLong(cursor.getColumnIndex(idColumnName));

        int colNameIndex = cursor.getColumnIndex(nameColumnName);
        itemName = cursor.getString(colNameIndex);

        int colBrandIdx = cursor.getColumnIndex(brandColumnName);
        itemBrand = cursor.getString(colBrandIdx);

        int colDescriptionIdx = cursor.getColumnIndex(descriptionColumnName);
        itemDescription = cursor.getString(colDescriptionIdx);

        int colCountryOriginIdx = cursor.getColumnIndex(countryOriginColumnName);
        countryOrigin = cursor.getString(colCountryOriginIdx);

        item = new Item(itemId, itemName, itemBrand, countryOrigin, itemDescription, null);

        return item;
    }

    protected void populateItemInputFields(Item item)
    {
        etName.setText(item != null ? item.getName() : "");
        etBrand.setText(item != null ? item.getBrand() : "");
        etCountryOrigin.setText(item != null ? item.getCountryOrigin() : "");
        etDescription.setText(item != null ? item.getDescription() : "");
    }

    protected void populatePricesInputFields()
    {
        for (Price price : mPrices) {
            if (price.getPriceType() == Price.Type.UNIT_PRICE) {
                etUnitPrice.setText(NumberFormatter.formatToTwoDecimalPlaces(price.getUnitPrice()));
                setCurrencySymbol(etUnitPrice, price.getCurrencyCode());
            }

            if (price.getPriceType() == Price.Type.BUNDLE_PRICE) {
                etBundlePrice.setText(NumberFormatter.formatToTwoDecimalPlaces(price.getBundlePrice()));
                setCurrencySymbol(etBundlePrice, price.getCurrencyCode());
                etBundleQty.setText(NumberFormatter.formatToTwoDecimalPlaces(price.getBundleQuantity()));
            }
        }
    }

    protected void clearPriceInputFields()
    {
        etUnitPrice.setText("");
        etBundlePrice.setText("");
        etBundleQty.setText("");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        String[] projection = null;
        Uri uri = args.getParcelable(ITEM_URI);
        Loader<Cursor> loader = null;
        long itemId = -1;
        String selection = null;
        String[] selectionArgs = null;

        switch (loaderId) {
            case ITEM_LOADER_ID:
                projection = new String[]{
                        ItemsEntry._ID,
                        ItemsEntry.COLUMN_NAME,
                        ItemsEntry.COLUMN_BRAND,
                        ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                        ItemsEntry.COLUMN_DESCRIPTION,
                };
                loader = new CursorLoader(this, uri, projection, null, null, null);
                break;

            case ITEM_PRICE_LOADER_ID:
                projection = new String[]{PricesEntry._ID,
                        PricesEntry.COLUMN_PRICE_TYPE_ID,
                        PricesEntry.COLUMN_PRICE,
                        PricesEntry.COLUMN_BUNDLE_QTY,
                        PricesEntry.COLUMN_CURRENCY_CODE,
                        PricesEntry.COLUMN_SHOP_ID};
                itemId = ContentUris.parseId(uri);
                selection = PricesEntry.COLUMN_ITEM_ID + "=?";
                selectionArgs = new String[]{String.valueOf(itemId)};
                loader = new CursorLoader(this, PricesEntry.CONTENT_URI, projection, selection,
                        selectionArgs, null);
                break;

            case ITEM_PICTURE_LOADER_ID:
                projection = new String[]{PicturesEntry._ID, PicturesEntry.COLUMN_FILE_PATH};
                itemId = ContentUris.parseId(uri);
                selection = PicturesEntry.COLUMN_ITEM_ID + "=?";
                selectionArgs = new String[]{String.valueOf(itemId)};
                loader = new CursorLoader(this, PicturesEntry.CONTENT_URI, projection, selection,
                        selectionArgs, null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported query");

        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        if (cursor == null || cursor.getCount() < 1)
            return;

        int loaderId = loader.getId();
        switch (loaderId) {
            case ITEM_LOADER_ID:
                cursor.moveToFirst();
                Item item = createItem(ItemsEntry._ID, ItemsEntry.COLUMN_NAME,
                        ItemsEntry.COLUMN_BRAND, ItemsEntry.COLUMN_DESCRIPTION,
                        ItemsEntry.COLUMN_COUNTRY_ORIGIN, cursor);
                populateItemInputFields(item);
                break;
            case ITEM_PRICE_LOADER_ID:
                createPrices(cursor);
                populatePricesInputFields();
                break;

            case ITEM_PICTURE_LOADER_ID:
                List<Picture> pictures = createPictures(cursor);
                setPictureView(pictures);
                break;
        }
    }

    private List<Picture> createPictures(Cursor cursor)
    {
        //Clear the list of pictures
        mPictures.clear();

        int colRowId = cursor.getColumnIndex(PicturesEntry._ID);
        int colPicturePath = cursor.getColumnIndex(PicturesEntry.COLUMN_FILE_PATH);
        while (cursor.moveToNext()) {
            mPictures.add(new Picture(cursor.getLong(colRowId), new File(cursor.getString(colPicturePath))));
        }
        return mPictures;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // If the loader is invalidated, clear out all the data from the input fields.
        populateItemInputFields(null);
        clearPriceInputFields();
        clearPictureField();
    }

    private void clearPictureField()
    {
        mImgItemPic.setImageBitmap(null);
    }

    protected void createPrices(Cursor cursor)
    {
        mPrices = new ArrayList<>();

        while (cursor.moveToNext()) {
            int colPriceTypeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE_TYPE_ID);
            int priceTypeVal = cursor.getInt(colPriceTypeIdx);

            int colPriceIdIdx = cursor.getColumnIndex(PricesEntry._ID);
            long priceId = cursor.getLong(colPriceIdIdx);

            int colCurrencyCodeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
            String currencyCode = cursor.getString(colCurrencyCodeIdx);

            int colShopIdIdx = cursor.getColumnIndex(PricesEntry.COLUMN_SHOP_ID);
            long shopId = cursor.getLong(colShopIdIdx);

            int colPriceIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);

            Price price = null;

            if (priceTypeVal == UNIT_PRICE.getType()) {
                double unitPrice = cursor.getDouble(colPriceIdx) / 100;
                price = new Price(priceId, unitPrice, currencyCode, shopId, null);
            }

            if (priceTypeVal == BUNDLE_PRICE.getType()) {
                double bundlePrice = cursor.getDouble(colPriceIdx) / 100;
                int colBundleQtyIdx = cursor.getColumnIndex(PricesEntry.COLUMN_BUNDLE_QTY);
                double bundleQty = cursor.getDouble(colBundleQtyIdx) / 100;
                price = new Price(priceId, bundlePrice, bundleQty, currencyCode, shopId, null);
            }
            mPrices.add(price);
        }
    }
}
