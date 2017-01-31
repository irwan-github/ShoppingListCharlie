package com.mirzairwan.shopping.firebase;

import android.app.FragmentTransaction;
import android.os.Bundle;

import com.mirzairwan.shopping.R;

import java.util.HashSet;

/**
 * Created by Mirza Irwan on 31/1/17.
 */

public class SendSharedActivity extends MainFirebaseActivity
{
        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
        }

        @Override
        protected void startFragment()
        {
                HashSet<Long> shoppingItemIds = (HashSet<Long>) getIntent().getSerializableExtra(SendShareFragment.ITEM_TO_SHARE);
                String shareeEmail = getIntent().getStringExtra(SendShareFragment.SHAREE_EMAIL);
                FragmentTransaction fragTxn = getFragmentManager().beginTransaction();
                SendShareFragment sendShareFragment = SendShareFragment.getInstance(shoppingItemIds, shareeEmail);
                fragTxn = fragTxn.replace(R.id.activity_main_firebase_container, sendShareFragment).addToBackStack(null);
                fragTxn.commit();
        }
}
