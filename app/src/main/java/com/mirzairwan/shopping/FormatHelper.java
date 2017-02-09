package com.mirzairwan.shopping;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Helper class to format numbers, currencies, etc.
 */

public class FormatHelper
{
        /**
         * Formats a currency fit for display
         *
         * @param countryCode  Country code is independent of currency code.
         * @param currencyCode if currency code is invalid, a currency code based on a default locale will be used
         * @param value
         * @return formatted value prefix/suffixed wilth a currency symbol
         */
        public static String formatCountryCurrency(String countryCode, String currencyCode, double value)
        {
                if (countryCode == null)
                {
                        throw new IllegalArgumentException("Country code cannot be null");
                }

                Locale defaultLocale = Locale.getDefault();
                Locale homeLocale = new Locale(defaultLocale.getLanguage(), countryCode);
                NumberFormat currencyFormatter = null;

                try
                {
                        currencyFormatter = NumberFormat.getCurrencyInstance(homeLocale);
                        currencyFormatter.setCurrency(Currency.getInstance(currencyCode));
                }
                catch(Exception ex1)
                {
                        try
                        {
                                //Fallback to default currency
                                currencyFormatter = NumberFormat.getInstance(Locale.getDefault());
                                currencyFormatter.setCurrency(Currency.getInstance(currencyCode));
                        }
                        catch(Exception ex2)
                        {
                                // Fallback to whatever NumberFormat want to do!
                                currencyFormatter = NumberFormat.getInstance();
                                currencyFormatter.setCurrency(Currency.getInstance(currencyCode));
                        }
                }

                return currencyFormatter.format(value);
        }

        /**
         * Formats a currency value. Country code is extracted from the three-letter currency code
         *
         * @param currencyCode if currency code is invalid, a currency code based on a default locale will be used
         * @param value
         * @return formatted value prefix/suffixed wilth a currency symbol
         */
        public static String formatCountryCurrency(String currencyCode, double value)
        {
                if (currencyCode == null)
                {
                        throw new IllegalArgumentException("Currency code cannot be null");
                }

                String countryCode = currencyCode.substring(0, 2);
                Locale homeLocale = new Locale(Locale.getDefault().getLanguage(), countryCode);
                NumberFormat currencyformatter = null;

                try
                {
                        currencyformatter = NumberFormat.getCurrencyInstance(homeLocale);
                }
                catch(Exception ex1)
                {
                        try
                        {
                                //Fallback to default currency
                                currencyformatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
                        }
                        catch(Exception ex2)
                        {
                                // Fallback to whatever NumberFormat want to do!
                                currencyformatter = NumberFormat.getCurrencyInstance();
                        }
                }

                return currencyformatter.format(value);
        }

        /**
         * Decimal point symbol and round off to 2 decimal places according to default locale
         *
         * @param value
         * @return
         */
        public static String formatToTwoDecimalPlaces(double value)
        {
                return formatToTwoDecimalPlaces(Locale.getDefault(), value);
        }

        /**
         * Decimal point symbol and round off to 2 decimal places according to country locale
         *
         * @param value
         * @return
         */
        public static String formatToTwoDecimalPlaces(String countryCode, double value)
        {
                Locale locale = new Locale(Locale.getDefault().getLanguage(), countryCode);
                return formatToTwoDecimalPlaces(locale, value);
        }

        public static String formatToTwoDecimalPlaces(Locale locale, double value)
        {
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                ((DecimalFormat) numberFormat).applyPattern("###,##0.00");
                String formattedVal = numberFormat.format(value);
                return formattedVal;
        }

        public static double parseTwoDecimalPlaces(String value) throws ParseException
        {
                return parseTwoDecimalPlaces(Locale.getDefault(), value);
        }

        public static double parseTwoDecimalPlaces(Locale locale, String value) throws ParseException
        {
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                ((DecimalFormat) numberFormat).applyPattern("###,##0.00");
                Number formattedVal = numberFormat.parse(value);
                return formattedVal.doubleValue();
        }


        /**
         * Get currency symbol based on country code
         *
         * @param countryCode
         * @return
         */
        public static String getCurrencySymbolOfCountry(String countryCode)
        {
                Locale userLocale = new Locale(Locale.getDefault().getLanguage(), countryCode);
                Currency currency = Currency.getInstance(userLocale);
                return getCurrencySymbol(userLocale, currency.getCurrencyCode());
        }

        /**
         * Extract the country code from currency code. Create a Locale based on the country code
         * and default language of the device. The currency symbol is based on the created locale
         *
         * @param currencyCode
         * @return currency symbol
         */
        public static String getCurrencySymbol(String currencyCode)
        {
                //Extract 2-letter country-code
                String countryCode = currencyCode.substring(0, 2);
                Locale userLocale = new Locale(Locale.getDefault().getLanguage(), countryCode);
                return Currency.getInstance(currencyCode).getSymbol(userLocale);
        }

        public static String getCurrencySymbol(String countryCode, String currencyCode)
        {
                Locale userLocale = new Locale(Locale.getDefault().getLanguage(), countryCode);
                return getCurrencySymbol(userLocale, currencyCode);
        }

        public static String getCurrencySymbol(Locale locale, String currencyCode)
        {
                Currency currency;
                try
                {
                        currency = Currency.getInstance(locale);
                }
                catch(Exception ex)
                {
                        currency = Currency.getInstance(locale);
                }

                return currency.getSymbol(locale);
        }

        public static String getCurrencyCode(String countryCode)
        {
                Locale userLocale = new Locale(Locale.getDefault().getLanguage(), countryCode);
                Currency currency = Currency.getInstance(userLocale);
                return currency.getCurrencyCode();
        }


        public static String capitalizeFirstLetter(String original)
        {
                if (original == null || original.length() == 0)
                {
                        return original;
                }
                return original.substring(0, 1).toUpperCase() + original.substring(1);
        }

        public static boolean isValidCurrencyCode(String currencyCode)
        {
                if (currencyCode.equalsIgnoreCase("XXX")) //This is a valid currency code used for testing. But not accepted so as not to confuse users.
                {
                        return false;
                }
                Currency currency;
                try
                {
                        currency = Currency.getInstance(currencyCode);
                }
                catch(Exception ex)
                {
                        return false;
                }

                return currency.getCurrencyCode().equals(currencyCode);
        }

        public static Date strToDate(String strDate, String splitter)
        {
                Calendar cal = Calendar.getInstance();

                if (!strDate.contains(splitter))
                {
                        return null;
                }

                String[] tokens = strDate.split(splitter);
                cal.set(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                return cal.getTime();
        }
}
