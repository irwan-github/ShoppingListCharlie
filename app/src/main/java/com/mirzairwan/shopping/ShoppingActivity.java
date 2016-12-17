package com.mirzairwan.shopping;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mirzairwan.shopping.data.AndroidDatabaseManager;

public class ShoppingActivity extends AppCompatActivity
{

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
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        if (menuItem.getItemId() == R.id.menu_database_shopping_list) {
            Intent intentDb = new Intent(this, AndroidDatabaseManager.class);
            startActivity(intentDb);
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
