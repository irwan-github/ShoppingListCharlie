package com.mirzairwan.shopping;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;

import com.mirzairwan.shopping.domain.ExchangeRate;

/**
 * Created by Mirza Irwan on 16/1/17.
 */

public class PriceField
{
    private View mProgressBar;
    private String mHomeCountryCode;
    private EditText mEtCurrencyCode;
    private EditText mEtPrice;
    private EditText mEtTranslatedPrice;
    private String mTextHint;

    public PriceField(EditText etCurrencyCode, EditText etPrice, EditText etTranslatedPrice, View progressBar,
                      String homeCountryCode, String textHintId)
    {
        mEtCurrencyCode = etCurrencyCode;
        mEtPrice = etPrice;
        mEtTranslatedPrice = etTranslatedPrice;
        mProgressBar = progressBar;
        mHomeCountryCode = homeCountryCode;
        mTextHint = textHintId;
    }

    public void setTranslatedPrice(ExchangeRate exchangeRate)
    {
        mProgressBar.setVisibility(View.INVISIBLE);
        //Unit price greater than 0.00
        boolean isPriceValid = !TextUtils.isEmpty(mEtPrice.getText()) &&
                Double.parseDouble(mEtPrice.getText().toString()) > 0;
        if (isPriceValid && !mEtPrice.hasFocus())
        {
            String sUnitPx = mEtPrice.getText().toString();
            double priceVal;
            setTranslatedPricesVisibility(View.VISIBLE);
            priceVal = Double.parseDouble(sUnitPx);
            double translated = exchangeRate.compute(priceVal);
            mEtTranslatedPrice.setText(FormatHelper.formatToTwoDecimalPlaces(translated));
            clearHintInTranslatedPrice();
            setCurrencyCodeInTranslatedPriceHint(mTextHint, mHomeCountryCode);
        }
    }

    protected View getProgressBar()
    {
        return mProgressBar;
    }

    public void clearHintInTranslatedPrice()
    {
        ViewParent viewParent = mEtTranslatedPrice.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setHint("");
    }

    public void setCurrencyCodeInTranslatedPriceHint(String hint, String homeCountryCode)
    {
        String currencyCode = FormatHelper.getCurrencyCode(homeCountryCode);
        String hintPx = hint + " (" + currencyCode + ")";
        ViewParent viewParent = mEtTranslatedPrice.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setHint(hintPx);
    }


    public void setCurrencySymbolInPriceHint()
    {
        String currencySymbol = FormatHelper.getCurrencySymbol(mEtCurrencyCode.getText().toString());
        String hintPx = mTextHint + " (" + currencySymbol + ")";
        ViewParent viewParent = mEtPrice.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setHint(hintPx);
    }

    public void setTranslatedPricesVisibility(int visibleId)
    {
        mEtTranslatedPrice.setVisibility(visibleId);
        ViewParent viewParent = mEtTranslatedPrice.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setVisibility(visibleId);
    }

    public EditText getEditTextSourcePrice()
    {
        return mEtPrice;
    }

    public void setFormattedPrice(String priceForDisplay)
    {
        mEtPrice.setText(priceForDisplay);
        setCurrencySymbolInPriceHint();
    }

    public String getPrice()
    {
        if (mEtPrice != null && !TextUtils.isEmpty(mEtPrice.getText()))
        {
            return mEtPrice.getText().toString();
        }
        else
            return "0.00";
    }

    public boolean isEmpty()
    {
        return TextUtils.isEmpty(mEtPrice.getText());
    }

    public void clear()
    {
        mEtPrice.setText(null);
        mEtTranslatedPrice.setText(null);
        setTranslatedPricesVisibility(View.INVISIBLE);
    }
}
