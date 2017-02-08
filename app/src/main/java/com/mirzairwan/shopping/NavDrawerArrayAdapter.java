package com.mirzairwan.shopping;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class NavDrawerArrayAdapter extends ArrayAdapter<NavItem>
{
    public NavDrawerArrayAdapter(Context context, int resource, NavItem[] objects)
    {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.nav_drawer_row, parent, false);
        }

        ImageView iv = (ImageView)convertView.findViewById(R.id.iv_nav);
        TextView tvSetting = (TextView)convertView.findViewById(R.id.tv_nav);

        NavItem navItem = getItem(position);

        iv.setImageResource(navItem.drawable);
        tvSetting.setText(navItem.text);
        return convertView;
    }
}
