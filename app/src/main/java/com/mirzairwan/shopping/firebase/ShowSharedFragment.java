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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mirzairwan.shopping.R;

import java.util.ArrayList;

import static com.mirzairwan.shopping.firebase.Constants.EMAIL_OF_ORIGINATOR;
import static com.mirzairwan.shopping.firebase.Constants.SHARED_SHOPPING_LIST;

/**
 * Created by Mirza Irwan on 26/1/17.
 */

public class ShowSharedFragment extends Fragment
{
        private static final String LOG_TAG = ShowSharedFragment.class.getSimpleName();
        private ListView shoppingListView;
        private DatabaseReference mRootRef;
        private FirebaseUser mDatabaseUser;
        //private ArrayAdapter<String> mItemRows;

        private View mRootView;
        private ArrayList<Item> mItems = new ArrayList<>();
        private ArrayList<Item> mItemsDelete = new ArrayList<>();
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
                shoppingListView = (ListView) mRootView.findViewById(R.id.shared_cloud_shopping_list);
                //mItemRows = new ArrayAdapter<>(getActivity(), R.layout.row_shared_shopping_item, R.id.remote_item_name);
                mItemRows = new ShareeShoppingListAdapter(getActivity(), mItems);
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
                                        mItemsDelete.add(mItems.get(position));
                                }
                                else
                                {
                                        mItemsDelete.remove(mItems.get(position));
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
                                                deleteRemoteItem();

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

        private void deleteRemoteItem()
        {
                for (final Item itemToDelete : mItemsDelete)
                {
                        mRootRef.child(SHARED_SHOPPING_LIST).child(mDatabaseUser.getUid()).child(itemToDelete.getKey()).
                                removeValue(new DatabaseReference.CompletionListener()
                                {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                                        {
                                                //mItems.remove(itemToDelete);
                                                mItemsDelete.remove(itemToDelete);
                                                mItemRows.remove(itemToDelete);
                                        }
                                });
                }
                Snackbar.make(mRootView, "Deleting shared item", Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onResume()
        {
                super.onResume();
                Toast.makeText(getActivity(), "Show shared shopping list", Toast.LENGTH_LONG).show();
                mRootRef = FirebaseDatabase.getInstance().getReference();
                queryShoppingList();
        }

        private void queryShoppingList()
        {
                DatabaseReference sharedShoppingListRef = mRootRef.child(SHARED_SHOPPING_LIST);
                Query query = sharedShoppingListRef.child(mDatabaseUser.getUid()).orderByChild(EMAIL_OF_ORIGINATOR);

                query.addListenerForSingleValueEvent(new ValueEventListener()
                {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                                Log.d(LOG_TAG, ">>> key " + dataSnapshot.getKey());
                                Log.d(LOG_TAG, ">>> value " + dataSnapshot.getValue());

                                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren())
                                {
//                                        Log.d(LOG_TAG, ">>> item key " + itemSnapshot.getKey());
//                                        Log.d(LOG_TAG, ">>> item value " + itemSnapshot.getValue());
                                        Item sharedItem = itemSnapshot.getValue(Item.class);
                                        sharedItem.setKey(itemSnapshot.getKey());

                                        Log.d(LOG_TAG, ">>> item name " + sharedItem.getItem());
                                        Log.d(LOG_TAG, ">>> item email " + sharedItem.getEmailOfOriginator());
                                        Log.d(LOG_TAG, ">>> item uid " + sharedItem.getUidOfOriginator());
                                        mItems.add(sharedItem);
                                }
                                ShowSharedFragment.this.mItemRows.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                });
        }
}
