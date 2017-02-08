package com.mirzairwan.shopping;

import com.mirzairwan.shopping.domain.ExchangeRate;

import java.util.Map;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 *  Contact owner at mirza.irwan.osman@gmail.com
 *
 * ExchangeRate request by Shopping List fragment will be fulfilled by ShoppingActivity class. Fragment
 * must send this callback class when requesting to ShoppingActivity. ShoppingActivity will then
 * start ExchangeRateLoader which will call back ShoppingActivity's onLoadFinished.
 */
public interface ExchangeRateCallback
{
    /**
     * Call back by ShoppingActivity upon receiving ExchangeRate from ExchangeRateLoader
     * @param exchangeRates May be null
     */
    void doCoversion(Map<String, ExchangeRate> exchangeRates);
}
