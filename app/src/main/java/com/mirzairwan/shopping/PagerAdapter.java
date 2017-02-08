package com.mirzairwan.shopping;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class PagerAdapter extends FragmentPagerAdapter
{
    public static final int PAGE_COUNT = 2;
    public static final int BUY_LIST = 0;
    public static final int CATALOG = 1;
    private Context mContext;
    private String[] mTitles;


    public PagerAdapter(FragmentManager fm, String[] titles)
    {
        super(fm);
        mTitles = titles;
    }


    @Override
    public Fragment getItem(int position)
    {
        switch(position) {
            case BUY_LIST:
                  return ShoppingListFragment.newInstance();
            case CATALOG:
                return ShoppingHistoryFragment.newInstance();
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
                return mTitles[BUY_LIST];
            case CATALOG:
                return mTitles[CATALOG];
            default:
                return null;
        }
    }

}
