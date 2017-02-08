package com.mirzairwan.shopping.firebase;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

@IgnoreExtraProperties
public class User
{

        public String username;
        public String email;

        public User()
        {
                // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email)
        {
                this.username = username;
                this.email = email;
        }

}
// [END blog_user_class]
