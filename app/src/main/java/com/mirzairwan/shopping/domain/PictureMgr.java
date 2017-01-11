package com.mirzairwan.shopping.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mirza Irwan on 29/12/16.
 * <p>
 * Helper class to track all the pictures taken for an item.
 * Use the class to decide which pictures to delete and which one to save on the database
 * and the filesystem.
 */

public class PictureMgr implements Parcelable
{
    private Picture mPictureInDb; //Currently stored in database
    private List<Picture> mTargetPictureForViewing = new ArrayList<>(); //Currently shown to the user. For this implementation, only one picture is allowed.
    private List<Picture> mDiscardedPictures = new ArrayList<>(); //To be deleted from filesystem
    private long mItemId = -1;
    private static final String SHOPPING_LIST_PICS = "Item_";
    private String mAuthorityPackage = null;

    public PictureMgr(String authorityPackage)
    {
        mAuthorityPackage = authorityPackage;
    }

    /**
     * @param pictureInDb will be the picture used for viewing initially.
     * @param itemId
     */
    public PictureMgr(Picture pictureInDb, long itemId, String authorityPackage)
    {
        mPictureInDb = pictureInDb;
        mItemId = itemId;
        setPictureForViewing(mPictureInDb);
    }

    public PictureMgr(long itemId, String authorityPackage)
    {
        mItemId = itemId;
    }

    public long getItemId()
    {
        return mItemId;
    }

    /**
     * The current picture will replaced by the new picture.
     * If the current picture is different from newPicture,
     * the current picture will be put in the discarded list.
     * If not different, the target picture will be ignored.
     *
     * @param newPictureFile
     */
    public void setPictureForViewing(File newPictureFile)
    {
        if (newPictureFile != null)
            setPictureForViewing(new Picture(newPictureFile));
    }

    /**
     * The current picture will replaced by the new picture.
     * If the current picture is different from newPicture,
     * the current picture will be put in the discarded list.
     * If not different, the target picture will be ignored.
     *
     * @param newPicture will be the picture to be used for viewing
     */
    public void setPictureForViewing(Picture newPicture)
    {
        if(newPicture == null)
            return;

        if (sameAsCurrentViewedPicture(newPicture))
            return;

        Picture discardedPic = null;

        if (mTargetPictureForViewing.size() == 1)
            discardedPic = mTargetPictureForViewing.set(0, newPicture);
        else
            mTargetPictureForViewing.add(newPicture);

        if (discardedPic != null)
            mDiscardedPictures.add(discardedPic);
    }

    private boolean sameAsCurrentViewedPicture(Picture targetPicture)
    {
        Picture currentViewedPicture = null;
        if(mTargetPictureForViewing.size() == 0)
            return false;
        else
            currentViewedPicture = mTargetPictureForViewing.get(0);

        if (currentViewedPicture.getPath().equals(targetPicture.getPath()))
            return true;
        else
            return false;
    }


    /**
     * Get picture for viewing
     *
     * @return
     */
    public Picture getPictureForViewing()
    {
        if (mTargetPictureForViewing.size() > 0)
            return mTargetPictureForViewing.get(0);
        else
            return mPictureInDb;
    }

    public List<Picture> getPictureForSaving()
    {
        return mTargetPictureForViewing;
    }


    public List<Picture> getDiscardedPictures()
    {
        return mDiscardedPictures;
    }

    /**
     * @return Picture stored in database
     */
    public Picture getOriginalPicture()
    {
        return mPictureInDb;
    }

    /**
     * Sets the original picture associated with the item and its path stored in database.
     * It does not replace the picture for viewing.
     * To use thid picture for viewng, call setViewOriginalPicture after calling this method.
     *
     * @param pictureInDb
     */
    public void setOriginalPicture(Picture pictureInDb)
    {
        mPictureInDb = pictureInDb;
    }

    /**
     * Original picture, if exist,  wll be the pictute target picture and removed from discarded pile
     */
    public void setViewOriginalPicture()
    {
        if (mPictureInDb == null)
            return;
        setPictureForViewing(mPictureInDb);
        if (mDiscardedPictures.contains(mPictureInDb))
            mDiscardedPictures.remove(mPictureInDb);
    }

    public static File createFileHandle(File dirPictures) throws IOException
    {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String picFilename = SHOPPING_LIST_PICS + "_" + timeStamp + "_";

        //Get file handle
        File filePicture = File.createTempFile(picFilename, ".jpg", dirPictures);

        return filePicture;
    }


    public void setItemId(long id)
    {
        mItemId = id;
    }

    /**
     * Move original picture (path stored in database) to discarded list if it was NOT already in.
     * Remove original picture from vurrent viewed picture
     */
    public void discardOriginalPicture()
    {
        if (mPictureInDb != null && !mDiscardedPictures.contains(mPictureInDb))
        {
            mDiscardedPictures.add(mPictureInDb);
        }
        if (mPictureInDb != null &&
                mTargetPictureForViewing.contains(mPictureInDb))
        {
            mTargetPictureForViewing.remove(mPictureInDb);
        }
    }

    /**
     * Move picture to discarded list if it was NOT already in.
     * The current picture for viewing is removed.
     */
    public Picture discardCurrentPictureInView()
    {
        Picture pictureForViewing = getPictureForViewing();
        if (pictureForViewing != null && !mDiscardedPictures.contains(pictureForViewing))
        {
            mDiscardedPictures.add(pictureForViewing);
            mTargetPictureForViewing.clear();
        }
        return pictureForViewing;
    }

    public boolean isExternalFile(Picture picture)
    {
        return !picture.getPath().contains(mAuthorityPackage);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(mItemId);
        dest.writeTypedList(mTargetPictureForViewing);
        dest.writeTypedList(mDiscardedPictures);
        dest.writeParcelable(mPictureInDb, flags);
        dest.writeString(mAuthorityPackage);
    }

    public static final Creator<PictureMgr> CREATOR
            = new Creator<PictureMgr>()
    {
        public PictureMgr createFromParcel(Parcel in)
        {
            return new PictureMgr(in);
        }

        public PictureMgr[] newArray(int size)
        {
            return new PictureMgr[size];
        }
    };

    private PictureMgr(Parcel in)
    {
        mItemId = in.readLong();
        in.readTypedList(mTargetPictureForViewing, Picture.CREATOR);
        in.readTypedList(mDiscardedPictures, Picture.CREATOR);
        mPictureInDb = in.readParcelable(getClass().getClassLoader());
        mAuthorityPackage = in.readString();
    }

}
