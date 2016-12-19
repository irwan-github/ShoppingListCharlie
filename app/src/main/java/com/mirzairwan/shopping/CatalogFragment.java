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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.DaoContentProv;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ShoppingList;
import com.mirzairwan.shopping.domain.ToBuyItem;


/**
 * Created by Mirza Irwan on 22/11/16.
 */

public class CatalogFragment extends Fragment implements OnToggleCatalogItemListener,
                                                            LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener
{
    private static final int CATALOG_ID = 2;
    private CatalogAdapter catalogAdapter;
    private ShoppingList shoppingList;
    private DaoManager daoManager;
    private OnFragmentInteractionListener mOnFragmentInteractionListener;

    public static CatalogFragment newInstance()
    {
        CatalogFragment catalogFragment = new CatalogFragment();
        return catalogFragment;
    }

    public CatalogFragment()
    {

    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        daoManager = new DaoContentProv(activity);
        mOnFragmentInteractionListener = (OnFragmentInteractionListener)activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_catalog, container, false);
        ListView allItemsListView = (ListView)rootView.findViewById(R.id.lv_all_items);
        setupListView(allItemsListView);
        setEmptyView(rootView, allItemsListView);

        //Kick start Loader manager
        getLoaderManager().initLoader(CATALOG_ID, null, this);
        return rootView;
    }

    private void setEmptyView(View rootView, ListView allItemsListView)
    {
        View emptyView = rootView.findViewById(R.id.empty_view);
        allItemsListView.setEmptyView(emptyView);
    }


    public void setupListView(ListView lvAllItems)
    {
        catalogAdapter = new CatalogAdapter(getActivity(), null, this);
        lvAllItems.setAdapter(catalogAdapter);

        lvAllItems.setOnItemClickListener(this);
    }

    @Override
    public void onToggleItem(boolean isItemChecked, int position)
    {
        String msg;
        if(isItemChecked) {
            ToBuyItem buyItem = shoppingList.buyItem(position);
            msg = daoManager.insert(buyItem);
        }
        else
        {
            ToBuyItem buyItem = shoppingList.removeItem(position);
            msg = daoManager.delete(buyItem) == 1? "Removed from shopping list" :
                                                                            "Failed to remove item";
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri uri = Contract.Catalogue.URI;
        String[] projection = new String[]{ItemsEntry._ID, ItemsEntry.COLUMN_NAME,
                                            ItemsEntry.COLUMN_BRAND, ToBuyItemsEntry.ALIAS_ID,
                                            PricesEntry.ALIAS_ID};

        String selection = Contract.PricesEntry.COLUMN_PRICE_TYPE_ID + "=?";

        //Display unit price as a default in catalogue.
        String[] selectionArgs = new String[]{String.valueOf(Price.Type.UNIT_PRICE.getType())};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs,
                                                        null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        catalogAdapter.swapCursor(cursor);
        shoppingList = Builder.getShoppingList(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        catalogAdapter.swapCursor(null);
        shoppingList = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        mOnFragmentInteractionListener.onViewItemDetails(id);
    }

    public interface OnFragmentInteractionListener
    {
        void onViewItemDetails(long itemId);
    }
}
