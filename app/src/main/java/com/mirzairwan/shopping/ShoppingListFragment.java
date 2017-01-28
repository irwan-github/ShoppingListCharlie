package com.mirzairwan.shopping;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.ExchangeRate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.mirzairwan.shopping.Builder.getDaoManager;
import static com.mirzairwan.shopping.R.xml.preferences;

/**
 * Display shopping list screen
 * Created by Mirza Irwan on 19/11/16.
 */

public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ShoppingListAdapter.OnCheckBuyItemListener, SharedPreferences.OnSharedPreferenceChangeListener

{
        private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();
        private static final int LOADER_BUY_ITEM_ID = 1;
        private static final String SORT_COLUMN = "SORT_COLUMN";
        private OnFragmentInteractionListener onFragmentInteractionListener;
        private ShoppingListAdapter shoppingListAdapter;
        private Toolbar mShoppingListToolbar;

        //User's home country code used to determine default/base currency for translation of prices.
        private String mCountryCode;

        //The following are listeners to service this fragment's request.
        private OnPictureRequestListener mOnPictureRequestListener;
        private OnExchangeRateRequestListener mOnExchangeRateListener;

        //Handles conversion and display of translated prices
        private ExchangeRateCallback mExchangeRateCallback;

        private View rootView;
        private ShoppingCursorList mShoppingCursorList;
        private ExchangeRatesReturned mExchangeRatesReturned;
        private Snackbar mSnackBar;
        private HashSet<Long> shareShoppingItemIds = new HashSet<>();

        public static ShoppingListFragment newInstance()
        {
                return new ShoppingListFragment();
        }

        public void setupUserLocale()
        {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                mCountryCode = sharedPreferences.getString(getString(R.string.user_country_pref), null);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                mExchangeRatesReturned = new ExchangeRatesReturned();
        }

        /**
         * The system calls this when it's time for the fragment to draw its user interface for the
         * first time. To draw a UI for your fragment, you must return a View from this method
         * that is
         * the root of your fragment's layout.
         *
         * @param inflater
         * @param container
         * @param savedInstanceState
         * @return
         */
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
                Log.d(LOG_TAG, ">>>>>>> onCreateView");

                rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
                ListView lvBuyItems = (ListView) rootView.findViewById(R.id.lv_to_buy_items);
                //setupFloatingActionButton(rootView);
                setupListView(lvBuyItems);
                setupListItemListener(lvBuyItems);
                setupEmptyView(rootView, lvBuyItems);
                setupShoppingListToolbar((Toolbar) rootView.findViewById(R.id.shopping_list_toolbar));

                PreferenceManager.setDefaultValues(getActivity(), preferences, false);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
                setupUserLocale();
                mExchangeRateCallback = new ExchangeRateCallbackImpl();

                if (savedInstanceState != null)
                {
                        //Nothing was saved before because the data involved might be huge and
                        // saving to bundle
                        //will be a complex task. So let LoaderManager auto-query the database
                        // again,
                }

                return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
                Log.d(LOG_TAG, ">>>>>>> onActivityCreated");

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                Bundle args = new Bundle();
                args.putString(SORT_COLUMN, sharedPrefs.getString(getString(R.string.user_sort_pref), null));

                //Kick off the loader. Note that kicking of the loader in the Resumed state
                // prevents the
                //CursorLoader from calling onLoadFinished twice.
                Log.d(LOG_TAG, " >>>>>>> initLoader(LOADER_BUY_ITEM_ID)");
                getLoaderManager().initLoader(LOADER_BUY_ITEM_ID, args, this);

                PreferenceManager.getDefaultSharedPreferences(getActivity()).
                        registerOnSharedPreferenceChangeListener(this);

                onFragmentInteractionListener.onInitialized(mExchangeRateCallback);
                super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onResume()
        {
                Log.d(LOG_TAG, " >>>>>>> onResume");
                if (!PermissionHelper.isInternetUp(getActivity()))
                {
                        Snackbar.make(rootView, R.string.internet_not_available_exchange_rate, Snackbar.LENGTH_LONG).show();
                }
                shareShoppingItemIds.clear();
                super.onResume();
        }

        private void setupShoppingListToolbar(final Toolbar shoppingListToolbar)
        {
                shoppingListToolbar.inflateMenu(R.menu.shopping_list_toolbar);

                final Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener()
                {
                        @Override
                        public boolean onMenuItemClick(MenuItem item)
                        {
                                int itemId = item.getItemId();
                                switch (itemId)
                                {
                                        case R.id.clear_checked_item:
                                                DaoManager daoMgr = Builder.getDaoManager(ShoppingListFragment.this.getActivity());
                                                daoMgr.deleteCheckedItems();
                                                return true;
                                        case R.id.summary_totals:
                                                mShoppingCursorList.listItemsAdded(mCountryCode);
                                                mShoppingCursorList.listItemsChecked(mCountryCode);
                                                displaySummaryTotals(mExchangeRatesReturned.getExchangeRates());
                                                return true;
                                        default:
                                                return false;
                                }
                        }
                };
                mShoppingListToolbar = shoppingListToolbar;
                shoppingListToolbar.setOnMenuItemClickListener(onMenuItemClickListener);

                final MenuItem menuItem = shoppingListToolbar.getMenu().findItem(R.id.clear_checked_item);
                View menuItemView = menuItem.getActionView();
                menuItemView.setOnClickListener(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                onMenuItemClickListener.onMenuItemClick(menuItem);
                        }
                });

                final MenuItem menuItemTotals = shoppingListToolbar.getMenu().findItem(R.id.summary_totals);
                View menuTotalsView = menuItemTotals.getActionView();
                menuTotalsView.setOnClickListener(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                onMenuItemClickListener.onMenuItemClick(menuItemTotals);
                        }
                });

                Button btnAddItemToShoppingList = (Button) shoppingListToolbar.findViewById(R.id.shopping_list_add_item);
                btnAddItemToShoppingList.setOnClickListener(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                onFragmentInteractionListener.onAdditem();
                        }
                });

        }

        private void setupEmptyView(View rootView, ListView lvBuyItems)
        {
                // Find and set empty view on the ListView, so that it only shows when the list
                // has 0 items.
                View emptyView = rootView.findViewById(R.id.empty_view);
                lvBuyItems.setEmptyView(emptyView);
        }

        @Override
        public void onAttach(Activity activity)
        {
                Log.d(LOG_TAG, ">>>>>>> onAttach");
                super.onAttach(activity);
                onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
                mOnPictureRequestListener = (OnPictureRequestListener) activity;
                mOnExchangeRateListener = (OnExchangeRateRequestListener) activity;
        }

        @Override
        public void onDestroyView()
        {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
                super.onDestroyView();
        }

        private void setupListItemListener(ListView lvBuyItems)
        {
                lvBuyItems.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                                //The id parameter is a buy_item id. However, the requirement is
                                // item id
                                Cursor cursor = (Cursor) shoppingListAdapter.getItem(position);
                                int colItemIdIdx = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_ITEM_ID);
                                int colItemCurrencyCode = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
                                long itemId = cursor.getLong(colItemIdIdx);
                                String currencyCode = cursor.getString(colItemCurrencyCode);
                                onFragmentInteractionListener.onViewBuyItem(itemId, currencyCode);
                        }
                });
        }

        private void setupListView(ListView lvBuyItems)
        {
                //Set up adapter
                shoppingListAdapter = new ShoppingListAdapter(getActivity(), null, this, mOnPictureRequestListener);
                lvBuyItems.setAdapter(shoppingListAdapter);

                //Set up Contextual Action Bar
                lvBuyItems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                lvBuyItems.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
                {
                        @Override
                        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
                        {
                                if (checked)
                                {
                                        shareShoppingItemIds.add(id);
                                }
                                else
                                {
                                        shareShoppingItemIds.remove(id);
                                }
                        }

                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu)
                        {
                                mode.setTitle(getString(R.string.share_shopping_list_txt));
                                mode.getMenuInflater().inflate(R.menu.firebase_share_shopping_list, menu);
                                return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
                        {
                                return false;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
                        {
                                if (shareShoppingItemIds.size() > 0)
                                {
                                        onFragmentInteractionListener.onFirebaseShareShoppingList(shareShoppingItemIds);
                                }
                                else
                                {
                                        Toast.makeText(getActivity(), "Select item(s) before clicking share ", Toast.LENGTH_LONG).show();
                                }
                                mode.finish();
                                return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode)
                        {
                                Log.d(LOG_TAG, ">>> onDestroyActionMode");
                                shareShoppingItemIds.clear();
                        }
                });
        }

        private void setupFloatingActionButton(View view)
        {
//                FloatingActionButton btnAdd = (FloatingActionButton) view.findViewById(R.id.btn_add_item);
//                btnAdd.setOnClickListener(new View.OnClickListener()
//                {
//                        @Override
//                        public void onClick(View v)
//                        {
//                                onFragmentInteractionListener.onAdditem();
//                        }
//                });
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
                Log.d(LOG_TAG, ">>>>>>> onCreateLoader");
                String[] projection = new String[]{ToBuyItemsEntry._ID, ToBuyItemsEntry.COLUMN_ITEM_ID, ToBuyItemsEntry.COLUMN_QUANTITY, ToBuyItemsEntry.COLUMN_IS_CHECKED, ItemsEntry.COLUMN_NAME, ItemsEntry.COLUMN_BRAND, ItemsEntry.COLUMN_COUNTRY_ORIGIN, ItemsEntry.COLUMN_DESCRIPTION, PicturesEntry.COLUMN_FILE_PATH, PricesEntry
                        .COLUMN_PRICE_TYPE_ID, PricesEntry.COLUMN_PRICE, PricesEntry.COLUMN_CURRENCY_CODE};

                Uri uri = Contract.ShoppingList.CONTENT_URI;

                //Summary screen shows only selected price
                String selection = PricesEntry.TABLE_NAME + "." + PricesEntry._ID + "=" +
                        ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID;

                String sortPref = args.getString(SORT_COLUMN);
                String sortOrder = null;
                if (sortPref != null)
                {
                        sortOrder = sortPref.equalsIgnoreCase(ItemsEntry.COLUMN_NAME) ? ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_NAME : ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_BRAND;
                }

                return new CursorLoader(getActivity(), uri, projection, selection, null, sortOrder);
        }

        /**
         * Receive data from database
         * Set and display the shopping list adapter with data
         * Fetch exchange rates for foreign-priced items, if any, from the web and
         * redisplay with translated prices
         * If internet is down, show summary of local-priced items only.
         *
         * @param loader
         * @param cursor
         */
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
        {
                Log.d(LOG_TAG, ">>>>>>> onLoadFinished Cursor");
                shoppingListAdapter.swapCursor(cursor);
                mShoppingCursorList = new ShoppingCursorList(cursor);

                displayTranslatedPricesAndTotals();

                //Show or hide the "Clear" action item
                MenuItem clearItemsInShoppingList = mShoppingListToolbar.getMenu().findItem(R.id.clear_checked_item);
                clearItemsInShoppingList.setEnabled(mShoppingCursorList.atLeastAnItemChecked());
                if (mShoppingCursorList.atLeastAnItemChecked())
                {
                        clearItemsInShoppingList.getActionView().setAlpha(0.4f);
                }
                else
                {
                        clearItemsInShoppingList.getActionView().setAlpha(0.2f);
                }
        }

        /**
         * Fetch exchange rates if any foreign-priced items in shopping list.
         * Add up total cost of items added and checked in the shopping list.
         * Display total cost of items added and checked in the shopping list.
         * If internet is down, show summary of local-priced items only.
         */
        protected void displayTranslatedPricesAndTotals()
        {
                Set<String> sourceCurrencyCodes = mShoppingCursorList.prepareSourceCurrencyCodes(mCountryCode);
                //Fetch exchange rate and total up in the background
                if (!sourceCurrencyCodes.isEmpty())
                {
                        //Call the hosting activity to fetch exchange rates. When hosting
                        // activity has the exchange rates, it will call back this fragment. At that future point,
                        // update shopping list summary and notify CursorAdapter to show translated prices.
                        mOnExchangeRateListener.onRequest(sourceCurrencyCodes);
                }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
                shoppingListAdapter.swapCursor(null);
                mShoppingCursorList = null;
        }

        /**
         * The togglebutton in the shopping list will call this when there is a checked event.
         * This will trigger Cursor loader to force load again and call onLoadFinished.
         *
         * @param isChecked
         * @param mBuyItemPosition
         */
        @Override
        public void onCheckBuyItem(boolean isChecked, int mBuyItemPosition)
        {
                Log.d(LOG_TAG, ">>>>>>> onCheckBuyItem");
                //Save buy item check status
                Cursor cursor = (Cursor) shoppingListAdapter.getItem(mBuyItemPosition);
                int buyItemIdColIdx = cursor.getColumnIndex(ToBuyItemsEntry._ID);
                long buyItemId = cursor.getLong(buyItemIdColIdx);
                getDaoManager(getActivity()).update(buyItemId, isChecked);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
                Log.d(LOG_TAG, ">>>>>>> onSharedPreferenceChanged");
                if (key.equals(getString(R.string.user_country_pref)))
                {
                        Log.d(LOG_TAG, "onSharedPreferenceChanged Change in home country");
                        mCountryCode = sharedPreferences.getString(key, null);
                        //For exchange rate repercussion, ShoopingActivity will handle it.
                }

                if (key.equals(getString(R.string.user_sort_pref)))
                {
                        String prefSort = sharedPreferences.getString(key, null);
                        Bundle args = new Bundle();
                        args.putString(SORT_COLUMN, prefSort);
                        Log.d(LOG_TAG, ">>>>>>> restartLoader(LOADER_BUY_ITEM_ID)");
                        getLoaderManager().restartLoader(LOADER_BUY_ITEM_ID, args, this);
                }
        }

        /**
         * Add up cost of all items added and checked
         *
         * @param exchangeRates When exchange rate is null, only cost of local-priced items are
         *                      added
         */
        private void displaySummaryTotals(Map<String, ExchangeRate> exchangeRates)
        {
                Log.d(LOG_TAG, ">>>>>>> setSummaryTotalsForLocalAndForeignItems");
                String costAdded = mShoppingCursorList.totalCostItemsAdded(exchangeRates, mCountryCode);
                String costChecked = mShoppingCursorList.totalCostItemsChecked(exchangeRates, mCountryCode);

                String info = getString(R.string.added_to_shoppinglist) + costAdded +
                        " | " + getString(R.string.checked_to_shoppinglist) + costChecked;

                mSnackBar = Snackbar.make(rootView, info, Snackbar.LENGTH_INDEFINITE);
                mSnackBar.setAction(R.string.dismiss, new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                mSnackBar.dismiss();
                        }
                });
                mSnackBar.setActionTextColor(Color.LTGRAY);
                mSnackBar.show();
        }

        private void dismissSummaryTotalsView()
        {
                if (mSnackBar != null)
                {
                        mSnackBar.dismiss();
                }
        }

        /**
         * Hosting Activity implements this interface to respond to user click on shopping list
         * to view item details.
         */
        public interface OnFragmentInteractionListener
        {
                void onAdditem();

                void onViewBuyItem(long itemId, String currencyCode);

                void onInitialized(ExchangeRateCallback exchangeRateCallback);

                void onFirebaseShareShoppingList(HashSet<Long> ids);
        }

        /**
         * Called by its container class, ShoppingActivity when ShoppingActivity has received the
         * exchange rates. This class will then update the summary of the shopping list. In
         * addition,
         * it will update the cursor adapter with the exchange rates and refresh it.
         */
        private class ExchangeRateCallbackImpl implements ExchangeRateCallback
        {
                private final String LOG_TAG = ExchangeRateCallbackImpl.class.getSimpleName();

                @Override
                public void doCoversion(Map<String, ExchangeRate> exchangeRates)
                {
                        Log.d(LOG_TAG, ">>>>>>> doCoversion()");

                        //Set summary total costs of domestic and foreign items
                        mShoppingCursorList.listItemsAdded(mCountryCode);
                        mShoppingCursorList.listItemsChecked(mCountryCode);

                        //Not used for caching results. This object is observable
                        mExchangeRatesReturned.setExchangeRates(exchangeRates);

                        //Update the items in the shopping list with exchange rates
                        if (exchangeRates != null)
                        {
                                shoppingListAdapter.setExchangeRates(exchangeRates);
                                shoppingListAdapter.notifyDataSetChanged();
                        }
                }
        }

        private class ExchangeRatesReturned extends java.util.Observable
        {
                private Map<String, ExchangeRate> mExchangeRates;

                public Map<String, ExchangeRate> getExchangeRates()
                {
                        return mExchangeRates;
                }

                public void setExchangeRates(Map<String, ExchangeRate> exchangeRates)
                {
                        mExchangeRates = exchangeRates;
                        setChanged();
                        notifyObservers();
                }
        }

}
