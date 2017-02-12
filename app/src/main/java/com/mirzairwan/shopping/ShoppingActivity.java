package com.mirzairwan.shopping;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.mirzairwan.shopping.data.AndroidDatabaseManager;
import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.domain.ExchangeRate;
import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.PictureMgr;
import com.mirzairwan.shopping.firebase.SendShareFragment;
import com.mirzairwan.shopping.firebase.ShareeShoppingListFragment;
import com.mirzairwan.shopping.firebase.SignOutDialogFrag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.mirzairwan.shopping.ItemEditingActivity.ITEM_IS_IN_SHOPPING_LIST;
import static com.mirzairwan.shopping.LoaderHelper.EXCHANGE_RATES_SHOPPING_LIST_LOADER_ID;
import static com.mirzairwan.shopping.R.id.menu_database_shopping_list;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * The main activity
 */

public class ShoppingActivity extends AppCompatActivity implements ShoppingListFragment.OnFragmentInteractionListener,
                                                                   ShoppingHistoryFragment.OnFragmentInteractionListener,
                                                                   OnPictureRequestListener,
                                                                   OnExchangeRateRequestListener,
                                                                   SharedPreferences.OnSharedPreferenceChangeListener,
                                                                   SignOutDialogFrag.OnSignOutListener
{
        public static final String EXCHANGE_RATE = "EXCHANGE_RATE";
        private static final String LOG_TAG = ShoppingActivity.class.getSimpleName();

        //ExchangeRates are cleared, populated and cached by ExchangeRateAwareLoader. Do not persist exchange rate in onSavedInstanceState
        Map<String, ExchangeRate> mExchangeRates = new HashMap<>();

        private DrawerLayout mDrawerLayout;
        private ActionBarDrawerToggle mDrawerToggle;
        private ImageResizer mImageResizer;
        private String mCountryCode;
        private ExchangeRateCallback mExchangeRateCallback;

        //Web API for fetching exchange rates
        private String mBaseEndPoint;

        private ShoppingListExchangeRateLoaderCb mShoppingListExchangeRateLoaderCb;
        private ExchangeRateInput mExchangeRateInput;
        private NavigationView mNavDrawer;
        private Toolbar mToolbar;

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

                FragmentTransaction txn = getFragmentManager().beginTransaction();
                FragmentTransaction fragmentTransaction = txn.add(R.id.frag_container, ShoppingListFragment.newInstance());
                fragmentTransaction.addToBackStack(getString(R.string.shopping_list));
                fragmentTransaction.commit();

                // Set a Toolbar to replace the ActionBar.
                mToolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(mToolbar);

                setUpNavDrawer();

                mImageResizer = new ImageResizer(this, getResources().getDimensionPixelSize(R.dimen.image_summary_width), getResources().getDimensionPixelSize(R.dimen.list_item_height));

                setupUserLocale();

                SharedPreferences sharedPrefs = getDefaultSharedPreferences(this);
                String webUriKey = getString(R.string.key_forex_web_api_1);
                mBaseEndPoint = sharedPrefs.getString(webUriKey, null);
                mCountryCode = sharedPrefs.getString(getString(R.string.user_country_pref), null);

                // recovering the instance state
                if (savedInstanceState != null)
                {
                        Log.d(LOG_TAG, ">>>>>>> savedInstanceState != null");
                }


                //Initialize the exchange rate loader but do NOT let it fetch exchange rate until
                //shopping list fragment has finished fetching shopping list. Upon fetching the shopping list
                //shopping list fragment will request to this activity class to fetch exchange rates.
                mExchangeRateInput = new ExchangeRateInput();

                //Setting the following before initLoader will not notify exchange rate loader
                mExchangeRateInput.setBaseWebApi(mBaseEndPoint);
                mExchangeRateInput.setBaseCurrency(FormatHelper.getCurrencyCode(mCountryCode));
                mShoppingListExchangeRateLoaderCb = new ShoppingListExchangeRateLoaderCb(this, mExchangeRateInput);

                Log.d(LOG_TAG, ">>>>>>> initLoader EXCHANGE_RATES");
                getLoaderManager().initLoader(EXCHANGE_RATES_SHOPPING_LIST_LOADER_ID, null, mShoppingListExchangeRateLoaderCb);
        }

        @Override
        protected void onStart()
        {
                Log.d(LOG_TAG, ">>>>>>> onStart");
                super.onStart();
        }

        @Override
        protected void onResume()
        {
                Log.d(LOG_TAG, ">>>>>>> onResume");
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
                SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
                mCountryCode = sharedPreferences.getString(getString(R.string.user_country_pref), null);
        }

        private void setUpNavDrawer()
        {
                //Find drawer layout
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

                //Find navigation drawer view
                mNavDrawer = (NavigationView) findViewById(R.id.nav_drawer);
                mNavDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
                {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item)
                        {
                                selectDrawerItem(item);
                                return true;
                        }
                });

                mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);

                mDrawerLayout.addDrawerListener(mDrawerToggle);

        }

        private void selectDrawerItem(MenuItem menuItem)
        {
                int itemId = menuItem.getItemId();

                // Highlight the selected item has been done by NavigationView
                menuItem.setChecked(true);

                switch (itemId)
                {
                        case R.id.nav_shopping_list:
                                getFragmentManager().popBackStack(getString(R.string.shopping_list), 0);
                                break;
                        case R.id.nav_shared_shopping_list:
                                FragmentTransaction socialShoppingListTxn = getFragmentManager().beginTransaction().replace(R.id.frag_container, new ShareeShoppingListFragment());
                                socialShoppingListTxn.addToBackStack(getString((R.string.share_shopping_list_txt))).commit();
                                break;
                        case R.id.nav_cloud_sign_out:
                                SignOutDialogFrag signOutDialogFrag = new SignOutDialogFrag();
                                signOutDialogFrag.show(getFragmentManager(), "SIGN_OUT");
                                break;
                        case R.id.nav_settings_screen:
                                Intent settingIntent = new Intent(this, SettingsActivity.class);
                                startActivity(settingIntent);
                                break;
                        case R.id.nav_history:
                                FragmentTransaction historyTxn = getFragmentManager().beginTransaction().replace(R.id.frag_container, ShoppingHistoryFragment.newInstance());
                                historyTxn.addToBackStack(getString(R.string.history)).commit();
                                break;
                        default:
                                menuItem.setChecked(false);
                }

                mDrawerLayout.closeDrawers();
        }

        @Override
        public void onBackPressed()
        {
                FragmentManager fragmentManager = getFragmentManager();
                int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                if (backStackEntryCount > 0)
                {
                        int index = backStackEntryCount - 1;
                        fragmentManager.popBackStack();
                        --index;
                        FragmentManager.BackStackEntry backStackEntryAt = fragmentManager.getBackStackEntryAt(index);
                        String name = backStackEntryAt.getName();
                        Log.d(LOG_TAG, ">>> name of backstactentry " + name);
                        setTitle(name);
                }
                else
                {
                        super.onBackPressed();
                }
        }

        @Override
        public void onSignOut()
        {
                getFragmentManager().popBackStack(getString(R.string.shopping_list), 0);
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
                        case android.R.id.home:
                                mDrawerLayout.openDrawer(GravityCompat.START);
                                return true;
                        default:
                                return super.onOptionsItemSelected(menuItem);
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
         * @param itemId       Part of URI to target activity.
         * @param currencyCode Belonging to price of item
         */
        @Override
        public void onViewBuyItem(long itemId, String currencyCode)
        {
                Intent intentToViewItem = new Intent();
                intentToViewItem.setClass(this, ShoppingListEditingActivity.class);
                Uri uri = Contract.ShoppingList.CONTENT_URI;
                uri = ContentUris.withAppendedId(uri, itemId);
                intentToViewItem.setData(uri);
                ExchangeRate exchangeRate = null;
                if (mExchangeRates != null)
                {
                        String homeCurrencyCode = FormatHelper.getCurrencyCode(mCountryCode);
                        if (!TextUtils.isEmpty(currencyCode) && !currencyCode.equalsIgnoreCase(homeCurrencyCode))
                        {
                                exchangeRate = mExchangeRates.get(currencyCode);
                        }
                }
                intentToViewItem.putExtra(EXCHANGE_RATE, exchangeRate);
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
         *
         * @param outState
         */
        @Override
        protected void onSaveInstanceState(Bundle outState)
        {
                Log.d(LOG_TAG, ">>>>>>> onSaveInstanceState");
                super.onSaveInstanceState(outState);
        }

        /**
         * Not working when device orientation changes
         *
         * @param sourceCurrencies
         */
        @Override
        public void onRequest(Set<String> sourceCurrencies)
        {
                Log.d(LOG_TAG, ">>>>>>> onRequest");

                boolean isInputChanged = mExchangeRateInput.setSourceCurrencies(sourceCurrencies);

                //Loader will not fetch exchange rates if there is NO change in sourceCurrencies of
                // mExchangeRateInput. However, to get it's cached exchange rate, we need to initialize
                // ExchangeRateLoader again
                if (!isInputChanged)
                {
                        Log.d(LOG_TAG, ">>>>>>> initLoader EXCHANGE_RATES");
                        getLoaderManager().initLoader(EXCHANGE_RATES_SHOPPING_LIST_LOADER_ID, null, mShoppingListExchangeRateLoaderCb);
                }

        }

        @Override
        public void onInitialized(ExchangeRateCallback exchangeRateCallback)
        {
                mExchangeRateCallback = exchangeRateCallback;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
                Log.d(LOG_TAG, ">>>>>>> onSharedPreferenceChanged");
                if (key.equalsIgnoreCase(getString(R.string.key_forex_web_api_1)))
                {
                        //Update the base uri of Web API
                        mBaseEndPoint = sharedPreferences.getString(key, null);
                        mExchangeRateInput.setBaseWebApi(mBaseEndPoint);
                }

                if (key.equals(getString(R.string.user_country_pref)))
                {
                        Log.d(LOG_TAG, ">>>>>>> onSharedPreferenceChanged Change in home country");

                        //Add old user's preference country code to source currencies before assigning the new country code because translation may be needed
                        mExchangeRateInput.addSourceCurrency(FormatHelper.getCurrencyCode(mCountryCode));

                        //Assign new country code
                        mCountryCode = sharedPreferences.getString(key, null);

                        mExchangeRateInput.setBaseCurrency(FormatHelper.getCurrencyCode(mCountryCode));
                }
        }

        @Override
        public void onFirebaseShareShoppingList(HashSet<Long> ids, String shareeEmail)
        {
                Long[] idd = new Long[ids.size()];
                idd = ids.toArray(idd);
                SendShareFragment sendItemsFragment = SendShareFragment.getInstance(idd, shareeEmail);
                FragmentTransaction sendTxn = getFragmentManager().beginTransaction();
                sendTxn.add(sendItemsFragment, "SEND ITEMS").addToBackStack("SEND ITEM STACK").commit();
        }

        private class ShoppingListExchangeRateLoaderCb implements LoaderManager.LoaderCallbacks<Map<String, ExchangeRate>>
        {
                private final String LOG_TAG = ShoppingListExchangeRateLoaderCb.class.getSimpleName();
                private ExchangeRateInput mExchangeRateInput;
                private Context mContext;

                ShoppingListExchangeRateLoaderCb(Context context, ExchangeRateInput exchangeRateInput)
                {
                        //Log.d(LOG_TAG, "Construct");
                        mContext = context;
                        mExchangeRateInput = exchangeRateInput;
                }

                @Override
                public Loader<Map<String, ExchangeRate>> onCreateLoader(int id, Bundle args)
                {
                        Log.d(LOG_TAG, " >>>>>>> onCreateLoader() ExchangeRate");

                        return new ExchangeRateAwareLoader(mContext, mExchangeRateInput);
                }

                @Override
                public void onLoadFinished(Loader<Map<String, ExchangeRate>> loader, Map<String, ExchangeRate> exchangeRates)
                {
                        Log.d(LOG_TAG, " >>>>>>> onLoadFinished ExchangeRate: " + exchangeRates);

                        mExchangeRates = exchangeRates;

                        //Do exchange rate conversion

                        Log.d(LOG_TAG, " >>>>>>> Calling doConversion");
                        if (mExchangeRateCallback != null)
                        {
                                mExchangeRateCallback.doCoversion(exchangeRates);
                        }
                }

                @Override
                public void onLoaderReset(Loader<Map<String, ExchangeRate>> loader)
                {
                        Log.d(LOG_TAG, " >>>>>>> onLoaderReset() ExchangeRate");
                        mExchangeRates = null;
                }
        }
}
