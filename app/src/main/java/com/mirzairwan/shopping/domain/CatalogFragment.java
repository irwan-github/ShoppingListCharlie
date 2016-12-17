package com.mirzairwan.shopping.domain;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mirzairwan.shopping.R;

/**
 * Created by Mirza Irwan on 22/11/16.
 */

public class CatalogFragment extends Fragment
{
    private static final String ALL_ITEMS = "1";
    private FragmentInteractionListener fragmentInteractionListener;

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
        setAdapter(allItemsListView);
        View emptyView = rootView.findViewById(R.id.empty_view);
        allItemsListView.setEmptyView(emptyView);
        return rootView;
    }


    public void setAdapter(ListView lvAllItems)
    {

    }

    public interface FragmentInteractionListener
    {
        void onToggleItem(boolean isItemChecked, int catalogPosition, String fragmentTag);
    }
}
