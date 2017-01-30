package com.mirzairwan.shopping.firebase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mirzairwan.shopping.R;

public class SignInDialogFrag extends DialogFragment implements View.OnClickListener
{

        private static final String LOG_TAG = SignInDialogFrag.class.getSimpleName();
        private DatabaseReference mFireDatabase;
        private FirebaseAuth mAuth;
        private EditText mEmailField;
        private EditText mPasswordField;
        private Button mSignInButton;
        private Button mSignUpPassword;
        private OnFragmentAuthentication mOnFragmentAuthentication;
        private TextView mTvErrorMsg;
        private String mUserEmail;

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
                                signIn();
                        }
                });
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_sign_in, null);
                mTvErrorMsg = (TextView) rootView.findViewById(R.id.sign_in_error_msg);

                mEmailField = (EditText) rootView.findViewById(R.id.sign_in_email);
                mPasswordField = (EditText) rootView.findViewById(R.id.sign_in_password);
                builder.setView(rootView).setPositiveButton(R.string.sign_in, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                SignInDialogFrag.this.dismiss();
                                getActivity().finish();
                        }
                }).setNeutralButton(R.string.sign_up, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                mOnFragmentAuthentication.onSignUp();
                        }
                });

                if (!TextUtils.isEmpty(mUserEmail))
                {
                        mEmailField.setText(mUserEmail);
                        mPasswordField.requestFocus();
                }

                return builder.create();
        }


        @Override
        public void onAttach(Activity activity)
        {
                super.onAttach(activity);
                mOnFragmentAuthentication = (OnFragmentAuthentication) activity;
        }


        @Override
        public void onClick(View v)
        {
                if (v.getId() == R.id.button_sign_in)
                {
                        signIn();
                }
                else if (v.getId() == R.id.button_sign_up)
                {
                        mOnFragmentAuthentication.onSignUp();
                }
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

                                if (task.isSuccessful())
                                {
                                        SignInDialogFrag.this.dismiss();
                                        mOnFragmentAuthentication.onAuthenticationSuccess(task.getResult().getUser());
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
