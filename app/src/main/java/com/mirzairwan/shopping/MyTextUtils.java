package com.mirzairwan.shopping;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class MyTextUtils
{

        public static void setAllCapsInputFilter(EditText et)
        {
                InputFilter[] prefFilters;
                prefFilters = et.getFilters();
                ArrayList<InputFilter> filters = new ArrayList<>(Arrays.asList(prefFilters));
                filters.add(new InputFilter.AllCaps());
                prefFilters = new InputFilter[filters.size()];
                filters.toArray(prefFilters);
                et.setFilters(prefFilters);
        }

        /**
         * Check that text is null or does NOT contain String or has value 0.00
         *
         * @param editText
         * @return true if text is null or does NOT contain String or has value 0.00
         */
        public static boolean isZeroValue(EditText editText) throws ParseException
        {
                boolean isEmpty = TextUtils.isEmpty(editText.getText());
                boolean isZero = false;

                if (isEmpty)
                {
                        return isZero;
                }
                else
                {
                        String val = editText.getText().toString();
                        int parsed = (int) FormatHelper.parseTwoDecimalPlaces(val);
                        isZero = parsed == 0;
                }

                return isZero;
        }

        public static void setPrice(Context context, String srcCurrencyCode, long priceTag, TextView tvSelectedPrice)
        {
                String countryCode = PreferenceManager.getDefaultSharedPreferences(context).getString("home_country_preference", null);
                String destCurrencyCode = FormatHelper.getCurrencyCode(countryCode);
                double value = (double)priceTag / 100;
                tvSelectedPrice.setText(FormatHelper.formatCountryCurrency(countryCode, srcCurrencyCode, value));
        }

        public static String getUserNameFromEmailAddress(String userEmail)
        {
                return userEmail.split("@")[0];
        }

}
