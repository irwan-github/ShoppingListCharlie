package com.mirzairwan.shopping.domain;

/**
 * Created by Mirza Irwan on 27/12/16.
 */

public class Picture
{
    private long _id;
    private String mPicturePath;

    public Picture(long _id, String picturePath)
    {
        this._id = _id;
        this.mPicturePath = picturePath;
    }

    public Picture(String picturePath)
    {
        this.mPicturePath = picturePath;
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
        return mPicturePath;
    }

    public void setPicturePath(String mPicturePath)
    {
        this.mPicturePath = mPicturePath;
    }
}
