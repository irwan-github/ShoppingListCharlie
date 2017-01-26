package com.mirzairwan.shopping.firebase;

/**
 * Created by Mirza Irwan on 26/1/17.
 */

public class Item
{
        private String emailOfOriginator;
        private String item;
        private String uidOfOriginator;
        private String mKey;

        public Item()
        {
                // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
        }

        public Item(String emailOfOriginator, String uidOfOriginator, String item)
        {
                this.emailOfOriginator = emailOfOriginator;
                this.uidOfOriginator = uidOfOriginator;
                this.item = item;
        }

        public String getEmailOfOriginator()
        {
                return emailOfOriginator;
        }

        public Item setEmailOfOriginator(String emailOfOriginator)
        {
                this.emailOfOriginator = emailOfOriginator;
                return this;
        }

        public String getItem()
        {
                return item;
        }

        public Item setItem(String item)
        {
                this.item = item;
                return this;
        }

        public String getUidOfOriginator()
        {
                return uidOfOriginator;
        }

        public Item setUidOfOriginator(String uidOfOriginator)
        {
                this.uidOfOriginator = uidOfOriginator;
                return this;
        }

        public void setKey(String key)
        {
                mKey = key;
        }

        public String getKey()
        {
                return mKey;
        }
}
