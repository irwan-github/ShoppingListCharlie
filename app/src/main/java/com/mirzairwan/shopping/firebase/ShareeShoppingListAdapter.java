package com.mirzairwan.shopping.firebase;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.mirzairwan.shopping.MyTextUtils;
import com.mirzairwan.shopping.R;

import java.util.ArrayList;

/**
 * Created by Mirza Irwan on 27/1/17.
 */

public class ShareeShoppingListAdapter extends ArrayAdapter<Item>
{
        private static final String LOG_TAG = ShareeShoppingListAdapter.class.getSimpleName();
        private final String mCountryCode;
        private DatabaseReference mDatabaseRef;

        public ShareeShoppingListAdapter(Context context, DatabaseReference databaseReference)
        {
                super(context, 0, new ArrayList<Item>());
                mCountryCode = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("home_country_preference", null);
                mDatabaseRef = databaseReference;

                ChildEventListener childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot itemSnapshot, String s)
                        {
                                Log.d(LOG_TAG, ">>>onChildAdded: " + itemSnapshot.getKey());
                                Log.d(LOG_TAG, ">>>onChildAdded: " + itemSnapshot.getValue());
                                Item sharedItem = itemSnapshot.getValue(Item.class);
                                sharedItem.setKey(itemSnapshot.getKey());
                                add(sharedItem);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s)
                        {
                                Log.d(LOG_TAG, ">>>onChildChanged: " + dataSnapshot.getKey());
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot itemSnapshot)
                        {
                                Log.d(LOG_TAG, ">>>onChildRemoved: " + itemSnapshot.getKey());
                                Item sharedItem = itemSnapshot.getValue(Item.class);
                                sharedItem.setKey(itemSnapshot.getKey());
                                remove(sharedItem);
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s)
                        {
                                Log.d(LOG_TAG, ">>>onChildMoved: " + dataSnapshot.getKey());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                                Log.d(LOG_TAG, ">>>onCancelled");
                        }
                };

                mDatabaseRef.addChildEventListener(childEventListener);
        }



        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
               if(convertView == null )
               {
                       Tag tag = new Tag();
                       convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_shared_shopping_item, parent, false);
                       tag.tvItemName = (TextView)convertView.findViewById(R.id.item_name_row);
                       tag.tvItemBrand = (TextView)convertView.findViewById(R.id.item_brand_row);
                       tag.tvItemOriginator = (TextView)convertView.findViewById(R.id.item_originator) ;
                       tag.tvSelectedPrice = (TextView)convertView.findViewById(R.id.item_selected_price_row);
                       tag.checkItem = (ToggleButton) convertView.findViewById(R.id.check_item);
                       convertView.setTag(tag);
               }

                Tag tag = (Tag)convertView.getTag();
                Item item = getItem(position);
                tag.tvItemName.setText(item.getName());
                tag.tvItemBrand.setText(item.getBrand());

                tag.checkItem.setText(String.valueOf(item.getQuantity()));
                tag.checkItem.setTextOn(String.valueOf(item.getQuantity()));
                tag.checkItem.setTextOff(String.valueOf(item.getQuantity()));

                MyTextUtils.setPrice(getContext(), item.getCurrencyCode(), item.getPrice(), tag.tvSelectedPrice);

                String sharedBy = getContext().getString(R.string.item_shared_by) + " " + item.getEmailOfOriginator();
                tag.tvItemOriginator.setText(sharedBy);
                return convertView;
        }

        private static class Tag
        {
                TextView tvItemName;
                TextView tvItemBrand;
                TextView tvItemOriginator;
                TextView tvSelectedPrice;
                ToggleButton checkItem;
        }
}
