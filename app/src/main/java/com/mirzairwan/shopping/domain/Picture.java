package com.mirzairwan.shopping.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 *  * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * A picture must have a file path and a File object associated with it.
 */

public class Picture implements Parcelable
{
        public static final Creator<Picture> CREATOR = new Creator<Picture>()
        {
                public Picture createFromParcel(Parcel in)
                {
                        return new Picture(in);
                }

                public Picture[] newArray(int size)
                {
                        return new Picture[size];
                }
        };
        private long _id = -1L;
        private File mPictureFile;
        private String mPath;

        public Picture(long id, File pictureFile)
        {
                _id = id;
                mPictureFile = pictureFile;
                mPath = pictureFile.getPath();
                if (mPath == null)
                {
                        throw new IllegalArgumentException("Picture cannot have empth path");
                }

        }


        public Picture(long id, String pictureFilePath)
        {
                _id = id;
                mPath = pictureFilePath;
                mPictureFile = new File(pictureFilePath);
                if (mPath == null)
                {
                        throw new IllegalArgumentException("Picture cannot have empth path");
                }

        }

        public Picture(File pictureFile)
        {
                mPictureFile = pictureFile;
                mPath = pictureFile.getPath();
                if (mPath == null)
                {
                        throw new IllegalArgumentException("Picture cannot have empth path");
                }

        }


        public Picture(String pictureFilePath)
        {
                mPath = pictureFilePath;
                if (mPath == null)
                {
                        throw new IllegalArgumentException("Picture cannot have empty path");
                }
                mPictureFile = new File(pictureFilePath);
        }

        private Picture(Parcel in)
        {
                _id = in.readLong();
                mPath = in.readString();
                mPictureFile = (File) in.readSerializable();
        }

        public String getPath()
        {
                if (mPath == null)
                {
                        throw new IllegalArgumentException("Picture cannot have empth path");
                }
                return mPath;
        }

        public void setPath(String path)
        {
                this.mPath = path;
                mPictureFile = new File(path);
                if (mPath == null)
                {
                        throw new IllegalArgumentException("Picture cannot have empth path");
                }

        }

        public long getId()
        {
                return _id;
        }

        public void setId(long id)
        {
                this._id = id;
        }

        /**
         * Get absolute path of picture file
         * @return
         */
        public String getPicturePath()
        {
                if (mPath == null)
                {
                        throw new IllegalArgumentException("Picture cannot have empth path");
                }
                return mPath;
        }

        public File getFile()
        {
                return mPictureFile;

        }

        public void setFile(File pictureFile)
        {
                mPictureFile = pictureFile;
                mPath = pictureFile.getPath();
                if (mPath == null)
                {
                        throw new IllegalArgumentException("Picture cannot have empth path");
                }

        }

        @Override
        public int describeContents()
        {
                return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
                dest.writeLong(_id);
                dest.writeString(mPath);
                dest.writeSerializable(mPictureFile);
        }
}
