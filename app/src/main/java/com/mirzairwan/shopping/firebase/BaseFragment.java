package com.mirzairwan.shopping.firebase;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mirzairwan.shopping.R;

import static com.mirzairwan.shopping.firebase.AuthenticationDialogFrag.REQUEST_LOGIN_CODE;

/**
 * Created by Mirza Irwan on 10/2/17.
 */

public abstract class BaseFragment extends Fragment
{
        protected FirebaseAuth mAuth;
        protected String mUserId;
        private DatabaseReference mFireDatabase;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                //Get the firebase database
                mFireDatabase = FirebaseDatabase.getInstance().getReference();

                //Get firebase authentication
                mAuth = FirebaseAuth.getInstance();
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
                super.onActivityCreated(savedInstanceState);

        }

        @Override
        public void onResume()
        {
                super.onResume();
                if (mAuth.getCurrentUser() != null)
                {
                        mUserId = mAuth.getCurrentUser().getUid();
                        onAuthenticationSuccess(mAuth.getCurrentUser());
                        processSocialShoppingList();
                }
                else
                {
                        AuthenticationDialogFrag authenticationFrag = new AuthenticationDialogFrag();
                        authenticationFrag.setTargetFragment(this, REQUEST_LOGIN_CODE);
                        authenticationFrag.show(getFragmentManager(), "SIGN_IN");
                }
        }

        protected abstract void processSocialShoppingList();

        public void onAuthenticationSuccess(FirebaseUser currentUser)
        {
                mUserId = currentUser.getUid();

                //Write new user
                String emailCurrentUser = currentUser.getEmail();
                writeNewUser(currentUser.getUid(), currentUser.getDisplayName(), emailCurrentUser);

                //Update shared preference
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                editor.putString(getString(R.string.key_cloud_email), emailCurrentUser);
                editor.commit();
        }

        private void writeNewUser(String uid, String userName, String email)
        {
                com.mirzairwan.shopping.domain.User user = new com.mirzairwan.shopping.domain.User(userName, email);
                mFireDatabase.child("users").child(uid).setValue(user);
        }

}
