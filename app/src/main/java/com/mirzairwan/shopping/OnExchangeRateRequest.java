package com.mirzairwan.shopping;

/**
 * Created by Mirza Irwan on 18/1/17.
 */

public interface OnExchangeRateRequest
{
    void doConversion(String sourceCurrencyCode, int loaderID, boolean isRestart);
}
