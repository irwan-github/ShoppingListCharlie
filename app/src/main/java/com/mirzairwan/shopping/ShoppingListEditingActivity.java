package com.mirzairwan.shopping;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.mirzairwan.shopping.data.Contract;

import static com.mirzairwan.shopping.LoaderHelper.PURCHASE_ITEM_LOADER_ID;

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
        private Uri mUriItem;
        private ShoppingItemControl mShoppingItemControl;
        private ItemBuyQtyControl mItemBuyQtyControl;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                mShoppingItemControl = new ShoppingItemControl(this);
                mItemControl = mShoppingItemControl;

                super.onCreate(savedInstanceState);

                mItemBuyQtyControl = new ItemBuyQtyControl(this);
                mItemBuyQtyControl.setOnTouchListener(mOnTouchListener);
                mShoppingItemControl.setItemEditFieldControl(mItemEditFieldControl);

                Log.d(LOG_TAG, ">>>savedInstantState is " + (savedInstanceState == null ? "NULL" : "NOT " + "NULL"));

                mContainer = findViewById(R.id.shopping_list_editor_container);

                mItemBuyQtyControl.setPriceMgr(mPriceMgr);
                mItemBuyQtyControl.setPriceEditFieldControl(mPriceEditFieldControl);
                mShoppingItemControl.setItemBuyQtyFieldControl(mItemBuyQtyControl);
                mShoppingItemControl.setPriceEditFieldControl(mPriceEditFieldControl);

                PurchaseEditorExpander purchaseEditorExpander = new PurchaseEditorExpander(this);

                if (savedInstanceState != null) /* Restore from previous activity */
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
                        PurchaseManager purchaseManager = new PurchaseManager();
                        mShoppingItemControl.onNewItem();
                        mShoppingItemControl.setPurchaseManager(purchaseManager);
                        mItemBuyQtyControl.setPurchaseManager(purchaseManager);

                        /* Set unit price selection as default*/
                        //mItemBuyQtyControl.selectPriceType(Price.Type.UNIT_PRICE);
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

                                mShoppingItemControl.setPurchaseManager(mPurchaseManager);

                                mItemEditFieldControl.onLoadItemFinished(mPurchaseManager.getitem());

                                mItemBuyQtyControl.setPurchaseManager(mPurchaseManager);

                                mItemBuyQtyControl.onLoadFinished();

                                mPictureMgr.setItemId(mPurchaseManager.getitem().getId());
                                break;

                        default:
                                super.onLoadFinished(loader, cursor);
                }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
                int loaderId = loader.getId();
                switch (loaderId)
                {
                        case PURCHASE_ITEM_LOADER_ID:
                                mItemBuyQtyControl.onLoaderReset();
                                break;
                        default:
                                super.onLoaderReset(loader);
                }
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
                super.onSaveInstanceState(outState);
                outState.putParcelable(URI_ITEM, mUriItem);
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
                mDbMsg = daoManager.update(purchaseManager.getItemInShoppingList(), purchaseManager.getitem(), mPriceMgr.getPrices(), mPictureMgr);
        }

        public void insert(PurchaseManager purchaseManager)
        {
                mDbMsg = daoManager.insert(purchaseManager.getItemInShoppingList(), purchaseManager.getitem(), mPriceMgr.getPrices(), mPictureMgr);
        }

}
