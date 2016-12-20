package com.mirzairwan.shopping;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;

/**
 * Display buy list
 * Created by Mirza Irwan on 19/11/16.
 */

public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ShoppingListAdapter.OnCheckBuyItemListener

{
    public static final String BUY_LIST = "BUY_LIST";
    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();
    private static final int BUY_ITEM = 1;
    private OnFragmentInteractionListener onFragmentInteractionListener;
    private ShoppingListAdapter shoppingListAdapter;

    public static ShoppingListFragment newInstance() {

        ShoppingListFragment buyListFragment = new ShoppingListFragment();

        return buyListFragment;
    }


    public ShoppingListFragment() {
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

        //Kick off the loader
        getLoaderManager().initLoader(BUY_ITEM, null, this);
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

        Uri uri = Contract.ShoppingList.URI;
        String selection = PricesEntry.TABLE_NAME + "." + PricesEntry._ID + "=" + ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID;
        String orderBy = ItemsEntry.COLUMN_NAME;
        CursorLoader loader = new CursorLoader(getActivity(), uri, projection, selection, null, null);
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
        Builder.getDaoManager(getActivity()).update(buyItemId, isChecked);

    }

    public interface OnFragmentInteractionListener
    {
        void onAdditem();

        void onViewBuyItem(long rowId);
    }

    public void showCostOfItemsAdded()
    {
        TextView tvTotalValueAdded = (TextView)getActivity().findViewById(R.id.tv_total_buy);
        Double totalValueOfItemsAdded = 0.00d;
        Cursor cursor = shoppingListAdapter.getCursor();
        while(cursor.moveToNext())
        {
            int colSelectedPriceTag = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);
            totalValueOfItemsAdded += cursor.getDouble(colSelectedPriceTag)/100;
        }

        tvTotalValueAdded.setText(NumberFormatter.formatToTwoDecimalPlaces(totalValueOfItemsAdded));

    }

    public void showCostOfItemsChecked()
    {
        TextView tvTotalValueChecked = (TextView)getActivity().findViewById(R.id.tv_total_checked);
        Double totalValueOfItemsChecked = 0.00d;
        Cursor cursor = shoppingListAdapter.getCursor();
        cursor.moveToPosition(-1);
        while(cursor.moveToNext())
        {
            int colSelectedPriceTag = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);
            int colIsItemChecked = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_IS_CHECKED);
            boolean isItemChecked = cursor.getInt(colIsItemChecked) > 0;
            if(isItemChecked)
                totalValueOfItemsChecked += cursor.getDouble(colSelectedPriceTag)/100;
        }

        tvTotalValueChecked.setText(NumberFormatter.formatToTwoDecimalPlaces(totalValueOfItemsChecked));
    }




}
