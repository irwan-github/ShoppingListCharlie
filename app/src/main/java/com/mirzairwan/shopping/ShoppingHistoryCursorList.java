package com.mirzairwan.shopping;

import android.database.Cursor;

import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.ItemInShoppingList;
import com.mirzairwan.shopping.domain.Price;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */
public class ShoppingHistoryCursorList
{
        private Cursor mCursor;

        public ShoppingHistoryCursorList(Cursor cursor)
        {
                mCursor = cursor;
        }

        public ItemInShoppingList addToShoopingList(int position)
        {
                mCursor.moveToPosition(position);
                long itemid = mCursor.getLong(mCursor.getColumnIndex(ItemsEntry._ID));
                int colItemName = mCursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
                String itemName = mCursor.getString(colItemName);
                Item item = new Item(itemid);
                item.setName(itemName);
                long priceId = mCursor.getLong(mCursor.getColumnIndex(PricesEntry.ALIAS_ID));
                Price price = new Price(priceId);
                return new ItemInShoppingList(item, 1, price);
        }

        public ItemInShoppingList removeFromShoppingList(int position)
        {
                mCursor.moveToPosition(position);
                int colBuyItemIdIdx = mCursor.getColumnIndex(ToBuyItemsEntry.ALIAS_ID);
                long buyItemId = mCursor.getLong(colBuyItemIdIdx);
                int colItemName = mCursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
                String itemName = mCursor.getString(colItemName);
                Item item = new Item(itemName);
                ItemInShoppingList itemInShoppingList = new ItemInShoppingList(buyItemId);
                itemInShoppingList.setItem(item);
                return itemInShoppingList;
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


}
