package com.mirzairwan.shopping;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Display buy list
 * Created by Mirza Irwan on 19/11/16.
 */

public class ShoppingListFragment extends Fragment {
    public static final String BUY_LIST = "BUY_LIST";
    private static final String LOG_TAG = ShoppingListFragment.class.getSimpleName();

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
        setupListAdapter(lvBuyItems);
        setupFloatingActionButton(rootView);
        setupListItemListener(lvBuyItems);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.empty_view);
        lvBuyItems.setEmptyView(emptyView);

        return rootView;
    }

    private void setupListItemListener(ListView lvBuyItems) {
        lvBuyItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
            }
        });
    }


    private void setupListAdapter(ListView lvBuyItems) {
    }

    private void setupFloatingActionButton(View rootView) {
        FloatingActionButton btnAdd = (FloatingActionButton) rootView.findViewById(R.id.btn_add_item);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }


}
