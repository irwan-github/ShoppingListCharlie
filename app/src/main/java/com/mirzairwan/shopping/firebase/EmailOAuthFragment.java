package com.mirzairwan.shopping.firebase;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mirzairwan.shopping.R;

public class EmailOAuthFragment extends Fragment implements View.OnClickListener
{

        private static final String LOG_TAG = EmailOAuthFragment.class.getSimpleName();
        private DatabaseReference mFireDatabase;
        private FirebaseAuth mAuth;
        private EditText mEmailField;
        private EditText mPasswordField;
        private Button mSignInButton;
        private Button mSignUpPassword;
        private onFragmentAuthentication mOnFragmentAuthentication;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
                View rootView = inflater.inflate(R.layout.activity_sign_in, container, false);

                //Get the firebase database
                mFireDatabase = FirebaseDatabase.getInstance().getReference();

                //Get firebase authentication
                mAuth = FirebaseAuth.getInstance();

                //Android Views
                mEmailField = (EditText) rootView.findViewById(R.id.field_email);
                mPasswordField = (EditText) rootView.findViewById(R.id.field_password);
                mSignInButton = (Button) rootView.findViewById(R.id.button_sign_in);
                mSignUpPassword = (Button) rootView.findViewById(R.id.button_sign_up);

                //Click listeners
                mSignInButton.setOnClickListener(this);
                mSignUpPassword.setOnClickListener(this);
                return rootView;
        }

        @Override
        public void onAttach(Activity activity)
        {
                super.onAttach(activity);
                mOnFragmentAuthentication = (onFragmentAuthentication) activity;
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
                                Log.d(LOG_TAG, "signIn:onComplete:" + task.isSuccessful());

                                if (task.isSuccessful())
                                {
                                        //onAuthenticationSuccess(task.getResult().getUser());
                                        mOnFragmentAuthentication.onAuthenticationOk(task.getResult().getUser());
                                }
                                else
                                {
                                        Toast.makeText(EmailOAuthFragment.this.getActivity(), "Sign In Failed", Toast.LENGTH_SHORT).show();
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

        public interface onFragmentAuthentication
        {
                void onAuthenticationOk(FirebaseUser firebaseUser);
                void onSignUp();
        }

}
