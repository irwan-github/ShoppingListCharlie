package com.mirzairwan.shopping;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mirza Irwan on 24/12/16.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private EditTextPreference etPref;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        etPref = (EditTextPreference)findPreference(getString(R.string.user_country_pref));
        setAllCapsInputFilter(etPref.getEditText());

        etPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                boolean result = false;
                String countryCode = (String)newValue;
                try {

                    NumberFormatter.getCurrencyCode(countryCode);
                    result = true;
                }
                catch (Exception ex)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.invalid_country_code);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });
                    builder.show();

                }


                return result;
            }
        });

        updateCountryPreference(PreferenceManager.getDefaultSharedPreferences(getActivity()),
                                                        getString(R.string.user_country_pref));
    }

    private void setAllCapsInputFilter(EditText et)
    {
        InputFilter[] prefFilters;
        prefFilters = et.getFilters();
        ArrayList<InputFilter> filters = new ArrayList<>(Arrays.asList(prefFilters));
        filters.add(new InputFilter.AllCaps());
        prefFilters = new InputFilter[filters.size()];
        filters.toArray(prefFilters);
        et.setFilters(prefFilters);
    }

    private void updateCountryPreference(SharedPreferences sharedPreferences, String key)
    {
        String prefSummary = sharedPreferences.getString(key, null);
        Preference pref = findPreference(key);
        pref.setSummary(prefSummary);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equalsIgnoreCase(getString(R.string.user_country_pref))) {
            updateCountryPreference(sharedPreferences, key);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

}
