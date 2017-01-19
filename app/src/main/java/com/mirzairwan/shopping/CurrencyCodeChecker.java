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
    private OnExchangeRateRequest mOnExchangeRateListener;
    protected String mExistingCurrecyCode;
    protected String mPrevCurrecyCode;
    private EditText mEtCurrencyCode;

    public CurrencyCodeChecker(EditText etCurrencyCode, String countryCode, PriceField unitPrice,
                               PriceField bundlePrice,
                               OnExchangeRateRequest onExchangeRateListener)
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

        //Check currency code and update the symbols of the price input fields
        if (isCurrencyCodeValid && !mEtCurrencyCode.hasFocus())
        {
            boolean isNewCodeIdenticalToCurrentCode = mExistingCurrecyCode.equals(newCurrencyCode);

            if(!isNewCodeIdenticalToCurrentCode)
            {
                mPrevCurrecyCode = mExistingCurrecyCode;
                mExistingCurrecyCode = newCurrencyCode;

                mUnitPrice.setCurrencySymbolInPriceHint(mExistingCurrecyCode);
                mBundlePrice.setCurrencySymbolInPriceHint(mExistingCurrecyCode);
            }

            if (!isExistingCurrencySameAsHomeCurrency())
            {

                mUnitPrice.displayProgress();

                mBundlePrice.displayProgress();

                if (isNewCodeIdenticalToCurrentCode)
                {
                    mOnExchangeRateListener.doConversion(newCurrencyCode,
                                                            ITEM_EXCHANGE_RATE_LOADER_ID, false);
                }
                else
                    mOnExchangeRateListener.doConversion(newCurrencyCode,
                                                            ITEM_EXCHANGE_RATE_LOADER_ID, true);
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

    @Override
    public boolean isExistingCurrencySameAsHomeCurrency()
    {
        String newCurrencyCode = mEtCurrencyCode.getText().toString();
        String homeCurrencyCode = FormatHelper.getCurrencyCode(mCountryCode);
        return homeCurrencyCode.equals(newCurrencyCode);
    }

    @Override
    public boolean hasFocus()
    {
        return mEtCurrencyCode.hasFocus();
    }

    @Override
    public String getCurrencyCode()
    {
        return mEtCurrencyCode.getText().toString();
    }
}
