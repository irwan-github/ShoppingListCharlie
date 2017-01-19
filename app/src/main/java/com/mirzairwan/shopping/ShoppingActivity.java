package com.mirzairwan.shopping;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.mirzairwan.shopping.data.AndroidDatabaseManager;
import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.domain.ExchangeRate;
import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.PictureMgr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.mirzairwan.shopping.ItemEditingActivity.ITEM_IS_IN_SHOPPING_LIST;
import static com.mirzairwan.shopping.LoaderHelper.EXCHANGE_RATES_SHOPPING_LIST_LOADER_ID;
import static com.mirzairwan.shopping.R.id.menu_database_shopping_list;
import static com.mirzairwan.shopping.domain.ExchangeRate.DESTINATION_CURRENCY_CODE;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREIGN_CURRENCY_CODES;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREX_API_URL;

public class ShoppingActivity extends AppCompatActivity implements
        ShoppingListFragment.OnFragmentInteractionListener,
        CatalogFragment.OnFragmentInteractionListener, OnPictureRequestListener,
        OnExchangeRateRequestListener, SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Map<String,
                ExchangeRate>>
{
    private static final String LOG_TAG = ShoppingActivity.class.getSimpleName();

    public static final String EXCHANGE_RATE = "EXCHANGE_RATE";
    private static final int PERMISSION_ACCESS_NETWORK_STATE = 44;
    private static final String SOURCE_CURRENCIES = "SOURCE_CURRENCIES";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageResizer mImageResizer;
    Map<String, ExchangeRate> mExchangeRates = new HashMap<>();
    ExchangeRateLoaderCallback mExchangeRateLoaderCallback;
    private String mCountryCode;
    private ExchangeRateCallback mExchangeRateCallback;
    private Set<String> mSourceCurrencies = new HashSet<>();
    private String mBaseEndPoint; //Web API for fetching exchange rates
    private Loader<Map<String, ExchangeRate>> loader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOG_TAG, ">>>>>>> onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        PermissionHelper.setupStorageReadPermission(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(this);

        //The following array order is important
        String[] titles = new String[]{getString(R.string.buy_list), getString(R.string.catalogue)};
        PagerAdapter pagerAdapter = new PagerAdapter(getFragmentManager(), titles);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager_shopping);
        viewPager.setAdapter(pagerAdapter);

        setUpNavDrawer();

        mImageResizer =
                new ImageResizer(this,
                        getResources().getDimensionPixelSize(R.dimen.image_summary_width),
                        getResources().getDimensionPixelSize(R.dimen.list_item_height)
                );

        setupUserLocale();

        SharedPreferences sharedPrefs = getDefaultSharedPreferences(this);
        String webUriKey = getString(R.string.key_forex_web_api_1);
        mBaseEndPoint = sharedPrefs.getString(webUriKey, null);
        mCountryCode = sharedPrefs.getString(getString(R.string.user_country_pref), null);

        // recovering the instance state
        if (savedInstanceState != null)
        {
            Log.d(LOG_TAG, ">>>>>>> savedInstanceState != null");

            //On orientation change,
            //The following will be referred to when shopping list fragment call this activity
            // to fetch exchange rates. see onRequest(). This variable will determine whether
            //loadermanager needs to restart loader
            mExchangeRates = (Map<String, ExchangeRate>) savedInstanceState.getSerializable
                    (EXCHANGE_RATE);
            mSourceCurrencies = (Set<String>) savedInstanceState.getSerializable(SOURCE_CURRENCIES);
        }

        //Initialize the loader but defer fetching exchange rate until shopping list
        // fragment request for exchange rate. To achieve that, send in null mSourceCurrencies
        Bundle args = getBundleForExchangeRateLoader(null);

        Log.d(LOG_TAG, ">>>>>>> initLoader EXCHANGE_RATES");
        getLoaderManager().initLoader
                (EXCHANGE_RATES_SHOPPING_LIST_LOADER_ID,
                        args, this);
    }

    @Override
    protected void onStart()
    {
        Log.d(LOG_TAG, ">>>>>>> onResume");
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        Log.d(LOG_TAG, ">>>>>>> onResume");
        Log.d(LOG_TAG, ">>>>>>> Cached source currencies onResume: " + (mSourceCurrencies == null
                ? "empty" : mSourceCurrencies.toString()));

        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Log.d(LOG_TAG, ">>>>>>> onPause");
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        Log.d(LOG_TAG, ">>>>>>> onStop");
        super.onStop();
    }

    public void setupUserLocale()
    {
        SharedPreferences sharedPreferences =
                getDefaultSharedPreferences(this);
        mCountryCode = sharedPreferences.getString(getString(R.string.user_country_pref), null);
    }

    private void setUpNavDrawer()
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        NavDrawerArrayAdapter navDrawerAdapter = new NavDrawerArrayAdapter
                (this, R.layout.nav_drawer_row, new NavItem[]
                        {new NavItem(R.string.settings_screen, R.drawable.ic_settings)}
                );

        mDrawerList.setAdapter(navDrawerAdapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
                | ActionBar.DISPLAY_SHOW_TITLE);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        Log.d(LOG_TAG, ">>>>>>> onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_shopping, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        int menuItemId = menuItem.getItemId();

        switch (menuItemId)
        {
            case menu_database_shopping_list:
                Intent intentDb = new Intent(this, AndroidDatabaseManager.class);
                startActivity(intentDb);
                return true;
            default:
                // Pass the event to ActionBarDrawerToggle, if it returns
                // true, then it has handled the app icon touch event
                if (mDrawerToggle.onOptionsItemSelected(menuItem))
                {
                    return true;
                }
                else
                {
                    return super.onOptionsItemSelected(menuItem);
                }
        }
    }

    @Override
    public void onAdditem()
    {
        Intent intentToEditItem = new Intent();
        intentToEditItem.setClass(this, ShoppingListEditingActivity.class);
        startActivity(intentToEditItem);
    }

    /**
     * Called when user clicks on a shopping list item
     *
     * @param itemId       Used to send URI to target activity.
     * @param currencyCode
     */
    @Override
    public void onViewBuyItem(long itemId, String currencyCode)
    {
        Intent intentToViewItem = new Intent();
        intentToViewItem.setClass(this, ShoppingListEditingActivity.class);
        Uri uri = Contract.ShoppingList.CONTENT_URI;
        uri = ContentUris.withAppendedId(uri, itemId);
        intentToViewItem.setData(uri);
        if (mExchangeRates != null)
        {
            intentToViewItem.putExtra(EXCHANGE_RATE, mExchangeRates.get(currencyCode));
        }
        startActivity(intentToViewItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Do nothing for now
    }

    /**
     * Called when user clicks on the history list
     *
     * @param itemId
     * @param currencyCode
     * @param isInShoppingList
     */
    @Override
    public void onViewItemDetails(long itemId, String currencyCode, boolean isInShoppingList)
    {
        Intent intentToViewItem = new Intent();
        intentToViewItem.setClass(this, ItemEditingActivity.class);
        intentToViewItem.putExtra(ITEM_IS_IN_SHOPPING_LIST, isInShoppingList);
        Uri uri = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, itemId);
        intentToViewItem.setData(uri);
        if (mExchangeRates != null)
        {
            intentToViewItem.putExtra(EXCHANGE_RATE, mExchangeRates.get(currencyCode));
        }
        startActivity(intentToViewItem);
    }

    @Override
    public void onRequest(Picture picture, ImageView ivItem)
    {
        Log.d(LOG_TAG, ">>>onRequest(Picture picture...");

        //If user does not permit reading of device storage drive, degrade the service gracefully.
        if (PictureMgr.isExternalFile(picture) && !PermissionHelper.hasReadStoragePermission(this))
        {
            mImageResizer.loadImage(null, ivItem);
        }
        else
        {
            mImageResizer.loadImage(picture.getFile(), ivItem);
        }
    }

    /**
     * Save the following:
     * Source currencies
     * Exchange rates
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.d(LOG_TAG, ">>>>>>> onSaveInstanceState");
        outState.putSerializable(EXCHANGE_RATE, (HashMap) mExchangeRates);
        outState.putSerializable(SOURCE_CURRENCIES, (HashSet) mSourceCurrencies);
        super.onSaveInstanceState(outState);
    }

    /**
     * Not working when device orientation changes
     *
     * @param sourceCurrencies
     * @param exchangeRateCallback
     */
    @Override
    public void onRequest(Set<String> sourceCurrencies,
                          ExchangeRateCallback exchangeRateCallback)
    {
        Log.d(LOG_TAG, ">>>>>>> onRequest");

        if (!PermissionHelper.isInternetUp(this))
        {
            return;
        }

        mExchangeRateCallback = exchangeRateCallback; //Called by onLoadFinished

        Bundle args = getBundleForExchangeRateLoader(sourceCurrencies);

        //If there is zero source currencies requested, don't fetch exchange rates
        if (sourceCurrencies != null)
        {
            Log.d(LOG_TAG, ">>>>>>> Requested source currencies: " + sourceCurrencies.toString());
            Log.d(LOG_TAG, ">>>>>>> Cached source currencies: " + (mSourceCurrencies == null ?
                    "empty" : mSourceCurrencies.toString()));

            if (!mSourceCurrencies.containsAll(sourceCurrencies))
            {
                Log.d(LOG_TAG, ">>>>>>>  New source currencies requested means fetch.");
                Log.d(LOG_TAG, ">>>>>>> restartLoader EXCHANGE_RATES");
                getLoaderManager().restartLoader
                        (EXCHANGE_RATES_SHOPPING_LIST_LOADER_ID,
                                args, this);

                //mSourceCurrencies = sourceCurrencies;
                mSourceCurrencies.addAll(sourceCurrencies);
                Log.d(LOG_TAG, ">>>>>>> Cached source currencies after assignment: " +
                        (mSourceCurrencies == null ? "empty" : mSourceCurrencies.toString()));

            }
            else
            {
                Log.d(LOG_TAG, ">>>>>>>  No new source currencies requested means nothing to " +
                        "fetch. doConversion on cached exchange rates");
                mExchangeRateCallback.doCoversion(mExchangeRates);
            }
        }
        else
        {
            Log.d(LOG_TAG, ">>>>>>> No source currencies requested means nothing to fetch or " +
                    "convert");
        }

    }

    @NonNull
    protected Bundle getBundleForExchangeRateLoader(Set<String> foreignCurrencyCode)
    {
        Bundle args = new Bundle();

        if (foreignCurrencyCode != null)
        {
            String[] codes = new String[foreignCurrencyCode.size()];
            args.putStringArray(FOREIGN_CURRENCY_CODES, foreignCurrencyCode.toArray(codes));
        }

        args.putString(DESTINATION_CURRENCY_CODE, FormatHelper.getCurrencyCode(mCountryCode));
        args.putString(FOREX_API_URL, mBaseEndPoint);
        return args;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.d(LOG_TAG, "onSharedPreferenceChanged");
        if (key.equalsIgnoreCase(getString(R.string.key_forex_web_api_1)))
        {

            mBaseEndPoint = sharedPreferences.getString(key, null);
            Bundle args = getBundleForExchangeRateLoader(mSourceCurrencies);

            if (mExchangeRateCallback != null)
            {
                getLoaderManager().restartLoader
                        (EXCHANGE_RATES_SHOPPING_LIST_LOADER_ID,
                                args, mExchangeRateLoaderCallback);
            }
        }

        if (key.equals(getString(R.string.user_country_pref)))
        {
            Log.d(LOG_TAG, "onSharedPreferenceChanged Change in home country");
            mCountryCode = sharedPreferences.getString(key, null);
            getLoaderManager().restartLoader(EXCHANGE_RATES_SHOPPING_LIST_LOADER_ID,
                    getBundleForExchangeRateLoader(mSourceCurrencies), this);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            mDrawerLayout.closeDrawer(mDrawerList);
            Intent intentSettings = new Intent(ShoppingActivity.this, SettingsActivity.class);
            startActivity(intentSettings);
        }
    }

    @Override
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
        return new ExchangeRateLoader(this,
                sourceCurrencies,
                args.getString(FOREX_API_URL), destCurrencyCode);
    }

    /**
     * Callback by ExchangeRateLoader.
     * @param loader
     * @param exchangeRates May be null
     */
    @Override
    public void onLoadFinished(Loader<Map<String, ExchangeRate>> loader, Map<String,
            ExchangeRate> exchangeRates)
    {
        Log.d(LOG_TAG, " >>>>>>> onLoadFinished ExchangeRate");
        if (exchangeRates != null)
        {
            //Log.d(LOG_TAG, " >>>>>>> onLoadFinished ExchangeRates request: " + exchangeRates.toString());
            //Log.d(LOG_TAG, " >>>>>>> onLoadFinished ExchangeRates cache before: " + mExchangeRates.toString());
            mExchangeRates.putAll(exchangeRates); //Cache the exchange rate
            //Log.d(LOG_TAG, " >>>>>>> onLoadFinished ExchangeRates cache after: " + mExchangeRates.toString());

        }

        if (mExchangeRateCallback != null)
        {
            Log.d(LOG_TAG, " >>>>>>> Calling doConversion");
            mExchangeRateCallback.doCoversion(mExchangeRates);
        }
    }

    @Override
    public void onLoaderReset(Loader<Map<String, ExchangeRate>> loader)
    {
        Log.d(LOG_TAG, " >>>>>>> onLoaderReset() ExchangeRate");
    }

    private class ShoppingListExchangeRateLoaderCb extends ExchangeRateLoaderCallback
    {
        private final String LOG_TAG = ShoppingListExchangeRateLoaderCb.class.getSimpleName();

        ShoppingListExchangeRateLoaderCb(Context context)
        {
            super(context);
            Log.d(LOG_TAG, " >>>>>>> ShoppingListExchangeRateLoaderCb()");
        }

        @Override
        public void onLoadFinished(Loader<Map<String, ExchangeRate>> loader, Map<String,
                ExchangeRate> exchangeRates)
        {
            Log.d(LOG_TAG, " >>>>>>> onLoadFinished() ExchangeRate");
            mExchangeRates = exchangeRates;
            mExchangeRateCallback.doCoversion(exchangeRates);
        }

        @Override
        public void onLoaderReset(Loader<Map<String, ExchangeRate>> loader)
        {
            Log.d(LOG_TAG, " >>>>>>> onLoaderReset() ExchangeRate");
            mExchangeRates = null;
        }
    }
}
