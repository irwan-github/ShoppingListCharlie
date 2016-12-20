package com.mirzairwan.shopping;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mirzairwan.shopping.data.AndroidDatabaseManager;
import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.Contract.Catalogue;

public class ShoppingActivity extends AppCompatActivity implements
                        ShoppingListFragment.OnFragmentInteractionListener,
                        CatalogFragment.OnFragmentInteractionListener
{
    public static final String HOME_COUNTRY_CODE = "HOME_COUNTRY_CODE";
    public static final String PERSONAL = "PERSONAL";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        PagerAdapter pagerAdapter = new PagerAdapter(getFragmentManager(), this);
        ViewPager viewPager = (ViewPager)findViewById(R.id.pager_shopping);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_shopping, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        SharedPreferences.Editor editor = getSharedPreferences(PERSONAL, Activity.MODE_PRIVATE).edit();
        editor.putString(HOME_COUNTRY_CODE, "SG");
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        if (menuItem.getItemId() == R.id.menu_database_shopping_list) {
            Intent intentDb = new Intent(this, AndroidDatabaseManager.class);
            startActivity(intentDb);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onAdditem()
    {
        Intent intentToEditItem = new Intent();
        intentToEditItem.setClass(this, BuyingActivity.class);
        startActivity(intentToEditItem);
    }

    @Override
    public void onViewBuyItem(long itemId)
    {
        Intent intentToViewItem = new Intent();
        intentToViewItem.setClass(this, BuyingActivity.class);
        Uri uri = Uri.withAppendedPath(ToBuyItemsEntry.CONTENT_URI, Contract.PATH_ITEMS);
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
    public void onViewItemDetails(long itemId)
    {
        Intent intentToViewItem = new Intent();
        intentToViewItem.setClass(this, ItemEditingActivity.class);
        Uri uri = ContentUris.withAppendedId(Catalogue.ITEMS_PRICES_URI, itemId);
        intentToViewItem.setData(uri);
        startActivity(intentToViewItem);
    }
}
