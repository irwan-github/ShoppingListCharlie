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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract.Catalogue;
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
    private static final String LOG_TAG = CatalogFragment.class.getSimpleName();
    private static final int LOADER_CATALOG_ID = 2;
    private static final int LOADER_PRICE_ID = 3;
    private CatalogAdapter catalogAdapter;
    private ShoppingList shoppingList;
    private DaoManager daoManager;
    private OnFragmentInteractionListener mOnFragmentInteractionListener;
    private Cursor mCursor;

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
        mOnFragmentInteractionListener = (OnFragmentInteractionListener) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_catalog, container, false);
        ListView allItemsListView = (ListView) rootView.findViewById(R.id.lv_all_items);
        setupListView(allItemsListView);
        setEmptyView(rootView, allItemsListView);

        //Kick start Loader manager
        getLoaderManager().initLoader(LOADER_CATALOG_ID, null, this);
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
        if (isItemChecked) {

            ToBuyItem buyItem = shoppingList.buyItem(position);
            msg = daoManager.insert(buyItem) > 0 ? getString(R.string.added_to_shopping_list_ok) :
                    getString(R.string.added_to_shopping_list_error);
        } else {
            ToBuyItem buyItem = shoppingList.removeItem(position);
            msg = daoManager.delete(buyItem) > 0 ? getString(R.string.remove_item_from_shopping_list_ok) :
                    getString(R.string.remove_item_from_shopping_list_error);
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        CursorLoader cursorLoader = null;
        switch (loaderId) {
            case LOADER_CATALOG_ID:
                Uri uriCatalogue = Catalogue.CONTENT_URI;
                String[] projection = new String[]{ItemsEntry._ID, ItemsEntry.COLUMN_NAME,
                        ItemsEntry.COLUMN_BRAND, ToBuyItemsEntry.ALIAS_ID, PricesEntry.ALIAS_ID};
                String selection = PricesEntry.COLUMN_PRICE_TYPE_ID + "=?";

                String[] selectionArgs = new String[]{String.valueOf(Price.Type.UNIT_PRICE.getType())};
                cursorLoader = new CursorLoader(getActivity(), uriCatalogue, projection, selection,
                        selectionArgs,
                        null);
                break;
        }


        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        mCursor = cursor;
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
        Log.d(LOG_TAG, ">>> " + view.getClass().getSimpleName());

        //Check that item is in shopping list
        mCursor.moveToPosition(position);
        boolean isInShoppingList = !mCursor.isNull(mCursor.getColumnIndex(ToBuyItemsEntry.ALIAS_ID));
        mOnFragmentInteractionListener.onViewItemDetails(id, isInShoppingList);
    }

    public interface OnFragmentInteractionListener
    {
        void onViewItemDetails(long itemId, boolean isInShoppingList);

    }
}
