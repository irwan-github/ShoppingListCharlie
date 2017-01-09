package com.mirzairwan.shopping;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        getActivity().setTitle(R.string.settings_screen);
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
        InputFilterUtil.setAllCapsInputFilter(etPref.getEditText());

        etPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                boolean result = false;
                String countryCode = (String)newValue;
                try {

                    FormatHelper.getCurrencyCode(countryCode);
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

        updatePrefSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()),
                                                        getString(R.string.user_country_pref));

        updatePrefSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()),
                getString(R.string.user_sort_pref));


    }

    private void updatePrefSummary(SharedPreferences sharedPreferences, String key)
    {
        String prefSummary = sharedPreferences.getString(key, null);
        Preference pref = findPreference(key);
        pref.setSummary(prefSummary);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
            updatePrefSummary(sharedPreferences, key);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

}
