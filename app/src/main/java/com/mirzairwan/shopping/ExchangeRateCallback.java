package com.mirzairwan.shopping;

import com.mirzairwan.shopping.domain.ExchangeRate;

import java.util.Map;

/**
 * Created by Mirza Irwan on 13/1/17.
 */
public interface ExchangeRateCallback
{
    void doCoversion(Map<String, ExchangeRate> exchangeRates);
}
