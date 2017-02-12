package com.mirzairwan.shopping;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * This object is observed by ExchangeRateAwareLoader for changes. ShoppingActivity and ItemEditingActivity will use this
 * class to make changes to exchange rate inputs as necessary.
 * <p>
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

        public boolean addSourceCurrency(String sourceCurrency)
        {
                boolean isChanged = mSourceCurrencies.add(sourceCurrency);
                if (isChanged)
                {
                        setChanged();
                        notifyObservers(mSourceCurrencies);
                }
                return isChanged;
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
