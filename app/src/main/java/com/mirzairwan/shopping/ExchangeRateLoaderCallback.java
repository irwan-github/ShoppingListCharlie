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

import static com.mirzairwan.shopping.domain.ExchangeRate.FOREIGN_CURRENCY_CODES;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREX_API_URL;

/**
 * Created by Mirza Irwan on 13/1/17.
 */

public class ExchangeRateLoaderCallback implements LoaderManager.LoaderCallbacks<Map<String,
        ExchangeRate>>
{
    private final String LOG_TAG = ExchangeRateLoaderCallback.class.getSimpleName();
    private String mBaseCurrencyCode;
    private Context mContext;

    ExchangeRateLoaderCallback(String baseCurrecyCode, Context context)
    {
        mBaseCurrencyCode = baseCurrecyCode;
        mContext = context;
    }

    @Override
    public Loader<Map<String, ExchangeRate>> onCreateLoader(int id, Bundle args)
    {
        Log.d(LOG_TAG, ">>>>onCreateLoader()");
        String[] codes = args.getStringArray(FOREIGN_CURRENCY_CODES);
        HashSet<String> sourceCurrencies = null;
        if (codes != null && codes.length > 0)
        {
            List<String> foreignCurrencies = Arrays.asList(codes);
            sourceCurrencies = new HashSet<>(foreignCurrencies);
        }
        return new ExchangeRateLoader(mContext,
                sourceCurrencies,
                args.getString(FOREX_API_URL));
    }

    @Override
    public void onLoadFinished(Loader<Map<String, ExchangeRate>> loader,
                               Map<String, ExchangeRate> exchangeRates)
    {

    }


    @Override
    public void onLoaderReset(Loader<Map<String, ExchangeRate>> loader)
    {

    }


}
