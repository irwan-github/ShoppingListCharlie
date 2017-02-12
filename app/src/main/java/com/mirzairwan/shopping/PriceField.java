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
        private EditText mEtTranslatedPrice;
        private String mTextHint;
        private View mProgressBar;
        private String mHomeCountryCode;
        private ExchangeRate mExchangeRate;

        public PriceField(EditText etPrice,
                          EditText etTranslatedPrice,
                          View progressBar,
                          String homeCountryCode,
                          String textHint)
        {
                mEtPrice = etPrice;
                mEtPrice.setOnFocusChangeListener(this);
                mEtTranslatedPrice = etTranslatedPrice;
                mProgressBar = progressBar;
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
        public void onFocusChange(View v,
                                  boolean hasFocus)
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
                        if(isPriceChange() && mExchangeRate != null)
                                setTranslatedPrice(mExchangeRate);
                }
                else if(!v.hasFocus())//Price is empty
                {
                        clear();
                }
        }

        private boolean isPriceChange()
        {
                double newPrice = 0.00;
                try
                {
                        newPrice =  FormatHelper.parseTwoDecimalPlaces(mEtPrice.getText().toString());
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                }
                if(newPrice != mCurrentPrice)
                        return true;
                else
                        return false;
        }

        /**
         * Show translated price. Hides progress bar.
         * If exchange rate is null, the translated price is set to null and hidden.
         *
         * @param exchangeRate
         */
        public void setTranslatedPrice(ExchangeRate exchangeRate)
        {
                mExchangeRate = exchangeRate;
                mProgressBar.setVisibility(View.INVISIBLE);
                String mBaseCurrencyCode = FormatHelper.getCurrencyCode(mHomeCountryCode);

                if (exchangeRate == null)
                {
                        mEtTranslatedPrice.setText(null);
                        setTranslatedPricesVisibility(View.INVISIBLE);
                        return;
                }

                //Unit price greater than 0.00
                setCurrencySymbolInPriceHint(exchangeRate.getSourceCurrencyCode());

                boolean isPriceValid = !TextUtils.isEmpty(mEtPrice.getText()) && Double.parseDouble(mEtPrice.getText().toString()) > 0;
                if (isPriceValid && !mEtPrice.hasFocus())
                {
                        String sUnitPx = mEtPrice.getText().toString();
                        double priceVal;
                        setTranslatedPricesVisibility(View.VISIBLE);
                        priceVal = Double.parseDouble(sUnitPx);
                        double translated = exchangeRate.compute(priceVal, mBaseCurrencyCode);
                        mEtTranslatedPrice.setText(FormatHelper.formatToTwoDecimalPlaces(translated));
                        clearHintInTranslatedPrice();
                        setCurrencyCodeInTranslatedPriceHint(mTextHint, mHomeCountryCode);
                }
        }

        private void clearHintInTranslatedPrice()
        {
                ViewParent viewParent = mEtTranslatedPrice.getParent();
                TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
                etLayout.setHint("");
        }

        private void setCurrencyCodeInTranslatedPriceHint(String hint,
                                                          String homeCountryCode)
        {
                String currencyCode = FormatHelper.getCurrencyCode(homeCountryCode);
                String hintPx = hint + " (" + currencyCode + ")";
                ViewParent viewParent = mEtTranslatedPrice.getParent();
                TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
                etLayout.setHint(hintPx);
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
         * Pre-condition:
         * 1. All the views have been materialized.
         * 2. The edit text has a pre-exising value to be displayed
         * <p>
         * Show the translated price if source price is not zero value.
         *
         * @param visibleId
         */
        private void setTranslatedPricesVisibility(int visibleId)
        {
                try
                {
                        if (MyTextUtils.isZeroValue(mEtPrice))
                        {
                                return;
                        }
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                        return;
                }

                mEtTranslatedPrice.setVisibility(visibleId);
                ViewParent viewParent = mEtTranslatedPrice.getParent();
                TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
                etLayout.setVisibility(visibleId);
                clearHintInTranslatedPrice();
                setCurrencyCodeInTranslatedPriceHint(mTextHint, mHomeCountryCode);
        }

        /**
         * Set price of item
         *
         * @param formattedPrice
         * @param currencyCode
         */
        public void setPrice(String currencyCode,
                             String formattedPrice)
        {
                mEtPrice.setText(formattedPrice);
                setCurrencySymbolInPriceHint(currencyCode);
        }

        public String getPrice()
        {
                if (mEtPrice != null && !TextUtils.isEmpty(mEtPrice.getText()))
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
                mEtTranslatedPrice.setText(null);
                setTranslatedPricesVisibility(View.INVISIBLE);
        }

        public void displayProgress()
        {
                try
                {
                        if (!MyTextUtils.isZeroValue(mEtPrice))
                        {
                                {
                                        mProgressBar.setVisibility(View.VISIBLE);
                                        setTranslatedPricesVisibility(View.INVISIBLE);
                                }
                        }
                }
                catch(ParseException e)
                {
                        e.printStackTrace();
                }
        }
}
