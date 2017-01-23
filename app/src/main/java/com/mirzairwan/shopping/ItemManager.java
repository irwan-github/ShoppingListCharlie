package com.mirzairwan.shopping;

import android.database.Cursor;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.domain.Item;

/**
 * Created by Mirza Irwan on 23/1/17.
 */

public class ItemManager
{
        private Cursor mCursor;
        private Item mItem;

        public ItemManager(Cursor cursor, boolean itemIsInShoppingList)
        {
                mCursor = cursor;
                createExistingItem(itemIsInShoppingList)             ;
        }

        /**
         * Create Item object
         *Contract.ItemsEntry._ID, Contract.ItemsEntry.COLUMN_NAME, Contract.ItemsEntry.COLUMN_BRAND, Contract.ItemsEntry.COLUMN_DESCRIPTION, Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN, cursor
         */
        protected void createExistingItem(boolean itemIsInShoppingList)
        {
                if (mCursor == null)
                {
                        throw new IllegalArgumentException("Cursor cannot be null");
                }

                mCursor.moveToFirst();

                long itemId;
                String itemName, itemBrand, itemDescription, countryOrigin;

                itemId = mCursor.getLong(mCursor.getColumnIndex(Contract.ItemsEntry._ID));

                int colNameIndex = mCursor.getColumnIndex(Contract.ItemsEntry.COLUMN_NAME);
                itemName = mCursor.getString(colNameIndex);

                int colBrandIdx = mCursor.getColumnIndex(Contract.ItemsEntry.COLUMN_BRAND);
                itemBrand = mCursor.getString(colBrandIdx);

                int colDescriptionIdx = mCursor.getColumnIndex(Contract.ItemsEntry.COLUMN_DESCRIPTION);
                itemDescription = mCursor.getString(colDescriptionIdx);

                int colCountryOriginIdx = mCursor.getColumnIndex(Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN);
                countryOrigin = mCursor.getString(colCountryOriginIdx);

                mItem = new Item(itemId, itemName, itemBrand, countryOrigin, itemDescription, null);
                mItem.setInBuyList(itemIsInShoppingList);
        }

        public Item getItem()
        {
                return mItem;
        }
}
