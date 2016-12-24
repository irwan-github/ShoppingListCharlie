package com.mirzairwan.shopping;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction txn = fm.beginTransaction();
        SettingsFragment settingsFragment = new SettingsFragment();
        txn.add(android.R.id.content, settingsFragment);
        txn.commit();
    }

}
