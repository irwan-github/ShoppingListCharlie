package com.mirzairwan.shopping;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by Mirza Irwan on 24/12/16.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateCountryPreference(PreferenceManager.getDefaultSharedPreferences(getActivity()),
                                                        getString(R.string.user_country_pref));
    }

    private void updateCountryPreference(SharedPreferences sharedPreferences, String key)
    {
        String example = getString(R.string.example_home_country_preference);
        String prefSummary = sharedPreferences.getString(key, null);
        Preference pref = findPreference(key);
        pref.setSummary(prefSummary + "\n" + example);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equalsIgnoreCase(getString(R.string.user_country_pref)))
            updateCountryPreference(sharedPreferences, key);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }
}
