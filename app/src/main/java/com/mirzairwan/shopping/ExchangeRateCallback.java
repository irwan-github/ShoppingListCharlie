package com.mirzairwan.shopping;

import com.mirzairwan.shopping.domain.ExchangeRate;

import java.util.Map;

/**
 * Created by Mirza Irwan on 13/1/17.
 * ExchangeRate request by fragment(s) will be fulfilled by ShoppingActivity class. Fragment
 * must send this callback class to be called back by ShoppingActivity when ExchangeRateLoader
 * call back ShoppingActivity onLoadFinished
 */
public interface ExchangeRateCallback
{
    /**
     * Call back by ExchangeRate Loader
     * @param exchangeRates May be null
     */
    void doCoversion(Map<String, ExchangeRate> exchangeRates);
}
