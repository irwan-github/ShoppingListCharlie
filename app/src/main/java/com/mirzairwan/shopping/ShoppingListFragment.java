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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;

import static com.mirzairwan.shopping.R.xml.preferences;

/**
 * Display shopping list
 * Created by Mirza Irwan on 19/11/16.
 */

public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ShoppingListAdapter.OnCheckBuyItemListener, SharedPreferences.OnSharedPreferenceChangeListener

{
    public static final String BUY_LIST = "BUY_LIST";
    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();
    private static final int LOADER_BUY_ITEM_ID = 1;
    private OnFragmentInteractionListener onFragmentInteractionListener;
    private ShoppingListAdapter shoppingListAdapter;
    private String countryCode;
    private static final String SORT_COLUMN = "SORT_COLUMN";


    public static ShoppingListFragment newInstance() {

        ShoppingListFragment buyListFragment = new ShoppingListFragment();
        return buyListFragment;
    }


    public ShoppingListFragment() {
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setupUserLocale();
    }

    public void setupUserLocale()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        countryCode = sharedPreferences.getString(getString(R.string.user_country_pref), null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // If activity recreated (such as from screen rotate), restore
        // any necessary data set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {

        }

        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        ListView lvBuyItems = (ListView) rootView.findViewById(R.id.lv_to_buy_items);
        setupFloatingActionButton(rootView);
        setupListView(lvBuyItems);
        setupListItemListener(lvBuyItems);
        setupEmptyView(rootView, lvBuyItems);

        PreferenceManager.setDefaultValues(getActivity(), preferences, false);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Bundle args = new Bundle();
        args.putString(SORT_COLUMN, sharedPrefs.getString(getString(R.string.user_sort_pref), null));


        //Kick off the loader
        getLoaderManager().initLoader(LOADER_BUY_ITEM_ID, args, this);

        return rootView;
    }

    private void setupEmptyView(View rootView, ListView lvBuyItems)
    {
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.empty_view);
        lvBuyItems.setEmptyView(emptyView);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
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
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    private void setupListItemListener(ListView lvBuyItems) {
        lvBuyItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //The id parameter is a buy_item id. However, the requirement is item id
                Cursor cursor = (Cursor)shoppingListAdapter.getItem(position);
                int colItemIdIdx = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_ITEM_ID);
                long itemId = cursor.getLong(colItemIdIdx);
                onFragmentInteractionListener.onViewBuyItem(itemId);
            }
        });
    }


    private void setupListView(ListView lvBuyItems) {
        shoppingListAdapter = new ShoppingListAdapter(getActivity(), null,
                this);
        lvBuyItems.setAdapter(shoppingListAdapter);
    }

    private void setupFloatingActionButton(View view) {
        FloatingActionButton btnAdd = (FloatingActionButton) view.findViewById(R.id.btn_add_item);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.onAdditem();
                showCostOfItemsAdded();
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
                                            PricesEntry.COLUMN_PRICE_TYPE_ID,
                                            PricesEntry.COLUMN_PRICE,
                                            PricesEntry.COLUMN_CURRENCY_CODE};

        Uri uri = Contract.ShoppingList.CONTENT_URI;

        //Summary screen shows only selected price
        String selection = PricesEntry.TABLE_NAME + "." + PricesEntry._ID + "=" + ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID;

        String sortPref = args.getString(SORT_COLUMN);
        String sortOrder = null;
        if (sortPref != null) {
            sortOrder = sortPref.equalsIgnoreCase(ItemsEntry.COLUMN_NAME) ?
                    ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_NAME :
                    ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_BRAND;
        }

        CursorLoader loader = new CursorLoader(getActivity(), uri, projection, selection, null, sortOrder);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        shoppingListAdapter.swapCursor(cursor);
        showCostOfItemsAdded();
        showCostOfItemsChecked();
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
        Cursor cursor = (Cursor)shoppingListAdapter.getItem(mBuyItemPosition);
        int buyItemIdColIdx = cursor.getColumnIndex(ToBuyItemsEntry._ID);
        long buyItemId = cursor.getLong(buyItemIdColIdx);
        String msg = Builder.getDaoManager(getActivity()).update(buyItemId, isChecked);
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equals(getString(R.string.user_country_pref)))
        {
            countryCode = sharedPreferences.getString(key, null);
            showCostOfItemsAdded();
            showCostOfItemsChecked();
            shoppingListAdapter.notifyDataSetChanged();
        }

        if (key.equals(getString(R.string.user_sort_pref))) {
            String prefSort = sharedPreferences.getString(key, null);
            Bundle args = new Bundle();
            args.putString(SORT_COLUMN, prefSort);
            getLoaderManager().restartLoader(LOADER_BUY_ITEM_ID, args, this);
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onAdditem();

        void onViewBuyItem(long rowId);
    }

    public void showCostOfItemsAdded()
    {
        String currencyCode = NumberFormatter.getCurrencyCode(countryCode);
        TextView tvTotalValueAdded = (TextView)getActivity().findViewById(R.id.tv_total_buy);
        Double totalValueOfItemsAdded = 0.00d;
        Cursor cursor = shoppingListAdapter.getCursor();
        cursor.moveToPosition(-1);
        while(cursor.moveToNext())
        {
            int colSelectedPriceTag = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);

            int colCurrencyCode = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
            String lCurrencyCode = cursor.getString(colCurrencyCode);

            int colQtyPurchased = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
            int qtyPurchased = cursor.getInt(colQtyPurchased);

            //Only add item with same currency code as user home currency code
            if(lCurrencyCode.trim().equalsIgnoreCase(currencyCode))
                totalValueOfItemsAdded += ((cursor.getDouble(colSelectedPriceTag)/100) * qtyPurchased);
        }

        tvTotalValueAdded.setText(NumberFormatter.formatCountryCurrency(countryCode, currencyCode, totalValueOfItemsAdded));

    }

    public void showCostOfItemsChecked()
    {
        String currencyCode = NumberFormatter.getCurrencyCode(countryCode);
        TextView tvTotalValueChecked = (TextView)getActivity().findViewById(R.id.tv_total_checked);
        Double totalValueOfItemsChecked = 0.00d;
        Cursor cursor = shoppingListAdapter.getCursor();
        cursor.moveToPosition(-1);
        while(cursor.moveToNext())
        {
            int colSelectedPriceTag = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);
            int colIsItemChecked = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_IS_CHECKED);
            int colCurrencyCode = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
            int colQtyPurchased = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
            int qtyPurchased = cursor.getInt(colQtyPurchased);

            String lCurrencyCode = cursor.getString(colCurrencyCode);
            boolean isItemChecked = cursor.getInt(colIsItemChecked) > 0;

            //Only add item with same currency code as user home currency code
            if(isItemChecked && lCurrencyCode.trim().equalsIgnoreCase(currencyCode))
                totalValueOfItemsChecked += ((cursor.getDouble(colSelectedPriceTag)/100) * qtyPurchased);
        }

        tvTotalValueChecked.setText(NumberFormatter.formatCountryCurrency(countryCode, currencyCode, totalValueOfItemsChecked));
    }




}
