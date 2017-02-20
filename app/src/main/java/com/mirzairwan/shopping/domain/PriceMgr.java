package com.mirzairwan.shopping.domain;

import android.database.Cursor;

import com.mirzairwan.shopping.FormatHelper;
import com.mirzairwan.shopping.data.Contract;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.mirzairwan.shopping.FormatHelper.formatToTwoDecimalPlaces;
import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * PriceManager handles the prices of one particular item
 * It handles 2 price types: Unit price and bundle price.
 * The cursor loader sends cursor data so that PriceMgr can create the 2 price object
 */

public class PriceMgr
{
        private static final long DEFAULT_SHOP_ID = 1;
        private final String mCountryCode;
        private long mItemId = -1;
        private Price mUnitPrice = null;
        private Price mBundlePrice = null;

        public PriceMgr(String countryCode)
        {
                mCountryCode = countryCode;
                mUnitPrice = new Price(0.00d, FormatHelper.getCurrencyCode(countryCode), DEFAULT_SHOP_ID);
                mBundlePrice = new Price(0.00d, 0, FormatHelper.getCurrencyCode(countryCode), DEFAULT_SHOP_ID);
        }


        /**
         * Create unit price object and bundle price object
         *
         * @param cursor
         */
        public void createPrices(Cursor cursor)
        {

                while (cursor.moveToNext())
                {
                        int colItemId = cursor.getColumnIndex(Contract.PicturesEntry.COLUMN_ITEM_ID);
                        mItemId = cursor.getLong(colItemId);

                        int colPriceTypeIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE_TYPE_ID);
                        int priceTypeVal = cursor.getInt(colPriceTypeIdx);

                        int colPriceIdIdx = cursor.getColumnIndex(Contract.PricesEntry._ID);
                        long priceId = cursor.getLong(colPriceIdIdx);

                        int colCurrencyCodeIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
                        String currencyCode = cursor.getString(colCurrencyCodeIdx);

                        int colShopIdIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_SHOP_ID);
                        long shopId = cursor.getLong(colShopIdIdx);

                        int colPriceIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE);


                        if (priceTypeVal == UNIT_PRICE.getType())
                        {
                                double unitPrice = cursor.getDouble(colPriceIdx) / 100;
                                mUnitPrice = new Price(priceId, unitPrice, currencyCode, shopId, null);
                        }

                        if (priceTypeVal == BUNDLE_PRICE.getType())
                        {
                                double bundlePrice = cursor.getDouble(colPriceIdx) / 100;
                                int colBundleQtyIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_BUNDLE_QTY);
                                int bundleQty = cursor.getInt(colBundleQtyIdx);
                                mBundlePrice = new Price(priceId, bundlePrice, bundleQty, currencyCode, shopId, null);
                        }

                }
        }

        public List<Price> getPrices()
        {
                ArrayList<Price> mPrices = new ArrayList<>();
                mPrices.add(mUnitPrice);
                mPrices.add(mBundlePrice);
                return mPrices;
        }

        public Price getSelectedPrice(Price.Type selectedPriceType)
        {
                return selectedPriceType == Price.Type.UNIT_PRICE ? mUnitPrice : mBundlePrice;
        }

        public String getUnitPriceForDisplay()
        {
                return formatToTwoDecimalPlaces(mUnitPrice.getUnitPrice());
        }

        public String getBundlePriceForDisplay()
        {
                return FormatHelper.formatToTwoDecimalPlaces(mBundlePrice.getBundlePrice());
        }

        public void setItemPricesForSaving(String unitPriceFromInputField, String bundlePriceFromInputField, String bundleQtyFromInputField) throws ParseException
        {
                double unitPrice = FormatHelper.parseTwoDecimalPlaces(unitPriceFromInputField);
                mUnitPrice.setUnitPrice(unitPrice);
                double bundlePrice = FormatHelper.parseTwoDecimalPlaces(bundlePriceFromInputField);
                int bundleQuantity = Integer.parseInt(bundleQtyFromInputField);
                mBundlePrice.setBundlePrice(bundlePrice, bundleQuantity);
        }

        public Price getUnitPrice()
        {
                return mUnitPrice;
        }

        public void setUnitPrice(Price unitPrice)
        {
                mUnitPrice = unitPrice;
        }

        public Price getBundlePrice()
        {
                return mBundlePrice;
        }

        public void setBundlePrice(Price bundlePrice)
        {
                mBundlePrice = bundlePrice;
        }

        public String getCurrencyCode()
        {
                return mUnitPrice.getCurrencyCode();
        }

        public void setCurrencyCode(String currencyCode)
        {
                mUnitPrice.setCurrencyCode(currencyCode);
                mBundlePrice.setCurrencyCode(currencyCode);
        }
}
