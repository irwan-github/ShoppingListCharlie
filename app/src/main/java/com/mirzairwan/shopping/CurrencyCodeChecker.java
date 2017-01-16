package com.mirzairwan.shopping;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import static com.mirzairwan.shopping.LoaderHelper.ITEM_EXCHANGE_RATE_LOADER_ID;

/**
 * Created by Mirza Irwan on 17/1/17.
 */

public class CurrencyCodeChecker implements View.OnFocusChangeListener, ExchangeRateDisplayState.CurrencyCodeObserver
{
    private PriceField mBundlePrice;
    private PriceField mUnitPrice;
    private String mCountryCode;
    private ExchangeRateDisplayState.OnExchangeRateListener mOnExchangeRateListener;
    protected String mExistingCurrecyCode;
    protected String mPrevCurrecyCode;
    private EditText mEtCurrencyCode;
    //private String newCurrencyCode;

    public CurrencyCodeChecker(EditText etCurrencyCode, String countryCode, PriceField unitPrice,
                               PriceField bundlePrice,
                               ExchangeRateDisplayState.OnExchangeRateListener onExchangeRateListener)
    {
        mEtCurrencyCode = etCurrencyCode;
        mExistingCurrecyCode = etCurrencyCode.getText().toString();
        mPrevCurrecyCode = mExistingCurrecyCode;
        mCountryCode = countryCode;
        mUnitPrice = unitPrice;
        mBundlePrice = bundlePrice;
        mOnExchangeRateListener = onExchangeRateListener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        String newCurrencyCode = mEtCurrencyCode.getText().toString();

        boolean isCurrencyCodeValid = !TextUtils.isEmpty(mEtCurrencyCode.getText()) &&
                FormatHelper.isValidCurrencyCode(newCurrencyCode);

        String homeCurrencyCode = FormatHelper.getCurrencyCode(mCountryCode);

        boolean isNewCodeIdenticalToHomeCode = homeCurrencyCode.equals(newCurrencyCode);

        //Check currency code and update the symbols of the price input fields
        if (isCurrencyCodeValid && !mEtCurrencyCode.hasFocus())
        {
            boolean isNewCodeIdenticalToCurrentCode = mExistingCurrecyCode.equals(newCurrencyCode);

            if(!isNewCodeIdenticalToCurrentCode)
            {
                mPrevCurrecyCode = mExistingCurrecyCode;
                mExistingCurrecyCode = newCurrencyCode;
            }

            mUnitPrice.setCurrencySymbolInPriceHint();

            mBundlePrice.setCurrencySymbolInPriceHint();

            boolean isAnyPriceExist = !mUnitPrice.isEmpty() ||
                    !mBundlePrice.isEmpty();



            if (isAnyPriceExist && !isNewCodeIdenticalToHomeCode)
            {
                mUnitPrice.setTranslatedPricesVisibility(View.INVISIBLE);
                mUnitPrice.getProgressBar().setVisibility(View.VISIBLE);

                mBundlePrice.setTranslatedPricesVisibility(View.INVISIBLE);
                mBundlePrice.getProgressBar().setVisibility(View.VISIBLE);

                if (isNewCodeIdenticalToCurrentCode)
                {
                    mOnExchangeRateListener.processExchangeRate(newCurrencyCode, ITEM_EXCHANGE_RATE_LOADER_ID);
                }
                else
                    mOnExchangeRateListener.restartProcessExchangeRate(newCurrencyCode, ITEM_EXCHANGE_RATE_LOADER_ID);
            }


        }
        else if (!mEtCurrencyCode.hasFocus())
        {
            mEtCurrencyCode.setText(mExistingCurrecyCode);
//            Toast.makeText(ItemActivity.this,
//                    "Invalid currency code: " + newCurrencyCode,
//                    Toast.LENGTH_SHORT)
//                    .show();
        }
    }

    @Override
    public boolean isCurrecyCodeSameAsPrev()
    {
        boolean isNewCodeSameAsPrevCode = mExistingCurrecyCode.equals(mPrevCurrecyCode);
        return isNewCodeSameAsPrevCode;
    }
}
