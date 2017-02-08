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
 * Responsible for storing item, quantity and chosen price.
 * A default unit price and bundle price of value $0.00 will be assigned if price is not given.
 * Price type is defaulted to unit price
 * The buy item has a checked status in which the item is item is ina check or uncheck,
 */

public class ItemInShoppingList implements Parcelable
{
        public static final Creator<ItemInShoppingList> CREATOR = new Creator<ItemInShoppingList>()
        {
                public ItemInShoppingList createFromParcel(Parcel in)
                {
                        return new ItemInShoppingList(in);
                }

                public ItemInShoppingList[] newArray(int size)
                {
                        return new ItemInShoppingList[size];
                }
        };
        private static AtomicInteger nextId = new AtomicInteger(0);
        private long _id; //sqlite id
        private int mId; //domain id. Used internally -in-memory. Not stored in database.
        private int mQuantity; //Refers to single unit or bundle units
        private Item mItem;
        private boolean mIsChecked = false; //Checked/Unchecked state in the buy list
        private Price mSelectedPrice;
        private Date mLastUpdatedOn;

        public ItemInShoppingList(Item item, int quantity, Price selectedPrice)
        {
                if (selectedPrice == null)
                {
                        throw new IllegalArgumentException("Price cannot be null");
                }
                mId = nextId.incrementAndGet();
                mQuantity = quantity;
                mItem = item;
                mSelectedPrice = selectedPrice;
                mItem.setInBuyList(true);
        }

        public ItemInShoppingList(long _idBuyItem, int quantity, Price selectedPrice, Item item, Date lastUpdatedOn)
        {
                if (selectedPrice == null)
                {
                        throw new IllegalArgumentException("Price cannot be null");
                }
                this._id = _idBuyItem;
                mId = nextId.incrementAndGet();
                mQuantity = quantity;
                mItem = item;
                mItem.setInBuyList(true);
                mSelectedPrice = selectedPrice;
                mLastUpdatedOn = lastUpdatedOn;
        }

        public ItemInShoppingList(long buyItemId)
        {
                _id = buyItemId;
        }

        public ItemInShoppingList(Parcel in)
        {
                _id = in.readLong();
                mId = in.readInt();
                mQuantity = in.readInt();
                mItem = in.readParcelable(getClass().getClassLoader());
                mSelectedPrice = in.readParcelable(getClass().getClassLoader());
                mIsChecked = in.readInt() == 1;
                mLastUpdatedOn = in.readLong() > 0 ? new Date(in.readLong()) : null;
        }

        public ItemInShoppingList(Item item)
        {
                mItem = item;
        }

//    int getBuyItemId()
//    {
//        return mId;
//    }

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

        public Item getItem()
        {
                return mItem;
        }

        public void setItem(Item item)
        {
                mItem = item;
        }

        public void selectPrice(long shopId, Price.Type selectedPriceType)
        {
                mSelectedPrice = mItem.getItemPrice(shopId, selectedPriceType);
        }

        public Price.Type getSelectedPriceType()
        {
                return mSelectedPrice.getPriceType();
        }

        public Price getSelectedPrice()
        {
                return mSelectedPrice;
        }

        /**
         * @return unit price or bundle price
         */
        public double getSelectedPriceTag()
        {
                if (mSelectedPrice.getPriceType() == Price.Type.BUNDLE_PRICE)
                {
                        return mSelectedPrice.getBundlePrice();
                }
                else
                {
                        return mSelectedPrice.getUnitPrice();
                }
        }

        public double getUnitPrice()
        {
                return mItem.getItemPrice(1, Price.Type.UNIT_PRICE).getUnitPrice();
                //return mSelectedPrice.getUnitPrice();
        }

        public double getBundlePrice()
        {
                int shopId = 1; //Supporting only  1 shop
                return mItem.getItemPrice(shopId, Price.Type.BUNDLE_PRICE).getBundlePrice();
        }

        public double getBundleQuantity()
        {
                int shopId = 1; //Supporting only  1 shop
                return mItem.getItemPrice(shopId, Price.Type.BUNDLE_PRICE).getBundleQuantity();
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

        double totalPrice()
        {
                double totalLineItemPrice = 0.00d;
                totalLineItemPrice += getSelectedPriceTag() * mQuantity;
                return totalLineItemPrice;
        }

        @Override
        public String toString()
        {
                return mItem.getName();
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
                dest.writeInt(mId);
                dest.writeInt(mQuantity);
                dest.writeParcelable(mItem, flags);
                dest.writeParcelable(mSelectedPrice, flags);
                dest.writeInt(mIsChecked ? 1 : 0);
                if (mLastUpdatedOn != null)
                {
                        dest.writeLong(mLastUpdatedOn.getTime());
                }
        }


}
