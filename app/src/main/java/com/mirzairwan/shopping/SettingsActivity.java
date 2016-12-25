package com.mirzairwan.shopping;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction txn = fm.beginTransaction();
        SettingsFragment settingsFragment = new SettingsFragment();
        txn.replace(android.R.id.content, settingsFragment);
        txn.commit();
    }

}
