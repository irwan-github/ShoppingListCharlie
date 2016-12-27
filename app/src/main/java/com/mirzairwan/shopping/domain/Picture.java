package com.mirzairwan.shopping.domain;

import java.io.File;

/**
 * Created by Mirza Irwan on 27/12/16.
 */

public class Picture
{
    private long _id = -1L;
    private File mPictureFile;

    public Picture(long id, File pictureFile)
    {
        _id = id;
        mPictureFile = pictureFile;
    }


    public Picture(File pictureFile)
    {
        mPictureFile = pictureFile;
    }

    public long getId()
    {
        return _id;
    }

    public void setId(long id)
    {
        this._id = id;
    }

    public String getPicturePath()
    {
        return mPictureFile.getPath();
    }

    public void setFile(File pictureFile)
    {
        mPictureFile = pictureFile;
    }

    public File getFile()
    {
        return mPictureFile;
    }
}
