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

import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.PictureMgr;
import com.mirzairwan.shopping.domain.PriceMgr;
import com.mirzairwan.shopping.domain.ShoppingList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This is a base class for subclasses to leverage on the following:
 * <p>
 * Loader for Prices table. Subclass just need to call initLoader with the correct loader id.
 * <p>
 * Loader for Pictures table. Subclass just need to call initLoader with the correct loader id.
 * <p>
 * Create Item objects and populate screen.
 * <p>
 * Ask permission for camera use
 * <p>
 * Ask permission for file access
 * <p>
 * Provide picture toolbar to capture image and pick photos from device storage.
 * <p>
 * Provide app bar for save and delete operation
 * <p>
 * Validate fields of provided xml layouts.
 * <p>
 * Provide user preference for country code
 * <p>
 * Provide user preference for sort by criteria
 * <p>
 * Subclass must include in their screen the following xml layout provided:
 * <p>
 * item_image_editor.xml
 * <p>
 * item_editing.xml
 * <p>
 * price_editing.xml
 */
public abstract class ItemActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String ITEM_IS_IN_SHOPPING_LIST = "ITEM_IS_IN_SHOPPING_LIST";
    protected static final String ITEM_URI = "ITEM_URI";
    protected static final int ITEM_PRICE_LOADER_ID = 21;
    protected static final int ITEM_PICTURE_LOADER_ID = 22;
    private static final int REQUEST_SNAP_PICTURE = 15;
    private static final String LOG_TAG = ItemActivity.class.getSimpleName();
    private static final int REQUEST_PICK_PHOTO = 16;
    private static final int PERMISSION_GIVE_ITEM_PICTURE = 32;
    private static final String BITMAP_STORAGE_KEY = "bsk";
    private static final String UNIT_PRICE = "unit_px";
    private static final String BUNDLE_PRICE = "bundle_px";
    private static final String PICTURE_MANAGER = "picture_mgr";

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

    protected Item mItem;
    protected DaoManager daoManager;
    protected PictureMgr mPictureMgr;
    protected PriceMgr priceMgr;
    protected String mCountryCode;
    private long itemId;
    //private String mSortPref;
    private Toolbar toolbarPicture;
    protected ShoppingList shoppingList;
    private Bitmap mTargetBitmap;
    protected EditText etCurrencyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutXml());
        setupPictureToolbar();

        daoManager = Builder.getDaoManager(this);
        shoppingList = Builder.getShoppingList();

        if (savedInstanceState == null)
        {
            mPictureMgr = new PictureMgr(getApplicationInfo().packageName);
        }
        else
        {
            mPictureMgr = savedInstanceState.getParcelable(PICTURE_MANAGER);
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCountryCode = sharedPrefs.getString(getString(R.string.user_country_pref), null);
        priceMgr = new PriceMgr(mCountryCode);

    }

    protected abstract int getLayoutXml();

    protected void setupPictureToolbar()
    {

        toolbarPicture = (Toolbar) findViewById(R.id.picture_toolbar);

        toolbarPicture.inflateMenu(R.menu.picture_item_toolbar);

        //check for the availability of the camera at runtime
        PackageManager pMgr = getPackageManager();
        if (!pMgr.hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            MenuItem itemCamera = toolbarPicture.getMenu().findItem(R.id.menu_camera);
            itemCamera.setEnabled(false);
        }

        //Add menu click handler
        toolbarPicture.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.menu_camera:
                        startSnapShotActivity();
                        return true;

                    case R.id.choose_picture:
                        startPickPictureActivity();
                        return true;

                    case R.id.remove_picture:
                        deletePictureInView();
                        return true;
                    default:
                        return false;
                }

            }
        });

        ArrayList<String> permissionRequest = new ArrayList<>();

        int permissionPickPicture = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionPickPicture == PackageManager.PERMISSION_DENIED)
        {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (permissionRequest.size() > 0)
        {
            ActivityCompat.requestPermissions(this, permissionRequest.toArray(new
                            String[permissionRequest.size()]),
                    PERMISSION_GIVE_ITEM_PICTURE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_GIVE_ITEM_PICTURE:

                for (int k = 0; k < permissions.length; ++k)
                {

                    if (permissions[k].equalsIgnoreCase(Manifest.permission.READ_EXTERNAL_STORAGE))
                    {

                        MenuItem itemPicturePicker = toolbarPicture.getMenu().findItem(R.id
                                .choose_picture);

                        if (grantResults[k] == PackageManager.PERMISSION_DENIED &&
                                itemPicturePicker != null)
                        {
                            itemPicturePicker.setEnabled(false);
                        }

                        if (grantResults[k] == PackageManager.PERMISSION_GRANTED &&
                                itemPicturePicker != null)
                        {
                            itemPicturePicker.setEnabled(true);
                        }

                    }
                }

                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    protected void initPriceLoader(Uri uri, LoaderManager.LoaderCallbacks<Cursor> callback)
    {
        if (ContentUris.parseId(uri) == -1)
        {
            throw new IllegalArgumentException("uri and item id cannot be empty or -1");
        }

        Bundle arg = new Bundle();
        arg.putParcelable(ITEM_URI, uri);
        //getLoaderManager().restartLoader(ITEM_PRICE_LOADER_ID, arg, callback);
        getLoaderManager().initLoader(ITEM_PRICE_LOADER_ID, arg, callback);
    }

    protected void initPictureLoader(Uri uri, LoaderManager.LoaderCallbacks<Cursor> callback)
    {
        if (ContentUris.parseId(uri) == -1)
        {
            throw new IllegalArgumentException("uri and item id cannot be empty or -1");
        }

        Bundle arg = new Bundle();
        arg.putParcelable(ITEM_URI, uri);
        getLoaderManager().initLoader(ITEM_PICTURE_LOADER_ID, arg, callback);
        //getLoaderManager().restartLoader(ITEM_PICTURE_LOADER_ID, arg, callback);
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

        switch (menuItem.getItemId())
        {
            case R.id.save_item_details:
                save();
                mItemHaveChanged = false;
                return true;
            case R.id.menu_remove_item_from_list:
                delete();
                return true;
            case android.R.id.home:
                if (mItemHaveChanged)
                {
                    showUnsavedDialog(new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            NavUtils.navigateUpFromSameTask(ItemActivity.this);
                        }
                    });
                }
                else
                {
                    NavUtils.navigateUpFromSameTask(ItemActivity.this);
                    removeUnwantedPicturesFromApp();
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    /**
     * Invoked when user click system back or navigate up with no intention of saving any pictures.
     * Does not delete original picture because it is not the intention of the user
     */
    protected void removeUnwantedPicturesFromApp()
    {
        if (mPictureMgr.getOriginalPicture() == null && mPictureMgr.getPictureForViewing() != null)
        {
            //Does not delete original picture because it is not the intention of the user
            mPictureMgr.discardCurrentPictureInView();
        }

        if (mPictureMgr.getDiscardedPictures().size() > 0)
        {
            mPictureMgr.setViewOriginalPicture();
            String msg = daoManager.cleanUpDiscardedPictures(mPictureMgr);
//            Toast.makeText(ItemActivity.this, msg, Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Invoked when user clicks on delete button in picture toolbar
     * Delete record if any that associates picture to the item in the database.
     * If the item is written to storage memory by this app, the image file is deleted.
     * If the currently view picture is not the original picture, the original picture will be used
     * for viewing if exist.
     */
    private void deletePictureInView()
    {
        if (mPictureMgr.getPictureForViewing() == null)
        {
            return;
        }

        Picture discardedPic = mPictureMgr.discardCurrentPictureInView();

        if (discardedPic != null && discardedPic.getId() > 0) // This is the original. Delete
        // record in database.
        {
            int deleted = daoManager.deletePicture(itemId);
        }
        else //Current picture is not stored in database
        {
            Picture originalPicture = mPictureMgr.getOriginalPicture();

            //PictureMgr replace viewed picture with original picture
            mPictureMgr.setViewOriginalPicture();
        }
        setPictureView(mPictureMgr.getPictureForViewing());
        String msg = daoManager.cleanUpDiscardedPictures(mPictureMgr);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Current implemetation supports only 1 picture. Therefore when user make subsequent snapshots,
     * the previous photos MUST be put in discarded pile.
     */
    protected void startSnapShotActivity()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null)
        {
            File itemPicFile = null;
            try
            {
                itemPicFile = PictureMgr.createFileHandle(getExternalFilesDir(Environment
                        .DIRECTORY_PICTURES));
                mPictureMgr.setPictureForViewing(itemPicFile);
                Uri itemPicUri = FileProvider.getUriForFile(this, "com.mirzairwan.shopping" +
                        ".fileprovider", itemPicFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, itemPicUri);
                startActivityForResult(cameraIntent, REQUEST_SNAP_PICTURE);

            } catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Photo file cannot be created. Aborting camera operation",
                        Toast.LENGTH_SHORT).show();
                return;
            }

        }
    }

    /**
     * Use this when picking picture from media gallery. I do not need to create a
     * filename for the picture as it was created outside this app. This method query the filepath
     * of the picture using ContentResolver.
     */
    protected void startPickPictureActivity()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (resultCode)
        {
            case Activity.RESULT_OK:
                switch (requestCode)
                {
                    case REQUEST_SNAP_PICTURE:
                        daoManager.cleanUpDiscardedPictures(mPictureMgr); //Original picture is not deleted.
                        setPictureView(mPictureMgr.getPictureForViewing());
                        break;
                    case REQUEST_PICK_PHOTO:
                        setPictureView(data);
                        break;
                }
                break;

            default: //Assume the worst. No picture from camera. Delete the useless file.
                mPictureMgr.setViewOriginalPicture();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Invoked when picking picture from media gallery. I do not need to create a
     * filename for the picture as it was created outside this app. This method query the filepath
     * of the picture using ContentResolver.
     *
     * @param data
     */
    protected void setPictureView(Intent data)
    {
        Uri photoUri = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null);
        String filePath = null;
        if (cursor.moveToFirst())
        {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }

        if (filePath == null)
        {
            return;
        }
        Picture externalPicture = new Picture(filePath);
        mPictureMgr.setPictureForViewing(externalPicture); //Update PictureMgr on the target picture
        daoManager.cleanUpDiscardedPictures(mPictureMgr); //delete previous picture. But original picture will not be deleted
        setPictureView(externalPicture);
    }

    /**
     * Called after external camera task or picture picture task
     * @param picture
     */
    protected void setPictureView(Picture picture)
    {
        if (picture == null)
        {
            mImgItemPic.setImageBitmap(null);
            return;
        }

        // Get the dimensions of the View
        int targetW = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_width);
        int targetH = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_height);

        ImageResizer imageResizer = new ImageResizer(this, targetW, targetH);
        imageResizer.loadImage(picture.getFile(), mImgItemPic);
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
        //InputFilterUtil.setInitialCapInputFilter(etName);
        etBrand = (EditText) findViewById(R.id.et_item_brand);
        //InputFilterUtil.setInitialCapInputFilter(etBrand);
        etDescription = (EditText) findViewById(R.id.et_item_description);
        etCountryOrigin = (EditText) findViewById(R.id.et_item_country_origin);
        mImgItemPic = (ImageView) findViewById(R.id.img_item);

        etName.setOnTouchListener(mOnTouchListener);
        etBrand.setOnTouchListener(mOnTouchListener);
        etDescription.setOnTouchListener(mOnTouchListener);
        etCountryOrigin.setOnTouchListener(mOnTouchListener);

        etCurrencyCode = (EditText) findViewById(R.id.et_currency_code);
        InputFilterUtil.setAllCapsInputFilter(etCurrencyCode);
        etCurrencyCode.setOnFocusChangeListener(new CurrencyCodeChecker());

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
        {
            showUnsavedDialog(new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    removeUnwantedPicturesFromApp();
                    finish();
                }
            });
        }
        else
        {
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
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    protected void alertItemInShoppingList(int messageId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Delete item if only item is NOT in shoppinglist
     */
    protected abstract void delete();

    /**
     * Validate item details
     *
     * @return
     */
    protected boolean fieldsValidated()
    {
        if (TextUtils.isEmpty(etName.getText()))
        {
            alertRequiredField(R.string.message_title, R.string.mandatory_name);
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Call fieldsValidated method before calling this method
     * <p>
     * Update an existing member item or if creation, create a new item
     *
     * @return
     */
    protected Item getItemFromInputField()
    {
        String itemName = etName.getText().toString();
        String itemBrand = etBrand.getText().toString();
        String countryOrigin = etCountryOrigin.getText().toString();
        String itemDescription = etDescription.getText().toString();

        if (mItem == null)
        {
            mItem = new Item(itemName);
        }
        else
        {
            mItem.setName(itemName);
        }

        mItem.setBrand(itemBrand);
        mItem.setCountryOrigin(countryOrigin);
        mItem.setDescription(itemDescription);
        return mItem;
    }

    protected String getBundleQtyFromInputField()
    {
        String bundleQty;
        bundleQty = "0.00";
        if (etBundleQty != null && !TextUtils.isEmpty(etBundleQty.getText()))
        {
            bundleQty = etBundleQty.getText().toString();
        }
        return bundleQty;
    }

    protected String getBundlePriceFromInputField()
    {
        String bundlePrice;
        bundlePrice = "0.00";
        if (etBundlePrice != null && !TextUtils.isEmpty(etBundlePrice.getText()))
        {
            bundlePrice = etBundlePrice.getText().toString();
        }
        return bundlePrice;
    }

    protected String getUnitPriceFromInputField()
    {
        String unitPrice;
        unitPrice = "0.00";
        if (etUnitPrice != null && !TextUtils.isEmpty(etUnitPrice.getText()))
        {
            unitPrice = etUnitPrice.getText().toString();
        }
        return unitPrice;
    }

    protected abstract void save();

    /**
     * Create Item object
     *
     * @param cursor
     */
    protected Item createItem(String idColumnName, String nameColumnName, String brandColumnName,
                              String descriptionColumnName, String countryOriginColumnName,
                              Cursor cursor)
    {
        if (cursor == null)
        {
            throw new IllegalArgumentException("Cursor cannot be null");
        }

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

        mItem = new Item(itemId, itemName, itemBrand, countryOrigin, itemDescription, null);
        boolean itemIsInShoppingList = getIntent().getBooleanExtra(ITEM_IS_IN_SHOPPING_LIST, false);
        mItem.setInBuyList(itemIsInShoppingList);
        return mItem;
    }

    protected void populateItemInputFields(Item item)
    {
        etName.setText(item != null ? item.getName() : "");
        etBrand.setText(item != null ? item.getBrand() : "");
        etCountryOrigin.setText(item != null ? item.getCountryOrigin() : "");
        etDescription.setText(item != null ? item.getDescription() : "");
    }

    /**
     * When displaying existing item in shopping list, set currency symbol to the saved currency
     * code irregardless of current country code preference
     */
    protected void populatePricesInputFields()
    {
        etCurrencyCode.setText(priceMgr.getUnitPrice().getCurrencyCode());

        etUnitPrice.setText(priceMgr.getUnitPriceForDisplay());
        setCurrencySymbol(priceMgr.getUnitPrice().getCurrencyCode());

        etBundlePrice.setText(priceMgr.getBundlePriceForDisplay());
        setCurrencySymbol(priceMgr.getBundlePrice().getCurrencyCode());
        etBundleQty.setText(FormatHelper.formatToTwoDecimalPlaces(priceMgr.getBundlePrice()
                .getBundleQuantity()));
    }

    protected void setCurrencySymbol(String currencyCode)
    {
        String currencySymbol = FormatHelper.getCurrencySymbol(mCountryCode, currencyCode);

        String hintUnitPx = getString(R.string.unit_price_txt) + " (" + currencySymbol + ")";
        setCurrencySymbol(etUnitPrice, hintUnitPx);

        String hintBundlePx = getString(R.string.bundle_price_txt) + " (" + currencySymbol + ")";
        setCurrencySymbol(etBundlePrice, hintBundlePx);

    }

    protected void setCurrencySymbol(EditText et, String hint)
    {
        ViewParent viewParent = et.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setHint(hint);
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


    /**
     * Select item its following child records:
     * 1. Prices
     * 2. Pictures
     *
     * @param loaderId
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        String[] projection = null;
        Uri uri = args.getParcelable(ITEM_URI);
        Loader<Cursor> loader = null;
        itemId = -1;
        String selection = null;
        String[] selectionArgs = null;

        switch (loaderId)
        {
            case ITEM_PRICE_LOADER_ID:
                projection = new String[]{PricesEntry._ID,
                        PricesEntry.COLUMN_ITEM_ID,
                        PricesEntry.COLUMN_PRICE_TYPE_ID,
                        PricesEntry.COLUMN_PRICE,
                        PricesEntry.COLUMN_BUNDLE_QTY,
                        PricesEntry.COLUMN_CURRENCY_CODE,
                        PricesEntry.COLUMN_SHOP_ID};
                itemId = ContentUris.parseId(uri);
                if (itemId == -1)
                {
                    throw new IllegalArgumentException("uri and item id cannot be empty or -1");
                }

                selection = PricesEntry.COLUMN_ITEM_ID + "=?";
                selectionArgs = new String[]{String.valueOf(itemId)};
                loader = new CursorLoader(this, PricesEntry.CONTENT_URI, projection, selection,
                        selectionArgs, null);
                break;

            case ITEM_PICTURE_LOADER_ID:
                projection = new String[]{PicturesEntry._ID, PicturesEntry.COLUMN_FILE_PATH,
                        PicturesEntry.COLUMN_ITEM_ID};
                itemId = ContentUris.parseId(uri);
                if (itemId == -1)
                {
                    throw new IllegalArgumentException("uri and item id cannot be empty or -1");
                }

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
        {
            return;
        }
        int colItemId;
        int loaderId = loader.getId();
        switch (loaderId)
        {
            case ITEM_PRICE_LOADER_ID:
                priceMgr = new PriceMgr(mCountryCode);
                priceMgr.createPrices(cursor);
                populatePricesInputFields();
                break;

            case ITEM_PICTURE_LOADER_ID:
                Picture pictureInDb = createPicture(cursor);
                colItemId = cursor.getColumnIndex(PicturesEntry.COLUMN_ITEM_ID);
                itemId = cursor.getLong(colItemId);
                mPictureMgr.setItemId(itemId);
                mPictureMgr.setOriginalPicture(pictureInDb);
                mPictureMgr.setViewOriginalPicture();
                setPictureView(mPictureMgr.getPictureForViewing());
                break;
        }
    }

    protected Picture createPicture(Cursor cursor)
    {
        int colRowId = cursor.getColumnIndex(PicturesEntry._ID);
        int colPicturePath = cursor.getColumnIndex(PicturesEntry.COLUMN_FILE_PATH);
        Picture pictureInDb = null;
        if (cursor.moveToFirst())
        {
            pictureInDb = new Picture(cursor.getLong(colRowId), cursor.getString(colPicturePath));
        }

        return pictureInDb;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // If the loader is invalidated, clear out all the data from the input fields.
        clearPriceInputFields();
        clearPictureField();
    }

    private void clearPictureField()
    {
        mImgItemPic.setImageBitmap(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PICTURE_MANAGER, mPictureMgr);

    }

    private class CurrencyCodeChecker implements View.OnFocusChangeListener
    {
        @Override
        public void onFocusChange(View v, boolean hasFocus)
        {
            EditText etCurrencyCode = (EditText) v;
            String newCurrencyCode = etCurrencyCode.getText().toString();

            String originalCurrencyCode = ItemActivity.this.priceMgr.getUnitPrice()
                    .getCurrencyCode();
            try
            {
                if (FormatHelper.validateCurrencyCode(newCurrencyCode) &&
                        !newCurrencyCode.equals(priceMgr.getUnitPrice().getCurrencyCode()))
                {
                    setCurrencySymbol(newCurrencyCode);
                }

            } catch (IllegalArgumentException argEx)
            {
                Toast.makeText(ItemActivity.this, argEx.getMessage(), Toast.LENGTH_SHORT).show();
                etCurrencyCode.setText(originalCurrencyCode);
                setCurrencySymbol(originalCurrencyCode);
            } catch (Exception ex)
            {
                Toast.makeText(ItemActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                etCurrencyCode.setText(originalCurrencyCode);
                setCurrencySymbol(originalCurrencyCode);
            }
        }
    }
}
