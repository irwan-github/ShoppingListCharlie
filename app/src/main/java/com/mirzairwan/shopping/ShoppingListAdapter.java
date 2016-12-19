package com.mirzairwan.shopping;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.mirzairwan.shopping.ShoppingListFragment.OnFragmentInteractionListener;
import com.mirzairwan.shopping.data.ShoppingListContract.ItemsEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.PricesEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.ToBuyItemsEntry;

import java.util.Locale;

/**
 * Created by Mirza Irwan on 18/12/16.
 */

public class ShoppingListAdapter extends CursorAdapter
{
    private static final String LOG_TAG = ShoppingListAdapter.class.getSimpleName();
    private OnFragmentInteractionListener mOnFragmentInteractionListener;

    public ShoppingListAdapter(Context context, Cursor cursor, OnFragmentInteractionListener onFragmentInteractionListener)
    {
        super(context, cursor, 0);
        mOnFragmentInteractionListener = onFragmentInteractionListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.row_buy_item, parent, false);
    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor)
    {
        if (convertView.getTag() == null) {
            TagBuyItemViews tag = new TagBuyItemViews();
            tag.tvItemName = (TextView) convertView.findViewById(R.id.item_name_row);
            tag.tvItemBrand = (TextView) convertView.findViewById(R.id.item_brand_row);
            tag.tvSelectedPrice = (TextView) convertView.findViewById(R.id.item_selected_price_row);
            tag.tvItemQty = (TextView) convertView.findViewById(R.id.item_qty_row);
            tag.checkItem = (CheckBox) convertView.findViewById(R.id.check_item);

            //Create a listener for checkbox checked events
            OnItemCheckedChangeListener onItemCheckedChangeListener =
                    new OnItemCheckedChangeListener(mOnFragmentInteractionListener);

            tag.onItemCheckedChangeListener = onItemCheckedChangeListener;
            convertView.setTag(tag);
        }

        TagBuyItemViews tagViews = (TagBuyItemViews)convertView.getTag();

        int colNameIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
        tagViews.tvItemName.setText(cursor.getString(colNameIdx));

        int colBrandIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_BRAND);
        tagViews.tvItemBrand.setText(cursor.getString(colBrandIdx));

        tagViews.checkItem.setOnCheckedChangeListener(null); //Disable checkbox listener to disable firing pre-existing checked items on the buy list
        int colIsChecked = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_IS_CHECKED);
        tagViews.checkItem.setChecked(cursor.getInt(colIsChecked) == 1); //Now it is safe to associate the 'checked' status of buy item with the checkbox
        tagViews.onItemCheckedChangeListener.setItemPosition(cursor.getPosition()); //Associate the listener with the position on the shopping list
        tagViews.checkItem.setOnCheckedChangeListener(tagViews.onItemCheckedChangeListener); //Enable checkbox listener
        tagViews.checkItem.setFocusable(false);

        int colCurrencyCodeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
        String currencyCode = cursor.getString(colCurrencyCodeIdx);
        int colSelectedPriceTagIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);
        double priceTag = cursor.getDouble(colSelectedPriceTagIdx);

        SharedPreferences prefs = context.getSharedPreferences(ShoppingActivity.PERSONAL,
                                        Activity.MODE_PRIVATE);
        String countryCode = prefs.getString(ShoppingActivity.HOME_COUNTRY_CODE,
                                                        Locale.getDefault().getCountry());
        //Log.d(LOG_TAG, ">>>Default Country " + Locale.getDefault().getCountry());
        tagViews.tvSelectedPrice.setText(NumberFormatter.formatCountryCurrency(countryCode,
                                                                     currencyCode, priceTag/100));

        int colBuyItemQty = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
        tagViews.tvItemQty.setText(String.valueOf(cursor.getInt(colBuyItemQty)));
        tagViews.tvItemQty.setFocusable(false);



    }

    private static class TagBuyItemViews
    {
        TextView tvItemName;
        TextView tvItemBrand;
        TextView tvItemQty;
        CheckBox checkItem;
        OnItemCheckedChangeListener onItemCheckedChangeListener;
        public TextView tvSelectedPrice;
    }

    private static class OnItemCheckedChangeListener implements CompoundButton.OnCheckedChangeListener
    {
        private OnFragmentInteractionListener mOnFragmentInteractionListener;
        private int mBuyItemPosition;

        public OnItemCheckedChangeListener(OnFragmentInteractionListener onFragmentInteractionListener)
        {
            mOnFragmentInteractionListener = onFragmentInteractionListener;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            mOnFragmentInteractionListener.onCheckBuyItem(isChecked, mBuyItemPosition);
        }

        public void setItemPosition(int position)
        {
            mBuyItemPosition = position;
        }
    }
}
