package com.mirzairwan.shopping.firebase;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Mirza Irwan on 29/1/17.
 */

public interface OnFragmentAuthentication
{
        void onAuthenticationSuccess(FirebaseUser firebaseUser);
        void onSignUp();
}
