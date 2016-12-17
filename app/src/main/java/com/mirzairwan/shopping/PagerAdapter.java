package com.mirzairwan.shopping;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.mirzairwan.shopping.domain.CatalogFragment;

/**
 * Created by Mirza Irwan on 22/11/16.
 */

public class PagerAdapter extends FragmentPagerAdapter
{
    public static final int PAGE_COUNT = 2;
    public static final int BUY_LIST = 0;
    public static final int CATALOG = 1;
    private Context mContext;


    public PagerAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        mContext = context;
    }


    @Override
    public Fragment getItem(int position)
    {
        switch(position) {
            case BUY_LIST:
                  return ShoppingListFragment.newInstance();
            case CATALOG:
                return CatalogFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {

        switch(position) {
            case BUY_LIST:
                return mContext.getString(R.string.buy_list);
            case CATALOG:
                return mContext.getString(R.string.catalogue);
            default:
                return null;
        }
    }

}
