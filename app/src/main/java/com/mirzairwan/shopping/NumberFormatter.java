package com.mirzairwan.shopping;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by Mirza Irwan on 7/12/16.
 */

public class NumberFormatter
{

    public static final String COUNTRY_CODE = "COUNTRY_CODE";
    public static final String CURRENCY_CODE = "CURRENCY_CODE";

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

}
