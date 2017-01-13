package com.mirzairwan.shopping.domain;

import java.util.Date;

/**
 * Created by Mirza Irwan on 13/1/17.
 */
public class ExchangeRate
{
    private String mSourceCurrencyCode; //source currency
    private int numCurrencyCode;
    private String mBaseCurrencyCode;  //destination currency
    private double mRate;
    private Date mDate;

    public final static String FOREIGN_CURRENCY_CODES = "FOREIGN_CURRENCY_CODES";
    public final static String FOREX_API_URL = "FOREX_API_URL";

    public ExchangeRate(String sourceCurrencyCode, String destinationCurrencyCode, double rate,
                        Date date)
    {
        mSourceCurrencyCode = sourceCurrencyCode;
        mBaseCurrencyCode = destinationCurrencyCode;
        mRate = rate;
        mDate = date;
    }

    public double compute(double sourceCurrencyVal)
    {
        return (1/mRate) * sourceCurrencyVal;
    }

}
