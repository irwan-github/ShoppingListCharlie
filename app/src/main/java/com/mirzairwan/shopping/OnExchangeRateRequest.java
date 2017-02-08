package com.mirzairwan.shopping;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public interface OnExchangeRateRequest
{
    void doConversion(String sourceCurrencyCode, int loaderID, boolean isRestart);
}
