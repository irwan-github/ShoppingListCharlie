package com.mirzairwan.shopping;

import org.junit.Test;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import static com.mirzairwan.shopping.NumberFormatter.getCurrencyCode;

/**
 * Created by Mirza Irwan on 18/12/16.
 */
public class NumberFormatterTest
{
    @Test
    public void formatCountryCurrency() throws Exception
    {
        String formattedValue = NumberFormatter.formatCountryCurrency("SG", "SGD", 4.55);
        System.out.println(formattedValue);
        formattedValue = NumberFormatter.formatCountryCurrency("GB", "SGD", 4.55);
        System.out.println(formattedValue);
    }

    @Test
    public void formatCurrency2() throws Exception
    {
        Locale[] locales = Locale.getAvailableLocales();

        for (Locale locale : locales)
        {
            System.out.println(">>>>> " + locale.getCountry());

        }

        //String formatted = NumberFormatter.formatCurrency(currencyCode, 5.588d);



    }

    @Test
    public void formatCurrency3() throws Exception
    {
        String currencyCode = "SGD";

        Locale localeSg = new Locale(Locale.ENGLISH.getLanguage(), "SG");
        Locale localeUk = new Locale(Locale.ENGLISH.getLanguage(), "GB");
        Currency currency = Currency.getInstance(localeSg);
        Currency currencyUk = Currency.getInstance("GBP");

        //System.out.println(">>>>> " + locale.toString());
        System.out.println(">>>>> " + currencyUk.getSymbol(localeUk));
        System.out.println(">>>>> " + currencyUk.getSymbol(localeSg));

        double value = 5.15d;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(localeUk);
        System.out.println(">> " + formatter.format(value));

        NumberFormat formatterSg = NumberFormat.getCurrencyInstance(localeSg);
        System.out.println(">> " + formatterSg.format(value));

    }

    @Test
    public void formatCurrency4() throws Exception
    {
        String output = NumberFormatter.formatCountryCurrency("GB", "GBP", 5.55d);
        String output2 = NumberFormatter.formatCountryCurrency("SG", "GBP", 5.55d);
        System.out.println(">> " + output);
        System.out.println(">> " + output2);
    }

    @Test
    public void testCurrencySymbol() throws Exception
    {
//        String symbol = getCurrencySymbol("SG");
//        System.out.println(">> " + symbol);

//        symbol = getCurrencySymbol("GB");
//        System.out.println(">> " + symbol);
//
//        symbol = getCurrencySymbol("US");
//        System.out.println(">> " + symbol);

//        String symbol = NumberFormatter.getCurrencySymbol("SG", "SGD");
//        System.out.println(">> " + symbol);
//
//        symbol = NumberFormatter.getCurrencySymbol("GB", "GBP");
//        System.out.println(">> " + symbol);

        String symbol = NumberFormatter.getCurrencySymbol("SG");
        System.out.println(">> " + symbol);

        symbol = NumberFormatter.getCurrencySymbol("GB");
        System.out.println(">> " + symbol);

        symbol = NumberFormatter.getCurrencySymbol("MY");
        System.out.println(">> " + symbol);

        symbol = NumberFormatter.getCurrencySymbol("JP");
        System.out.println(">> " + symbol);
    }

    @Test
    public void testCurrencyCode() throws Exception{
        String symbol = getCurrencyCode("SG");
        System.out.println(">> " + symbol);
    }

}