package com.mirzairwan.shopping;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Mirza Irwan on 7/12/16.
 */

public class FormatHelper
{
    /**
     * Formats a value of type double
     *
     * @param countryCode
     * @param value
     * @return formatted value prefix/suffixed wilth a currency symbol
     */
    public static String formatCountryCurrency(String countryCode, String currencyCode, double value)
    {
        Locale defaultLocale = Locale.getDefault();
        Locale homeLocale = new Locale(defaultLocale.getLanguage(), countryCode);

        NumberFormat formatterHome = NumberFormat.getCurrencyInstance(homeLocale);
        Currency currency = Currency.getInstance(currencyCode);
        formatterHome.setCurrency(currency);
        return formatterHome.format(value);
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

    public static String formatToTwoDecimalPlaces(Locale locale, double value)
    {
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        ((DecimalFormat) numberFormat).applyPattern("#,##0.00");
        return numberFormat.format(value);
    }

    public static String getCurrencySymbol(String countryCode)
    {
        Locale userLocale = new Locale(Locale.getDefault().getLanguage(), countryCode);
        Currency currency = Currency.getInstance(userLocale);
        return getCurrencySymbol(userLocale, currency.getCurrencyCode());
    }

    public static String getCurrencySymbol(Locale locale, String currencyCode)
    {
        return Currency.getInstance(currencyCode).getSymbol(locale);
    }

    public static String getCurrencyCode(String countryCode)
    {
        Locale userLocale = new Locale(Locale.getDefault().getLanguage(), countryCode);
        Currency currency = Currency.getInstance(userLocale);
        return currency.getCurrencyCode();
    }

    public static String getCurrencySymbol(String countryCode, String currencyCode)
    {
        Locale userLocale = new Locale(Locale.getDefault().getLanguage(), countryCode);
        return getCurrencySymbol(userLocale, currencyCode);
    }

    public static String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    public static boolean validateCurrencyCode(String currencyCode)
    {
        if(currencyCode.equalsIgnoreCase("XXX")) //This is a valid currency code used for testing. But not accepted so as not to confuse users.
            throw new IllegalArgumentException("Unsupported ISO 4217 currency code: " + currencyCode);
        return Currency.getInstance(currencyCode).getCurrencyCode().equals(currencyCode);
    }

    public static Date strToDate(String strDate, String splitter)
    {
        Calendar cal = Calendar.getInstance();

        if(!strDate.contains(splitter))
        {
            return null;
        }

        String[] tokens = strDate.split(splitter);
        cal.set(Integer.parseInt(tokens[0]),
                Integer.parseInt(tokens[1]),
                Integer.parseInt(tokens[2]));
        return cal.getTime();
    }
}
