package com.mirzairwan.shopping.firebase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mirzairwan.shopping.R;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class AuthenticationDialogFrag extends DialogFragment //implements View.OnClickListener
{

        private static final String LOG_TAG = AuthenticationDialogFrag.class.getSimpleName();
        private DatabaseReference mFireDatabase;
        private FirebaseAuth mAuth;
        private EditText mEmailField;
        private EditText mPasswordField;
        private TextView mTvErrorMsg;
        private String mUserEmail;
        private ProgressBar mProgressBar;
        public static int REQUEST_LOGIN_CODE = 88;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                //Get the firebase database
                mFireDatabase = FirebaseDatabase.getInstance().getReference();

                //Get firebase authentication
                mAuth = FirebaseAuth.getInstance();

                //Get email account from prefererence
                String keyEmail = getString(R.string.key_cloud_email);
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                mUserEmail = defaultSharedPreferences.getString(keyEmail, null);
        }

        @Override
        public void onStart()
        {
                super.onStart();
                AlertDialog d = (AlertDialog) getDialog();
                d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                mProgressBar.setVisibility(View.VISIBLE);
                                signIn();
                        }
                });
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_sign_in, null);
                mProgressBar = (ProgressBar)rootView.findViewById(R.id.pb_authentication);
                mTvErrorMsg = (TextView) rootView.findViewById(R.id.sign_in_error_msg);

                mEmailField = (EditText) rootView.findViewById(R.id.sign_in_email);
                mPasswordField = (EditText) rootView.findViewById(R.id.sign_in_password);
                builder.setView(rootView).setPositiveButton(R.string.sign_in, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                Log.d(LOG_TAG, ">>> signin button click");
                        }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                Log.d(LOG_TAG, ">>> cancel button click");
                                getTargetFragment().onActivityResult(REQUEST_LOGIN_CODE, Activity.RESULT_CANCELED, null);
                                AuthenticationDialogFrag.this.dismiss();

                        }
                }).setNeutralButton(R.string.sign_up, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                Log.d(LOG_TAG, ">>> signup button click");
                                onSignUp();
                        }
                });

                if (!TextUtils.isEmpty(mUserEmail))
                {
                        mEmailField.setText(mUserEmail);
                        mPasswordField.requestFocus();
                }

                return builder.create();
        }

        public void onSignUp()
        {
                SignUpDialogFrag signUpDialogFrag = new SignUpDialogFrag();
                signUpDialogFrag.show(getFragmentManager(), "SIGN_UP");
        }

        private void signIn()
        {
                Log.d(LOG_TAG, "signIn");
                if (!areFieldsValid())
                {
                        return;
                }

                //TO DO show progress dialog
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();

                Task<AuthResult> authResult = mAuth.signInWithEmailAndPassword(email, password);

                authResult.addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>()
                {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                                Log.d(LOG_TAG, ">>>signIn:onComplete:" + task.isSuccessful());
                                mProgressBar.setVisibility(View.INVISIBLE);

                                if (task.isSuccessful())
                                {
                                        onAuthenticationSuccess(task.getResult().getUser());
                                        Intent data = new Intent();
                                        data.putExtra("isFirebaseUserAuthenticated",  true);
                                        getTargetFragment().onActivityResult(REQUEST_LOGIN_CODE, Activity.RESULT_OK, data);
                                        AuthenticationDialogFrag.this.dismiss();
                                }
                                else
                                {
                                        Log.d(LOG_TAG, ">>>signIn:onComplete: Unsuccessful");
                                        task.addOnFailureListener(new OnFailureListener()
                                        {
                                                @Override
                                                public void onFailure(@NonNull Exception e)
                                                {
                                                        mTvErrorMsg.setText(e.getMessage());
                                                        mTvErrorMsg.setVisibility(View.VISIBLE);
                                                }
                                        });
                                }
                        }
                });
        }

        public void onAuthenticationSuccess(FirebaseUser currentUser)
        {
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

        private boolean areFieldsValid()
        {
                boolean result = true;
                if (TextUtils.isEmpty(mEmailField.getText()))
                {
                        mEmailField.setError("Required");
                        result = false;
                }
                else
                {
                        mEmailField.setError(null);
                }

                if (TextUtils.isEmpty(mPasswordField.getText()))
                {
                        mPasswordField.setError("Required");
                        result = false;
                }
                else
                {
                        mPasswordField.setError(null);
                }

                return result;
        }

}
