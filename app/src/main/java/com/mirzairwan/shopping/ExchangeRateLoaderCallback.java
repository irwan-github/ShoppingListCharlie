package com.mirzairwan.shopping;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import com.mirzairwan.shopping.domain.ExchangeRate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.mirzairwan.shopping.domain.ExchangeRate.DESTINATION_CURRENCY_CODE;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREIGN_CURRENCY_CODES;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREX_API_URL;

/**
 * Created by Mirza Irwan on 13/1/17.
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


        public Loader<Map<String, ExchangeRate>> onCreateLoader(int id, Bundle args)
        {
                Log.d(LOG_TAG, " >>>>>>> onCreateLoader() ExchangeRate");
                String destCurrencyCode = args.getString(DESTINATION_CURRENCY_CODE);
                String[] foreignCurrencyCodes = args.getStringArray(FOREIGN_CURRENCY_CODES);
                HashSet<String> sourceCurrencies = null;
                if (foreignCurrencyCodes != null && foreignCurrencyCodes.length > 0)
                {
                        List<String> foreignCurrencies = Arrays.asList(foreignCurrencyCodes);
                        sourceCurrencies = new HashSet<>(foreignCurrencies);
                }
//        return new ExchangeRateLoader(mContext,
//                sourceCurrencies,
//                args.getString(FOREX_API_URL), destCurrencyCode);
                ExchangeRateInput exchangeRateInput = new ExchangeRateInput();
                exchangeRateInput.setBaseWebApi(args.getString(FOREX_API_URL));
                exchangeRateInput.setSourceCurrencies(sourceCurrencies);
                exchangeRateInput.setBaseCurrency(destCurrencyCode);
                return new ExchangeRateAwareLoader(mContext, exchangeRateInput);
        }


}
