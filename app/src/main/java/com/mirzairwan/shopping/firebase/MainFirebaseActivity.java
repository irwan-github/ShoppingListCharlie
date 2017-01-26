package com.mirzairwan.shopping.firebase;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mirzairwan.shopping.R;
import com.mirzairwan.shopping.domain.User;

import java.util.HashSet;

public class MainFirebaseActivity extends AppCompatActivity implements SignInFragment.onFragmentAuthentication
{
        private FirebaseAuth mAuth;
        private DatabaseReference mFireDatabase;
        private String mUserId;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_firebase);

                //Get the firebase database
                mFireDatabase = FirebaseDatabase.getInstance().getReference();
                //Get firebase authentication
                mAuth = FirebaseAuth.getInstance();

        }

        @Override
        protected void onStart()
        {
                super.onStart();
                //Check if user is authenticated
                if (mAuth.getCurrentUser() != null)
                {
                        mUserId = mAuth.getCurrentUser().getUid();
                        onAuthenticationSuccess(mAuth.getCurrentUser());
                }
                else
                {
                        FragmentTransaction fragTxn = getFragmentManager().beginTransaction();
                        fragTxn = fragTxn.add(R.id.activity_main_firebase_container, new SignInFragment()).addToBackStack(null);
                        fragTxn.commit();
                }
        }

        private void onAuthenticationSuccess(FirebaseUser currentUser)
        {
                mUserId = currentUser.getUid();
                //Write new iser
                writeNewUser(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());

                startFragment();
        }

        protected void startFragment()
        {
                HashSet<Long> args = (HashSet<Long>)getIntent().getSerializableExtra(SendShareFragment.ITEM_TO_SHARE);
                FragmentTransaction fragTxn = getFragmentManager().beginTransaction();
                fragTxn = fragTxn.replace(R.id.activity_main_firebase_container, SendShareFragment.getInstance(args)).addToBackStack(null);
                fragTxn.commit();
        }

        private void writeNewUser(String uid, String userName, String email)
        {
                User user = new User(userName, email);
                mFireDatabase.child("users").child(uid).setValue(user);
        }

        private String usernameFromEmail(String email)
        {
                if (email.contains("@"))
                {
                        return email.split("@")[0];
                }
                else
                {
                        return email;
                }
        }

        @Override
        public void onAuthenticationOk(FirebaseUser firebaseUser)
        {
                onAuthenticationSuccess(firebaseUser);
        }
}
