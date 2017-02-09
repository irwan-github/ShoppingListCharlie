package com.mirzairwan.shopping.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Item added to shopping list
 * Every item currently will and must have 2 price objects
 */
public class Item implements Parcelable
{
        public static final Creator<Item> CREATOR = new Creator<Item>()
        {
                public Item createFromParcel(Parcel in)
                {
                        return new Item(in);
                }

                public Item[] newArray(int size)
                {
                        return new Item[size];
                }
        };
        static AtomicInteger nextId = new AtomicInteger(0);
        private long _id; //Assigned and used by SQLite and Android
        private String mItemName;
        private String mBrand;
        private String mDescription;
        private String mCountryOrigin;
        private boolean mIsInBuyList = false;
        private Date mLastUpdatedOn;
        //private List<Price> mPrices = new ArrayList<>();

        public Item()
        {

        }

        public Item(String itemName)
        {
                mItemName = itemName;
        }

        public Item(long itemId)
        {
                _id = itemId;
        }

        public Item(long id, String itemName, String brand, String country, String description, Date lastUpdatedOn)
        {
                if (itemName == null || itemName.trim().equals(""))
                {
                        throw new IllegalArgumentException("Item name cannot empty");
                }
                _id = id;
                mItemName = itemName;
                mBrand = brand;
                mCountryOrigin = country;
                mDescription = description;
                mLastUpdatedOn = lastUpdatedOn;
        }

        private Item(Parcel in)
        {
                _id = in.readLong();
                mItemName = in.readString();
                mBrand = in.readString();
                mDescription = in.readString();
                mCountryOrigin = in.readString();
                mIsInBuyList = in.readByte() != 0;
                mLastUpdatedOn = new Date(in.readLong());
        }

        public long getId()
        {
                return _id;
        }

        public void setId(long _id)
        {
                this._id = _id;
        }

        public String getName()
        {
                return mItemName;
        }

        public void setName(String itemName)
        {
                mItemName = itemName;
        }

        public String getBrand()
        {
                return mBrand;
        }

        public void setBrand(String brand)
        {
                this.mBrand = brand;
        }

        public String getDescription()
        {
                return mDescription;
        }

        public void setDescription(String description)
        {
                this.mDescription = description;
        }

        public String getCountryOrigin()
        {
                return mCountryOrigin;
        }

        public void setCountryOrigin(String countryOrigin)
        {
                mCountryOrigin = countryOrigin;
        }

        public boolean isInBuyList()
        {
                return mIsInBuyList;
        }

        public void setInBuyList(boolean inBuyList)
        {
                mIsInBuyList = inBuyList;
        }

        public Date getLastUpdateOn()
        {
                return mLastUpdatedOn;
        }

        public void setLastUpdateOn(Date timeOfUpdate)
        {
                mLastUpdatedOn = timeOfUpdate;
        }

        @Override
        public int describeContents()
        {
                return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
                dest.writeLong(_id);

                dest.writeString(mItemName);
                dest.writeString(mBrand);
                dest.writeString(mDescription);
                dest.writeString(mCountryOrigin);
                dest.writeByte((byte) (mIsInBuyList ? 1 : 0));
                if (mLastUpdatedOn != null)
                {
                        dest.writeLong(mLastUpdatedOn.getTime());
                }
        }


}
