package com.mirzairwan.shopping;

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
import android.widget.ListView;

import com.mirzairwan.shopping.data.ShoppingListContract;
import com.mirzairwan.shopping.data.ShoppingListContract.ItemsEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.ToBuyItemsEntry;


/**
 * Created by Mirza Irwan on 22/11/16.
 */

public class CatalogFragment extends Fragment implements OnToggleCatalogItemListener,
                                                            LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int CATALOG_ID = 2;
    private CatalogAdapter catalogAdapter;

    public static CatalogFragment newInstance()
    {
        CatalogFragment catalogFragment = new CatalogFragment();
        return catalogFragment;
    }

    public CatalogFragment()
    {
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
    }

    @Override
    public void onToggleItem(boolean isItemChecked, int position)
    {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri uri = Uri.withAppendedPath(ItemsEntry.CONTENT_URI, ShoppingListContract.PATH_BUY_ITEMS);
        String[] projection = new String[]{ItemsEntry._ID, ItemsEntry.COLUMN_NAME,
                                            ItemsEntry.COLUMN_BRAND, ToBuyItemsEntry.ALIAS_ID};
        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, null, null,
                                                        null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        catalogAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        catalogAdapter.swapCursor(null);
    }
}
