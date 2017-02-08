package com.mirzairwan.shopping.firebase;

import android.app.FragmentTransaction;
import android.os.Bundle;
import com.mirzairwan.shopping.R;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class ShowSharedActivity extends MainFirebaseActivity
{

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                //Used superclass layout container
                super.onCreate(savedInstanceState);
        }

        @Override
        protected void startFragment()
        {
                FragmentTransaction fragTxn = getFragmentManager().beginTransaction();
                fragTxn = fragTxn.replace(R.id.activity_main_firebase_container, new ShareeShoppingListFragment()).addToBackStack(null);
                fragTxn.commit();
        }
}
