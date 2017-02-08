package com.mirzairwan.shopping;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Performs the following long running task:
 * 1. Spawn a new thread
 * 2. Setting a placeholder image for given bitmap.
 * 3. Retrieves imaga from cache if image is available
 */

public abstract class ImageWorker
{
        protected Resources mResources;
        protected LruCache<String, Bitmap> mBitmapCache;
        private Bitmap mPlaceHolderBitmap;

        protected ImageWorker(Context context)
        {
                mResources = context.getResources();
                mBitmapCache = PictureCache.createCache();
                mPlaceHolderBitmap = BitmapFactory.decodeResource(mResources, R.drawable.empty_photo);
        }

        public static boolean cancelPotentialWork(Object data, ImageView imageView)
        {
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (bitmapWorkerTask != null)
                {
                        final Object bitmapData = bitmapWorkerTask.mData;
                        // If bitmapData is not yet set or it differs from the new data
                        if (bitmapData == null || !bitmapData.equals(data))
                        {
                                // Cancel previous task
                                bitmapWorkerTask.cancel(true);
                        }
                        else
                        {
                                // The same work is already in progress.
                                return false;
                        }
                }
                // No task associated with the ImageView, or an existing task was cancelled
                return true;

        }

        /**
         * A helper used to retrieve the task associated with a particular ImageView:
         *
         * @param imageView
         * @return
         */
        private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView)
        {
                if (imageView != null)
                {
                        final Drawable drawable = imageView.getDrawable();
                        if (drawable instanceof AsyncDrawable)
                        {
                                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                                return asyncDrawable.getBitmapWorkerTask();
                        }
                }
                return null;
        }

        /**
         * Load inage from memory cache if available.
         * Spin a background thread to load an image specified by the data parameter into an ImageView if unavailable in memory cache.
         *
         * @param file      The URL of the image to download.
         * @param imageView The ImageView to bind the downloaded image to.
         */
        public void loadImage(File file, ImageView imageView)
        {
                if (file == null || !file.exists())
                {
                        return;
                }

                Bitmap bitmapFromMemCache = getBitmapFromMemCache(file.getPath());
                if (bitmapFromMemCache != null)
                {
                        imageView.setImageBitmap(bitmapFromMemCache);
                        return;
                }

                if (cancelPotentialWork(file, imageView))
                {
                        BitmapWorkerTask task = new BitmapWorkerTask(file, imageView);
                        final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mPlaceHolderBitmap, task);
                        imageView.setImageDrawable(asyncDrawable);
                        task.execute();
                }
        }

        public void addBitmapToMemoryCache(String key, Bitmap bitmap)
        {
                if (mBitmapCache != null && getBitmapFromMemCache(key) == null)
                {
                        mBitmapCache.put(key, bitmap);
                }
        }

        public Bitmap getBitmapFromMemCache(String key)
        {
                if (mBitmapCache != null)
                {
                        return mBitmapCache.get(key);
                }
                else
                {
                        return null;
                }
        }

        /**
         * Subclasses should override this to define any processing or work that must happen to produce
         * the final bitmap. This will be executed in a background thread and be long running. For
         * example, you could resize a large bitmap here, or pull down an image from the network.
         *
         * @param data The data to identify which image to process, as provided by
         * @return The processed bitmap
         */
        protected abstract Bitmap processBitmap(Object data);

        /**
         * Called when the processing is complete and the final drawable should be
         * set on the ImageView.
         *
         * @param imageView
         * @param bitmap
         */
        private void setImageDrawable(ImageView imageView, Bitmap bitmap)
        {
                imageView.setImageBitmap(bitmap);
        }

        static class AsyncDrawable extends BitmapDrawable
        {
                private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

                public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask)
                {
                        super(res, bitmap);
                        bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
                }

                public BitmapWorkerTask getBitmapWorkerTask()
                {
                        return bitmapWorkerTaskReference.get();
                }
        }

        class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap>
        {
                private final WeakReference<ImageView> imageViewReference;
                private Object mData;

                public BitmapWorkerTask(Object data, ImageView imageView)
                {
                        // Use a WeakReference to ensure the ImageView can be garbage collected
                        imageViewReference = new WeakReference<>(imageView);
                        mData = data;
                }

                @Override
                protected Bitmap doInBackground(Void... params)
                {
                        Bitmap bitmap = processBitmap(mData);
                        String key = String.valueOf(mData);
                        addBitmapToMemoryCache(key, bitmap);
                        return bitmap;
                }

                // Once complete, see if ImageView is still around and set bitmap.
                @Override
                protected void onPostExecute(Bitmap bitmap)
                {
                        if (imageViewReference != null && bitmap != null)
                        {
                                final ImageView imageView = imageViewReference.get();
                                if (imageView != null)
                                {
                                        setImageDrawable(imageView, bitmap);
                                }
                        }

                }
        }
}
