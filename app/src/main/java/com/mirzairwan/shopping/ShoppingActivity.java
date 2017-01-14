package com.mirzairwan.shopping;

import android.app.LoaderManager;
import android.content.ContentUris;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mirzairwan.shopping.ItemEditingActivity.ITEM_IS_IN_SHOPPING_LIST;
import static com.mirzairwan.shopping.R.id.menu_database_shopping_list;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREIGN_CURRENCY_CODES;
import static com.mirzairwan.shopping.domain.ExchangeRate.FOREX_API_URL;

public class ShoppingActivity extends AppCompatActivity implements
        ShoppingListFragment.OnFragmentInteractionListener,
        CatalogFragment.OnFragmentInteractionListener, OnPictureRequestListener,
        OnExchangeRateRequestListener
{
    private static final String LOG_TAG = ShoppingActivity.class.getSimpleName();
    private static final int LOADER_EXCHANGE_RATES = 1;
    public static final String EXCHANGE_RATE = "EXCHANGE_RATE";
    private static final int PERMISSION_ACCESS_NETWORK_STATE = 44;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageResizer mImageResizer;
    private ExchangeRateLoader mExchangeRateLoader;
    Map<String, ExchangeRate> mExchangeRates;
    ExchangeRateLoaderCallback mExchangeRateLoaderCallback;
    private String mCountryCode;
    private ExchangeRateCallback mExchangeRateCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        PermissionHelper.setupStorageReadPermission(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        PagerAdapter pagerAdapter = new PagerAdapter(getFragmentManager(), this);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager_shopping);
        viewPager.setAdapter(pagerAdapter);

        setUpNavDrawer();

        mImageResizer =
                new ImageResizer(this,
                        getResources().getDimensionPixelSize(R.dimen.image_summary_width),
                        getResources().getDimensionPixelSize(R.dimen.list_item_height)
                );

        setupUserLocale();

        mExchangeRateLoaderCallback = new ExchangeRateLoaderCallback(
                FormatHelper.getCurrencyCode(mCountryCode));
    }

    public void setupUserLocale()
    {
        SharedPreferences sharedPreferences = PreferenceManager.
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

    @Override
    public void onRequest(Set<String> sourceCurrencies,
                          ExchangeRateCallback exchangeRateCallback)
    {
        Bundle args = getBundleForExchangeRateLoader(sourceCurrencies);

        if(!PermissionHelper.isInternetUp(this))
            return;

        if (mExchangeRateLoader == null || mExchangeRateLoader.getSourceCurrencies().containsAll
                (sourceCurrencies))
        {
            mExchangeRateLoader = (ExchangeRateLoader) getLoaderManager().initLoader
                    (LOADER_EXCHANGE_RATES,
                            args, mExchangeRateLoaderCallback);
        }
        else
        {
            mExchangeRateLoader = (ExchangeRateLoader) getLoaderManager().restartLoader
                    (LOADER_EXCHANGE_RATES,
                            args, mExchangeRateLoaderCallback);
        }

        mExchangeRateCallback = exchangeRateCallback;

    }

    @NonNull
    protected Bundle getBundleForExchangeRateLoader(Set<String> foreignCurrencyCode)
    {
        String baseEndPoint = "http://api.fixer.io/latest";
        Bundle args = new Bundle();
        String[] codes = new String[foreignCurrencyCode.size()];

        args.putStringArray(FOREIGN_CURRENCY_CODES, foreignCurrencyCode.toArray(codes));
        args.putString(FOREX_API_URL, baseEndPoint);
        return args;
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

    private class ExchangeRateLoaderCallback implements LoaderManager.LoaderCallbacks<Map<String,
            ExchangeRate>>
    {
        private final String LOG_TAG = ExchangeRateLoaderCallback.class.getSimpleName();
        private final String mBaseCurrencyCode;

        ExchangeRateLoaderCallback(String baseCurrecyCode)
        {
            mBaseCurrencyCode = baseCurrecyCode;
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
            return new ExchangeRateLoader(ShoppingActivity.this,
                    sourceCurrencies,
                    args.getString(FOREX_API_URL));
        }

        @Override
        public void onLoadFinished(Loader<Map<String, ExchangeRate>> loader, Map<String,
                ExchangeRate> exchangeRates)
        {
            Log.d(LOG_TAG, ">>>>onLoadFinished()");
            mExchangeRates = exchangeRates;
            mExchangeRateCallback.doCoversion(exchangeRates);
        }

        @Override
        public void onLoaderReset(Loader<Map<String, ExchangeRate>> loader)
        {
            Log.d(LOG_TAG, ">>>>onLoaderReset()");
            mExchangeRates = null;
        }
    }
}
