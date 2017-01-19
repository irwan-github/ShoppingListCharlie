package com.mirzairwan.shopping;

import android.app.LoaderManager;
import android.os.Bundle;

import static com.mirzairwan.shopping.domain.ExchangeRate.DESTINATION_CURRENCY_CODE;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREIGN_CURRENCY_CODES;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREX_API_URL;

/**
 * Created by Mirza Irwan on 18/1/17.
 */

public class OnExchangeRateRequestImpl implements OnExchangeRateRequest
{
    private LoaderManager mLoaderMgr;
    private String mDestCurrencyCode;
    private String mBaseEndPoint;
    private ItemActivity.ItemExchangeRateLoaderCallback mItemExchangeRateCallback;
    private final String LOG_TAG = OnExchangeRateRequestImpl.class.getSimpleName();

    public OnExchangeRateRequestImpl(ItemActivity.ItemExchangeRateLoaderCallback
                                             itemExchangeRateCallback, String baseEndPoint,
                                     String destCurrencyCode, LoaderManager loaderManager)
    {
        mItemExchangeRateCallback = itemExchangeRateCallback;
        mBaseEndPoint = baseEndPoint;
        mDestCurrencyCode = destCurrencyCode;
        mLoaderMgr = loaderManager;
    }

    @Override
    public void doConversion(String foreignCurrencyCode, int loaderID, boolean isRestart)
    {
        Bundle args = new Bundle();
        args.putString(DESTINATION_CURRENCY_CODE, mDestCurrencyCode);
        args.putStringArray(FOREIGN_CURRENCY_CODES, new String[]{foreignCurrencyCode});
        args.putString(FOREX_API_URL, mBaseEndPoint);
        if (isRestart)
        {
            mLoaderMgr.restartLoader(loaderID, args,
                    mItemExchangeRateCallback);
        }
        else
        {
            mLoaderMgr.initLoader(loaderID, args,
                    mItemExchangeRateCallback);
        }
    }

}
