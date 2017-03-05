package com.mirzairwan.shopping;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.mirzairwan.shopping.domain.ExchangeRate;

import java.text.ParseException;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * Manages the item's price field
 *
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
        private double mCurrentPrice = 0.00;
        private ItemControl mItemControl;
        private TextInputEditText mTextInputPrice;
        private TextInputLayout mEtPriceWrapper;
        private String mTextHint;
        private ExchangeRate mExchangeRate;

        public PriceField(TextInputLayout priceWrapper, String textHint, int textInputId, ItemControl itemControl)
        {
                mEtPriceWrapper = priceWrapper;
                mTextInputPrice = (TextInputEditText) mEtPriceWrapper.findViewById(textInputId);
                mTextInputPrice.setOnFocusChangeListener(this);
                mTextInputPrice.setOnEditorActionListener(new Action());
                mItemControl = itemControl;
                mTextHint = textHint;
                try
                {
                        String value = mTextInputPrice.getText().toString();
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
//                boolean isPriceEmpty = false;
//                try
//                {
//                        isPriceEmpty = MyTextUtils.isZeroValue(mTextInputPrice);
//                }
//                catch(ParseException e)
//                {
//                        e.printStackTrace();
//                        return;
//                }
//                if (!v.hasFocus() && !isPriceEmpty)
//                {
//                        if (isPriceChange() && mExchangeRate != null)
//                        {
//                                setCurrencySymbolInPriceHint(mExchangeRate);
//                        }
//                }
//                else if (!v.hasFocus())//Price is empty
//                {
//                        clear();
//                }
        }

        private boolean isPriceChange()
        {
                double newPrice = 0.00;
                try
                {
                        newPrice = FormatHelper.parseTwoDecimalPlaces(mTextInputPrice.getText().toString());
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
                //                ViewParent viewParent = mEtPrice.getParent();
                //                TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
                //                etLayout.setHint(hintPx);


                mEtPriceWrapper.setHint(hintPx);
        }

        /**
         * Set price of item
         *
         * @param formattedPrice
         * @param currencyCode
         */
        public void setPrice(String currencyCode, String formattedPrice)
        {
                mTextInputPrice.setText(formattedPrice);
                setCurrencySymbolInPriceHint(currencyCode);
        }

        public String getPrice()
        {
                if (!TextUtils.isEmpty(mTextInputPrice.getText()))
                {
                        return mTextInputPrice.getText().toString();
                }
                else
                {
                        return "0.00";
                }
        }

        public boolean isEmpty()
        {
                return TextUtils.isEmpty(mTextInputPrice.getText());
        }

        /**
         * Set the text value of source price to null.
         * Set the text value of destination price to null.
         * Remove the translated price field and header/hint
         */
        public void clear()
        {
                mTextInputPrice.setText(null);
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mTextInputPrice.setOnTouchListener(onTouchListener);
        }

        public void setEnabled(boolean isEnabled)
        {
                mTextInputPrice.setEnabled(isEnabled);
        }

        class Action implements TextView.OnEditorActionListener
        {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                        if (actionId == EditorInfo.IME_ACTION_DONE)
                        {
                                mItemControl.onOk();
                        }
                        return false;
                }
        }
}
