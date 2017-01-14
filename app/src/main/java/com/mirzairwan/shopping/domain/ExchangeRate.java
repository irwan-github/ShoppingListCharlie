package com.mirzairwan.shopping.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Mirza Irwan on 13/1/17.
 */
public class ExchangeRate implements Parcelable
{
    private String mSourceCurrencyCode; //source currency
    private String mDestCurrencyCode;  //destination currency
    private double mRate; //One unit of mDestCurrency = mRate units of mSourceCurrency
    private Date mDate;

    public final static String FOREIGN_CURRENCY_CODES = "FOREIGN_CURRENCY_CODES";
    public final static String FOREX_API_URL = "FOREX_API_URL";

    public ExchangeRate(String sourceCurrencyCode, String destinationCurrencyCode, double rate,
                        Date date)
    {
        mSourceCurrencyCode = sourceCurrencyCode;
        mDestCurrencyCode = destinationCurrencyCode;
        mRate = rate;
        mDate = date;
    }

    protected ExchangeRate(Parcel in)
    {
        mSourceCurrencyCode = in.readString();
        mDestCurrencyCode = in.readString();
        mRate = in.readDouble();
        mDate = new Date(in.readLong());
    }

    public double compute(double sourceCurrencyVal)
    {
        return (1/mRate) * sourceCurrencyVal;
    }

    public String getDestCurrencyCode()
    {
        return mDestCurrencyCode;
    }

    public String getSourceCurrencyCode()
    {
        return mSourceCurrencyCode;
    }

    public double compute(double sourceCurrencyVal, String destCurrencyCode)
    {
        if(!destCurrencyCode.equals(mDestCurrencyCode))
            return (1/mRate) * sourceCurrencyVal;
        else
            return -1;
    }

    public static final Creator<ExchangeRate> CREATOR = new Creator<ExchangeRate>()
    {
        @Override
        public ExchangeRate createFromParcel(Parcel in)
        {
            return new ExchangeRate(in);
        }

        @Override
        public ExchangeRate[] newArray(int size)
        {
            return new ExchangeRate[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mSourceCurrencyCode);
        dest.writeString(mDestCurrencyCode);
        dest.writeDouble(mRate);
        dest.writeLong(mDate.getTime());
    }


}