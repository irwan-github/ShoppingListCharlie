package com.mirzairwan.shopping;

import android.database.Cursor;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.ItemInShoppingList;
import com.mirzairwan.shopping.domain.Price;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Manages only one item placed or to-be placed into shopping list
 */

public class PurchaseManager
{
        private Cursor mCursor;
        private Item mItem;
        private ItemInShoppingList mItemInShoppingList;
        private Price mSelectedPrice;

        public PurchaseManager()
        {
                createNewItem();
                addNewItemInShoppingList();
        }

        public PurchaseManager(Cursor cursor)
        {
                mCursor = cursor;
                createExistingItem();
                addExistingItemInShoppingList();
        }

        private void addNewItemInShoppingList()
        {
                mItemInShoppingList = new ItemInShoppingList();
        }

        private void createNewItem()
        {
                mItem = new Item();
        }

        /**
         * Create Item object that exist in database.
         */
        private void createExistingItem()
        {
                if (mCursor == null)
                {
                        throw new IllegalArgumentException("Cursor cannot be null");
                }
                mCursor.moveToFirst();
                long itemId;
                String itemName, itemBrand, itemDescription, countryOrigin;

                itemId = mCursor.getLong(mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_ITEM_ID));

                int colNameIndex = mCursor.getColumnIndex(Contract.ItemsEntry.COLUMN_NAME);
                itemName = mCursor.getString(colNameIndex);

                int colBrandIdx = mCursor.getColumnIndex(Contract.ItemsEntry.COLUMN_BRAND);
                itemBrand = mCursor.getString(colBrandIdx);

                int colDescriptionIdx = mCursor.getColumnIndex(Contract.ItemsEntry.COLUMN_DESCRIPTION);
                itemDescription = mCursor.getString(colDescriptionIdx);

                int colCountryOriginIdx = mCursor.getColumnIndex(Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN);
                countryOrigin = mCursor.getString(colCountryOriginIdx);

                mItem = new Item(itemId, itemName, itemBrand, countryOrigin, itemDescription, null);

                mItem.setInBuyList(true);
        }

        private void addExistingItemInShoppingList()
        {
                mCursor.moveToFirst();
                long buyItemId = mCursor.getLong(mCursor.getColumnIndex(Contract.ToBuyItemsEntry._ID));

                int colQtyIdx = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_QUANTITY);
                int buyQty = mCursor.getInt(colQtyIdx);

                int colIsChecked = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED);
                boolean isItemChecked = mCursor.getInt(colIsChecked) > 0;

                int colPriceTypeIdx = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE_TYPE_ID);
                int priceTypeVal = mCursor.getInt(colPriceTypeIdx);

                int colPriceIdIdx = mCursor.getColumnIndex(Contract.PricesEntry.ALIAS_ID);
                long priceId = mCursor.getLong(colPriceIdIdx);

                int colBundleQtyIdx = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_BUNDLE_QTY);
                long bundleQty = mCursor.getLong(colBundleQtyIdx);

                int colCurrencyCodeIdx = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
                String currencyCode = mCursor.getString(colCurrencyCodeIdx);

                int colShopIdIdx = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_SHOP_ID);
                long shopId = mCursor.getLong(colShopIdIdx);

                mSelectedPrice = null;
                int colPriceIdx = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE);

                double priceDbl = mCursor.getDouble(colPriceIdx) / 100;

                if (priceTypeVal == Price.Type.UNIT_PRICE.getType())
                {
                        mSelectedPrice = new Price(priceId, priceDbl, currencyCode, shopId, null);
                }

                if (priceTypeVal == Price.Type.BUNDLE_PRICE.getType())
                {
                        mSelectedPrice = new Price(priceId, priceDbl, bundleQty, currencyCode, shopId, null);
                }

                mItemInShoppingList = new ItemInShoppingList(buyItemId, buyQty, mSelectedPrice, null);
                mItemInShoppingList.setCheck(isItemChecked);

        }

        public Item getitem()
        {
                return mItem;
        }

        public ItemInShoppingList getItemInShoppingList()
        {
                return mItemInShoppingList;
        }

        public void setItem(Item item)
        {
                mItem = item;
        }

        public void setSelectedPrice(Price defaultPrice)
        {
                mSelectedPrice = defaultPrice;
        }
}
