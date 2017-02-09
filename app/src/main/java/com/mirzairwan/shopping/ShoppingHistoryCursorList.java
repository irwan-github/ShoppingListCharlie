package com.mirzairwan.shopping;

import android.database.Cursor;

import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.domain.Item;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Manage history of items added to shopping list
 */
public class ShoppingHistoryCursorList
{
        private Cursor mCursor;

        public ShoppingHistoryCursorList(Cursor cursor)
        {
                mCursor = cursor;
        }

        /**
         * Get item in history to the shopping list.
         * @param position Refers to position of item in the history of items which is position of cursor.
         * @return Item to be added to shopping list.
         */
        public Item getItem(int position)
        {
                mCursor.moveToPosition(position);
                long itemid = mCursor.getLong(mCursor.getColumnIndex(ItemsEntry._ID));
                int colItemName = mCursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
                String itemName = mCursor.getString(colItemName);
                Item item = new Item(itemid);
                item.setName(itemName);
                return item;
        }

        /**
         * Add an pre-existing item in history to the shopping list.
         * @param position Refers to position of item in the history of items
         * @return Item to be added to shopping list.
         */
        public long getPriceId(int position)
        {
                mCursor.moveToPosition(position);
                return mCursor.getLong(mCursor.getColumnIndex(PricesEntry.ALIAS_ID));
        }

        public boolean isInShoppingList(int cursorPosition)
        {
                mCursor.moveToPosition(cursorPosition);
                return !mCursor.isNull(mCursor.getColumnIndex(ToBuyItemsEntry.ALIAS_ID));
        }

        public String getItemCurrencyCode(int cursorPosition)
        {
                mCursor.moveToPosition(cursorPosition);
                return mCursor.getString(mCursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE));
        }


        public long getShoppingListItemId(int position)
        {
                mCursor.moveToPosition(position);
                int colBuyItemIdIdx = mCursor.getColumnIndex(ToBuyItemsEntry.ALIAS_ID);
                return mCursor.getLong(colBuyItemIdIdx);
        }

        public String getItemName(int position)
        {
                mCursor.moveToPosition(position);
                int colItemName = mCursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
                return  mCursor.getString(colItemName);
        }

        public long getItemId(int position)
        {
                mCursor.moveToPosition(position);
                return mCursor.getLong(mCursor.getColumnIndex(ItemsEntry._ID));
        }
}
