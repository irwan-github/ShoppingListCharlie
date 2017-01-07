package com.mirzairwan.shopping;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.io.File;

/**
 * Created by Mirza Irwan on 4/1/17.
 */

public class ImageResizer extends ImageWorker
{
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
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageResizer(Context context, int imageWidth, int imageHeight, LruCache<String, Bitmap> thumbBitmapCache)
    {
        super(context, thumbBitmapCache);
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
        return PictureUtil.decodeSampledBitmap(file.getPath(), reqWidth, reqHeight);
    }

    @Override
    protected Bitmap processBitmap(File file)
    {
        return decodeSampledBitmapFromDescriptor(file, mImageWidth, mImageHeight);

    }
}
