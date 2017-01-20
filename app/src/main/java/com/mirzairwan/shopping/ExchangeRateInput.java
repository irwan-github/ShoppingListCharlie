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

    public ExchangeRateInput()
    {
        Set<String> mSourceCurrencies = new HashSet<>();
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
}
