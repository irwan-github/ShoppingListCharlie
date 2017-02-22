package com.mirzairwan.shopping;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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
import static com.mirzairwan.shopping.ItemStateMachine.State.NEW;
import static com.mirzairwan.shopping.ItemStateMachine.State.EXIST;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 *
 * Events delegated to state machine:
 *
 * onBackPressed
 * onUpHome
 * onButtonSaveClick
 * onButtoDeleteClick
 */

public class ShoppingListEditingActivity extends ItemActivity implements ItemStateMachine.ItemContext
{
        private static final String LOG_TAG = ShoppingListEditingActivity.class.getSimpleName();
        private static final String URI_ITEM = "uri"; //Used for saving instant mState

        private EditText etQtyToBuy;
        private RadioGroup rgPriceTypeChoice;
        private long defaultShopId = 1;
        private ItemInShoppingList mToBuyItem;
        private Uri mUriItem;
        private PurchaseManager mPurchaseManager;
        private View mContainer;
        private String mDbMsg;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                Log.d(LOG_TAG, ">>>savedInstantState is " + (savedInstanceState == null ? "NULL" : "NOT " + "NULL"));

                mContainer = findViewById(R.id.shopping_list_editor_container);
                etQtyToBuy = (EditText) findViewById(R.id.et_item_quantity);
                etQtyToBuy.setOnTouchListener(mOnTouchListener);

                //Set default quantity to 1. If I set this in xml layout , it will jumble up the number and hint. This did not happen at SDK v24
                etQtyToBuy.setText("1");

                //set touchListener for Radio Group
                rgPriceTypeChoice = (RadioGroup) findViewById(R.id.price_type_choice);
                rgPriceTypeChoice.setOnTouchListener(mOnTouchListener);
                findViewById(R.id.rb_unit_price).setOnTouchListener(mOnTouchListener);
                findViewById(R.id.rb_bundle_price).setOnTouchListener(mOnTouchListener);

                PurchaseEditorExpander purchaseEditorExpander = new PurchaseEditorExpander(this);

                if (savedInstanceState != null) //Restore from previous mState
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
                        mItemStateMachine = new ItemStateMachine(this, NEW);
                }
                else
                {
                        setTitle(R.string.view_buy_item_details);
                        mItemStateMachine = new ItemStateMachine(this, EXIST);
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
                                super.onLoadFinished(loader, cursor);
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
                if (mItemStateMachine.getState() == NEW)
                {
                        menu.removeItem(R.id.menu_remove_item_from_list);
                }
                return super.onPrepareOptionsMenu(menu);
        }

        private void populatePurchaseInputFields(ItemInShoppingList toBuyItem)
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
        public void delete()
        {
                Uri uriDeleteBuyItem = ContentUris.withAppendedId(Contract.ToBuyItemsEntry.CONTENT_URI, mToBuyItem.getId());
                getContentResolver().delete(uriDeleteBuyItem, null, null);
                mDbMsg = "Deleted item " + mPurchaseManager.getitem().getName();
        }

        @Override
        public boolean areFieldsValid()
        {
                boolean areFieldsValid = super.areFieldsValid();
                if (!areFieldsValid)
                {
                        return areFieldsValid;
                }
                String qtyToBuy = etQtyToBuy.getText().toString();
                if (mPurchaseManager.isQuantityToBuyZero(qtyToBuy))
                {
                        etQtyToBuy.setError(getString(R.string.mandatory_quantity));
                        areFieldsValid = false;
                }

                if (getSelectedPriceType() == Price.Type.BUNDLE_PRICE)
                {
                        String bundleQty = etBundleQty.getText().toString();

                        if (mPurchaseManager.isBundleQuantityOne(qtyToBuy))
                        {
                                etQtyToBuy.setError(getString(R.string.invalid_bundle_buy_quantity_one));
                                areFieldsValid = false;
                        }
                        else if (mPurchaseManager.isBundleQuantityOne(bundleQty))
                        {
                                etBundleQty.setError(getString(R.string.invalid_bundle_quantity_one));
                                mPriceEditorExpander.expandMore();
                                areFieldsValid = false;
                        }
                        else if (!mPurchaseManager.isBundleQuantityToBuyValid(qtyToBuy, etBundleQty.getText().toString()))
                        {
                                etQtyToBuy.setError(getString(R.string.invalid_bundle_buy_quantity) + " " + bundleQty);
                                areFieldsValid = false;
                        }
                }
                return areFieldsValid;
        }

        @Override
        public void save()
        {
                mItemStateMachine.onProcessSave();
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

        protected void prepareForDbOperation()
        {
                populateItemFromInputFields(mPurchaseManager.getitem());

                String itemQuantity = etQtyToBuy.getText().toString();
                mPurchaseManager.getItemInShoppingList().setQuantity(Integer.parseInt(itemQuantity));

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
                mPurchaseManager.getItemInShoppingList().setSelectedPrice(selectedPrice);
        }


        @Override
        public void postDbProcess()
        {
                Snackbar.make(mContainer, mDbMsg, 5000).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>()
                {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event)
                        {
                                mItemStateMachine.onLeave();
                        }
                }).show();
        }

        @Override
        public void update()
        {
                prepareForDbOperation();

                mDbMsg = daoManager.update(mPurchaseManager.getItemInShoppingList(), mPurchaseManager.getitem(), mPriceMgr.getPrices(), mPictureMgr);

        }


        @Override
        public void insert()
        {
                prepareForDbOperation();

                mDbMsg = daoManager.insert(mPurchaseManager.getItemInShoppingList(), mPurchaseManager.getitem(), mPriceMgr.getPrices(), mPictureMgr);

        }

}
