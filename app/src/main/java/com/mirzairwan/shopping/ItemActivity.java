package com.mirzairwan.shopping;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.ExchangeRate;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.PictureMgr;
import com.mirzairwan.shopping.domain.PriceMgr;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mirzairwan.shopping.LoaderHelper.ITEM_PICTURE_LOADER_ID;
import static com.mirzairwan.shopping.LoaderHelper.ITEM_PRICE_LOADER_ID;
import static com.mirzairwan.shopping.ShoppingActivity.EXCHANGE_RATE;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * This is a base class for subclasses to leverage on the following:
 * <p>
 * Loader for Prices table. Subclass just need to call initLoader with the correct loader id.
 * <p>
 * Loader for Pictures table. Subclass just need to call initLoader with the correct loader id.
 * <p>
 * Create Item objects and populate screen.
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
public abstract class ItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
        public static final String ITEM_IS_IN_SHOPPING_LIST = "ITEM_IS_IN_SHOPPING_LIST";
        protected static final String ITEM_URI = "ITEM_URI";
        private static final int REQUEST_SNAP_PICTURE = 15;
        private static final String LOG_TAG = ItemActivity.class.getSimpleName();
        private static final int REQUEST_PICK_PHOTO = 16;
        private static final String PICTURE_MANAGER = "picture_mgr";

        protected EditText etName;
        protected EditText etBrand;
        protected EditText etDescription;
        protected EditText etCountryOrigin;
        protected EditText etBundleQty;
        protected View.OnTouchListener mOnTouchListener;
        protected boolean mItemHaveChanged = false;
        protected Item mItem;
        protected DaoManager daoManager;
        protected PictureMgr mPictureMgr;
        protected PriceMgr priceMgr;
        protected String mCountryCode;
        protected EditText etCurrencyCode;
        protected PriceField mUnitPriceEditField;
        protected PriceField mBundlePriceEditField;
        private ImageView mImgItemPic;
        private long itemId;
        private ExchangeRateInput mExchangeRateInput;

        //During orientation, the exchange rate fields are not populated by the exchange rate loader. So need to save its instance state
        //and restore when device orientates to landscape.
        private ExchangeRate mExchangeRate;

        private String mWebApiBase;
        private ItemEditorView mItemEditorView;
        private PriceEditorView mPriceEditorView;

        @Override
        protected void onPause()
        {
                super.onPause();
                Log.d(LOG_TAG, "onPause");
        }

        @Override
        protected void onResume()
        {
                super.onResume();
                Log.d(LOG_TAG, "onResume");
        }

        @Override
        protected void onStop()
        {
                super.onStop();
                Log.d(LOG_TAG, "onStop");
        }

        @Override
        protected void onRestart()
        {
                super.onRestart();
                Log.d(LOG_TAG, "onRestart");
        }

        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState)
        {
                Log.d(LOG_TAG, "onRestoreInstanceState");
                super.onRestoreInstanceState(savedInstanceState);

                //Any translated price will be invisible when device orientates. So show it again
                mExchangeRate = savedInstanceState.getParcelable(EXCHANGE_RATE);
                mUnitPriceEditField.setTranslatedPrice(mExchangeRate);
                mBundlePriceEditField.setTranslatedPrice(mExchangeRate);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
                setContentView(getLayoutXml());

                Toolbar mainToolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(mainToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                setupPictureToolbar();

                daoManager = Builder.getDaoManager(this);

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                mCountryCode = sharedPrefs.getString(getString(R.string.user_country_pref), null);
                priceMgr = new PriceMgr(mCountryCode);

                String webApiKeyPref = getString(R.string.key_forex_web_api_1);
                mWebApiBase = sharedPrefs.getString(webApiKeyPref, null);
                mExchangeRateInput = new ExchangeRateInput();
                mExchangeRateInput.setBaseWebApi(mWebApiBase);
                mExchangeRateInput.setBaseCurrency(FormatHelper.getCurrencyCode(mCountryCode));

                mExchangeRate = getIntent().getParcelableExtra(EXCHANGE_RATE);
                if (mExchangeRate != null)
                {
                        mExchangeRateInput.addSourceCurrency(mExchangeRate.getSourceCurrencyCode());
                }

                setupViews();

                if (savedInstanceState == null)
                {
                        Log.d(LOG_TAG, "OnCreate with NULL savedInstanceState");
                        mPictureMgr = new PictureMgr();
                }
                else
                {
                        Log.d(LOG_TAG, "OnCreate with savedInstanceState");
                        mPictureMgr = savedInstanceState.getParcelable(PICTURE_MANAGER);
                }
        }

        protected void setupViews()
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
                mItemEditorView = new ItemEditorView(this);
                mImgItemPic = (ImageView) findViewById(R.id.img_item);

                etName.setOnTouchListener(mOnTouchListener);
                etBrand.setOnTouchListener(mOnTouchListener);
                etDescription.setOnTouchListener(mOnTouchListener);
                etCountryOrigin.setOnTouchListener(mOnTouchListener);

                //Set default currency code
                etCurrencyCode = (EditText) findViewById(R.id.et_currency_code);
                etCurrencyCode.setText(FormatHelper.getCurrencyCode(mCountryCode));

                EditText etUnitPrice = (EditText) findViewById(R.id.et_unit_price);
                EditText etTranslatedUnitPrice = (EditText) findViewById(R.id.et_translated_unit_price);
                etUnitPrice.setOnTouchListener(mOnTouchListener);
                View pbUnitPx = findViewById(R.id.pb_translated_unit_px);
                mUnitPriceEditField = new PriceField(etUnitPrice, etTranslatedUnitPrice, pbUnitPx, mCountryCode, getString(R.string.unit_price_txt));

                EditText etBundlePrice = (EditText) findViewById(R.id.et_bundle_price);
                EditText etTranslatedBundlePrice = (EditText) findViewById(R.id.et_translated_bundle_price);
                etBundlePrice.setOnTouchListener(mOnTouchListener);
                View pbBundlePx = findViewById(R.id.pb_translated_bundle_px);
                mBundlePriceEditField = new PriceField(etBundlePrice, etTranslatedBundlePrice, pbBundlePx, mCountryCode, getString(R.string.bundle_price_txt));

                OnCurrencyCodeChange onCurrencyCodeChange = new OnCurrencyCodeChange(this, etCurrencyCode, mCountryCode, mUnitPriceEditField, mBundlePriceEditField, mExchangeRateInput);
                MyTextUtils.setAllCapsInputFilter(etCurrencyCode);
                etCurrencyCode.setOnTouchListener(mOnTouchListener);
                etCurrencyCode.setOnFocusChangeListener(onCurrencyCodeChange);
                ItemExchangeRateLoaderCallback pxExLoaderCb = new ItemExchangeRateLoaderCallback(this);
                getLoaderManager().initLoader(78, null, pxExLoaderCb);

                etBundleQty = (EditText) findViewById(R.id.et_bundle_qty);
                etBundleQty.setOnTouchListener(mOnTouchListener);

                mPriceEditorView = new PriceEditorView(this);
        }

        protected abstract int getLayoutXml();

        protected void setupPictureToolbar()
        {

                Toolbar toolbarPicture = (Toolbar) findViewById(R.id.picture_toolbar);

                // Inflate a menu to be displayed in the toolbar
                toolbarPicture.inflateMenu(R.menu.picture_item_toolbar);

                //check for the availability of the camera at runtime. If unavailable, disable the affected action item
                PackageManager pMgr = getPackageManager();
                if (!pMgr.hasSystemFeature(PackageManager.FEATURE_CAMERA))
                {
                        MenuItem itemCamera = toolbarPicture.getMenu().findItem(R.id.menu_camera);
                        itemCamera.setEnabled(false);
                }

                //Add handler to handle menu item clicks
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
                                                boolean hasReadStoragePermission = PermissionHelper.hasReadStoragePermission(ItemActivity.this);
                                                if (hasReadStoragePermission)
                                                {
                                                        startPickPictureActivity();
                                                }
                                                else
                                                {
                                                        PermissionHelper.setupStorageReadPermission(ItemActivity.this);
                                                }
                                                return true;
                                        case R.id.remove_picture:
                                                deletePictureInView();
                                                return true;
                                        default:
                                                return false;
                                }

                        }
                });

                PermissionHelper.setupStorageReadPermission(this);
        }

        protected void initPriceLoader(Uri uri, LoaderManager.LoaderCallbacks<Cursor> callback)
        {
                if (ContentUris.parseId(uri) == -1)
                {
                        throw new IllegalArgumentException("uri and item id cannot be empty or -1");
                }

                Bundle arg = new Bundle();
                arg.putParcelable(ITEM_URI, uri);
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

                /**
                 * Performing this check is important because if you call startActivityForResult() using an intent that no app can handle, your app will crash.
                 */
                if (cameraIntent.resolveActivity(getPackageManager()) != null)
                {
                        File itemPicFile = null;

                        /**
                         * This method returns a standard location for saving pictures and videos which are associated with your application.
                         * If your application is uninstalled, any files saved in this location are removed.
                         * Security is not enforced for files in this location and other applications may read, change and delete them.
                         * However, DAC also states that beginning with Android 4.4, the permission is no longer required because the directory is not accessible by other apps ....
                         * On Nexus 5, the storage directory returned is "/storage/emulated/0/Android/data/com.mirzairwan.shopping/files/Pictures"
                         */
                        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                        try
                        {
                                //File for use with ACTION_VIEW intents.
                                //File path is /storage/emulated/0/Android/data/com.mirzairwan.shopping/files/Pictures/Item__***_***_-***.jpg
                                itemPicFile = PictureMgr.createFileHandle(externalFilesDir);
                        }
                        catch(IOException e)
                        {
                                e.printStackTrace();
                                Toast.makeText(this, "Photo file cannot be created. Aborting camera operation", Toast.LENGTH_SHORT).show();
                                return;
                        }

                        if (itemPicFile != null)
                        {
                                mPictureMgr.setPictureForViewing(itemPicFile);

                                /**
                                 * Returns a content:// URI. For more recent apps targeting Android 7.0 (API level 24) and higher, passing a file:// URI across a package boundary causes a
                                 * FileUriExposedException. Therefore, we now use a more generic way of storing images using a FileProvider.
                                 * We need to configure the FileProvider. In app's manifest, add a provider to your application
                                 *
                                 * content://com.mirzairwan.shopping.fileprovider/item_images/Item__30012017_190919_-283901926.jpg
                                 */
                                String appPackage = getApplicationContext().getPackageName();
                                Uri itemPicUri = FileProvider.getUriForFile(this, appPackage + ".fileprovider", itemPicFile);

                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, itemPicUri);
                                startActivityForResult(cameraIntent, REQUEST_SNAP_PICTURE);
                        }

                }
                else
                {
                        Toast.makeText(this, "No camera app(s) found. Aborting camera operation", Toast.LENGTH_SHORT).show();
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
                                                //Delete existing file except original picture.
                                                daoManager.cleanUpDiscardedPictures(mPictureMgr);
                                                String picturePath = mPictureMgr.getPictureForViewing().getPicturePath();
                                                ImageFsResizer imageFsResizer = new ImageFsResizer(mImgItemPic);
                                                imageFsResizer.execute(picturePath);
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
                daoManager.cleanUpDiscardedPictures(mPictureMgr); //delete previous picture. But original
                // picture will not be deleted
                setPictureView(externalPicture);
        }

        /**
         * Called after external camera task or picture picking task.
         * Also called when activity is being created and loading data.
         *
         * @param picture
         */
        protected void setPictureView(Picture picture)
        {
                if (picture == null)
                {
                        mImgItemPic.setImageBitmap(null);
                        return;
                }

                // Get the dimensions of the desired scale dimension
                int targetW = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_width);
                int targetH = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_height);

                //If picture is an external file, make sure read storage permission is granted
                if (!PictureMgr.isExternalFile(picture) | PermissionHelper.hasReadStoragePermission(this))
                {
                        //Spin a background thread to display picture
                        ImageResizer imageResizer = new ImageResizer(this, targetW, targetH);
                        imageResizer.loadImage(picture.getFile(), mImgItemPic);
                }
        }

        @Override
        protected void onStart()
        {
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
         * Validate item details. Invoked when user clicks on save button.
         * Show error messages.
         *
         * @return true if input fields are valid
         */
        protected boolean areFieldsValid()
        {
                if (TextUtils.isEmpty(etName.getText()))
                {
                        alertRequiredField(R.string.message_title, R.string.mandatory_name);
                        return false;
                }

                Editable currencyCodeEditable = etCurrencyCode.getText();
                boolean isCurrencyCodeValid = !TextUtils.isEmpty(currencyCodeEditable) && FormatHelper.isValidCurrencyCode(currencyCodeEditable.toString());

                if (!isCurrencyCodeValid)
                {
                        etCurrencyCode.clearFocus();
                        alertRequiredField(R.string.invalid_currency_code_msg, R.string.valid_country_code_msg);
                        return false;
                }

                return true;
        }

        /**
         * Call fieldsValidated method before calling this method
         * <p>
         * Update an existing member item or if creation, create a new item
         *
         * @return
         */
        protected void populateItemFromInputFields(Item item)
        {
                String itemName = etName.getText().toString();
                String itemBrand = etBrand.getText().toString();
                String countryOrigin = etCountryOrigin.getText().toString();
                String itemDescription = etDescription.getText().toString();

                item.setName(itemName);
                item.setBrand(itemBrand);
                item.setCountryOrigin(countryOrigin);
                item.setDescription(itemDescription);
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

        protected abstract void save();

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
                String currencyCode = priceMgr.getUnitPrice().getCurrencyCode();
                etCurrencyCode.setText(currencyCode);
                mUnitPriceEditField.setPrice(currencyCode, priceMgr.getUnitPriceForDisplay());
                mBundlePriceEditField.setPrice(currencyCode, priceMgr.getBundlePriceForDisplay());
                etBundleQty.setText(FormatHelper.formatToTwoDecimalPlaces((priceMgr.getBundlePrice().getBundleQuantity())));

                if (!isItemLocalPriced(currencyCode))
                {
                        mUnitPriceEditField.setTranslatedPrice(mExchangeRate);
                        mBundlePriceEditField.setTranslatedPrice(mExchangeRate);
                }
        }

        private boolean isItemLocalPriced(String currencyCode)
        {
                String homeCurrencyCode = FormatHelper.getCurrencyCode(mCountryCode);
                return currencyCode.equalsIgnoreCase(homeCurrencyCode);
        }

        protected void clearPriceInputFields()
        {
                mUnitPriceEditField.clear();
                mBundlePriceEditField.clear();
                etBundleQty.setText("");
        }

        /**
         * Select following child records of  item:
         * 1. Prices
         * 2. Pictures
         *
         * @param loaderId Identify type of SQL query
         * @param args     Store item id
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
                                loader = new CursorLoader(this, PricesEntry.CONTENT_URI, projection, selection, selectionArgs, null);
                                break;

                        case ITEM_PICTURE_LOADER_ID:
                                projection = new String[]{PicturesEntry._ID,
                                                          PicturesEntry.COLUMN_FILE_PATH,
                                                          PicturesEntry.COLUMN_ITEM_ID};
                                itemId = ContentUris.parseId(uri);
                                if (itemId == -1)
                                {
                                        throw new IllegalArgumentException("uri and item id cannot be empty or -1");
                                }

                                selection = PicturesEntry.COLUMN_ITEM_ID + "=?";
                                selectionArgs = new String[]{String.valueOf(itemId)};
                                loader = new CursorLoader(this, PicturesEntry.CONTENT_URI, projection, selection, selectionArgs, null);
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

                                //Important to move cursor back before the first record because when device switches to landscape, it gives back the same cursor with
                                // the pre-exisiting state
                                cursor.moveToPosition(-1);
                                break;

                        case ITEM_PICTURE_LOADER_ID:
                                Picture pictureInDb = createPicture(cursor);
                                colItemId = cursor.getColumnIndex(PicturesEntry.COLUMN_ITEM_ID);
                                itemId = cursor.getLong(colItemId);
                                mPictureMgr.setItemId(itemId);
                                mPictureMgr.setOriginalPicture(pictureInDb);
                                mPictureMgr.setViewOriginalPicture();
                                setPictureView(mPictureMgr.getPictureForViewing());

                                //Important to move cursor back before the first record because when device switches to landscape, it gives back the same cursor with
                                // the pre-exisiting state
                                cursor.moveToPosition(-1);
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
                Log.d(LOG_TAG, "onSaveInstanceState");
                super.onSaveInstanceState(outState);
                outState.putParcelable(PICTURE_MANAGER, mPictureMgr);
                outState.putParcelable(EXCHANGE_RATE, mExchangeRate);
        }

        class ImageFsResizer extends AsyncTask<String, Void, Bitmap>
        {
                private WeakReference<ImageView> imageViewReference;

                public ImageFsResizer(ImageView imageView)
                {
                        imageViewReference = new WeakReference<ImageView>(imageView);
                }

                @Override
                protected Bitmap doInBackground(String... picturePath)
                {
                        int width = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_width);
                        int height = getResources().getDimensionPixelSize(R.dimen.picture_item_detail_height);
                        Bitmap resizedBitmap = PictureUtil.decodeSampledBitmapFile(picturePath[0], width, height);
                        String appPackage = getApplicationContext().getPackageName();
                        Uri itemPicUri = FileProvider.getUriForFile(ItemActivity.this, appPackage + ".fileprovider", mPictureMgr.getPictureForViewing().getFile());

                        //Delete the big picture file
                        int deleted = getContentResolver().delete(itemPicUri, null, null);

                        //Save the resized image
                        PictureUtil.savePictureInFilesystem(resizedBitmap, picturePath[0]);
                        return resizedBitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap)
                {
                        if (imageViewReference != null && bitmap != null)
                        {
                                ImageView imageView = imageViewReference.get();
                                if (imageView != null)
                                {
                                        imageView.setImageBitmap(bitmap);
                                }
                        }
                }
        }

        protected class ItemExchangeRateLoaderCallback extends ExchangeRateLoaderCallback
        {
                private final String LOG_TAG = ItemExchangeRateLoaderCallback.class.getSimpleName();
                private List<PriceField> priceFields = new ArrayList<>();

                ItemExchangeRateLoaderCallback(Context context)
                {
                        super(context, mExchangeRateInput);
                }

                @Override
                public void onLoadFinished(Loader<Map<String, ExchangeRate>> loader, Map<String, ExchangeRate> exchangeRates)
                {
                        Log.d(LOG_TAG, "onLoadFinished");
                        Log.d(LOG_TAG, ">>> Loader id: " + loader.getId());
                        Log.d("CurrencyCode", "onLoadFinished");
                        if (exchangeRates != null)
                        {
                                String sourceCurrencyCode = etCurrencyCode.getText().toString();
                                mExchangeRate = exchangeRates.get(sourceCurrencyCode);
                                mUnitPriceEditField.setTranslatedPrice(mExchangeRate);
                                mBundlePriceEditField.setTranslatedPrice(mExchangeRate);
                        }
                        else
                        {
                                Log.d(LOG_TAG, ">>> Exchange rates is null");
                                mUnitPriceEditField.setTranslatedPrice(null);
                                mBundlePriceEditField.setTranslatedPrice(null);
                        }
                }

                @Override
                public void onLoaderReset(Loader<Map<String, ExchangeRate>> loader)
                {
                        Log.d(LOG_TAG, "onLoaderReset");
                        mExchangeRate = null;
                }
        }
}
