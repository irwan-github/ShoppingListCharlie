package com.mirzairwan.shopping.domain;

import java.util.Date;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * Responsible for storing information in shopping list.
 * The buy item has a checked status in which the item is item is a check or uncheck,
 */

public class ItemInShoppingList
{
        private long _id; //sqlite id
        private int mQuantity; //Refers to single unit or bundle units
        private boolean mIsChecked = false; //Checked/Unchecked state in the buy list
        private Price mSelectedPrice;
        private Date mLastUpdatedOn;

        /**
         * Used by Shopping List Manager
         *
         * @param _idBuyItem
         * @param quantity
         * @param selectedPrice
         * @param lastUpdatedOn
         */
        public ItemInShoppingList(long _idBuyItem, int quantity, Price selectedPrice, Date lastUpdatedOn)
        {
                if (selectedPrice == null)
                {
                        throw new IllegalArgumentException("Price cannot be null");
                }
                this._id = _idBuyItem;
                mQuantity = quantity;
                mSelectedPrice = selectedPrice;
                mLastUpdatedOn = lastUpdatedOn;
        }

        public ItemInShoppingList()
        {

        }

        public long getId()
        {
                return _id;
        }

        public void setId(long _id)
        {
                this._id = _id;
        }

        public int getQuantity()
        {
                return mQuantity;
        }

        public void setQuantity(int quantity)
        {
                this.mQuantity = quantity;
        }

        public Price.Type getSelectedPriceType()
        {
                return mSelectedPrice.getPriceType();
        }

        public Price getSelectedPrice()
        {
                return mSelectedPrice;
        }

        public void setSelectedPrice(Price selectedPrice)
        {
                mSelectedPrice = selectedPrice;
        }

        public boolean isChecked()
        {
                return mIsChecked;
        }

        public void setCheck(boolean isCheck)
        {
                mIsChecked = isCheck;
        }

        public void setLastUpdatedOn(Date updateTime)
        {
                mLastUpdatedOn = updateTime;
        }


}
