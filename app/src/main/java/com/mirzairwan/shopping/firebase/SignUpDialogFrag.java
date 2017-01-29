package com.mirzairwan.shopping.firebase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mirzairwan.shopping.R;

/**
 * Created by Mirza Irwan on 29/1/17.
 */

public class SignUpDialogFrag extends DialogFragment
{
        private static final String LOG_TAG = SignUpDialogFrag.class.getSimpleName();
        private EditText mEtEmail;
        private EditText mEtPassword;
        private EditText mEtPasswordConfirm;
        private FirebaseAuth mAuth;
        private OnFragmentAuthentication mOnFragmentAuthentication;
        private TextView mTvErrorMsg;

        @Override
        public void onAttach(Activity activity)
        {
                super.onAttach(activity);
                mOnFragmentAuthentication = (OnFragmentAuthentication) activity;
        }

        public void setMessage(String msg)
        {
                mTvErrorMsg.setText(msg);
                mTvErrorMsg.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                //Get firebase authentication
                mAuth = FirebaseAuth.getInstance();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                View rootDialog = inflater.inflate(R.layout.fragment_sign_up, null);
                mTvErrorMsg = (TextView) rootDialog.findViewById(R.id.signup_error_msg);
                mEtEmail = (EditText) rootDialog.findViewById(R.id.field_email);
                mEtPassword = (EditText) rootDialog.findViewById(R.id.field_password);
                mEtPasswordConfirm = (EditText) rootDialog.findViewById(R.id.field_password_confirm);
                builder.setView(rootDialog)
                        // Add action buttons
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                        public void onClick(DialogInterface dialog, int id)
                        {
                                SignUpDialogFrag.this.getDialog().cancel();
                                getActivity().finish();
                        }
                });

                return builder.create();
        }

        @Override
        public void onStart()
        {
                super.onStart();
                final AlertDialog d = (AlertDialog) getDialog();
                Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                boolean wantToCloseDialog = false;
                                if (areFieldsValid())
                                {
                                        mTvErrorMsg.setText(null);
                                        mTvErrorMsg.setVisibility(View.INVISIBLE);
                                        // sign up the user ..
                                        signUp(mEtEmail.getText().toString(), mEtPassword.getText().toString());
                                }
                        }
                });
        }

        private boolean areFieldsValid()
        {
                boolean result = true;
                if (TextUtils.isEmpty(mEtEmail.getText()))
                {
                        mEtEmail.setError("Required");
                        result = false;
                }
                else
                {
                        mEtEmail.setError(null);
                }

                boolean isPasswordEmpty = TextUtils.isEmpty(mEtPassword.getText());
                if (isPasswordEmpty)
                {
                        mEtPassword.setError("Required");
                        result = false;
                }
                else
                {
                        mEtPassword.setError(null);
                }

                boolean isConfirmPasswordEmpty = TextUtils.isEmpty(mEtPasswordConfirm.getText());
                if (isConfirmPasswordEmpty)
                {
                        mEtPasswordConfirm.setError("Required");
                        result = false;
                }
                else
                {
                        mEtPasswordConfirm.setError(null);
                }

                if (!isPasswordEmpty && !isConfirmPasswordEmpty)
                {
                        if (!mEtPassword.getText().toString().equals(mEtPasswordConfirm.getText().toString()))
                        {
                                mEtPassword.setError("Passwords do not match");
                                mEtPasswordConfirm.setError("Passwords do not match");
                                result = false;
                        }
                        else
                        {
                                mEtPassword.setError(null);
                                mEtPasswordConfirm.setError(null);
                        }

                }

                return result;
        }

        private void signUp(String email, String password)
        {
                Log.d(LOG_TAG, ">>>sign up");

                Task<AuthResult> authResult = mAuth.createUserWithEmailAndPassword(email, password);
                authResult.addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>()
                {
                        @Override
                        public void onComplete(@NonNull final Task<AuthResult> task)
                        {
                                Log.d(LOG_TAG, ">>>sign up:onComplete:" + task.isSuccessful());
                                if (task.isSuccessful())
                                {
                                        // onAuthenticationSuccess(task.getResult().getUser());
                                        SignUpDialogFrag.this.dismiss();
                                        mOnFragmentAuthentication.onAuthenticationOk(task.getResult().getUser());
                                }
                                else
                                {
                                        task.addOnFailureListener(new OnFailureListener()
                                        {
                                                @Override
                                                public void onFailure(@NonNull Exception e)
                                                {
                                                        setMessage(e.getMessage());
                                                }
                                        });
                                        Log.d(LOG_TAG, ">>>sign up:onComplete: " + task.getException().getMessage());
                                }
                        }
                });
        }

}