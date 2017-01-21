package com.mirzairwan.shopping;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mirzairwan.shopping.domain.ExchangeRate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Mirza Irwan on 13/1/17.
 * It fetches ECB exchange rates using web API.
 */

class ExchangeRateAwareLoader extends AsyncTaskLoader<Map<String, ExchangeRate>>
{
    private static final String LOG_TAG = ExchangeRateAwareLoader.class.getSimpleName();
    private ExchangeRateInput mExchangeRateInput;
    private Map<String, ExchangeRate> mExchangeRates;
    private Observer mObserver;

    ExchangeRateAwareLoader(Context context, ExchangeRateInput exchangeRateInput)
    {
        super(context);
        Log.d(LOG_TAG, ">>>>>>> Construct ExchangeRateAwareLoader()");
        mExchangeRateInput = exchangeRateInput;
    }

    /**
     * Good place to cache the exchange rate. Just remember this is on the UI thread so nothing
     * lengthy!
     */
    @Override
    public void deliverResult(Map<String, ExchangeRate> exchangeRates)
    {
        Log.d(LOG_TAG, ">>>>>>> deliverResult: " + exchangeRates);
        mExchangeRates = exchangeRates;
        super.deliverResult(exchangeRates);
    }


    @Override
    protected void onStartLoading()
    {
        Log.d(LOG_TAG, ">>>>>>> onStartLoading: " + getId());

        if (mObserver == null)
        {
            mObserver = new Observer()
            {
                @Override
                public void update(Observable o, Object arg)
                {
                    Log.d("Observer", ">>>>>>> update: " + arg.toString());
                    // Notify the loader to reload the data
                    onContentChanged();
                    // If the loader is started, this will kick off
                    // loadInBackground() immediately. Otherwise,
                    // the fact that something changed will be cached
                    // and can be later retrieved via takeContentChanged()
                }
            };

            mExchangeRateInput.addObserver(mObserver);
        }

        // Something has changed
        if (takeContentChanged())
        {
            //No source currencies, don't fetch. Set null exchange rates
            if (mExchangeRateInput.getSourceCurrencies() == null ||
                    mExchangeRateInput.getSourceCurrencies().size() == 0)
            {
                Log.d(LOG_TAG, ">>>>>>> onStartLoading takeContentChanged BUT source currency is " +
                        "empty. Deliver null");
                deliverResult(null);
            }
            else
            {
                Log.d(LOG_TAG, ">>>>>>> onStartLoading takeContentChanged & source currency is " +
                        "NOT empty. Force load");
                //start fetching
                forceLoad();
            }
        }
        else
        {
            Log.d(LOG_TAG, ">>>>>>> onStartLoading takeContentChanged NOT. Do NOT deliver");
        }

    }

    protected void onReset()
    {
        // Stop watching for file changes
        if (mExchangeRateInput != null)
        {
            mExchangeRateInput.deleteObservers();
            mExchangeRateInput = null;
        }
    }

    @Override
    public Map<String, ExchangeRate> loadInBackground()
    {
        Log.d(LOG_TAG, ">>>>>>> loadInBackground start");

        String queryWeb = createQueryUri();
        Log.d(LOG_TAG, ">>>>>>> web uri " + queryWeb);
        String jsonResp = null;

        try
        {
            jsonResp = makeHttpExchangeRateRequest(queryWeb);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception ex)
        {
            return null;
        }
        if (jsonResp == null)
        {
            return null;
        }

        Map<String, ExchangeRate> rates = null;
        try
        {
            rates = extractJsonResponse(jsonResp);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, ">>>>>>> loadInBackground end");
        return rates;
    }

    private Map<String, ExchangeRate> extractJsonResponse(String jsonResp) throws JSONException
    {
        Map<String, ExchangeRate> exchangeRates = new HashMap<>();
        JSONObject root = new JSONObject(jsonResp);
        String base = root.getString("base");
        String strDate = root.getString("date");
        Date date = FormatHelper.strToDate(strDate, "-");
        JSONObject rates = root.getJSONObject("rates");
        for (String sourceCurrency : mExchangeRateInput.getSourceCurrencies())
        {
            double rateVal = rates.getDouble(sourceCurrency);
            ExchangeRate fc = new ExchangeRate(sourceCurrency, base, rateVal, date);
            exchangeRates.put(sourceCurrency, fc);
        }

        return exchangeRates;
    }

    private String makeHttpExchangeRateRequest(String urlApi) throws IOException
    {
        URL url;
        HttpURLConnection conn = null;
        InputStream is = null;
        try
        {
            url = new URL(urlApi);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(60000 /*1min*/);
            int respCode = conn.getResponseCode();
            if (respCode >= 200 && respCode < 300)
            {
                is = conn.getInputStream();
            }

        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("Illegal url", e);
        } finally
        {

            if (conn != null)
            {
                conn.disconnect();
            }
        }

        String resp = null;
        StringBuilder sb = new StringBuilder();
        if (is != null)
        {
            BufferedInputStream bis = new BufferedInputStream(is);
            InputStreamReader inputStreamReader = new InputStreamReader(bis);
            BufferedReader br = new BufferedReader(inputStreamReader);
            try
            {
                resp = br.readLine();

                while (resp != null)
                {
                    sb.append(resp);
                    resp = br.readLine();
                }
            } finally
            {
                try
                {
                    is.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
        return sb.toString();
    }

    private String createQueryUri()
    {
        Uri.Builder builder = Uri.parse(mExchangeRateInput.getBaseWebApi()).buildUpon()
                .appendQueryParameter("base",
                mExchangeRateInput.getBaseCurrency());
        String symbols = "";
        Iterator<String> iterator = mExchangeRateInput.getSourceCurrencies().iterator();

        while (iterator.hasNext())
        {
            String srcCurrencyCode = iterator.next();
            symbols += srcCurrencyCode;
            if (iterator.hasNext())
            {
                symbols += ",";
            }
        }
        builder.appendQueryParameter("symbols", symbols);
        String queryUri = builder.build().toString();
        Log.d(LOG_TAG, ">>>" + queryUri);
        return queryUri;
    }

}
