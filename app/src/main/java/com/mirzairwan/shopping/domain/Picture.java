package com.mirzairwan.shopping.domain;

import java.io.File;

/**
 * A picture must have a file path and a File object associated with it.
 * Created by Mirza Irwan on 27/12/16.
 */

public class Picture
{
    private long _id = -1L;
    private File mPictureFile;
    private String mPath;

    public Picture(long id, File pictureFile)
    {
        _id = id;
        mPictureFile = pictureFile;
        mPath = pictureFile.getPath();
        if(mPath == null)
            throw new IllegalArgumentException("Picture cannot have empth path");

    }

    public Picture(long id, String pictureFilePath)
    {
        _id = id;
        mPath = pictureFilePath;
        mPictureFile = new File(pictureFilePath);
        if(mPath == null)
            throw new IllegalArgumentException("Picture cannot have empth path");

    }


    public Picture(File pictureFile)
    {
        mPictureFile = pictureFile;
        mPath = pictureFile.getPath();
        if(mPath == null)
            throw new IllegalArgumentException("Picture cannot have empth path");

    }

    public Picture(String pictureFilePath)
    {
        mPath = pictureFilePath;
        if(mPath == null)
            throw new IllegalArgumentException("Picture cannot have empty path");
        mPictureFile = new File(pictureFilePath);
    }


    public String getPath()
    {
        if(mPath == null)
            throw new IllegalArgumentException("Picture cannot have empth path");
        return mPath;
    }

    public void setPath(String path)
    {
        this.mPath = path;
        mPictureFile = new File(path);
        if(mPath == null)
            throw new IllegalArgumentException("Picture cannot have empth path");

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
        if(mPath == null)
            throw new IllegalArgumentException("Picture cannot have empth path");
        return mPath;
    }

    public void setFile(File pictureFile)
    {
        mPictureFile = pictureFile;
        mPath = pictureFile.getPath();
        if(mPath == null)
            throw new IllegalArgumentException("Picture cannot have empth path");

    }

    public File getFile()
    {
        return mPictureFile;

    }

}
