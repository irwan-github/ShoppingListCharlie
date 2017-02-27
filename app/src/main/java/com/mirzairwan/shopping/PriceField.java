package com.mirzairwan.shopping;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;

import com.mirzairwan.shopping.domain.ExchangeRate;

import java.text.ParseException;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * Manages the item's base price field and translated price field.
 * The currency the item is priced in is ignored. Caller of this class must supply an exchange rate in order for this class to display the conversion in the translated price field.
 * <p>
 * When user change the currency code of the item, the following are affected:
 * Translated price field.
 * <p>
 * In order to display translated price field, the following must known:
 * <p>
 * Home currency of the app so that it can display the correct hint.
 * Exchange rate of item's currency  to home currency.
 */

public class PriceField implements View.OnFocusChangeListener
{
        double mCurrentPrice = 0.00;
        private EditText mEtPrice;
        private String mTextHint;
        private String mHomeCountryCode;
        private ExchangeRate mExchangeRate;

        public PriceField(EditText etPrice, String homeCountryCode, String textHint)
        {
                mEtPrice = etPrice;
                mEtPrice.setOnFocusChangeListener(this);
                mHomeCountryCode = homeCountryCode;
                mTextHint = textHint;
                try
                {
                        String value = mEtPrice.getText().toString();
                        if (!TextUtils.isEmpty(value))
                        {
                                mCurrentPrice = FormatHelper.parseTwoDecimalPlaces(value);
                        }
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus)
        {
                boolean isPriceEmpty = false;
                try
                {
                        isPriceEmpty = MyTextUtils.isZeroValue(mEtPrice);
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                        return;
                }
                if (!v.hasFocus() && !isPriceEmpty)
                {
                        if (isPriceChange() && mExchangeRate != null)
                        {
                                setCurrencySymbolInPriceHint(mExchangeRate);
                        }
                }
                else if (!v.hasFocus())//Price is empty
                {
                        clear();
                }
        }

        private boolean isPriceChange()
        {
                double newPrice = 0.00;
                try
                {
                        newPrice = FormatHelper.parseTwoDecimalPlaces(mEtPrice.getText().toString());
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                }
                if (newPrice != mCurrentPrice)
                {
                        return true;
                }
                else
                {
                        return false;
                }
        }

        /**
         * Show translated price. Hides progress bar.
         * If exchange rate is null, the translated price is set to null and hidden.
         *
         * @param exchangeRate
         */
        public void setCurrencySymbolInPriceHint(ExchangeRate exchangeRate)
        {
                mExchangeRate = exchangeRate;
                setCurrencySymbolInPriceHint(exchangeRate.getSourceCurrencyCode());
        }

        /**
         * Set currency symbol using currency code. It depends heavily on Locale
         *
         * @param currencyCode
         */
        public void setCurrencySymbolInPriceHint(String currencyCode)
        {
                String currencySymbol = FormatHelper.getCurrencySymbol(currencyCode);
                String hintPx = mTextHint + " (" + currencySymbol + ")";
                ViewParent viewParent = mEtPrice.getParent();
                TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
                etLayout.setHint(hintPx);
        }

        /**
         * Set price of item
         *
         * @param formattedPrice
         * @param currencyCode
         */
        public void setPrice(String currencyCode, String formattedPrice)
        {
                mEtPrice.setText(formattedPrice);
                setCurrencySymbolInPriceHint(currencyCode);
        }

        public String getPrice()
        {
                if (!TextUtils.isEmpty(mEtPrice.getText()))
                {
                        return mEtPrice.getText().toString();
                }
                else
                {
                        return "0.00";
                }
        }

        public boolean isEmpty()
        {
                return TextUtils.isEmpty(mEtPrice.getText());
        }

        /**
         * Set the text value of source price to null.
         * Set the text value of destination price to null.
         * Remove the translated price field and header/hint
         */
        public void clear()
        {
                mEtPrice.setText(null);
        }

        public boolean isItemLocalPriced(String currencyCode)
        {
                String homeCurrencyCode = FormatHelper.getCurrencyCode(mHomeCountryCode);
                return currencyCode.equalsIgnoreCase(homeCurrencyCode);
        }

}
