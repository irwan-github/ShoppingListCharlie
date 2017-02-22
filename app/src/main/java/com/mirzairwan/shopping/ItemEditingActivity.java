package com.mirzairwan.shopping;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.mirzairwan.shopping.data.Contract;

import java.text.ParseException;

import static com.mirzairwan.shopping.ItemStateMachine.State.EXIST;
import static com.mirzairwan.shopping.LoaderHelper.ITEM_LOADER_ID;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class ItemEditingActivity extends ItemActivity implements ItemStateMachine.ItemContext
{
        private static final String URI_ITEM = "uri"; //Used for saving instant mState
        private Uri mUriItem;
        private ItemManager mItemManager;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                mContainer = findViewById(R.id.item_editing_container);

                if (savedInstanceState != null) //Restore from previous mState
                {
                        mUriItem = savedInstanceState.getParcelable(URI_ITEM);
                }
                else
                {
                        Intent intent = getIntent();
                        mUriItem = intent.getData();
                }

                initLoaders(mUriItem);
                setTitle(R.string.view_buy_item_details);
                mItemStateMachine = new ItemStateMachine(this, EXIST);
        }

        protected void initLoaders(Uri uri)
        {
                if (mUriItem != null)
                {
                        Bundle arg = new Bundle();
                        arg.putParcelable(ITEM_URI, uri);
                        getLoaderManager().initLoader(ITEM_LOADER_ID, arg, this);
                        super.initPictureLoader(uri, this);
                        super.initPriceLoader(uri, this);
                }
        }


        @Override
        protected int getLayoutXml()
        {
                return R.layout.activity_item_editing;
        }

        @Override
        public boolean areFieldsValid()
        {
                boolean areFieldsValid = super.areFieldsValid();
                return areFieldsValid;
        }

        @Override
        public boolean isItemInShoppingList()
        {
                if (mItemManager.getItem().isInBuyList())
                {
                        alertItemInShoppingList(R.string.item_is_in_shopping_list);
                        return true;
                }
                else
                {
                        return false;
                }
        }

        /**
         * Call by menu action
         * Delete  if only  is NOT in shoppinglist
         */
        @Override
        public void delete()
        {
                mDbMsg = daoManager.delete(mItemManager.getItem(), mPictureMgr);

                mDbMsg = mItemManager.getItem().getName() + " " + mDbMsg;
        }

        /**
         * Call by menu action
         */
        @Override
        protected void save()
        {
                mItemStateMachine.onProcessSave();
        }


        protected void prepareForDbOperation()
        {
                populateItemFromInputFields(mItemManager.getItem());

                String bundleQtyFromInputField = getBundleQtyFromInputField();

                try
                {
                        mPriceMgr.setCurrencyCode(etCurrencyCode.getText().toString());
                        String unitPrice = mUnitPriceEditField.getPrice();
                        mPriceMgr.setItemPricesForSaving(unitPrice, mBundlePriceEditField.getPrice(), bundleQtyFromInputField);
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                        alertRequiredField(R.string.dialog_invalid_title, R.string.invalid_price);
                        return;
                }
        }


        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
        {
                String[] projection = null;
                Uri uri = args.getParcelable(ITEM_URI);
                Loader<Cursor> loader = null;
                String selection = null;
                String[] selectionArgs = null;

                switch (loaderId)
                {
                        case ITEM_LOADER_ID:
                                projection = new String[]{
                                        Contract.ItemsEntry._ID, Contract.ItemsEntry.COLUMN_NAME, Contract.ItemsEntry.COLUMN_BRAND, Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN, Contract.ItemsEntry.COLUMN_DESCRIPTION,};
                                loader = new CursorLoader(this, uri, projection, selection, selectionArgs, null);
                                break;

                        default:
                                loader = super.onCreateLoader(loaderId, args);
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
                        case ITEM_LOADER_ID:
                                mItemManager = new ItemManager(cursor, getIntent().getBooleanExtra(ITEM_IS_IN_SHOPPING_LIST, false));
                                mPictureMgr.setItemId(mItemManager.getItem().getId());
                                populateItemInputFields(mItemManager.getItem());
                                break;

                        default:
                                super.onLoadFinished(loader, cursor);
                }
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
                outState.putParcelable(URI_ITEM, mUriItem);
                super.onSaveInstanceState(outState);
        }

        @Override
        public void update()
        {
                prepareForDbOperation();
                mDbMsg = daoManager.update(mItemManager.getItem(), mPriceMgr.getPrices(), mPictureMgr);
                mDbMsg = mItemManager.getItem().getName() + " " + mDbMsg;
        }

        @Override
        public void insert()
        {

        }
}
