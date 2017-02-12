package com.mirzairwan.shopping;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class OnCurrencyCodeChange implements View.OnFocusChangeListener
{
        protected String mExistingCurrecyCode;
        protected String mPrevCurrecyCode;
        private Context mContext;
        private PriceField mUnitPrice;
        private PriceField mBundlePrice;
        private ExchangeRateInput mExchangeRateInput;
        private String mCountryCode;
        private EditText mEtCurrencyCode;

        public OnCurrencyCodeChange(EditText etCurrencyCode,
                                    String countryCode,
                                    PriceField unitPrice,
                                    PriceField bundlePrice,
                                    ExchangeRateInput exchangeRateInput)
        {
                mEtCurrencyCode = etCurrencyCode;
                mExistingCurrecyCode = etCurrencyCode.getText().toString();
                mPrevCurrecyCode = mExistingCurrecyCode;
                mCountryCode = countryCode;
                mExchangeRateInput = exchangeRateInput;
                mUnitPrice = unitPrice;
                mBundlePrice = bundlePrice;
        }

        public OnCurrencyCodeChange(Context context,
                                    EditText etCurrencyCode,
                                    String countryCode,
                                    PriceField unitPrice,
                                    PriceField bundlePrice,
                                    ExchangeRateInput exchangeRateInput)
        {
                mEtCurrencyCode = etCurrencyCode;
                mExistingCurrecyCode = etCurrencyCode.getText().toString();
                mPrevCurrecyCode = mExistingCurrecyCode;
                mCountryCode = countryCode;
                mExchangeRateInput = exchangeRateInput;
                mUnitPrice = unitPrice;
                mBundlePrice = bundlePrice;
                mContext = context;
        }

        @Override
        public void onFocusChange(View v,
                                  boolean hasFocus)
        {
                if (hasFocus)
                {
                        return;
                }

                String newCurrencyCode = mEtCurrencyCode.getText().toString();

                boolean isNewCurrencyCodeValid = !TextUtils.isEmpty(newCurrencyCode) && FormatHelper.isValidCurrencyCode(newCurrencyCode);

                //Check currency code and update the symbols of the price input fields
                if (isNewCurrencyCodeValid && !mEtCurrencyCode.hasFocus())
                {
                        boolean isExistingCurrencySameAsNewCurrency = mExistingCurrecyCode.equals(newCurrencyCode);

                        //If new currency is not the same as existing currency, replace existing currency with the new currency
                        if (!isExistingCurrencySameAsNewCurrency)
                        {
                                mPrevCurrecyCode = mExistingCurrecyCode;
                                mExistingCurrecyCode = newCurrencyCode;
                                mExchangeRateInput.removeSourceCurrency(mPrevCurrecyCode);
                        }

                        if (!isExistingCurrencySameAsHomeCurrency())
                        {
                                boolean isChanged = mExchangeRateInput.addSourceCurrency(newCurrencyCode);
                                if (isChanged)
                                {
                                        if (PermissionHelper.isInternetUp(mContext))
                                        {
                                                mUnitPrice.displayProgress();
                                                mBundlePrice.displayProgress();
                                        }
                                }
                        }
                        else //new price currency is same as home currency
                        {

                                mUnitPrice.setTranslatedPrice(null);
                                mBundlePrice.setTranslatedPrice(null);

                        }


                }
                else if (!mEtCurrencyCode.hasFocus())
                {
                        mEtCurrencyCode.setText(mExistingCurrecyCode);
                }
        }

        public boolean isExistingCurrencySameAsHomeCurrency()
        {
                String newCurrencyCode = mEtCurrencyCode.getText().toString();
                String homeCurrencyCode = FormatHelper.getCurrencyCode(mCountryCode);
                return homeCurrencyCode.equals(newCurrencyCode);
        }
}
