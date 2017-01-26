package com.mirzairwan.shopping.firebase;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
        private ListView shoppingListView;
        private DatabaseReference mRootRef;
        private FirebaseUser mDatabaseUser;
        private static final String LOG_TAG =  ShowSharedFragment.class.getSimpleName();
        private ArrayAdapter<String> mItemRows;

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
                View rootView = inflater.inflate(R.layout.fragment_show_shared, container, false);
                shoppingListView = (ListView)rootView.findViewById(R.id.shared_cloud_shopping_list);
                mItemRows = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
                shoppingListView.setAdapter(mItemRows);
                return rootView;
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

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                                Log.d(LOG_TAG, ">>> key " + dataSnapshot.getKey());
                                Log.d(LOG_TAG, ">>> value " + dataSnapshot.getValue());

                                ArrayList<String> itemRows = null;
                                if(dataSnapshot.hasChildren())
                                        itemRows = new ArrayList<>();

                                for(DataSnapshot itemSnapshot : dataSnapshot.getChildren())
                                {
//                                        Log.d(LOG_TAG, ">>> item key " + itemSnapshot.getKey());
//                                        Log.d(LOG_TAG, ">>> item value " + itemSnapshot.getValue());
                                        Item sharedItem = itemSnapshot.getValue(Item.class);
                                        //Item sharedItem = itemSnapshot.getValue();

                                        Log.d(LOG_TAG, ">>> item name " + sharedItem.getItem());
                                        Log.d(LOG_TAG, ">>> item email " +  sharedItem.getEmailOfOriginator());
                                        Log.d(LOG_TAG, ">>> item uid " +  sharedItem.getUidOfOriginator());
                                        itemRows.add(sharedItem.getItem());
                                }

                                if(dataSnapshot.hasChildren())
                                        mItemRows.addAll(itemRows);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                });
        }
}
