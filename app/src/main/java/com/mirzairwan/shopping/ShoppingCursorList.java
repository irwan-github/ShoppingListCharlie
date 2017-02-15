package com.mirzairwan.shopping;

import android.database.Cursor;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.domain.ExchangeRate;
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
                        double cost = mCursor.getDouble(colSelectedPriceTag);

                        int colSelectedPriceType = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE_TYPE_ID);
                        int nPriceType = mCursor.getInt(colSelectedPriceType);
                        Price.Type priceType = nPriceType == 0 ? Price.Type.UNIT_PRICE : Price.Type.BUNDLE_PRICE;

                        int colBundleQty = mCursor.getColumnIndex(PricesEntry.COLUMN_BUNDLE_QTY);
                        int bundleQty = mCursor.getInt(colBundleQty);

                        int colCurrencyCode = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
                        String lCurrencyCode = mCursor.getString(colCurrencyCode);

                        int colQtyPurchased = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_QUANTITY);
                        int qtyPurchased = mCursor.getInt(colQtyPurchased);

                        //Separate foreign-price items from local-priced items
                        if (!lCurrencyCode.trim().equalsIgnoreCase(baseCurrencyCode))
                        {
                                SummaryItem foreignVal = new SummaryItem(cost / 100, lCurrencyCode, qtyPurchased, priceType, bundleQty);
                                mSummaryForeignItemsAdded.add(foreignVal);
                        }
                        else
                        {
                                SummaryItem localVal = new SummaryItem(cost / 100, lCurrencyCode, qtyPurchased, priceType, bundleQty);
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
                mSummaryForeignItemsChecked.clear();
                mSummaryLocalItemsChecked.clear();

                String homeCurrencyCode = getCurrencyCode(homeCountryCode);

                mCursor.moveToPosition(-1);

                int colShoppingItemId = mCursor.getColumnIndex(Contract.ToBuyItemsEntry._ID);
                int colSelectedPriceTag = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE);
                int colBundleQty = mCursor.getColumnIndex(PricesEntry.COLUMN_BUNDLE_QTY);
                int colIsItemChecked = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED);
                int colCurrencyCode = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
                int colQtyPurchased = mCursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_QUANTITY);


                while (mCursor.moveToNext())
                {
                        long shoppingItemId = mCursor.getLong(colShoppingItemId);

                        int colSelectedPriceType = mCursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE_TYPE_ID);
                        int nPriceType = mCursor.getInt(colSelectedPriceType);
                        Price.Type priceType = nPriceType == 0 ? Price.Type.UNIT_PRICE : Price.Type.BUNDLE_PRICE;
                        int bundleQty = mCursor.getInt(colBundleQty);

                        int qtyPurchased = mCursor.getInt(colQtyPurchased);
                        String itemCurrencyCode = mCursor.getString(colCurrencyCode);
                        boolean isItemChecked = mCursor.getInt(colIsItemChecked) > 0;

                        //Only add item with same currency code as user home currency code
                        double cost = mCursor.getDouble(colSelectedPriceTag);
                        if (isItemChecked)
                        {
                                if (!itemCurrencyCode.trim().equalsIgnoreCase(homeCurrencyCode))
                                {
                                        SummaryItem val = new SummaryItem(cost / 100, itemCurrencyCode, qtyPurchased, priceType, bundleQty);
                                        mSummaryForeignItemsChecked.add(val);
                                }
                                else
                                {
                                        SummaryItem localValChecked = new SummaryItem(cost / 100, itemCurrencyCode, qtyPurchased, priceType, bundleQty);
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
                        totalValueOfItemsChecked += localItem.getTotalCost() ;
                }

                if (exchangeRates != null)
                {
                        for (SummaryItem summaryItemChecked : mSummaryForeignItemsChecked)
                        {
                                ExchangeRate fc = exchangeRates.get(summaryItemChecked.getSourceCurrencyCode());
                                totalCostForexItemChecked += (fc.compute(summaryItemChecked.getTotalCost(), destCurrencyCode));
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
                        totalValueOfItemsAdded += localItem.getTotalCost();
                }

                if (exchangeRates != null)
                {
                        //Apply the rate and add the foreign currency
                        for (SummaryItem summaryItem : mSummaryForeignItemsAdded)
                        {
                                ExchangeRate exRate = exchangeRates.get(summaryItem.getSourceCurrencyCode());
                                if (exRate != null)
                                {
                                        totalForexCost += (exRate.compute(summaryItem.getTotalCost(), baseCurrencyCode));
                                }
                        }
                }
                String currencyCode = FormatHelper.getCurrencyCode(homeCountryCode);
                totalValueOfItemsAdded = totalValueOfItemsAdded + totalForexCost;
                String totalCostofItemsAdded = formatCountryCurrency(homeCountryCode, currencyCode, totalValueOfItemsAdded);
                return totalCostofItemsAdded;
        }

        private class SummaryItem
        {
                private long mId;
                private int mQtyToBuy;
                private double mCost;
                private String mSourceCurrencyCode;
                private Price.Type mPriceType;
                private int mBundleQty;

                public SummaryItem(double cost, String sourceCurrencyCode, int qtyToBuy, Price.Type priceType, int bundleQty)
                {
                        mCost = cost;
                        mSourceCurrencyCode = sourceCurrencyCode;
                        mQtyToBuy = qtyToBuy;
                        mPriceType = priceType;
                        mBundleQty = bundleQty;
                }

//                public SummaryItem(long id, double cost, String sourceCurrencyCode, int qtyToBuy,  Price.Type priceType, int bundleQty)
//                {
//                        mId = id;
//                        mCost = cost;
//                        mSourceCurrencyCode = sourceCurrencyCode;
//                        mQtyToBuy = qtyToBuy;
//                        mPriceType = priceType;
//                        mBundleQty = bundleQty;
//                }

                public double getCost()
                {
                        return mCost;
                }

                public double getTotalCost()
                {
                        if(mPriceType == Price.Type.BUNDLE_PRICE)
                        {
                                return (mQtyToBuy/mBundleQty) * mCost;
                        }
                        else
                        {
                                return mQtyToBuy * mCost;
                        }
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
