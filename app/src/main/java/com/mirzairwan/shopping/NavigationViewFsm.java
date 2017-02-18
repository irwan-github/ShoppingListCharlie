package com.mirzairwan.shopping;

import android.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;

import java.util.ArrayList;

/**
 * Created by Mirza Irwan on 18/2/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Acts like a Finite State Machine for NavigationView
 */

public class NavigationViewFsm
{
        private FragmentManager mFragmentManager;
        private NavigationViewState mState = new NavigationViewState();
        private NavigationView mNavigationView;

        public NavigationViewFsm(NavigationView navigationView, FragmentManager fragmentManager)
        {
                mNavigationView = navigationView;
                mFragmentManager = fragmentManager;
                loadViewItem(R.id.nav_shopping_list, mNavigationView.getMenu().findItem(R.id.nav_shopping_list).isCheckable());
                fragmentManager.addOnBackStackChangedListener(mState);
        }

        public void loadViewItem(MenuItem item)
        {
                loadViewItem(item.getItemId(), item.isCheckable());
        }

        private void loadViewItem(int viewItemId, boolean isBackPressable)
        {
                if (isBackPressable)
                {
                        mState.loadViewItem(viewItemId);
                }
                else
                {
                        mNavigationView.setCheckedItem(mState.getViewItemId());
                }
        }

        private class NavigationViewState implements FragmentManager.OnBackStackChangedListener
        {
                private ArrayList<Integer> backStates = new ArrayList<>();

                @Override
                public void onBackStackChanged()
                {
                        int backStackEntryCount = mFragmentManager.getBackStackEntryCount();
                        while(backStates.size() > backStackEntryCount)
                        {
                                backStates.remove(backStates.size() - 1);
                        }

                        if (hasMenuItemItem())
                        {
                                mNavigationView.setCheckedItem(mState.getViewItemId());
                        }
                }

                void loadViewItem(int viewItemId)
                {
                        if (!backStates.contains(viewItemId))
                        {
                                backStates.add(viewItemId);
                        }
                }

                void back()
                {
                        if (backStates.size() > 0)
                        {
                                backStates.remove(backStates.size() - 1);
                        }
                }

                int getViewItemId()
                {
                        return backStates.get(backStates.size() - 1);
                }

                boolean hasMenuItemItem()
                {
                        return backStates.size() > 0;
                }
        }
}
