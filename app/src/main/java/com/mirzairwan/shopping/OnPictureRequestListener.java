package com.mirzairwan.shopping;

import android.widget.ImageView;

import com.mirzairwan.shopping.domain.Picture;

/**
 * Created by Mirza Irwan on 9/1/17.
 */

public interface OnPictureRequestListener
{
    public void onRequest(Picture picture, ImageView ivItem);
}
