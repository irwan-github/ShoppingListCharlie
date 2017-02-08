package com.mirzairwan.shopping;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * Decodes and resizes bitmaps located in device filesystem
 */

public class ImageResizer extends ImageWorker
{
        private static final String LOG_TAG = ImageResizer.class.getSimpleName();
        protected int mImageWidth;
        protected int mImageHeight;


        /**
         * Initialize providing a single target image size (used for both width and height);
         *
         * @param context
         * @param imageWidth
         * @param imageHeight
         */
        public ImageResizer(Context context, int imageWidth, int imageHeight)
        {
                super(context);
                setImageSize(imageWidth, imageHeight);
        }

        /**
         * Set the target image width and height.
         *
         * @param width
         * @param height
         */
        public void setImageSize(int width, int height)
        {
                mImageWidth = width;
                mImageHeight = height;
        }

        public Bitmap decodeSampledBitmapFromDescriptor(File file, int reqWidth, int reqHeight)
        {
                return PictureUtil.decodeSampledBitmapFile(file.getPath(), reqWidth, reqHeight);
        }

        private Bitmap processBitmap(File file)
        {
                Log.d(LOG_TAG, "processBitmap(File file)");
                return decodeSampledBitmapFromDescriptor(file, mImageWidth, mImageHeight);
        }


        @Override
        protected Bitmap processBitmap(Object data)
        {
                if (data instanceof File)
                {
                        return processBitmap((File) data);
                }
                return null;
        }
}
