package com.mirzairwan.shopping;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.domain.Picture;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class CatalogAdapter extends CursorAdapter
{
        private static final String LOG_TAG = CatalogAdapter.class.getSimpleName();
        private OnPictureRequestListener mOnPictureRequestListener;
        private OnToggleCatalogItemListener mOnToggleCatalogItemListener;

        public CatalogAdapter(Context context, Cursor cursor, OnToggleCatalogItemListener onToggleCatalogItemListener, OnPictureRequestListener onPictureRequestListener)
        {
                super(context, cursor, 0);
                mOnToggleCatalogItemListener = onToggleCatalogItemListener;
                mOnPictureRequestListener = onPictureRequestListener;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
                View convertView = LayoutInflater.from(context).inflate(R.layout.row_history, parent, false);
                ImageView ivItem = (ImageView) convertView.findViewById(R.id.iv_history_pic_thb);
                TextView tvItemName = (TextView) convertView.findViewById(R.id.tv_all_item_name_row);
                TextView tvItemBrand = (TextView) convertView.findViewById(R.id.tv_all_item_brand_row);
                ToggleButton toggleItem = (ToggleButton) convertView.findViewById(R.id.toggle_buy_list);

                //Create a listener for toggle button events
                OnItemCheckedChangeListener onItemCheckedChangeListener = new OnItemCheckedChangeListener(mOnToggleCatalogItemListener);

                Tag tag = new Tag();
                tag.ivItem = ivItem;
                tag.itemName = tvItemName;
                tag.itemBrand = tvItemBrand;
                tag.toggleItem = toggleItem;
                tag.onItemCheckedChangeListener = onItemCheckedChangeListener;
                convertView.setTag(tag);

                return convertView;
        }

        @Override
        public void bindView(View convertView, Context context, Cursor cursor)
        {
                Tag tag = (Tag) convertView.getTag();

                //tag.ivItem.setImageResource(R.drawable.empty_photo);
                int colPicPath = cursor.getColumnIndex(Contract.PicturesEntry.COLUMN_FILE_PATH);
                String pathPic = cursor.getString(colPicPath);
                Log.d(LOG_TAG, ">>> bindView " + pathPic);
                setImageView(pathPic, tag.ivItem);

                int nameColIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
                tag.itemName.setText(cursor.getString(nameColIdx));
                int brandColIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_BRAND);
                tag.itemBrand.setText(cursor.getString(brandColIdx));

                //Show full star when item is in buy list. Otherwise show border-only star
                //But before that, we must unset its listener to prevent unwanted check events
                tag.toggleItem.setOnCheckedChangeListener(null);
                int colBuyIdx = cursor.getColumnIndex(Contract.ToBuyItemsEntry.ALIAS_ID);
                boolean isItemInShoppingList = !cursor.isNull(colBuyIdx);
                tag.toggleItem.setChecked(isItemInShoppingList);

                //Now give back the listener to toggle button
                tag.onItemCheckedChangeListener.setCursorPosition(cursor.getPosition());
                tag.onItemCheckedChangeListener.setItemInShoppingList(isItemInShoppingList);
                tag.toggleItem.setOnCheckedChangeListener(tag.onItemCheckedChangeListener);
        }

        private void setImageView(String pathPic, ImageView ivItem)
        {
                if (TextUtils.isEmpty(pathPic))
                {
                        ivItem.setImageResource(R.drawable.empty_photo);
                        return;
                }
                //mImageResizer.loadImage(new File(pathPic), ivItem);
                mOnPictureRequestListener.onRequest(new Picture(pathPic), ivItem);
        }

        private static class Tag
        {
                public ImageView ivItem;
                public TextView itemName;
                public TextView itemBrand;
                public ToggleButton toggleItem;
                public OnItemCheckedChangeListener onItemCheckedChangeListener;
        }

        /**
         * The function of this listener class is to fire
         * a custom event OnItemCheckedChangeListener.onToggleItem
         */
        private static class OnItemCheckedChangeListener implements CompoundButton.OnCheckedChangeListener
        {
                private int mPosition;
                private boolean isItemInShoppingList = false;

                //Interested listener to the onCheckedChanged event.
                private OnToggleCatalogItemListener mOnToggleCatalogItemListener;

                public OnItemCheckedChangeListener(OnToggleCatalogItemListener onToggleCatalogItemListener)
                {
                        mOnToggleCatalogItemListener = onToggleCatalogItemListener;
                }

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                        mOnToggleCatalogItemListener.onToggleItem(isChecked, mPosition);
                }

                /**
                 * @param position the current cursor position.
                 */
                public void setCursorPosition(int position)
                {
                        mPosition = position;
                }

                public void setItemInShoppingList(boolean itemInShoppingList)
                {
                        isItemInShoppingList = itemInShoppingList;
                }
        }
}
