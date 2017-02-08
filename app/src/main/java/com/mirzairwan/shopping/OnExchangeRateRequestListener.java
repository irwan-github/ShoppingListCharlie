package com.mirzairwan.shopping;

import java.util.Set;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public interface OnExchangeRateRequestListener
{
    void onRequest(Set<String> sourceCurrencies);
}
