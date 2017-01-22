package com.mirzairwan.shopping;

import java.util.Set;

/**
 * Created by Mirza Irwan on 13/1/17.
 */

public interface OnExchangeRateRequestListener
{
    void onRequest(Set<String> sourceCurrencies);
}
