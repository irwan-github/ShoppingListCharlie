package com.mirzairwan.shopping.firebase;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mirzairwan.shopping.OnPictureRequestListener;
import com.mirzairwan.shopping.R;
import com.mirzairwan.shopping.ShoppingListAdapter;
import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.domain.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mirza Irwan on 25/1/17.
 */

public class ShareFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
        public static final String ITEM_TO_SHARE = "ITEM_TO_SHARE";
        private static final String SORT_COLUMN = "SORT_COLUMN";
        private static final String LOG_TAG = ShareFragment.class.getSimpleName();
        private ShoppingListAdapter shoppingListAdapter;
        private OnPictureRequestListener mOnPictureRequestListener;
        private DatabaseReference mDatabase;

        public static ShareFragment instantiate(HashSet<Long> ids)
        {
                ShareFragment shareFragment = new ShareFragment();
                Bundle args = new Bundle();
                args.putSerializable(ITEM_TO_SHARE, ids);
                shareFragment.setArguments(args);
                return shareFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                mDatabase = FirebaseDatabase.getInstance().getReference();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
                View rootView = inflater.inflate(R.layout.fragment_share_shopping_list, container, false);
                ListView lvShareItems = (ListView) rootView.findViewById(R.id.lv_to_share_items);
                //setupListView(lvShareItems);
                return rootView;
        }

        private void setupListView(ListView lvShareItems)
        {
                shoppingListAdapter = new ShoppingListAdapter(getActivity(), null, null, mOnPictureRequestListener);

                lvShareItems.setAdapter(shoppingListAdapter);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
                super.onActivityCreated(savedInstanceState);

                getLoaderManager().initLoader(64, null, this);

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
                Log.d(LOG_TAG, ">>>>>>> onCreateLoader");
                String[] projection = new String[]{ToBuyItemsEntry._ID, ToBuyItemsEntry.COLUMN_ITEM_ID, ToBuyItemsEntry.COLUMN_QUANTITY, Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED, ItemsEntry.COLUMN_NAME, Contract.ItemsEntry.COLUMN_BRAND, Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN, Contract.ItemsEntry.COLUMN_DESCRIPTION, Contract.PicturesEntry
                        .COLUMN_FILE_PATH, Contract.PricesEntry.COLUMN_PRICE_TYPE_ID, Contract.PricesEntry.COLUMN_PRICE, Contract.PricesEntry.COLUMN_CURRENCY_CODE};

                Uri uri = Contract.ShoppingList.CONTENT_URI;

                Set<Long> itemIds = (Set<Long>) getArguments().getSerializable(ITEM_TO_SHARE);

                Iterator<Long> iterator = itemIds.iterator();

                String subSelection = null;
                if (itemIds.size() > 0)
                {
                        subSelection = " IN (";

                        while (iterator.hasNext())
                        {
                                iterator.next();
                                subSelection += "?";
                                if (!iterator.hasNext())
                                {
                                        subSelection += ")";
                                }
                                else
                                {
                                        subSelection += ",";
                                }

                        }
                }

                //Summary screen shows only selected price
                String selection = Contract.PricesEntry.TABLE_NAME + "." + Contract.PricesEntry._ID + "=" +
                        Contract.ToBuyItemsEntry.TABLE_NAME + "." + Contract.ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID +
                        " AND " + ToBuyItemsEntry.TABLE_NAME + "." + Contract.ToBuyItemsEntry._ID + subSelection;

                //String sortPref = args.getString(SORT_COLUMN);
                String sortOrder = null;
//                if (sortPref != null)
//                {
//                        sortOrder = sortPref.equalsIgnoreCase(Contract.ItemsEntry.COLUMN_NAME) ? Contract.ItemsEntry.TABLE_NAME + "." + Contract.ItemsEntry.COLUMN_NAME : Contract.ItemsEntry.TABLE_NAME + "." + Contract.ItemsEntry.COLUMN_BRAND;
//                }

                String[] selectionArgs = new String[itemIds.size()];
                int k = 0;
                for (Long idt : itemIds)
                {
                        selectionArgs[k] = String.valueOf(idt);
                        ++k;
                }

                return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
        {
                Log.d(LOG_TAG, ">>>>>>> onLoadFinished Cursor");
                //shoppingListAdapter.swapCursor(cursor);
                displaySharedItems(cursor);
        }

        private void displaySharedItems(Cursor cursor)
        {
                while (cursor.moveToNext())
                {
                        String itemName = cursor.getString(cursor.getColumnIndex(ItemsEntry.COLUMN_NAME));
                        Log.d(LOG_TAG, "Item name = " + itemName);
                }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
                shoppingListAdapter.swapCursor(null);
        }

        public void submitShoppingListTitle()
        {
                final String uid = getUid();
                mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                                // Get user value
                                User user = dataSnapshot.getValue(User.class);

                                // [START_EXCLUDE]
                                if (user == null)
                                {
                                        // User is null, error out
                                        Log.e(LOG_TAG, "User " + uid + " is unexpectedly null");
                                        Toast.makeText(getActivity(), "Error: could not fetch user.", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                        // Write new post
                                        writeNewPost(uid, user.getUsername(), "Help me");
                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                });
        }

        private void writeNewPost(String userId, String username, String title)
        {
                // Create new post at /user-posts/$userid/$postid and at
                // /posts/$postid simultaneously
                String key = mDatabase.child("shopping_lists").push().getKey();
                ShoppingListShare post = new ShoppingListShare(userId, username, title);
                Map<String, Object> postValues = post.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/posts/" + key, postValues);
                childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

                mDatabase.updateChildren(childUpdates);
        }

        public String getUid() {
                return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        private class ShoppingListShare {

                private String userId;
                private String userName;
                private String shoppingListTitle;

                public ShoppingListShare(String userId, String userName, String shoppingListTitle)
                {
                        this.userId = userId;
                        this.userName = userName;
                        this.shoppingListTitle = shoppingListTitle;
                }

                public Map<String, Object> toMap()
                {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("userid", userId);
                        data.put("username", userName);
                        data.put("title", shoppingListTitle);
                        return data;
                }
        }


}
