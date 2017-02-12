package com.mirzairwan.shopping;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class SettingsActivity extends AppCompatActivity
{
        private Toolbar mToolbar;
        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_settings);

                mToolbar = (Toolbar)findViewById(R.id.toolbar);
                setSupportActionBar(mToolbar);
                setTitle(R.string.settings_screen);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                FragmentManager fm = getFragmentManager();
                FragmentTransaction txn = fm.beginTransaction();
                SettingsFragment settingsFragment = new SettingsFragment();
                txn.replace(R.id.settings_container, settingsFragment);
                txn.commit();
        }

}
