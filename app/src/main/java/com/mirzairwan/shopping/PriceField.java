package com.mirzairwan.shopping;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;

import com.mirzairwan.shopping.domain.ExchangeRate;
import com.mirzairwan.shopping.domain.Price;

import java.text.ParseException;

import static com.mirzairwan.shopping.LoaderHelper.BUNDLE_PRICE_EXCHANGE_RATE_LOADER_ID;
import static com.mirzairwan.shopping.LoaderHelper.UNIT_PRICE_EXCHANGE_RATE_LOADER_ID;

/**
 * Created by Mirza Irwan on 16/1/17.
 */

public class PriceField
{
    private final Price.Type mPriceType;
    private View mProgressBar;
    private String mHomeCountryCode;
    private EditText mEtPrice;
    private EditText mEtTranslatedPrice;
    private String mTextHint;

    public PriceField(EditText etPrice, EditText etTranslatedPrice, View progressBar,
                      String homeCountryCode, String textHint, Price.Type priceType)
    {
        mEtPrice = etPrice;
        mEtTranslatedPrice = etTranslatedPrice;
        mProgressBar = progressBar;
        mHomeCountryCode = homeCountryCode;
        mTextHint = textHint;
        mPriceType = priceType;
    }

    /**
     * Show translated price. Hides progress bar.
     * If exchange rate is null, the translated price is set to null and hidden.
     * @param exchangeRate
     */
    public void setTranslatedPrice(ExchangeRate exchangeRate)
    {
        mProgressBar.setVisibility(View.INVISIBLE);

        if(exchangeRate == null)
        {
            mEtTranslatedPrice.setText(null);
            setTranslatedPricesVisibility(View.INVISIBLE);
            return;
        }

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

    private void clearHintInTranslatedPrice()
    {
        ViewParent viewParent = mEtTranslatedPrice.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setHint("");
    }

    private void setCurrencyCodeInTranslatedPriceHint(String hint, String homeCountryCode)
    {
        String currencyCode = FormatHelper.getCurrencyCode(homeCountryCode);
        String hintPx = hint + " (" + currencyCode + ")";
        ViewParent viewParent = mEtTranslatedPrice.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setHint(hintPx);
    }

    /**
     * Set currency symbol using currency code. It depends heavily on Locale
     * @param currencyCode
     */
    public void setCurrencySymbolInPriceHint(String currencyCode)
    {
        String currencySymbol = FormatHelper.getCurrencySymbol(currencyCode);
        String hintPx = mTextHint + " (" + currencySymbol + ")";
        ViewParent viewParent = mEtPrice.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setHint(hintPx);
    }

    /**
     * Pre-condition:
     * 1. All the views have been materialized.
     * 2. The edit text has a pre-exising value to be displayed
     *
     * Show the translated price if source price is not zero value.
     * @param visibleId
     */
    public void setTranslatedPricesVisibility(int visibleId)
    {
        try
        {
            if(MyTextUtils.isZeroValue(mEtPrice))
                return;
        } catch (ParseException e)
        {
            e.printStackTrace();
            return;
        }

        mEtTranslatedPrice.setVisibility(visibleId);
        ViewParent viewParent = mEtTranslatedPrice.getParent();
        TextInputLayout etLayout = (TextInputLayout) (viewParent.getParent());
        etLayout.setVisibility(visibleId);
        clearHintInTranslatedPrice();
        setCurrencyCodeInTranslatedPriceHint(mTextHint, mHomeCountryCode);
    }

    public EditText getEditTextSourcePrice()
    {
        return mEtPrice;
    }

    public void setFormattedPrice(String priceForDisplay, String currencyCode)
    {
        mEtPrice.setText(priceForDisplay);
        setCurrencySymbolInPriceHint(currencyCode);
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

    /**
     * Set the text value of source price to null.
     * Set the text value of destination price to null.
     * Remove the translated price field and header/hint
     */
    public void clear()
    {
        mEtPrice.setText(null);
        mEtTranslatedPrice.setText(null);
        setTranslatedPricesVisibility(View.INVISIBLE);
    }

    public String getHomeCurrencyCode()
    {
        return mHomeCountryCode;
    }

    public int getLoaderId()
    {
        if(mPriceType == Price.Type.UNIT_PRICE)
            return UNIT_PRICE_EXCHANGE_RATE_LOADER_ID;

        else if(mPriceType == Price.Type.BUNDLE_PRICE)
            return BUNDLE_PRICE_EXCHANGE_RATE_LOADER_ID;
        else
            throw new IllegalArgumentException("Loader not supported");
    }

    public void displayProgress()
    {
        try
        {
            if(!MyTextUtils.isZeroValue(mEtPrice))
            {
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                    setTranslatedPricesVisibility(View.INVISIBLE);
                }
            }
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public Price.Type getPriceType()
    {
        return mPriceType;
    }
}
