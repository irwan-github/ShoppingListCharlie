package com.mirzairwan.shopping;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.text.ParseException;

/**
 * Created by Mirza Irwan on 16/1/17.
 */

public class ExchangeRateDisplayState implements View.OnFocusChangeListener
{
    private PriceField mPriceField;
    String mHomeCurrencyCode;
    EditText mEtPrice;
    OnExchangeRateRequest mOnExchangeRateListener;
    CurrencyCodeObserver mCurrencyCodeObserver;
    private final String LOG_TAG = ExchangeRateDisplayState.class.getSimpleName();

    public ExchangeRateDisplayState(OnExchangeRateRequest onExchangeRateListener,
                                    PriceField priceField)
    {
        mHomeCurrencyCode = priceField.getHomeCurrencyCode();
        mEtPrice = priceField.getEditTextSourcePrice();
        mPriceField = priceField;
        mOnExchangeRateListener = onExchangeRateListener;
    }

    public void setCurrencyCodeObserver(CurrencyCodeObserver currencyCodeObserver)
    {
        mCurrencyCodeObserver = currencyCodeObserver;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        boolean isPriceEmpty = false;
        try
        {
            isPriceEmpty = MyTextUtils.isZeroValue(mEtPrice);
        } catch (ParseException e)
        {
            e.printStackTrace();
            return;
        }

        if(!v.hasFocus() && !isPriceEmpty)
        {
            if(!mCurrencyCodeObserver.hasFocus() &&
                    !mCurrencyCodeObserver.isExistingCurrencySameAsHomeCurrency()
                    && !isPriceEmpty)
            {
                mPriceField.displayProgress();
                if (mCurrencyCodeObserver.isCurrecyCodeSameAsPrev())
                {
                    Log.d(LOG_TAG, ">>>> processExchangeRate");
                    mOnExchangeRateListener.doConversion(mCurrencyCodeObserver.getCurrencyCode(),
                            mPriceField.getLoaderId(), false);
                }
                else
                {
                    Log.d(LOG_TAG, ">>>> restartProcessExchangeRate");
                    mOnExchangeRateListener.doConversion(mCurrencyCodeObserver.getCurrencyCode(),
                            mPriceField.getLoaderId(), true);
                }
            }
        }
        else if(!v.hasFocus())//Price is empty
        {
            mPriceField.clear();
        }
    }

    /**
     * Price field need observe currency code field to know whether to init or restart Loader process
     */
    public interface CurrencyCodeObserver
    {
        boolean isCurrecyCodeSameAsPrev();

        boolean isExistingCurrencySameAsHomeCurrency();

        boolean hasFocus();

        String getCurrencyCode();
    }

}
