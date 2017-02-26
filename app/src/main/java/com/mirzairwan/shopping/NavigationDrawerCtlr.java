package com.mirzairwan.shopping;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.mirzairwan.shopping.firebase.ShareeShoppingListFragment;
import com.mirzairwan.shopping.firebase.SignOutDialogFrag;

import java.util.HashMap;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * Handle the onNavigationItemSelected and onBackPressed events of NavigationView Item behaviour
 */

public class NavigationDrawerCtlr implements NavigationView.OnNavigationItemSelectedListener
{
        private Context mContext;
        private FragmentManager mFragmentManager;
        private NavigationView mNavigationView;
        private DrawerLayout mDrawerLayout;

        /* Map back stack mItemType to checkable navigation view item Id */
        private HashMap<String, Integer> mNavViewIdLookup = new HashMap<>();

        public NavigationDrawerCtlr(Context context, FragmentManager fragmentManager, NavigationView navigationView, DrawerLayout drawerLayout)
        {
                mContext = context;
                mFragmentManager = fragmentManager;
                mNavigationView = navigationView;
                mDrawerLayout = drawerLayout;
                mNavigationView.setNavigationItemSelectedListener(this);
        }

        /**
         * Check item in navigation menu. Called from activity onBackPressed() method.
         */
        public void onBackPressed()
        {
                int backCount = mFragmentManager.getBackStackEntryCount();
                if (backCount > 0)
                {
                        String fragName = mFragmentManager.getBackStackEntryAt(backCount - 1).getName();
                        mNavigationView.setCheckedItem(mNavViewIdLookup.get(fragName));
                }
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
                selectDrawerItem(item);
                return true;
        }

        private void selectDrawerItem(MenuItem menuItem)
        {
                String fragName = null;
                int itemId = menuItem.getItemId();

                switch (itemId)
                {
                        case R.id.nav_shopping_list:
                                fragName = mContext.getString(R.string.shopping_list);
                                if (!pushFragmentForeground(fragName))
                                {
                                        FragmentTransaction shoppingListTxn = mFragmentManager.beginTransaction().replace(R.id.frag_container, ShoppingListFragment.newInstance());
                                        shoppingListTxn.addToBackStack(fragName).commit();
                                }
                                break;
                        case R.id.nav_shared_shopping_list:
                                fragName = mContext.getString(R.string.share_shopping_list_txt);
                                if (!pushFragmentForeground(fragName))
                                {
                                        FragmentTransaction socialShoppingListTxn = mFragmentManager.beginTransaction().replace(R.id.frag_container, new ShareeShoppingListFragment());
                                        socialShoppingListTxn.addToBackStack(fragName).commit();
                                }
                                break;
                        case R.id.nav_cloud_sign_out:
                                SignOutDialogFrag signOutDialogFrag = new SignOutDialogFrag();
                                signOutDialogFrag.show(mFragmentManager, "SIGN_OUT");
                                break;
                        case R.id.nav_settings_screen:
                                Intent settingIntent = new Intent(mContext, SettingsActivity.class);
                                mContext.startActivity(settingIntent);
                                break;
                        case R.id.nav_history:
                                fragName = mContext.getString(R.string.history);
                                if (!pushFragmentForeground(fragName))
                                {
                                        FragmentTransaction historyTxn = mFragmentManager.beginTransaction().replace(R.id.frag_container, ShoppingHistoryFragment.newInstance());
                                        historyTxn.addToBackStack(fragName).commit();
                                }
                                break;
                        default:
                                menuItem.setChecked(false);
                }

                if (fragName != null && menuItem.isCheckable())
                {
                        mNavViewIdLookup.put(fragName, itemId);
                }
                mDrawerLayout.closeDrawers();
        }

        private boolean isFragmentForeground(String name)
        {
                boolean isFragmentForeground = false;
                int backStackEntryCount = mFragmentManager.getBackStackEntryCount();
                if (backStackEntryCount > 0)
                {
                        String fragName = mFragmentManager.getBackStackEntryAt(backStackEntryCount - 1).getName();
                        isFragmentForeground = fragName.equals(name);
                }

                return isFragmentForeground;
        }

        private boolean pushFragmentForeground(String name)
        {
                boolean isShowing = false;

                isShowing = isFragmentForeground(name);

                if (!isShowing)
                {
                        isShowing = mFragmentManager.popBackStackImmediate(name, 0);
                }

                return isShowing;
        }


        public boolean isDrawerOpen(int drawerGravity)
        {
                return mDrawerLayout.isDrawerOpen(drawerGravity);
        }

        public void closeDrawers()
        {
                mDrawerLayout.closeDrawers();
        }
}
