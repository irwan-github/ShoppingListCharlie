package com.mirzairwan.shopping;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import com.mirzairwan.shopping.domain.ExchangeRate;

import java.util.Map;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public abstract class ExchangeRateLoaderCallback implements LoaderManager.LoaderCallbacks<Map<String, ExchangeRate>>
{
        protected static final String LOG_TAG = ExchangeRateLoaderCallback.class.getSimpleName();
        private ExchangeRateInput mExchangeRateInput;
        private Context mContext;

        ExchangeRateLoaderCallback(Context context)
        {
                //Log.d(LOG_TAG, "Construct");
                mContext = context;
        }

        public ExchangeRateLoaderCallback(Context context, ExchangeRateInput exchangeRateInput)
        {
                mContext = context;
                mExchangeRateInput = exchangeRateInput;
        }


        public Loader<Map<String, ExchangeRate>> onCreateLoader(int id, Bundle args)
        {
                Log.d(LOG_TAG, " >>>>>>> onCreateLoader() ExchangeRate");
//                String destCurrencyCode = args.getString(DESTINATION_CURRENCY_CODE);
//                String[] foreignCurrencyCodes = args.getStringArray(FOREIGN_CURRENCY_CODES);
//                HashSet<String> sourceCurrencies = null;
//                if (foreignCurrencyCodes != null && foreignCurrencyCodes.length > 0)
//                {
//                        List<String> foreignCurrencies = Arrays.asList(foreignCurrencyCodes);
//                        sourceCurrencies = new HashSet<>(foreignCurrencies);
//                }

//                if(mExchangeRateInput == null)
//                {
//                        mExchangeRateInput = new ExchangeRateInput();
//                        mExchangeRateInput.setBaseWebApi(args.getString(FOREX_API_URL));
//                        mExchangeRateInput.setSourceCurrencies(sourceCurrencies);
//                        mExchangeRateInput.setBaseCurrency(destCurrencyCode);
//                }

                return new ExchangeRateAwareLoader(mContext, mExchangeRateInput);
        }


}
