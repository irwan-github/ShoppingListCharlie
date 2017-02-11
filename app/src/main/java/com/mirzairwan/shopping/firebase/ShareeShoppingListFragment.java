package com.mirzairwan.shopping.firebase;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mirzairwan.shopping.R;

import java.util.ArrayList;

import static com.mirzairwan.shopping.firebase.AuthenticationDialogFrag.REQUEST_LOGIN_CODE;
import static com.mirzairwan.shopping.firebase.Constants.SHARED_SHOPPING_LIST;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class ShareeShoppingListFragment extends BaseFragment
{
        private static final String LOG_TAG = ShareeShoppingListFragment.class.getSimpleName();
        private DatabaseReference mRootRef;
        private FirebaseUser mDatabaseUser;
        private View mRootView;
        private ArrayList<Item> mItemsToDelete = new ArrayList<>();
        private ShareeShoppingListAdapter mItemRows;
        private ListView mShoppingListView;
        private View mEmptyView;
        private DatabaseReference sharedShoppingListRef;
        private ProgressDialogFragment progressDialog;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                Log.d(LOG_TAG, ">>> onCreate");
                super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
                Log.d(LOG_TAG, ">>> onCreateView");
                mRootView = inflater.inflate(R.layout.fragment_show_shared, container, false);
                mEmptyView = mRootView.findViewById(R.id.empty_shared_shopping_image);
                mShoppingListView = (ListView) mRootView.findViewById(R.id.shared_cloud_shopping_list);
                getActivity().setTitle(R.string.share_shopping_list_txt);
                return mRootView;
        }

        protected void processSocialShoppingList()
        {
                progressDialog = new ProgressDialogFragment();
                progressDialog.show(getFragmentManager(), "Getting");
                setupFirebaseRoot();
                setupListView();
        }

        @Override
        public void onPause()
        {
                Log.d(LOG_TAG, ">>> onPause");
                super.onPause();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
                if (requestCode == REQUEST_LOGIN_CODE)
                {
                        if (resultCode == Activity.RESULT_OK)
                        {
                                processSocialShoppingList();
                        }
                        else if (resultCode == Activity.RESULT_CANCELED)
                        {
                                getFragmentManager().popBackStack();
                        }
                }
        }

        private void setupFirebaseRoot()
        {
                mRootRef = FirebaseDatabase.getInstance().getReference();
                mDatabaseUser = FirebaseAuth.getInstance().getCurrentUser();

                sharedShoppingListRef = mRootRef.child(SHARED_SHOPPING_LIST).child(mDatabaseUser.getUid());
                sharedShoppingListRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener()
                {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                                if (!dataSnapshot.hasChildren())
                                {
                                        progressDialog.dismiss();
                                }

                                mShoppingListView.setEmptyView(mEmptyView);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                });

        }

        private void setupListView()
        {
                mItemRows = new ShareeShoppingListAdapter(getActivity(), sharedShoppingListRef);
                mShoppingListView.setAdapter(mItemRows);

                mShoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                                Item item = mItemRows.getItem(position);
                                Toast.makeText(ShareeShoppingListFragment.this.getActivity(), "Shared by " + item.getEmailOfOriginator(), Toast.LENGTH_SHORT).show();
                        }
                });

                //Enable batch contextual action
                mShoppingListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                mShoppingListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
                {
                        @Override
                        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
                        {
                                // Here you can do something when items are selected/de-selected,
                                // such as update the title in the CAB
                                mode.setTitle("Shared shopping list");
                                Log.d(LOG_TAG, ">>> touch position " + position);
                                Log.d(LOG_TAG, ">>> touch checked " + checked);
                                if (checked)
                                {
                                        mItemsToDelete.add(mItemRows.getItem(position));
                                }
                                else
                                {
                                        mItemsToDelete.remove(mItemRows.getItem(position));
                                }
                        }

                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu)
                        {
                                // Inflate the menu for the CAB
                                MenuInflater inflater = mode.getMenuInflater();
                                inflater.inflate(R.menu.firebase_show_other_shopping_list, menu);
                                return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
                        {
                                return false;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
                        {
                                switch (item.getItemId())
                                {
                                        // Respond to clicks on the actions in the CAB
                                        case R.id.menu_delete_remote_item:

                                                deleteRemoteItems();

                                                // Action picked, so close the CAB
                                                mode.finish();
                                                return true;

                                        default:
                                                return false;
                                }
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode)
                        {

                        }
                });

                mItemRows.registerDataSetObserver(new DataSetObserver()
                {
                        @Override
                        public void onChanged()
                        {
                                progressDialog.dismiss();
                        }
                });

        }

        private void deleteRemoteItems()
        {
                Log.d(LOG_TAG, ">>>deleteRemoteItem3");
                mRootRef.child(SHARED_SHOPPING_LIST).child(mDatabaseUser.getUid()).runTransaction(new Transaction.Handler()
                {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData)
                        {
                                for (Item itemToDelete : mItemsToDelete)
                                {
                                        try
                                        {
                                                mutableData.child(itemToDelete.getKey()).setValue(null);
                                        }
                                        catch(DatabaseException dbEx)
                                        {
                                                return Transaction.abort();
                                        }
                                }

                                return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
                        {
                                if (databaseError == null)
                                {
                                        mItemsToDelete.clear();
                                        Snackbar.make(mRootView, "Shared item deleted: OK", Snackbar.LENGTH_LONG).show();
                                }
                                else
                                {
                                        Snackbar.make(mRootView, "Shared item deleted: Failed", Snackbar.LENGTH_LONG).show();
                                        Log.e(LOG_TAG, databaseError.getDetails());
                                }
                        }
                });

        }

        public static class ProgressDialogFragment extends DialogFragment
        {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState)
                {
                        ProgressDialog mProgressDialog = new ProgressDialog(getActivity());
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setMessage(getString(R.string.progress_dialog_msg));
                        return mProgressDialog;
                }
        }
}
