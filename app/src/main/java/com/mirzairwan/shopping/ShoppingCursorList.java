package com.mirzairwan.shopping;

import android.database.Cursor;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.domain.ExchangeRate;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.ItemInShoppingList;
import com.mirzairwan.shopping.domain.Price;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mirzairwan.shopping.FormatHelper.formatCountryCurrency;
import static com.mirzairwan.shopping.FormatHelper.getCurrencyCode;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class ShoppingCursorList
{
        private Cursor mCursor;
        private List<SummaryItem> mSummaryForeignItemsAdded = new ArrayList<>();
        private List<SummaryItem> mSummaryForeignItemsChecked = new ArrayList<>();
        private List<SummaryItem> mSummaryLocalItemsAdded = new ArrayList<>();
        private ArrayList<SummaryItem> mSummaryLocalItemsChecked = new ArrayList<>();

        public ShoppingCursorList(Cursor cursor)
        {
                mCursor = cursor;;
        }

        /**
         * List all items added to shopping list.
         */
        public void listItemsAdded(String countryCode)
        {
                mSummaryForeignItemsAdded.clear();
                mSummaryLocalItemsAdded.clear();
                String baseCurrencyCode = getCurrencyCode(countryCode);

                mCursor.moveToPosition(-1);
                while (mCursor.moveToNext())
                {


                        int colSelectedPriceTag = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE);

                        int colCurrencyCode = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
                        String lCurrencyCode = mCursor.getString(colCurrencyCode);

                        int colQtyPurchased = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_QUANTITY);
                        int qtyPurchased = mCursor.getInt(colQtyPurchased);

                        double cost = mCursor.getDouble(colSelectedPriceTag);
                        //Only add item with same currency code as user home currency code
                        if (!lCurrencyCode.trim().equalsIgnoreCase(baseCurrencyCode))
                        {
                                SummaryItem val = new SummaryItem(cost / 100, lCurrencyCode, qtyPurchased);
                                mSummaryForeignItemsAdded.add(val);
                        }
                        else
                        {
                                SummaryItem localVal = new SummaryItem(cost / 100, lCurrencyCode, qtyPurchased);
                                mSummaryLocalItemsAdded.add(localVal);
                        }
                }
        }

        /**
         * List local-priced items checked in shopping list
         * List foreign-priced items checked in the shopping list.
         */
        public void listItemsChecked(String homeCountryCode)
        {
                int colShoppingItemId = mCursor.getColumnIndex(Contract.ToBuyItemsEntry._ID);
                int colSelectedPriceTag = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE);
                int colIsItemChecked = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED);
                int colCurrencyCode = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
                int colQtyPurchased = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_QUANTITY);

                mSummaryForeignItemsChecked.clear();
                mSummaryLocalItemsChecked.clear();
                byte atLeastAnItemChecked = (byte) 0;
                String currencyCode = getCurrencyCode(homeCountryCode);

                mCursor.moveToPosition(-1);
                while (mCursor.moveToNext())
                {
                        long shoppingItemId = mCursor.getLong(colShoppingItemId);
                        int qtyPurchased = mCursor.getInt(colQtyPurchased);
                        String lCurrencyCode = mCursor.getString(colCurrencyCode);
                        boolean isItemChecked = mCursor.getInt(colIsItemChecked) > 0;
                        atLeastAnItemChecked |= (byte) mCursor.getInt(colIsItemChecked);

                        //Only add item with same currency code as user home currency code
                        double cost = mCursor.getDouble(colSelectedPriceTag);
                        if (isItemChecked)
                        {
                                if (!lCurrencyCode.trim().equalsIgnoreCase(currencyCode))
                                {
                                        SummaryItem val = new SummaryItem(shoppingItemId, cost / 100, lCurrencyCode, qtyPurchased);
                                        mSummaryForeignItemsChecked.add(val);
                                }
                                else
                                {
                                        SummaryItem localValChecked = new SummaryItem(shoppingItemId, cost / 100, lCurrencyCode, qtyPurchased);
                                        mSummaryLocalItemsChecked.add(localValChecked);
                                }
                        }
                }
        }

        public boolean atLeastAnItemChecked()
        {
                byte atLeastAnItemChecked = (byte) 0;
                mCursor.moveToPosition(-1);
                while (mCursor.moveToNext())
                {
                        int colIsItemChecked = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED);
                        atLeastAnItemChecked |= (byte) mCursor.getInt(colIsItemChecked);
                }

                return atLeastAnItemChecked == 1;
        }

        /**
         * Prepare a set of source foreign currency codes. Use this set to fetch exchange rates.
         */
        public Set<String> prepareSourceCurrencyCodes(String homeCurrencyCode)
        {
                Set<String> sourceForeignCurrencyCodes = new HashSet<>();
                String baseCurrencyCode = getCurrencyCode(homeCurrencyCode);
                mCursor.moveToPosition(-1); //Start at beginning
                while (mCursor.moveToNext())
                {
                        int colCurrencyCode = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
                        String lCurrencyCode = mCursor.getString(colCurrencyCode);

                        if (!lCurrencyCode.trim().equalsIgnoreCase(baseCurrencyCode))
                        {
                                sourceForeignCurrencyCodes.add(lCurrencyCode);
                        }
                }
                return sourceForeignCurrencyCodes;
        }

        /**
         * Add cost of all checked local-priced and foreign-priced items in the shopping list
         *
         * @param exchangeRates
         * @return total cost of checked items in shopping list with currency and decimails
         * formatted fit for display
         */
        protected String totalCostItemsChecked(Map<String, ExchangeRate> exchangeRates, String homeCountryCode)
        {
                double totalValueOfItemsChecked = 0.00d;
                String destCurrencyCode = FormatHelper.getCurrencyCode(homeCountryCode);
                double totalCostForexItemChecked = 0;

                for (SummaryItem localItem : mSummaryLocalItemsChecked)
                {
                        totalValueOfItemsChecked += localItem.getCost() * localItem.getQuantity();
                }

                if (exchangeRates != null)
                {
                        for (SummaryItem summaryItemChecked : mSummaryForeignItemsChecked)
                        {
                                ExchangeRate fc = exchangeRates.get(summaryItemChecked.getSourceCurrencyCode());
                                totalCostForexItemChecked += (fc.compute(summaryItemChecked.getCost(), destCurrencyCode) * summaryItemChecked.getQuantity());
                        }
                }
                totalValueOfItemsChecked = totalValueOfItemsChecked + totalCostForexItemChecked;
                return formatCountryCurrency(homeCountryCode, destCurrencyCode, totalValueOfItemsChecked);
        }

        /**
         * Add cost of all local-priced and foreign-priced items in the shopping list
         *
         * @param exchangeRates When exchange rate is null, only the cost of local-priced are added
         * @return Total cost of all items in shopping list with currency and decimals formatted fit
         * for display
         */
        protected String totalCostItemsAdded(Map<String, ExchangeRate> exchangeRates, String homeCountryCode)
        {
                double totalValueOfItemsAdded = 0.00d;
                double totalForexCost = 0;
                String baseCurrencyCode = FormatHelper.getCurrencyCode(homeCountryCode);

                for (SummaryItem localItem : mSummaryLocalItemsAdded)
                {
                        totalValueOfItemsAdded += localItem.getCost() * localItem.getQuantity();
                }

                if (exchangeRates != null)
                {
                        //Apply the rate and add the foreign currency
                        for (SummaryItem summaryItem : mSummaryForeignItemsAdded)
                        {
                                ExchangeRate exRate = exchangeRates.get(summaryItem.getSourceCurrencyCode());
                                if (exRate != null)
                                {
                                        totalForexCost += (exRate.compute(summaryItem.getCost(), baseCurrencyCode) * summaryItem.getQuantity());
                                }
                        }
                }
                String currencyCode = FormatHelper.getCurrencyCode(homeCountryCode);
                totalValueOfItemsAdded = totalValueOfItemsAdded + totalForexCost;
                String totalCostofItemsAdded = formatCountryCurrency(homeCountryCode, currencyCode, totalValueOfItemsAdded);
                return totalCostofItemsAdded;
        }

        public ItemInShoppingList addNewItem(Item item, int qtyToBuy, Price price)
        {
                ItemInShoppingList itemInShoppingList = new ItemInShoppingList(item, qtyToBuy, price);
                return itemInShoppingList;
        }

        public HashSet<Long> getCheckedItems()
        {
                HashSet<Long> ids = new HashSet<>();
                for(SummaryItem item : mSummaryForeignItemsChecked)
                {
                        ids.add(item.getItemInShoppingListId());
                }

                for(SummaryItem item : mSummaryLocalItemsChecked)
                {
                        ids.add(item.getItemInShoppingListId());
                }
                return ids;
        }

        private class SummaryItem
        {
                private long mId;
                private int mQtyToBuy;
                private double mCost;
                private String mSourceCurrencyCode;

                public SummaryItem(double cost, String sourceCurrencyCode, int qtyToBuy)
                {
                        mCost = cost;
                        mSourceCurrencyCode = sourceCurrencyCode;
                        mQtyToBuy = qtyToBuy;
                }

                public SummaryItem(long id, double cost, String sourceCurrencyCode, int qtyToBuy)
                {
                        mId = id;
                        mCost = cost;
                        mSourceCurrencyCode = sourceCurrencyCode;
                        mQtyToBuy = qtyToBuy;
                }

                public double getCost()
                {
                        return mCost;
                }

                public String getSourceCurrencyCode()
                {
                        return mSourceCurrencyCode;
                }

                public int getQuantity()
                {
                        return mQtyToBuy;
                }

                public Long getItemInShoppingListId()
                {
                        return mId;
                }
        }
}
