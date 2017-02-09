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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract.Catalogue;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PicturesEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.DaoContentProv;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Price;

import static com.mirzairwan.shopping.R.xml.preferences;


/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class ShoppingHistoryFragment extends Fragment implements OnToggleCatalogItemListener, LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener
{
        private static final String LOG_TAG = ShoppingHistoryFragment.class.getSimpleName();
        private static final int LOADER_CATALOG_ID = 2;
        private static final String SORT_COLUMN = "SORT_COLUMN";
        private CatalogAdapter catalogAdapter;
        private DaoManager daoManager;
        private OnFragmentInteractionListener mOnFragmentInteractionListener;
        private OnPictureRequestListener mOnPictureRequestListener;
        private ShoppingHistoryCursorList mShoppingHistoryList;
        private PurchaseManager mPurchaseManager;

        public static ShoppingHistoryFragment newInstance()
        {
                return new ShoppingHistoryFragment();
        }

        public ShoppingHistoryFragment()
        {
                mPurchaseManager = new PurchaseManager();
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
        }

        @Override
        public void onAttach(Activity activity)
        {
                super.onAttach(activity);
                daoManager = new DaoContentProv(activity);
                mOnFragmentInteractionListener = (OnFragmentInteractionListener) activity;
                mOnPictureRequestListener = (OnPictureRequestListener) activity;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
                View rootView = inflater.inflate(R.layout.fragment_catalog, container, false);
                ListView allItemsListView = (ListView) rootView.findViewById(R.id.lv_all_items);
                setupListView(allItemsListView);
                setEmptyView(rootView, allItemsListView);

                PreferenceManager.setDefaultValues(getActivity(), preferences, false);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                Bundle args = new Bundle();
                args.putString(SORT_COLUMN, sharedPrefs.getString(getString(R.string.user_sort_pref), null));

                //Kick start Loader manager
                getLoaderManager().initLoader(LOADER_CATALOG_ID, args, this);
                return rootView;
        }

        @Override
        public void onDestroy()
        {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
                super.onDestroy();
        }

        private void setEmptyView(View rootView, ListView allItemsListView)
        {
                View emptyView = rootView.findViewById(R.id.empty_view);
                allItemsListView.setEmptyView(emptyView);
        }


        private void setupListView(ListView lvAllItems)
        {
                catalogAdapter = new CatalogAdapter(getActivity(), null, this, mOnPictureRequestListener);
                lvAllItems.setAdapter(catalogAdapter);
                lvAllItems.setOnItemClickListener(this);
        }

        /**
         * Add/remove an item into/from shopping list when user clicks on the toggle button.
         * @param isItemChecked Toggle button is filled.
         * @param position the current cursor position.
         */
        @Override
        public void onToggleItem(boolean isItemChecked, int position)
        {
                String msg;
                if (isItemChecked)
                {
                        long itemId = mShoppingHistoryList.getItemId(position);
                        String itemName = mShoppingHistoryList.getItemName(position);
                        long defaultPriceId = mShoppingHistoryList.getPriceId(position);
                        long rowId = daoManager.insert(itemId, defaultPriceId);
                        msg = rowId > 0? itemName+ " " + getString(R.string.added_to_shopping_list_ok) : itemName+ " " + getString(R.string.added_to_shopping_list_error);
                }
                else
                {
                        long shoppingListItemId = mShoppingHistoryList.getShoppingListItemId(position);
                        String itemName = mShoppingHistoryList.getItemName(position);
                        msg = daoManager.delete(shoppingListItemId) > 0 ? itemName+ " " + getString(R.string.remove_item_from_shopping_list_ok) : itemName + " " + getString(R.string.remove_item_from_shopping_list_error);
                }
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }

        /**
         * Join tables: items, shopping list, prices and pictures. Only unit price type record is selected from prices table.
         * @param loaderId
         * @param args
         * @return
         */
        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
        {
                CursorLoader cursorLoader = null;
                switch (loaderId)
                {
                        case LOADER_CATALOG_ID:
                                Uri uriCatalogue = Catalogue.CONTENT_URI;
                                String[] projection = new String[]{ItemsEntry._ID,
                                                                   ItemsEntry.COLUMN_NAME,
                                                                   ItemsEntry.COLUMN_BRAND,
                                                                   ToBuyItemsEntry.ALIAS_ID,
                                                                   PricesEntry.ALIAS_ID,
                                                                   PricesEntry.COLUMN_CURRENCY_CODE,
                                                                   PicturesEntry.COLUMN_FILE_PATH};

                                String selection = PricesEntry.COLUMN_PRICE_TYPE_ID + "=?";

                                String[] selectionArgs = new String[]{String.valueOf(Price.Type.UNIT_PRICE.getType())};

                                String sortPref = args.getString(SORT_COLUMN);
                                String sortOrder = null;
                                if (sortPref != null)
                                {
                                        sortOrder = sortPref.equalsIgnoreCase(ItemsEntry.COLUMN_NAME) ? ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_NAME : ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_BRAND;
                                }

                                cursorLoader = new CursorLoader(getActivity(), uriCatalogue, projection, selection, selectionArgs, sortOrder);
                                break;
                }


                return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
        {
                Log.d(LOG_TAG, "Calling onLoadFinished");
                catalogAdapter.swapCursor(cursor);
                mShoppingHistoryList = new ShoppingHistoryCursorList(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
                catalogAdapter.swapCursor(null);
                //shoppingList = null;
                mShoppingHistoryList = null;
        }


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
                //Check that item is in shopping list
                boolean isInShoppingList = mShoppingHistoryList.isInShoppingList(position);
                String currencyCode = mShoppingHistoryList.getItemCurrencyCode(position);
                mOnFragmentInteractionListener.onViewItemDetails(id, currencyCode, isInShoppingList);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
                String pref = sharedPreferences.getString(key, null);
                if (key.equals(getString(R.string.user_sort_pref)))
                {
                        Bundle args = new Bundle();
                        args.putString(SORT_COLUMN, pref);
                        getLoaderManager().restartLoader(LOADER_CATALOG_ID, args, this);
                }
        }

        /**
         * Implemented by hosting activity to show an item detail when user clicks on an item in the
         * shopping list
         */
        public interface OnFragmentInteractionListener
        {
                /**
                 * Launches a ItemEditingActivity
                 *
                 * @param itemId           Row id of item in table
                 * @param currencyCode     3-letter currency code
                 * @param isInShoppingList Flag for Editing activity to alert user that item in shopping
                 *                         list cannot be deleted from app.
                 */
                void onViewItemDetails(long itemId, String currencyCode, boolean isInShoppingList);
        }
}
