package com.mirzairwan.shopping;

import android.widget.ImageView;

import com.mirzairwan.shopping.domain.Picture;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public interface OnPictureRequestListener
{
    public void onRequest(Picture picture, ImageView ivItem);
}
