package com.mirzairwan.shopping.domain;

import java.util.Date;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Item added to shopping list
 * Every item currently will and must have 2 price objects
 */
public class Item
{
        private long _id; //Assigned and used by SQLite and Android
        private String mItemName;
        private String mBrand;
        private String mDescription;
        private String mCountryOrigin;
        private boolean mIsInBuyList = false;
        private Date mLastUpdatedOn;

        public Item()
        {

        }

        public Item(long id, String itemName, String brand, String country, String description, Date lastUpdatedOn)
        {
//                if (itemName == null || itemName.trim().equals(""))
//                {
//                        throw new IllegalArgumentException("Item name cannot empty");
//                }
                _id = id;
                mItemName = itemName;
                mBrand = brand;
                mCountryOrigin = country;
                mDescription = description;
                mLastUpdatedOn = lastUpdatedOn;
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

}
