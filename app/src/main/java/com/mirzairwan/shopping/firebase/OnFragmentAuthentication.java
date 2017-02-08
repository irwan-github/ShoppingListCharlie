package com.mirzairwan.shopping.firebase;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public interface OnFragmentAuthentication
{
        void onAuthenticationSuccess(FirebaseUser firebaseUser);
        void onSignUp();
}
