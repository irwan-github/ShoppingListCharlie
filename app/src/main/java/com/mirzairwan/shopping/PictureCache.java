package com.mirzairwan.shopping;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by Mirza Irwan on 7/1/17.
 */

public class PictureCache
{
    public static LruCache<String, Bitmap> createCache()
    {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        LruCache<String, Bitmap> mThumbBitmapCache = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        return mThumbBitmapCache;
    }
}
