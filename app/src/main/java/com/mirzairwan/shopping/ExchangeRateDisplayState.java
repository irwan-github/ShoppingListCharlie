package com.mirzairwan.shopping;

import android.text.TextUtils;
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

    public ExchangeRateDisplayState(OnExchangeRateListener onExchangeRateListener,
                                    String homeCurrencyCode, EditText etCurrencyCode,
                                    PriceField translatedPrice)
    {
        mHomeCurrencyCode = homeCurrencyCode;
        mEtCurrencyCode = etCurrencyCode;
        mEtPrice = translatedPrice.getEditTextSourcePrice();
        mTranslatedPrice = translatedPrice;
        mOnExchangeRateListener = onExchangeRateListener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        boolean isNewCodeIdenticalToHomeCode = mHomeCurrencyCode.equals(mEtCurrencyCode.getText().toString());
        boolean isPriceEmpty = TextUtils.isEmpty(mEtPrice.getText());

        if(!v.hasFocus())
        {
            if(!mEtCurrencyCode.hasFocus() && !isNewCodeIdenticalToHomeCode && !isPriceEmpty)
            {
                mTranslatedPrice.getProgressBar().setVisibility(View.VISIBLE);
                mOnExchangeRateListener.processExchangeRate(mEtCurrencyCode.getText().toString());
            }
        }

    }

    public interface OnExchangeRateListener
    {
        void processExchangeRate(String currencyCode);

        void restartProcessExchangeRate(String newCurrencyCode);
    }


}
