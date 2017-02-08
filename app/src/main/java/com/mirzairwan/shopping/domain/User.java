package com.mirzairwan.shopping.domain;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
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
