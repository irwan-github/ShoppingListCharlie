package com.mirzairwan.shopping.firebase;

import android.app.Fragment;
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
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.mirzairwan.shopping.R;

import java.util.ArrayList;

import static com.mirzairwan.shopping.firebase.Constants.SHARED_SHOPPING_LIST;

/**
 * Created by Mirza Irwan on 26/1/17.
 */

public class ShareeShoppingListFragment extends Fragment
{
        private static final String LOG_TAG = ShareeShoppingListFragment.class.getSimpleName();
        private DatabaseReference mRootRef;
        private FirebaseUser mDatabaseUser;
        private View mRootView;
        private ArrayList<Item> mItemsToDelete = new ArrayList<>();
        private ShareeShoppingListAdapter mItemRows;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                mRootRef = FirebaseDatabase.getInstance().getReference();
                mDatabaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
                mRootView = inflater.inflate(R.layout.fragment_show_shared, container, false);
                setupListView();
                return mRootView;
        }

        private void setupListView()
        {
                DatabaseReference sharedShoppingListRef = mRootRef.child(SHARED_SHOPPING_LIST).child(mDatabaseUser.getUid());

                ListView shoppingListView = (ListView) mRootView.findViewById(R.id.shared_cloud_shopping_list);
                mItemRows = new ShareeShoppingListAdapter(getActivity(), sharedShoppingListRef);
                shoppingListView.setAdapter(mItemRows);

                //Enable batch contextual action
                shoppingListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                shoppingListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
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
        }

        private void deleteRemoteItems()
        {
                Log.d(LOG_TAG, ">>>deleteRemoteItem3");
                mRootRef.child(SHARED_SHOPPING_LIST).child(mDatabaseUser.getUid()).runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData)
                        {
                                for(Item itemToDelete : mItemsToDelete)
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
}
