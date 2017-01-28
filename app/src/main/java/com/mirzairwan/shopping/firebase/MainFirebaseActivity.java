package com.mirzairwan.shopping.firebase;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mirzairwan.shopping.R;
import com.mirzairwan.shopping.domain.User;

import java.util.HashSet;

public class MainFirebaseActivity extends AppCompatActivity implements EmailOAuthFragment.onFragmentAuthentication
{
        public static final int FIREBASE_SIGN_OUT = 1;
        public static final String FIREBASE_REQUEST_CODE = "FIREBASE_REQUEST_CODE";
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

                if (getIntent().getIntExtra(FIREBASE_REQUEST_CODE, 0) == FIREBASE_SIGN_OUT)
                {
                        mAuth.signOut();
                        finish();
                }

                //Check if user is authenticated
                if (mAuth.getCurrentUser() != null)
                {
                        mUserId = mAuth.getCurrentUser().getUid();
                        onAuthenticationSuccess(mAuth.getCurrentUser());
                }
                else
                {
                        FragmentTransaction fragTxn = getFragmentManager().beginTransaction();
                        fragTxn = fragTxn.add(R.id.activity_main_firebase_container, new EmailOAuthFragment()).addToBackStack(null);
                        fragTxn.commit();
                }
        }

        private void onAuthenticationSuccess(FirebaseUser currentUser)
        {
                mUserId = currentUser.getUid();
                //Write new iser
                String emailCurrentUser = currentUser.getEmail();
                writeNewUser(currentUser.getUid(), currentUser.getDisplayName(), emailCurrentUser);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString(getString(R.string.key_cloud_email), emailCurrentUser);
                editor.commit();
                startFragment();
        }

        protected void startFragment()
        {
                HashSet<Long> shoppingItemIds = (HashSet<Long>) getIntent().getSerializableExtra(SendShareFragment.ITEM_TO_SHARE);
                String shareeEmail = getIntent().getStringExtra(SendShareFragment.SHAREE_EMAIL);
//                FragmentTransaction fragTxn = getFragmentManager().beginTransaction();
//                fragTxn = fragTxn.replace(R.id.activity_main_firebase_container, SendShareFragment2.getInstance(shoppingItemIds, shareeEmail)).addToBackStack(null);
//                fragTxn.commit();

                SendShareFragment send = SendShareFragment.getInstance(shoppingItemIds, shareeEmail);
                send.show(getFragmentManager(), "Sharee");
        }

//        void showDialog() {
//                DialogFragment newFragment = MyAlertDialogFragment.newInstance(
//                        R.string.alert_dialog_two_buttons_title);
//                newFragment.show(getFragmentManager(), "dialog");
//        }


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
