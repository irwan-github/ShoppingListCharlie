package com.mirzairwan.shopping;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Mirza Irwan on 16/1/17.
 */

public class ExchangeRateDisplayState implements View.OnFocusChangeListener
{
    private PriceField mTranslatedPrice;
    String mHomeCurrencyCode;
    EditText mEtCurrencyCode;
    EditText mEtPrice;
    OnExchangeRateListener mOnExchangeRateListener;
    CurrencyCodeObserver mCurrencyCodeObserver;
    private final String LOG_TAG = ExchangeRateDisplayState.class.getSimpleName();

    public ExchangeRateDisplayState(OnExchangeRateListener onExchangeRateListener,
                                    PriceField translatedPrice)
    {
        mHomeCurrencyCode = translatedPrice.getHomeCurrencyCode();
        mEtCurrencyCode = translatedPrice.getCurrencyCodeView();
        mEtPrice = translatedPrice.getEditTextSourcePrice();
        mTranslatedPrice = translatedPrice;
        mOnExchangeRateListener = onExchangeRateListener;
    }

    public void setCurrencyCodeObserver(CurrencyCodeObserver currencyCodeObserver)
    {
        mCurrencyCodeObserver = currencyCodeObserver;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        boolean isCodeSameAsHomeCode = mHomeCurrencyCode.equals(mEtCurrencyCode.getText().toString());
        boolean isPriceEmpty = TextUtils.isEmpty(mEtPrice.getText());

        if(!v.hasFocus())
        {
            if(!mEtCurrencyCode.hasFocus() && !isCodeSameAsHomeCode && !isPriceEmpty &&
                    mCurrencyCodeObserver.isCurrecyCodeSameAsPrev())
            {
                Log.d(LOG_TAG, ">>>> processExchangeRate");
                mTranslatedPrice.getProgressBar().setVisibility(View.VISIBLE);
                mOnExchangeRateListener.processExchangeRate(mEtCurrencyCode.getText().toString(),
                        mTranslatedPrice.getLoaderId());
            }

            if(!mEtCurrencyCode.hasFocus() && !isCodeSameAsHomeCode && !isPriceEmpty &&
                    !mCurrencyCodeObserver.isCurrecyCodeSameAsPrev())
            {
                Log.d(LOG_TAG, ">>>> restartProcessExchangeRate");
                mTranslatedPrice.getProgressBar().setVisibility(View.VISIBLE);
                mOnExchangeRateListener.restartProcessExchangeRate(mEtCurrencyCode.getText().toString(),
                        mTranslatedPrice.getLoaderId());
            }

        }

    }

    public interface OnExchangeRateListener
    {
        void processExchangeRate(String currencyCode, int loaderID);

        void restartProcessExchangeRate(String newCurrencyCode, int loaderID);
    }

    public interface CurrencyCodeObserver
    {
        boolean isCurrecyCodeSameAsPrev();
    }



}
