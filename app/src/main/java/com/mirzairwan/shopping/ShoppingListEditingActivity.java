package com.mirzairwan.shopping;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.domain.ItemInShoppingList;
import com.mirzairwan.shopping.domain.Price;

import java.text.ParseException;

import static com.mirzairwan.shopping.LoaderHelper.PURCHASE_ITEM_LOADER_ID;
import static com.mirzairwan.shopping.R.id.rb_unit_price;
import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * <p>
 * Events delegated to state machine:
 * <p>
 * onBackPressed
 * onUpHome
 * onButtonSaveClick
 * onButtoDeleteClick
 */

public class ShoppingListEditingActivity extends ItemActivity implements ShoppingItemControl.ShoppingItemContext
{
        private static final String LOG_TAG = ShoppingListEditingActivity.class.getSimpleName();
        private static final String URI_ITEM = "uri"; /* Used for saving instant mItemType */

        private EditText etQtyToBuy;
        private RadioGroup rgPriceTypeChoice;
        private long defaultShopId = 1;

        private Uri mUriItem;
        private ShoppingItemControl mShoppingItemControl;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                Log.d(LOG_TAG, ">>>savedInstantState is " + (savedInstanceState == null ? "NULL" : "NOT " + "NULL"));

                mContainer = findViewById(R.id.shopping_list_editor_container);
                etQtyToBuy = (EditText) findViewById(R.id.et_item_quantity);
                etQtyToBuy.setOnTouchListener(mOnTouchListener);

                /* Set default quantity to 1. If I set this in xml layout , it will jumble up the number and hint. This did not happen at SDK v24 */
                etQtyToBuy.setText("1");

                /* set touchListener for Radio Group */
                rgPriceTypeChoice = (RadioGroup) findViewById(R.id.price_type_choice);
                rgPriceTypeChoice.setOnTouchListener(mOnTouchListener);
                findViewById(R.id.rb_unit_price).setOnTouchListener(mOnTouchListener);
                findViewById(R.id.rb_bundle_price).setOnTouchListener(mOnTouchListener);

                PurchaseEditorExpander purchaseEditorExpander = new PurchaseEditorExpander(this);

                if (savedInstanceState != null) //Restore from previous mItemType
                {
                        mUriItem = savedInstanceState.getParcelable(URI_ITEM);
                }
                else
                {
                        Intent intent = getIntent();
                        mUriItem = intent.getData();
                }

                mShoppingItemControl = new ShoppingItemControl(this);
                mItemControl = mShoppingItemControl;

                if (mUriItem == null)
                {
                        PurchaseManager mPurchaseManager = new PurchaseManager();
                        mShoppingItemControl.onNewItem(mPurchaseManager);
                }
                else
                {
                        mShoppingItemControl.onExistingItem();
                        initLoaders(mUriItem);
                }
        }

        @Override
        protected void onStart()
        {
                super.onStart();
        }

        @Override
        protected void onResume()
        {
                super.onResume();
        }

        private void initLoaders(Uri uri)
        {
                Bundle arg = new Bundle();
                arg.putParcelable(ITEM_URI, uri);

                getLoaderManager().initLoader(PURCHASE_ITEM_LOADER_ID, arg, this);
                super.initPictureLoader(uri, this);
                super.initPriceLoader(uri, this);
        }

        @Override
        protected int getLayoutXml()
        {
                return R.layout.activity_edt_shopping_list;
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
                                projection = new String[]{
                                        Contract.ToBuyItemsEntry._ID, Contract.ToBuyItemsEntry.COLUMN_ITEM_ID, Contract.ToBuyItemsEntry.COLUMN_QUANTITY, Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED, Contract.ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, Contract.ItemsEntry.COLUMN_NAME, Contract.ItemsEntry.COLUMN_BRAND, Contract.ItemsEntry
                                        .COLUMN_COUNTRY_ORIGIN, Contract.ItemsEntry.COLUMN_DESCRIPTION, Contract.PricesEntry.ALIAS_ID, Contract.PricesEntry.COLUMN_PRICE_TYPE_ID, Contract.PricesEntry.COLUMN_PRICE, Contract.PricesEntry.COLUMN_BUNDLE_QTY, Contract.PricesEntry.COLUMN_CURRENCY_CODE, Contract.PricesEntry.COLUMN_SHOP_ID};
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

                                /*
                                     Proceed with moving to the first row of the cursor for purchase mItem and
                                     reading data from it. This should be the only purchase  row in the cursor.
                                 */
                                PurchaseManager mPurchaseManager = new PurchaseManager(cursor);

                                mShoppingItemControl.onLoadItemFinished(mPurchaseManager);

                                mPictureMgr.setItemId(mPurchaseManager.getitem().getId());
                                break;

                        default:
                                super.onLoadFinished(loader, cursor);
                }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
                super.onLoaderReset(loader);
                clearPurchaseInputFields();
        }

        public void populatePurchaseInputFields(ItemInShoppingList toBuyItem)
        {
                etQtyToBuy.setText(String.valueOf(toBuyItem.getQuantity()));
                rgPriceTypeChoice.check(toBuyItem.getSelectedPriceType() == BUNDLE_PRICE ? R.id.rb_bundle_price : rb_unit_price);
        }

        private void clearPurchaseInputFields()
        {
                etQtyToBuy.setText("");
                rgPriceTypeChoice.clearCheck();
        }

        @Override
        public boolean areFieldsValid()
        {
                return false;
        }

        @Override
        public boolean areFieldsValid(PurchaseManager purchaseManager)
        {
                boolean areFieldsValid = super.areFieldsValid();
                if (!areFieldsValid)
                {
                        return areFieldsValid;
                }
                String qtyToBuy = etQtyToBuy.getText().toString();
                if (purchaseManager.isQuantityToBuyZero(qtyToBuy))
                {
                        etQtyToBuy.setError(getString(R.string.mandatory_quantity));
                        areFieldsValid = false;
                }

                if (getSelectedPriceType() == Price.Type.BUNDLE_PRICE)
                {
                        String bundleQty = etBundleQty.getText().toString();

                        if (purchaseManager.isBundleQuantityOne(qtyToBuy))
                        {
                                etQtyToBuy.setError(getString(R.string.invalid_bundle_buy_quantity_one));
                                areFieldsValid = false;
                        }
                        else if (purchaseManager.isBundleQuantityOne(bundleQty))
                        {
                                etBundleQty.setError(getString(R.string.invalid_bundle_quantity_one));
                                mPriceEditorExpander.expandMore();
                                areFieldsValid = false;
                        }
                        else if (!purchaseManager.isBundleQuantityToBuyValid(qtyToBuy, etBundleQty.getText().toString()))
                        {
                                etQtyToBuy.setError(getString(R.string.invalid_bundle_buy_quantity) + " " + bundleQty);
                                areFieldsValid = false;
                        }
                }
                return areFieldsValid;
        }

        /**
         * Query whether unit price or bundle price is selected
         *
         * @return unit price or bundle price
         */
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

        protected void prepareForDbOperation(PurchaseManager purchaseManager)
        {
                populateItemFromInputFields(purchaseManager.getitem());

                String itemQuantity = etQtyToBuy.getText().toString();
                purchaseManager.getItemInShoppingList().setQuantity(Integer.parseInt(itemQuantity));

                String currencyCode = etCurrencyCode.getText().toString();
                mPriceMgr.setCurrencyCode(currencyCode);

                try
                {
                        mPriceMgr.setItemPricesForSaving(mUnitPriceEditField.getPrice(), mBundlePriceEditField.getPrice(), getBundleQtyFromInputField());
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                        alertRequiredField(R.string.message_title, R.string.invalid_price);
                        return;
                }

                //Get selected price
                Price.Type selectedPriceType = getSelectedPriceType();
                Price selectedPrice = mPriceMgr.getSelectedPrice(selectedPriceType);
                purchaseManager.getItemInShoppingList().setSelectedPrice(selectedPrice);
        }

        @Override
        public void delete(long id)
        {
                Uri uriDeleteBuyItem = ContentUris.withAppendedId(Contract.ToBuyItemsEntry.CONTENT_URI, id);
                getContentResolver().delete(uriDeleteBuyItem, null, null);
                mDbMsg = "Deleted item ";
        }

        @Override
        public void update(PurchaseManager purchaseManager)
        {
                prepareForDbOperation(purchaseManager);
                mDbMsg = daoManager.update(purchaseManager.getItemInShoppingList(), purchaseManager.getitem(), mPriceMgr.getPrices(), mPictureMgr);
        }

        public void insert(PurchaseManager purchaseManager)
        {
                prepareForDbOperation(purchaseManager);
                mDbMsg = daoManager.insert(purchaseManager.getItemInShoppingList(), purchaseManager.getitem(), mPriceMgr.getPrices(), mPictureMgr);
        }

}
