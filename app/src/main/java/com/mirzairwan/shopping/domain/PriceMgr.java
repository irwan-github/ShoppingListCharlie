package com.mirzairwan.shopping.domain;

import android.database.Cursor;

import com.mirzairwan.shopping.NumberFormatter;
import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;

import java.util.ArrayList;
import java.util.List;

import static com.mirzairwan.shopping.NumberFormatter.formatToTwoDecimalPlaces;
import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;

/**
 * Created by Mirza Irwan on 29/12/16.
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
        mUnitPrice = new Price(0.00d, NumberFormatter.getCurrencyCode(countryCode), DEFAULT_SHOP_ID);
        mBundlePrice = new Price(0.00d, 0.00d, NumberFormatter.getCurrencyCode(countryCode), DEFAULT_SHOP_ID);
    }

    public PriceMgr(long itemId, String countryCode)
    {
        mItemId = itemId;
        mCountryCode = countryCode;
    }


    public void createPrices(Cursor cursor)
    {

        while (cursor.moveToNext()) {
            int colPriceTypeIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE_TYPE_ID);
            int priceTypeVal = cursor.getInt(colPriceTypeIdx);

            int colPriceIdIdx = cursor.getColumnIndex(Contract.PricesEntry._ID);
            long priceId = cursor.getLong(colPriceIdIdx);

            int colCurrencyCodeIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
            String currencyCode = cursor.getString(colCurrencyCodeIdx);

            int colShopIdIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_SHOP_ID);
            long shopId = cursor.getLong(colShopIdIdx);

            int colPriceIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE);


            if (priceTypeVal == UNIT_PRICE.getType()) {
                double unitPrice = cursor.getDouble(colPriceIdx) / 100;
                mUnitPrice = new Price(priceId, unitPrice, currencyCode, shopId, null);
            }

            if (priceTypeVal == BUNDLE_PRICE.getType()) {
                double bundlePrice = cursor.getDouble(colPriceIdx) / 100;
                int colBundleQtyIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_BUNDLE_QTY);
                double bundleQty = cursor.getDouble(colBundleQtyIdx) / 100;
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
        return NumberFormatter.formatToTwoDecimalPlaces(mBundlePrice.getBundlePrice());
    }

    public void setItemPricesForSaving(Item item, String unitPriceFromInputField, String bundlePriceFromInputField, String bundleQtyFromInputField)
    {
        if (item == null)
            throw new IllegalArgumentException("Item cannot be null");

        //Clear the prices in the  object first before adding because it will accumulate identical prices types
        item.clearPrices();

        mUnitPrice.setUnitPrice(Double.parseDouble(unitPriceFromInputField));
        mBundlePrice.setBundlePrice(Double.parseDouble(bundlePriceFromInputField),
                Double.parseDouble(bundleQtyFromInputField));

        item.addPrice(mUnitPrice);
        item.addPrice(mBundlePrice);

    }

    public Price getUnitPrice()
    {
        return mUnitPrice;
    }

    public Price getBundlePrice()
    {
        return mBundlePrice;
    }
}
