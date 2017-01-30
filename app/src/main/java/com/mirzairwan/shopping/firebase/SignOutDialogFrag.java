package com.mirzairwan.shopping.firebase;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.mirzairwan.shopping.R;

/**
 * Created by Mirza Irwan on 29/1/17.
 */

public class SignOutDialogFrag extends DialogFragment
{
        private FirebaseAuth mAuth;

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
                builder.setMessage(R.string.sign_out_msg).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                mAuth.signOut();
                                SignOutDialogFrag.this.dismiss();
                                getActivity().finish();
                        }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                SignOutDialogFrag.this.dismiss();
                                getActivity().finish();
                        }
                });

                return builder.create();
        }
}