package com.mirzairwan.shopping;

import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mirzairwan.shopping.data.AndroidDatabaseManager;
import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;

import static com.mirzairwan.shopping.ItemEditingActivity.ITEM_IS_IN_SHOPPING_LIST;
import static com.mirzairwan.shopping.R.id.menu_database_shopping_list;

public class ShoppingActivity extends AppCompatActivity implements
        ShoppingListFragment.OnFragmentInteractionListener,
        CatalogFragment.OnFragmentInteractionListener
{
    public static final String HOME_COUNTRY_CODE = "HOME_COUNTRY_CODE";
    public static final String PERSONAL = "PERSONAL";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        PagerAdapter pagerAdapter = new PagerAdapter(getFragmentManager(), this);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager_shopping);
        viewPager.setAdapter(pagerAdapter);

        setUpNavDrawer();
    }

    private void setUpNavDrawer()
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        NavDrawerArrayAdapter navDrawerAdapter = new NavDrawerArrayAdapter
                (this, R.layout.nav_drawer_row, new NavItem[]
                        {new NavItem(R.string.settings_screen, R.drawable.ic_settings)}
                );

        mDrawerList.setAdapter(navDrawerAdapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
                | ActionBar.DISPLAY_SHOW_TITLE);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_shopping, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case menu_database_shopping_list:
                Intent intentDb = new Intent(this, AndroidDatabaseManager.class);
                startActivity(intentDb);
                return true;
            default:
                // Pass the event to ActionBarDrawerToggle, if it returns
                // true, then it has handled the app icon touch event
                if (mDrawerToggle.onOptionsItemSelected(menuItem)) {
                    return true;
                } else
                    return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onAdditem()
    {
        Intent intentToEditItem = new Intent();
        intentToEditItem.setClass(this, AddItemToListActivity.class);
        startActivity(intentToEditItem);
    }

    @Override
    public void onViewBuyItem(long itemId)
    {
        Intent intentToViewItem = new Intent();
        //intentToViewItem.setClass(this, BuyingActivity.class);
        intentToViewItem.setClass(this, AddItemToListActivity.class);
        Uri uri = Contract.ShoppingList.CONTENT_URI;
        uri = ContentUris.withAppendedId(uri, itemId);
        intentToViewItem.setData(uri);
        startActivity(intentToViewItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Do nothing for now
    }

    @Override
    public void onViewItemDetails(long itemId, boolean isInShoppingList)
    {
        Intent intentToViewItem = new Intent();
        intentToViewItem.setClass(this, ItemEditingActivity.class);
        intentToViewItem.putExtra(ITEM_IS_IN_SHOPPING_LIST, isInShoppingList);
        Uri uri = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, itemId);
        intentToViewItem.setData(uri);
        startActivity(intentToViewItem);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            mDrawerLayout.closeDrawer(mDrawerList);
            Intent intentSettings = new Intent(ShoppingActivity.this, SettingsActivity.class);
            startActivity(intentSettings);
        }
    }


}
