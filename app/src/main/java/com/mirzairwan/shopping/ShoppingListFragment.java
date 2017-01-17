package com.mirzairwan.shopping;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.ExchangeRate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mirzairwan.shopping.Builder.getDaoManager;
import static com.mirzairwan.shopping.FormatHelper.formatCountryCurrency;
import static com.mirzairwan.shopping.FormatHelper.getCurrencyCode;
import static com.mirzairwan.shopping.R.xml.preferences;

/**
 * Display shopping list screen
 * Created by Mirza Irwan on 19/11/16.
 */

public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ShoppingListAdapter.OnCheckBuyItemListener, SharedPreferences
                .OnSharedPreferenceChangeListener

{
    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();
    private static final int LOADER_BUY_ITEM_ID = 1;
    private static final int LOADER_EXCHANGE_RATES = 2;
    private OnFragmentInteractionListener onFragmentInteractionListener;
    private ShoppingListAdapter shoppingListAdapter;
    private String mCountryCode;
    private static final String SORT_COLUMN = "SORT_COLUMN";
    private Toolbar mShoppingListToolbar;
    private OnPictureRequestListener mOnPictureRequestListener;
    //private ExchangeRateLoaderCallback mExchangeRateLoaderCallback;
    private List<ItemCost> mForeignItems = new ArrayList<>();
    private List<ItemCost> mForeignItemsChecked = new ArrayList<>();
    private TextView mTvTotalValueAdded;
    private TextView mTvTotalValueChecked;
    private Double mTotalValueOfItemsAdded = 0.00d;
    private Double mTotalValueOfItemsChecked = 0.00d;
    private Set<String> mForeignCurrencyCodes = new HashSet<>();
    private View mLoadingIndicator;
    private View mShoppingListTotalsView;
    private ExchangeRateLoader mExchangeRateLoader;
    private OnExchangeRateRequestListener mOnExchangeRateListener;
    private ExchangeRateCallback mExchangeRateCallback;

    public static ShoppingListFragment newInstance()
    {
        ShoppingListFragment buyListFragment = new ShoppingListFragment();
        return buyListFragment;
    }

    public void setupUserLocale()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (getActivity());
        mCountryCode = sharedPreferences.getString(getString(R.string.user_country_pref), null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {

        if (savedInstanceState != null)
        {
            //Nothing was saved before because the data involved is huge and saving will be a
            // complex task. So screen is going to query the database,
        }

        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        mTvTotalValueAdded = (TextView) rootView.findViewById(R.id.tv_total_buy);
        mLoadingIndicator = rootView.findViewById(R.id.loading_indicator);
        mShoppingListTotalsView = rootView.findViewById(R.id.shopping_list_totals);
        mShoppingListTotalsView.setVisibility(View.INVISIBLE);
        ListView lvBuyItems = (ListView) rootView.findViewById(R.id.lv_to_buy_items);
        setupFloatingActionButton(rootView);
        setupListView(lvBuyItems);
        setupListItemListener(lvBuyItems);
        setupEmptyView(rootView, lvBuyItems);
        setupShoppingListToolbar((Toolbar) rootView.findViewById(R.id.shopping_list_toolbar));

        PreferenceManager.setDefaultValues(getActivity(), preferences, false);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity
                ());
        Bundle args = new Bundle();
        args.putString(SORT_COLUMN, sharedPrefs.getString(getString(R.string.user_sort_pref),
                null));

        //Kick off the loader
        getLoaderManager().initLoader(LOADER_BUY_ITEM_ID, args, this);
        setupUserLocale();
//        mExchangeRateLoaderCallback = new ExchangeRateLoaderCallback(getCurrencyCode
//                (countryCode));
        mExchangeRateCallback = new ExchangeRateCallbackImpl();

        return rootView;
    }

    private void setupShoppingListToolbar(final Toolbar shoppingListToolbar)
    {
        shoppingListToolbar.inflateMenu(R.menu.shopping_list_toolbar);

        final Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar
                .OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                int itemId = item.getItemId();
                switch (itemId)
                {
                    case R.id.clear_checked_item:
                        DaoManager daoMgr = Builder.getDaoManager(ShoppingListFragment.this
                                .getActivity());
                        daoMgr.deleteCheckedItems();
                        return true;
                    default:
                        return false;
                }
            }
        };

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

        mShoppingListToolbar = shoppingListToolbar;
    }

    private void setupEmptyView(View rootView, ListView lvBuyItems)
    {
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.empty_view);
        lvBuyItems.setEmptyView(emptyView);
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.d(LOG_TAG, "onAttach");
        super.onAttach(activity);
        onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
        mOnPictureRequestListener = (OnPictureRequestListener) activity;
        mOnExchangeRateListener = (OnExchangeRateRequestListener)activity;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).
                registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView()
    {
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    private void setupListItemListener(ListView lvBuyItems)
    {
        lvBuyItems.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //The id parameter is a buy_item id. However, the requirement is item id
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
        shoppingListAdapter = new ShoppingListAdapter(getActivity(), null,
                this, mOnPictureRequestListener);

        lvBuyItems.setAdapter(shoppingListAdapter);
    }

    private void setupFloatingActionButton(View view)
    {
        FloatingActionButton btnAdd = (FloatingActionButton) view.findViewById(R.id.btn_add_item);
        btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onFragmentInteractionListener.onAdditem();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String[] projection = new String[]{ToBuyItemsEntry._ID,
                ToBuyItemsEntry.COLUMN_ITEM_ID,
                ToBuyItemsEntry.COLUMN_QUANTITY,
                ToBuyItemsEntry.COLUMN_IS_CHECKED,
                ItemsEntry.COLUMN_NAME,
                ItemsEntry.COLUMN_BRAND,
                ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                ItemsEntry.COLUMN_DESCRIPTION,
                PicturesEntry.COLUMN_FILE_PATH,
                PricesEntry.COLUMN_PRICE_TYPE_ID,
                PricesEntry.COLUMN_PRICE,
                PricesEntry.COLUMN_CURRENCY_CODE};

        Uri uri = Contract.ShoppingList.CONTENT_URI;

        //Summary screen shows only selected price
        String selection = PricesEntry.TABLE_NAME + "." + PricesEntry._ID + "=" + ToBuyItemsEntry
                .TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID;

        String sortPref = args.getString(SORT_COLUMN);
        String sortOrder = null;
        if (sortPref != null)
        {
            sortOrder = sortPref.equalsIgnoreCase(ItemsEntry.COLUMN_NAME) ?
                    ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_NAME :
                    ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_BRAND;
        }

        return new CursorLoader(getActivity(), uri, projection, selection, null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        Log.d(LOG_TAG, ">>>>onLoadFinished");
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mShoppingListTotalsView.setVisibility(View.INVISIBLE);
        shoppingListAdapter.swapCursor(cursor);

        prepareSummaryOfItemsAdded();
        prepareSummaryOfItemsChecked();

        if(PermissionHelper.isInternetUp(getActivity()) && !mForeignCurrencyCodes.isEmpty())
            mOnExchangeRateListener.onRequest(mForeignCurrencyCodes, mExchangeRateCallback);
        else
        {
            setSummaryTotalsForLocalItems();
        }
    }

    protected void setSummaryTotalsForLocalItems()
    {
        String currencyCode = FormatHelper.getCurrencyCode(mCountryCode);

        String totalCostofItemsAdded = formatCountryCurrency(mCountryCode,
                currencyCode, mTotalValueOfItemsAdded);

        String totalCostofItemsChecked = FormatHelper.formatCountryCurrency(mCountryCode,
                currencyCode, mTotalValueOfItemsChecked);

        setSummaryTotals(totalCostofItemsAdded, totalCostofItemsChecked);
    }

    /**
     * Set the summary views showing the total cost added and total cost checked.
     * Next, it removes the progress indicator.
     * @param totalCostAdded Cost of items added to shopping list
     * @param totalCostChecked Cost of items added to shopping list and checked
     */
    protected void setSummaryTotals(String totalCostAdded, String totalCostChecked)
    {
        mTvTotalValueAdded.setText(totalCostAdded);
        mTvTotalValueChecked.setText(totalCostChecked);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mShoppingListTotalsView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        shoppingListAdapter.swapCursor(null);
    }

    @Override
    public void onCheckBuyItem(boolean isChecked, int mBuyItemPosition)
    {
        //Save buy item check status
        Cursor cursor = (Cursor) shoppingListAdapter.getItem(mBuyItemPosition);
        int buyItemIdColIdx = cursor.getColumnIndex(ToBuyItemsEntry._ID);
        long buyItemId = cursor.getLong(buyItemIdColIdx);
        getDaoManager(getActivity()).update(buyItemId, isChecked);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(getString(R.string.user_country_pref)))
        {
            mCountryCode = sharedPreferences.getString(key, null);
            prepareSummaryOfItemsAdded();
            prepareSummaryOfItemsChecked();
            shoppingListAdapter.notifyDataSetChanged();
        }

        if (key.equals(getString(R.string.user_sort_pref)))
        {
            String prefSort = sharedPreferences.getString(key, null);
            Bundle args = new Bundle();
            args.putString(SORT_COLUMN, prefSort);
            getLoaderManager().restartLoader(LOADER_BUY_ITEM_ID, args, this);
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onAdditem();

        void onViewBuyItem(long itemId, String currencyCode);
    }

    /**
     * Add up total cost of local items added to shopping list
     * Populate the foreign currency codes list. This list will be used to fetch exchange rates
     * in the web.
     * List the foreign items found in the shopping list.
     */
    public void prepareSummaryOfItemsAdded()
    {
        mForeignCurrencyCodes.clear();
        mForeignItems.clear();
        mTotalValueOfItemsAdded = 0.00d;
        String baseCurrencyCode = getCurrencyCode(mCountryCode);


        Cursor cursor = shoppingListAdapter.getCursor();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext())
        {
            int colSelectedPriceTag = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);

            int colCurrencyCode = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
            String lCurrencyCode = cursor.getString(colCurrencyCode);

            int colQtyPurchased = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
            int qtyPurchased = cursor.getInt(colQtyPurchased);

            double cost = cursor.getDouble(colSelectedPriceTag);
            //Only add item with same currency code as user home currency code
            if (lCurrencyCode.trim().equalsIgnoreCase(baseCurrencyCode))
            {
                mTotalValueOfItemsAdded += ((cost / 100) *
                        qtyPurchased);
            }
            else
            {
                mForeignCurrencyCodes.add(lCurrencyCode);
                ItemCost val = new ItemCost(cost / 100, lCurrencyCode);
                mForeignItems.add(val);
            }
        }
    }

    /**
     * Add up total cost of local items checked in shopping list
     * List the foreign items checked in the shopping list.
     */
    public void prepareSummaryOfItemsChecked()
    {
        mForeignItemsChecked.clear();
        mTotalValueOfItemsChecked = 0.00d;
        byte atLeastAnItemChecked = (byte) 0;
        String currencyCode = getCurrencyCode(mCountryCode);
        mTvTotalValueChecked = (TextView) getActivity().findViewById(R.id.tv_total_checked);

        Cursor cursor = shoppingListAdapter.getCursor();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext())
        {
            int colSelectedPriceTag = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);
            int colIsItemChecked = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_IS_CHECKED);
            int colCurrencyCode = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
            int colQtyPurchased = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
            int qtyPurchased = cursor.getInt(colQtyPurchased);

            String lCurrencyCode = cursor.getString(colCurrencyCode);
            boolean isItemChecked = cursor.getInt(colIsItemChecked) > 0;
            atLeastAnItemChecked |= (byte) cursor.getInt(colIsItemChecked);

            //Only add item with same currency code as user home currency code
            double cost = cursor.getDouble(colSelectedPriceTag);
            if (isItemChecked && lCurrencyCode.trim().equalsIgnoreCase(currencyCode))
            {
                mTotalValueOfItemsChecked += ((cost / 100) *
                        qtyPurchased);
            }
            else if (isItemChecked && !lCurrencyCode.trim().equalsIgnoreCase(currencyCode))
            {
                ItemCost val = new ItemCost(cost / 100, lCurrencyCode);
                mForeignItemsChecked.add(val);
            }
        }
        //Show or hide the "Clear" action item
        mShoppingListToolbar.getMenu().findItem(R.id.clear_checked_item).
                setVisible(atLeastAnItemChecked == 1);

    }


    protected String totalCostItemsAdded(Map<String, ExchangeRate> exchangeRates)
    {
        double totalForexCost = 0;

        //Apply the rate and add the foreign currency
        for (ItemCost costOfItem : mForeignItems)
        {
            ExchangeRate fc = exchangeRates.get(costOfItem.getSourceCurrencyCode());
            totalForexCost += fc.compute(costOfItem.getAmt());
        }

        String currencyCode = FormatHelper.getCurrencyCode(mCountryCode);
        String totalCostofItemsAdded = formatCountryCurrency(mCountryCode,
                currencyCode, mTotalValueOfItemsAdded + totalForexCost);
        return totalCostofItemsAdded;
    }

    protected String totalItemsChecked(Map<String, ExchangeRate> exchangeRates)
    {
        double totalCostForexItemChecked = 0;
        for (ItemCost foreignItemChecked : mForeignItemsChecked)
        {
            ExchangeRate fc = exchangeRates.get(foreignItemChecked.getSourceCurrencyCode());
            totalCostForexItemChecked += fc.compute(foreignItemChecked.getAmt());
        }

        String destCurrencyCode = FormatHelper.getCurrencyCode(mCountryCode);

        return formatCountryCurrency(mCountryCode,
                destCurrencyCode,
                mTotalValueOfItemsChecked + totalCostForexItemChecked);
    }


    private class ExchangeRateCallbackImpl implements ExchangeRateCallback
    {
        private final String LOG_TAG = ExchangeRateCallbackImpl.class.getSimpleName();
        @Override
        public void doCoversion(Map<String, ExchangeRate> exchangeRates)
        {
            Log.d(LOG_TAG, ">>>>onLoadFinished()");

            //if exchange rate is null, only add for local-priced items
            if(exchangeRates == null)
                setSummaryTotalsForLocalItems();
            else
            {
                //set summary total costs of domestic and foreign items
                String totalCostAdded = totalCostItemsAdded(exchangeRates);
                String totalCostChecked = totalItemsChecked(exchangeRates);
                setSummaryTotals(totalCostAdded, totalCostChecked);
            }
        }
    }

    private class ItemCost
    {
        double mAmt;
        String mSourceCurrencyCode; //Foreign currency

        public ItemCost(double amt, String sourceCurrencyCode)
        {
            mAmt = amt;
            mSourceCurrencyCode = sourceCurrencyCode;
        }

        public double getAmt()
        {
            return mAmt;
        }

        public String getSourceCurrencyCode()
        {
            return mSourceCurrencyCode;
        }
    }

}
