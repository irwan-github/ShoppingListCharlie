package com.mirzairwan.shopping;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{
        private Preference countryCodePref;
        private Preference sortPref;
        private Preference mWebApiExchRatesEcb1Pref;
        private Preference mEmailPref;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                getActivity().setTitle(R.string.settings_screen);
                addPreferencesFromResource(R.xml.preferences);

                countryCodePref = findPreference(getString(R.string.user_country_pref));
                MyTextUtils.setAllCapsInputFilter(((EditTextPreference) countryCodePref).getEditText());
                countryCodePref.setOnPreferenceChangeListener(this);

                sortPref = findPreference(getString(R.string.key_user_sort_pref));
                sortPref.setOnPreferenceChangeListener(this);

                mWebApiExchRatesEcb1Pref = findPreference(getString(R.string.key_forex_web_api_1));
                mWebApiExchRatesEcb1Pref.setOnPreferenceChangeListener(this);

                mEmailPref = findPreference(getString(R.string.key_cloud_email));
                mEmailPref.setOnPreferenceChangeListener(this);
        }


        @Override
        public void onResume()
        {
                super.onResume();

                updatePrefSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()), getString(R.string.user_country_pref));

                updatePrefSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()), getString(R.string.user_sort_pref));

                updatePrefSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()), getString(R.string.key_forex_web_api_1));

                updatePrefSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()), getString(R.string.key_cloud_email));

        }

        private void updatePrefSummary(SharedPreferences sharedPreferences, String key)
        {
                String prefSummary = sharedPreferences.getString(key, null);
                Preference pref = findPreference(key);
                pref.setSummary(prefSummary);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue)
        {
                boolean result = true;
                String key = preference.getKey();
                if (key.equals(getString(R.string.user_country_pref)))
                {

                        String countryCode = (String) newValue;
                        try
                        {

                                FormatHelper.getCurrencyCode(countryCode);

                                preference.setSummary(newValue.toString());
                        }
                        catch(Exception ex)
                        {
                                result = false;
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

                }
                else
                {
                        preference.setSummary(newValue.toString());
                        preference.setSummary(newValue.toString());
                }

                return result;
        }

}
