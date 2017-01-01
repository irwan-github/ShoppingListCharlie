package com.mirzairwan.shopping;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Picture;

import java.io.File;
import java.io.IOException;

public abstract class ItemActivity extends AppCompatActivity
        implements
        LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String ITEM_IS_IN_SHOPPING_LIST = "ITEM_IS_IN_SHOPPING_LIST";
    protected static final String ITEM_URI = "ITEM_URI";
    protected static final int ITEM_LOADER_ID = 20;
    protected static final int ITEM_PRICE_LOADER_ID = 21;
    protected static final int ITEM_PICTURE_LOADER_ID = 22;
    private static final int REQUEST_SNAP_PICTURE = 15;
    private static final String LOG_TAG = ItemEditingActivity.class.getSimpleName();
    private static final int REQUEST_PICK_PHOTO = 16;
    private static final int PERMISSION_USER_CAMERA_REQUEST = 30;
    private static final int PERMISSION_USER_READ_EXTERNAL_STORAGE = 31;

    protected EditText etName;
    protected EditText etBrand;
    protected EditText etDescription;
    protected EditText etCountryOrigin;

    protected EditText etUnitPrice;
    protected EditText etBundlePrice;
    protected EditText etBundleQty;

    private ImageView mImgItemPic;

    protected View.OnTouchListener mOnTouchListener;
    protected boolean mItemHaveChanged = false;

    protected Item item;
    protected DaoManager daoManager;
    protected PictureMgr pictureMgr;
    protected PriceMgr priceMgr;
    protected String mCountryCode;
    protected long itemId;
    private String mSortPref;
    private Toolbar toolbarPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getActivityLayout());

        daoManager = Builder.getDaoManager(this);
        pictureMgr = new PictureMgr(getApplicationInfo().packageName);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCountryCode = sharedPrefs.getString(getString(R.string.user_country_pref), null);

        priceMgr = new PriceMgr(mCountryCode);

    }

    protected abstract int getActivityLayout();

    protected void setupPermitted(int requestPermissionCode, String manifestPermission)
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                manifestPermission);

        if (permissionCheck == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{manifestPermission},
                    requestPermissionCode);
    }

    protected void setupPictureToolbar()
    {

        toolbarPicture = (Toolbar) findViewById(R.id.picture_toolbar);

        toolbarPicture.inflateMenu(R.menu.picture_item);

        //Add menu click handler
        toolbarPicture.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId()) {
                    case R.id.menu_camera:
                        startSnapShotActivity();
                        return true;

                    case R.id.choose_picture:
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
                        return true;
                    default:
                        return false;
                }

            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();  // Always call the superclass method first

        setupPermitted(PERMISSION_USER_CAMERA_REQUEST, Manifest.permission.CAMERA);

        setupPermitted(PERMISSION_USER_READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSION_USER_CAMERA_REQUEST:
                MenuItem itemCamera = toolbarPicture.getMenu().findItem(R.id.menu_camera);

                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (itemCamera != null)
                        itemCamera.setEnabled(false);
                }

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (itemCamera != null)
                        itemCamera.setEnabled(true);
                }
                break;

            case PERMISSION_USER_READ_EXTERNAL_STORAGE:
                MenuItem itemPicturePicker = toolbarPicture.getMenu().findItem(R.id.choose_picture);
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (itemPicturePicker != null)
                        itemPicturePicker.setEnabled(false);
                }

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (itemPicturePicker != null)
                        itemPicturePicker.setEnabled(true);
                }
                break;

        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void setCurrencySymbol(EditText et, String currencyCode)
    {
        String currencySymbol = NumberFormatter.getCurrencySymbol(mCountryCode, currencyCode);

        ViewParent viewParent = et.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        String hint = etLayout.getHint().toString();
        etLayout.setHint(hint + " (" + currencySymbol + ")");
    }

    protected void initLoaders(Uri uri)
    {
        Bundle arg = new Bundle();
        arg.putParcelable(ITEM_URI, uri);
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
                mItemHaveChanged = false;
                finish();
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
                            NavUtils.navigateUpFromSameTask(ItemActivity.this);
                        }
                    });
                else {
                    NavUtils.navigateUpFromSameTask(ItemActivity.this);
                    removeUnwantedPicturesFromApp();
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    /**
     * Call this method when user back or navigate up with no intention of saving any pictures
     */
    protected void removeUnwantedPicturesFromApp()
    {
        if (pictureMgr.getOriginalPicture() == null && pictureMgr.getPictureForViewing() != null)
            pictureMgr.discardLastViewedPicture();

        if (pictureMgr.getDiscardedPictures().size() > 0) {
            pictureMgr.setViewOriginalPicture();
            String msg = daoManager.cleanUpDiscardedPictures(pictureMgr);
            Toast.makeText(ItemActivity.this, msg, Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Current implemetation supports only 1 picture. Therefore when user make subsequent snapshots,
     * the previous photos MUST be put in discarded pile.
     */
    protected void startSnapShotActivity()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File itemPicFile = null;
            try {
                itemPicFile = pictureMgr.createFileHandle(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                pictureMgr.setPictureForViewing(itemPicFile);
                Uri itemPicUri = FileProvider.getUriForFile(this, "com.mirzairwan.shopping.fileprovider", itemPicFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, itemPicUri);
                startActivityForResult(cameraIntent, REQUEST_SNAP_PICTURE);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Photo file cannot be created. Aborting camera operation", Toast.LENGTH_SHORT).show();
                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (resultCode) {
            case Activity.RESULT_OK:
                switch (requestCode) {
                    case REQUEST_SNAP_PICTURE:
                        setPictureView(pictureMgr.getPictureForViewing());
                        break;
                    case REQUEST_PICK_PHOTO:
                        setPictureView(data);
                        break;
                }
                break;

            default: //Assume the worst. No picture from camera. Delete the useless file.
                pictureMgr.setViewOriginalPicture();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPictureView(Intent data)
    {
        Uri photoUri = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null);
        String filePath = null;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }

        // Get the dimensions of the View
        int targetW = mImgItemPic.getWidth();
        int targetH = mImgItemPic.getHeight();
        if (filePath == null)
            return;

        pictureMgr.setExternalPictureForViewing(filePath);

        Bitmap bitmap = PictureUtil.sizeToView(targetW, targetH, filePath);
        Bitmap toBeBitmap = PictureUtil.correctOrientation(bitmap, filePath);

        mImgItemPic.setImageBitmap(toBeBitmap);


    }

    protected void setPictureView(File pictureFile)
    {
        // Get the dimensions of the View
        int targetW = mImgItemPic.getWidth();
        int targetH = mImgItemPic.getHeight();

        Bitmap bitmap = PictureUtil.sizeToView(targetW, targetH, pictureFile);
        Bitmap toBeBitmap = PictureUtil.correctOrientation(bitmap, pictureFile.getPath());

        mImgItemPic.setImageBitmap(toBeBitmap);

    }


    protected void setPictureView(Picture picture)
    {
        // Get the dimensions of the View
        int targetW = mImgItemPic.getWidth();
        int targetH = mImgItemPic.getHeight();

        Bitmap bitmap = PictureUtil.sizeToView(targetW, targetH, picture.getPath());
        Bitmap toBeBitmap = PictureUtil.correctOrientation(bitmap, picture.getPath());

        mImgItemPic.setImageBitmap(toBeBitmap);

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
                    removeUnwantedPicturesFromApp();
                    finish();
                }
            });
        else {
            removeUnwantedPicturesFromApp();
            super.onBackPressed();
        }
    }

    private void showUnsavedDialog(DialogInterface.OnClickListener onLeaveClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, onLeaveClickListener);
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

    protected void alertRequiredField(int titleId, int messageId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    protected void alertItemInShoppingList(int messageId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Delete item if only item is NOT in shoppinglist
     */
    protected abstract void delete();

    protected Item getItemFromInputField()
    {
        String itemName;
        if (TextUtils.isEmpty(etName.getText())) {
            alertRequiredField(R.string.message_title, R.string.mandatory_name);
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

    protected abstract void save();

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
        boolean itemIsInShoppingList = getIntent().getBooleanExtra(ITEM_IS_IN_SHOPPING_LIST, false);
        item.setInBuyList(itemIsInShoppingList);
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
        etUnitPrice.setText(priceMgr.getUnitPriceForDisplay());
        setCurrencySymbol(etUnitPrice, priceMgr.getUnitPrice().getCurrencyCode());

        etBundlePrice.setText(priceMgr.getBundlePriceForDisplay());
        setCurrencySymbol(etBundlePrice, priceMgr.getBundlePrice().getCurrencyCode());
        etBundleQty.setText(NumberFormatter.formatToTwoDecimalPlaces(priceMgr.getBundlePrice().getBundleQuantity()));
    }


    protected void clearPriceInputFields()
    {
        etUnitPrice.setText("");
        etBundlePrice.setText("");
        etBundleQty.setText("");
    }

    protected void clearFocus()
    {
        etName.setFocusable(false);
        etBrand.setFocusable(false);
        etCountryOrigin.setFocusable(false);
        etDescription.setFocusable(false);

        etUnitPrice.setFocusable(false);
        etBundlePrice.setFocusable(false);
        etBundleQty.setFocusable(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        String[] projection = null;
        Uri uri = args.getParcelable(ITEM_URI);
        Loader<Cursor> loader = null;
        itemId = -1;
        String selection = null;
        String[] selectionArgs = null;

        switch (loaderId) {
            case ITEM_PRICE_LOADER_ID:
                projection = new String[]{Contract.PricesEntry._ID,
                        Contract.PricesEntry.COLUMN_PRICE_TYPE_ID,
                        Contract.PricesEntry.COLUMN_PRICE,
                        Contract.PricesEntry.COLUMN_BUNDLE_QTY,
                        Contract.PricesEntry.COLUMN_CURRENCY_CODE,
                        Contract.PricesEntry.COLUMN_SHOP_ID};
                itemId = ContentUris.parseId(uri);
                selection = Contract.PricesEntry.COLUMN_ITEM_ID + "=?";
                selectionArgs = new String[]{String.valueOf(itemId)};
                loader = new CursorLoader(this, Contract.PricesEntry.CONTENT_URI, projection, selection,
                        selectionArgs, null);
                break;

            case ITEM_PICTURE_LOADER_ID:
                projection = new String[]{Contract.PicturesEntry._ID, Contract.PicturesEntry.COLUMN_FILE_PATH,
                        Contract.PicturesEntry.COLUMN_ITEM_ID};
                itemId = ContentUris.parseId(uri);
                selection = Contract.PicturesEntry.COLUMN_ITEM_ID + "=?";
                selectionArgs = new String[]{String.valueOf(itemId)};
                loader = new CursorLoader(this, Contract.PicturesEntry.CONTENT_URI, projection, selection,
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

                Item item = createItem(Contract.ItemsEntry._ID, Contract.ItemsEntry.COLUMN_NAME,
                        Contract.ItemsEntry.COLUMN_BRAND, Contract.ItemsEntry.COLUMN_DESCRIPTION,
                        Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN, cursor);
                pictureMgr.setItemId(item.getId());
                populateItemInputFields(item);

                break;
            case ITEM_PRICE_LOADER_ID:
                priceMgr = new PriceMgr(itemId, mCountryCode);
                priceMgr.createPrices(cursor);
                populatePricesInputFields();
                break;

            case ITEM_PICTURE_LOADER_ID:
                Picture pictureInDb = createPicture(cursor);
                int colItemId = cursor.getColumnIndex(Contract.PicturesEntry.COLUMN_ITEM_ID);
                long itemId = cursor.getLong(colItemId);
                pictureMgr.setItemId(itemId);
                pictureMgr.setOriginalPicture(pictureInDb);
                pictureMgr.setViewOriginalPicture();
                //setPictureView(pictureMgr.getPictureForViewing().getFile());
                setPictureView(pictureMgr.getPictureForViewing());
                break;
        }
    }

    protected Picture createPicture(Cursor cursor)
    {
        int colRowId = cursor.getColumnIndex(Contract.PicturesEntry._ID);
        int colPicturePath = cursor.getColumnIndex(Contract.PicturesEntry.COLUMN_FILE_PATH);
        Picture pictureInDb = null;
        if (cursor.moveToFirst()) {
            //pictureInDb = new Picture(cursor.getLong(colRowId), new File(cursor.getString(colPicturePath)));
            pictureInDb = new Picture(cursor.getLong(colRowId), cursor.getString(colPicturePath));
        }

        return pictureInDb;
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
}