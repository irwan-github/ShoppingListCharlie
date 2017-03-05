package com.mirzairwan.shopping;

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
        private ItemControl mItemControl;
        protected String mExistingCurrecyCode;
        protected String mPrevCurrecyCode;
        private PriceField mUnitPrice;
        private PriceField mBundlePrice;
        private EditText mEtCurrencyCode;

        public OnCurrencyCodeChange(EditText etCurrencyCode, PriceField unitPrice, PriceField bundlePrice, ItemControl itemControl)
        {
                mEtCurrencyCode = etCurrencyCode;
                mExistingCurrecyCode = etCurrencyCode.getText().toString();
                mPrevCurrecyCode = mExistingCurrecyCode;
                mUnitPrice = unitPrice;
                mBundlePrice = bundlePrice;
                mItemControl = itemControl;
                MyTextUtils.setAllCapsInputFilter(etCurrencyCode);
                etCurrencyCode.setOnFocusChangeListener(this);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus)
        {
                if (hasFocus)
                {
                        return;
                }

                String newCurrencyCode = mEtCurrencyCode.getText().toString();

                boolean isNewCurrencyCodeValid = !TextUtils.isEmpty(newCurrencyCode) && FormatHelper.isValidCurrencyCode(newCurrencyCode);

                /* Check currency code and update the symbols of the price input fields */
                if (isNewCurrencyCodeValid && !mEtCurrencyCode.hasFocus())
                {
                        boolean isExistingCurrencySameAsNewCurrency = mExistingCurrecyCode.equals(newCurrencyCode);

                        /* If new currency is not the same as existing currency:
                                Replace currency symbol hint price fields with new currency symbol
                                Set existing currency with the new currency
                                */
                        if (!isExistingCurrencySameAsNewCurrency)
                        {
                                mPrevCurrecyCode = mExistingCurrecyCode;
                                mExistingCurrecyCode = newCurrencyCode;
                                mUnitPrice.setCurrencySymbolInPriceHint(mExistingCurrecyCode);
                                mBundlePrice.setCurrencySymbolInPriceHint(mExistingCurrecyCode);
                                mItemControl.onChange();
                        }
                }
                else if (!mEtCurrencyCode.hasFocus())
                {
                        mEtCurrencyCode.setText(mExistingCurrecyCode);
                }
        }
}
