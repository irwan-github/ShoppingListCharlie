package com.mirzairwan.shopping.firebase;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mirzairwan.shopping.OnPictureRequestListener;
import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Mirza Irwan on 25/1/17.
 */

public class SendShareFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
        public static final String ITEM_TO_SHARE = "ITEM_TO_SHARE";
        private static final String SORT_COLUMN = "SORT_COLUMN";
        private static final String LOG_TAG = SendShareFragment.class.getSimpleName();
        public static final String SHAREE_EMAIL = "SHAREE_EMAIL" ;
        private OnPictureRequestListener mOnPictureRequestListener;
        private DatabaseReference mRootRef;
        private FirebaseUser mFirebaserUser;
        private ArrayList<Item> mItemsToShare = new ArrayList<>();
        private ProgressDialog mProgressDialog;

        public static SendShareFragment getInstance(HashSet<Long> ids, String shareeEmail)
        {
                SendShareFragment shareFragment = new SendShareFragment();
                Bundle args = new Bundle();
                args.putSerializable(ITEM_TO_SHARE, ids);
                args.putString(SHAREE_EMAIL, shareeEmail);
                shareFragment.setArguments(args);
                return shareFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                mFirebaserUser = FirebaseAuth.getInstance().getCurrentUser();
                mRootRef = FirebaseDatabase.getInstance().getReference();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage("Loading...");
                return mProgressDialog;
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
                String[] projection = new String[]{ToBuyItemsEntry._ID, ToBuyItemsEntry.COLUMN_ITEM_ID, ToBuyItemsEntry.COLUMN_QUANTITY, ToBuyItemsEntry.COLUMN_IS_CHECKED, ItemsEntry.COLUMN_NAME, ItemsEntry.COLUMN_BRAND, ItemsEntry.COLUMN_COUNTRY_ORIGIN, ItemsEntry.COLUMN_DESCRIPTION, Contract.PicturesEntry
                        .COLUMN_FILE_PATH, PricesEntry.COLUMN_PRICE_TYPE_ID, PricesEntry.COLUMN_PRICE, PricesEntry.COLUMN_CURRENCY_CODE};

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
                String selection = PricesEntry.TABLE_NAME + "." + PricesEntry._ID + "=" +
                        ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID +
                        " AND " + ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry._ID + subSelection;

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

                return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
        {
                Log.d(LOG_TAG, ">>>>>>> onLoadFinished Cursor");
                prepareSharedItems(cursor);
                String shareeEmail = getArguments().getString(SHAREE_EMAIL);
                submitShoppingList(shareeEmail);
        }

        private void prepareSharedItems(Cursor cursor)
        {
                while (cursor.moveToNext())
                {
                        String itemName = cursor.getString(cursor.getColumnIndex(ItemsEntry.COLUMN_NAME));
                        String brand = cursor.getString(cursor.getColumnIndex(ItemsEntry.COLUMN_BRAND));
                        int colSelectedPriceTagIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);
                        long priceTag = cursor.getLong(colSelectedPriceTagIdx);
                        int colCurrencyCodeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
                        String currencyCode = cursor.getString(colCurrencyCodeIdx);
                        int colBuyItemQty = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
                        int qty = cursor.getInt(colBuyItemQty);
                        //mItemsToShareAdapter.add(itemName);
                        mItemsToShare.add(new Item(mFirebaserUser.getEmail(), mFirebaserUser.getUid(), itemName, brand, priceTag, currencyCode, qty));
                        Log.d(LOG_TAG, "Item name = " + itemName);
                }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
        }

        private void submitShoppingList(String userEmail)
        {
                Log.d(LOG_TAG, ">>> submitShoppingList");
                // Create new shoppingList at shoppingLists/$shoppingListId simultaneously

                Query userQuery = getTargetUser(userEmail);

                userQuery.addListenerForSingleValueEvent(new ValueEventListener()
                {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                                String key = dataSnapshot.getKey();
                                Log.d(LOG_TAG, ">>> The key is  " + key);
                                Log.d(LOG_TAG, ">>> The value is  " + dataSnapshot.getValue());

                                Iterable<DataSnapshot> childen = dataSnapshot.getChildren();

                                String shareeUserId = null;

                                for (DataSnapshot data : childen)
                                {
                                        Log.d(LOG_TAG, ">>> The children key  is  " + data.getKey()); //This is the key to json user object
                                        shareeUserId = data.getKey();
                                        Log.d(LOG_TAG, ">>> The children value  is  " + data.getValue());

                                }

                                if (!TextUtils.isEmpty(shareeUserId))
                                {
                                        saveToCloud(shareeUserId);
                                }
                                else
                                {
                                        Toast.makeText(getActivity(), "This user does not have an account ", Toast.LENGTH_LONG).show();
                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                });
        }

        private void saveToCloud(String shareeUserId)
        {
                final DatabaseReference shoppingListShareWithRef = mRootRef.child("shopping_list_share_with").child(shareeUserId);

                final int countItemsToShare = mItemsToShare.size();

                if(countItemsToShare == 0)
                        return;

                shoppingListShareWithRef.runTransaction(new Transaction.Handler()
                {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData)
                        {
                                for (int j = 0; j < countItemsToShare; ++j)
                                {
                                        // Push the item, it will appear in the list
                                        try
                                        {
                                                shoppingListShareWithRef.push().setValue(mItemsToShare.get(j));
                                                Log.d(LOG_TAG, ">>>First push key " + shoppingListShareWithRef.getKey());
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
                                if(databaseError == null)
                                {
                                        mProgressDialog.setMessage("Completed!");
                                }
                                else
                                {
                                        Log.d(LOG_TAG, ">>>  databaseError.getDetails  " + databaseError.getDetails());
                                        Log.d(LOG_TAG, ">>> databaseError.getMessage " + databaseError.getMessage());
                                        Log.d(LOG_TAG, ">>> databaseError.getCode " + databaseError.getCode());
                                }
                                mItemsToShare.clear();
                                SendShareFragment.this.getActivity().finish();
                        }
                });
        }

        private Query getTargetUser(String userEmail)
        {
                //Get RootRef
                DatabaseReference rootRef = mRootRef;

                //Nest down to users. So users is our parent key
                DatabaseReference users = rootRef.child("users");

                Query usersQuery = users.orderByChild("email");

                Query user = usersQuery.equalTo(userEmail).limitToFirst(1);
                return user;
        }

        public Query getQuery(DatabaseReference databaseReference)
        {
                // All my posts
                return databaseReference.child("user-posts").child(getUid());
        }

        public String getUid()
        {
                return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

}
