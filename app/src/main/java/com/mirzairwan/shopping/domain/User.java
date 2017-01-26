package com.mirzairwan.shopping.domain;

/**
 * Created by Mirza Irwan on 24/1/17.
 */

public class User
{
        private String mUsername;
        private String mEmail;

        public User()
        {

        }

        public User(String username, String email)
        {
                mUsername = username;
                mEmail = email;
        }

        public String getUsername()
        {
                return mUsername;
        }

        public String getEmail()
        {
                return mEmail;
        }
}
