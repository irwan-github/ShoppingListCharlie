package com.mirzairwan.shopping;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Mirza Irwan on 16/1/17.
 */

public class ExchangeRateDisplayState implements View.OnFocusChangeListener
{
    String mHomeCurrencyCode;
    EditText mEtCurrencyCode;
    EditText mEtPrice;
    OnExchangeRateListener mOnExchangeRateListener;

    public ExchangeRateDisplayState(OnExchangeRateListener onExchangeRateListener,
                                    String homeCurrencyCode, EditText etCurrencyCode,
                                    EditText etPrice)
    {
        mHomeCurrencyCode = homeCurrencyCode;
        mEtCurrencyCode = etCurrencyCode;
        mEtPrice = etPrice;
        mOnExchangeRateListener = onExchangeRateListener;

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        boolean areCodesIdentical = mHomeCurrencyCode.equals(mEtCurrencyCode.getText().toString());
        boolean isPriceEmpty = TextUtils.isEmpty(mEtPrice.getText());

        if(v == mEtPrice && !v.hasFocus())
        {
            if(!mEtCurrencyCode.hasFocus() && !areCodesIdentical && !isPriceEmpty)
            {
                mOnExchangeRateListener.processExchangeRate(mEtCurrencyCode.getText().toString());
            }
        }
    }

    public interface OnExchangeRateListener
    {
        void processExchangeRate(String currencyCode);
    }


}
