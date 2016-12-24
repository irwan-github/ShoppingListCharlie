package com.mirzairwan.shopping;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Mirza Irwan on 24/12/16.
 */

public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
