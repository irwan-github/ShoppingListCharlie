package com.mirzairwan.shopping.firebase;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class Item
{
        private String emailOfOriginator;
        private String name;
        private String brand;
        private long price; //Expressed in cents. Eg: $5.20 = 520
        private String currencyCode;
        private int quantity;
        private String uidOfOriginator;
        private String mKey;

        public Item()
        {
                // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
        }

        public Item(String emailOfOriginator, String uidOfOriginator, String name, String brand, long price, String currencyCode, int quantity)
        {
                this.emailOfOriginator = emailOfOriginator;
                this.uidOfOriginator = uidOfOriginator;
                this.name = name;
                this.brand = brand;
                this.price = price;
                this.currencyCode = currencyCode;
                this.quantity = quantity;
        }

        public String getEmailOfOriginator()
        {
                return emailOfOriginator;
        }

        public void setEmailOfOriginator(String emailOfOriginator)
        {
                this.emailOfOriginator = emailOfOriginator;
        }

        public String getName()
        {
                return name;
        }

        public void setName(String name)
        {
                this.name = name;
        }

        public String getUidOfOriginator()
        {
                return uidOfOriginator;
        }

        public void setUidOfOriginator(String uidOfOriginator)
        {
                this.uidOfOriginator = uidOfOriginator;
        }

        public String getBrand()
        {
                return brand;
        }

        public void setBrand(String brand)
        {
                this.brand = brand;
        }

        public long getPrice()
        {
                return price;
        }

        public void setPrice(long price)
        {
                this.price = price;
        }

        public String getCurrencyCode()
        {
                return currencyCode;
        }

        public void setCurrencyCode(String currencyCode)
        {
                this.currencyCode = currencyCode;
        }

        public int getQuantity()
        {
                return quantity;
        }

        public void setQuantity(int quantity)
        {
                this.quantity = quantity;
        }

        public void setKey(String key)
        {
                mKey = key;
        }

        public String getKey()
        {
                return mKey;
        }

        @Override
        public boolean equals(Object o)
        {
                if (this == o)
                {
                        return true;
                }
                if (o == null || getClass() != o.getClass())
                {
                        return false;
                }

                Item item = (Item) o;

                return mKey != null ? mKey.equals(item.mKey) : item.mKey == null;

        }

        @Override
        public int hashCode()
        {
                return mKey != null ? mKey.hashCode() : 0;
        }
}
