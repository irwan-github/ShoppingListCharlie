package com.mirzairwan.shopping.firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mirzairwan.shopping.R;

import java.util.List;

/**
 * Created by Mirza Irwan on 27/1/17.
 */

public class ShareeShoppingListAdapter extends ArrayAdapter<Item>
{
        public ShareeShoppingListAdapter(Context context, List<Item> items)
        {
                super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
               if(convertView == null )
               {
                       Tag tag = new Tag();
                       convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_shared_shopping_item, parent, false);
                       tag.tvItemName = (TextView)convertView.findViewById(R.id.remote_item_name);
                       convertView.setTag(tag);
               }

                Tag tag = (Tag)convertView.getTag();
                tag.tvItemName.setText(getItem(position).getItem());
                return convertView;
        }

        private static class Tag
        {
                TextView tvItemName;
        }
}
