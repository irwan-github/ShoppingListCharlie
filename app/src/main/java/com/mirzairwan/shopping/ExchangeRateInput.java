package com.mirzairwan.shopping;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 * This object is observed by ExchangeRateAwareLoader for changes. ShoppingActivity will use this
 * class to make changes to exchange rate inputs as necessary.
 * <p>
 * Created by Mirza Irwan on 20/1/17.
 */

public class ExchangeRateInput extends Observable
{
    private Set<String> mSourceCurrencies;
    private String mBaseCurrency = "";
    private String mBaseUri = "";

    public ExchangeRateInput()
    {
        mSourceCurrencies = new HashSet<>();
    }

    public void addSourceCurrency(String sourceCurrencies)
    {
        boolean isChanged = mSourceCurrencies.add(sourceCurrencies);
        if (isChanged)
        {
            setChanged();
            notifyObservers(mSourceCurrencies);
        }
    }

    public boolean setSourceCurrencies(Set<String> sourceCurrencies)
    {
        boolean isAddAllChanged = mSourceCurrencies.addAll(sourceCurrencies);
        sourceCurrencies.retainAll(mSourceCurrencies);
        mSourceCurrencies = sourceCurrencies;

        if (isAddAllChanged)
        {
            setChanged();
            notifyObservers(mSourceCurrencies);
        }
        return isAddAllChanged;
    }

    public void setBaseCurrency(String baseCurrency)
    {
        if (!mBaseCurrency.equals(baseCurrency))
        {
            mBaseCurrency = baseCurrency;
            setChanged();
            notifyObservers(mBaseCurrency);
            removeSourceCurrency(baseCurrency);
        }
    }

    public void setBaseWebApi(String baseWebApi)
    {
        if (!mBaseUri.equals(baseWebApi))
        {
            mBaseUri = baseWebApi;
            setChanged();
            notifyObservers(mBaseCurrency);
        }
    }

    public Set<String> getSourceCurrencies()
    {
        return mSourceCurrencies;
    }

    public String getBaseCurrency()
    {
        return mBaseCurrency;
    }

    public String getBaseWebApi()
    {
        return mBaseUri;
    }

    public void removeSourceCurrency(String existingCountryCode)
    {
        mSourceCurrencies.remove(existingCountryCode);
    }
}
