package com.mirzairwan.shopping;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.domain.ItemInShoppingList;
import com.mirzairwan.shopping.domain.Price;

import java.text.ParseException;

import static com.mirzairwan.shopping.LoaderHelper.PURCHASE_ITEM_LOADER_ID;
import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class ShoppingListEditingActivity extends ItemActivity
{
        public static final int CREATE_BUY_ITEM_MODE = 1; //use for action mode
        public static final int EDIT_BUY_ITEM_MODE = 2; //use for action mode
        private static final String LOG_TAG = ShoppingListEditingActivity.class.getSimpleName();
        private static final String URI_ITEM = "uri"; //Used for saving instant state
        private int actionMode = -1; //Informs the editor whether this activity is creation or updating

        private boolean isItemDeleted = false; // After deleting, don't update picture and prices

        private EditText etQty;
        private RadioGroup rgPriceTypeChoice;

        private long defaultShopId = 1;
        private ItemInShoppingList mToBuyItem;
        private Uri mUriItem;

        private PurchaseManager mPurchaseManager;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                Log.d(LOG_TAG, ">>>savedInstantState is " + (savedInstanceState == null ? "NULL" : "NOT " + "NULL"));

                etQty = (EditText) findViewById(R.id.et_item_quantity);
                etQty.setOnTouchListener(mOnTouchListener);

                //Set default quantity to 1. If I set this in xml layout , it will jumble up the number and hint. This did not happen at SDK v24
                etQty.setText("1");

                //set touchListener for Radio Group
                rgPriceTypeChoice = (RadioGroup) findViewById(R.id.price_type_choice);
                rgPriceTypeChoice.setOnTouchListener(mOnTouchListener);


                if (savedInstanceState != null) //Restore from previous state
                {
                        mUriItem = savedInstanceState.getParcelable(URI_ITEM);
                }
                else
                {
                        Intent intent = getIntent();
                        mUriItem = intent.getData();
                }

                if (mUriItem == null)
                {
                        setTitle(R.string.new_buy_item_title);

                        // This flag is used for menu creation and database operation
                        actionMode = CREATE_BUY_ITEM_MODE;
                }
                else
                {
                        setTitle(R.string.view_buy_item_details);
                        actionMode = EDIT_BUY_ITEM_MODE; //This flag is used for database operation
                }

                initLoaders(mUriItem);
        }

        @Override
        protected void onResume()
        {
                super.onResume();
        }

        private void initLoaders(Uri uri)
        {
                //if no uri, then the request is for new item in shopping list
                if (uri != null)
                {
                        Bundle arg = new Bundle();
                        arg.putParcelable(ITEM_URI, uri);

                        getLoaderManager().initLoader(PURCHASE_ITEM_LOADER_ID, arg, this);
                        super.initPictureLoader(uri, this);
                        super.initPriceLoader(uri, this);
                }
                else
                {
                        mPurchaseManager = new PurchaseManager();
                }
        }

        @Override
        protected int getLayoutXml()
        {
                return R.layout.activity_buying_item;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
        {
                String[] projection = null;
                Uri uri;
                Loader<Cursor> loader = null;

                switch (loaderId)
                {
                        case PURCHASE_ITEM_LOADER_ID:
                                projection = new String[]{Contract.ToBuyItemsEntry._ID, Contract.ToBuyItemsEntry.COLUMN_ITEM_ID, Contract.ToBuyItemsEntry.COLUMN_QUANTITY, Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED, Contract.ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, Contract.ItemsEntry.COLUMN_NAME, Contract.ItemsEntry.COLUMN_BRAND, Contract
                                        .ItemsEntry.COLUMN_COUNTRY_ORIGIN, Contract.ItemsEntry.COLUMN_DESCRIPTION, Contract.PricesEntry.ALIAS_ID, Contract.PricesEntry.COLUMN_PRICE_TYPE_ID, Contract.PricesEntry.COLUMN_PRICE, Contract.PricesEntry.COLUMN_BUNDLE_QTY, Contract.PricesEntry.COLUMN_CURRENCY_CODE, Contract.PricesEntry.COLUMN_SHOP_ID};
                                uri = args.getParcelable(ITEM_URI);
                                loader = new CursorLoader(this, uri, projection, null, null, null);
                                break;

                        default:
                                loader = super.onCreateLoader(loaderId, args); //Reuse superclass
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
                int loaderId = loader.getId();
                switch (loaderId)
                {
                        case PURCHASE_ITEM_LOADER_ID:

                                mPurchaseManager = new PurchaseManager(cursor);

                                // Proceed with moving to the first row of the cursor for purchase mItem and
                                // reading data from it. This should be the only purchase  row in the cursor.
                                mItem = mPurchaseManager.getitem();
                                mToBuyItem = mPurchaseManager.getItemInShoppingList();
                                mPictureMgr.setItemId(mPurchaseManager.getitem().getId());
                                populateItemInputFields(mItem);
                                populatePurchaseInputFields(mToBuyItem);
                                break;

                        default:
                                if (isItemDeleted) //Don't populate prices and pictures
                                {
                                        return;
                                }
                                else
                                {
                                        super.onLoadFinished(loader, cursor);
                                }
                }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
                mPurchaseManager = null;
                super.onLoaderReset(loader);
                clearPurchaseInputFields();
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu)
        {
                if (actionMode == CREATE_BUY_ITEM_MODE)
                {
                        menu.removeItem(R.id.menu_remove_item_from_list);
                }
                return super.onPrepareOptionsMenu(menu);
        }

        private void populatePurchaseInputFields(ItemInShoppingList toBuyItem)
        {
                etQty.setText(String.valueOf(toBuyItem.getQuantity()));
                rgPriceTypeChoice.check(toBuyItem.getSelectedPriceType() == BUNDLE_PRICE ? R.id.rb_bundle_price : R.id.rb_unit_price);
        }

        private void clearPurchaseInputFields()
        {
                etQty.setText("");
                rgPriceTypeChoice.clearCheck();
        }

        @Override
        protected void delete()
        {
                //the onLoaderFinished method will use this flag to decide whether to load prices
                isItemDeleted = true;
                Uri uriDeleteBuyItem = ContentUris.withAppendedId(Contract.ToBuyItemsEntry.CONTENT_URI, mToBuyItem.getId());
                getContentResolver().delete(uriDeleteBuyItem, null, null);
                finish();
        }

        @Override
        protected boolean areFieldsValid()
        {
                boolean result = super.areFieldsValid();
                if (TextUtils.isEmpty(etQty.getText()) || Integer.parseInt(etQty.getText().toString()) < 1)
                {
                        alertRequiredField(R.string.message_title, R.string.mandatory_quantity);
                        etQty.requestFocus();
                        result = false;
                }

                return result;
        }

        @Override
        protected void save()
        {
                if (!areFieldsValid())
                {
                        return;
                }

                populateItemFromInputFields(mPurchaseManager.getitem());

                String itemQuantity = etQty.getText().toString();
                mPurchaseManager.getItemInShoppingList().setQuantity(Integer.parseInt(itemQuantity));

                String currencyCode = etCurrencyCode.getText().toString();
                priceMgr.setCurrencyCode(currencyCode);

                try
                {
                        priceMgr.setItemPricesForSaving(mPurchaseManager.getitem(), mUnitPriceEditField.getPrice(), mBundlePriceEditField.getPrice(), getBundleQtyFromInputField());
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                        alertRequiredField(R.string.message_title, R.string.invalid_price);
                        return;
                }

                mPurchaseManager.getItemInShoppingList().selectPrice(defaultShopId, getSelectedPriceType());

                String msg;

                if (actionMode == CREATE_BUY_ITEM_MODE)
                {

                        msg = daoManager.insert(mPurchaseManager.getItemInShoppingList(), mPurchaseManager.getitem(), mPurchaseManager.getitem().getPrices(), mPictureMgr);

                }
                else //Existing item in the shopping list
                {
                        msg = daoManager.update(mPurchaseManager.getItemInShoppingList(), mPurchaseManager.getitem(), mPurchaseManager.getitem().getPrices(), mPictureMgr);
                }

                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                finish();
        }

        protected Price.Type getSelectedPriceType()
        {
                int idSelected = rgPriceTypeChoice.getCheckedRadioButtonId();
                Price.Type selectedPriceType = (idSelected == R.id.rb_bundle_price) ? Price.Type.BUNDLE_PRICE : UNIT_PRICE;
                return selectedPriceType;
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
                super.onSaveInstanceState(outState);
                outState.putParcelable(URI_ITEM, mUriItem);
        }
}
