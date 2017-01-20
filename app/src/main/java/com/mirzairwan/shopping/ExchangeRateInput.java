package com.mirzairwan.shopping;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
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
        if(isChanged)
        {
            setChanged();
            notifyObservers(mSourceCurrencies);
        }
    }

    public void setSourceCurrencies(Set<String> sourceCurrencies)
    {
        mSourceCurrencies.clear();
        mSourceCurrencies.addAll(sourceCurrencies);
        setChanged();
        notifyObservers(mSourceCurrencies);
    }

    public void setBaseCurrency(String baseCurrency)
    {

        if(!mBaseCurrency.equals(baseCurrency))
        {
            mBaseCurrency = baseCurrency;
            mSourceCurrencies.remove(baseCurrency);
            setChanged();
            notifyObservers(mBaseCurrency);
        }
    }

    public void setBaseWebApi(String baseWebApi)
    {

        if(!mBaseUri.equals(baseWebApi))
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
        boolean isChanged = mSourceCurrencies.remove(existingCountryCode);
        if(isChanged)
        {
            setChanged();
            notifyObservers(mSourceCurrencies);
        }
    }
}
