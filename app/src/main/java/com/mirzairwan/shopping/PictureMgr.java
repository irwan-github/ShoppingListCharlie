package com.mirzairwan.shopping;

import com.mirzairwan.shopping.domain.Picture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mirza Irwan on 29/12/16.
 * <p>
 * Helper class to track all the pictures taken.
 * Use the class to decide which pictures to delete and which one to save on the database
 * and the filesystem.
 */

public class PictureMgr
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
     * The current picture will be put in the discarded list
     *
     * @param pictureFile
     */
    public void setPictureForViewing(File pictureFile)
    {
        if (pictureFile != null)
            setPictureForViewing(new Picture(pictureFile));
    }

    /**
     * The current picture will be put in the discarded list
     *
     * @param targetPicture will be the picture used for viewing
     */
    public void setPictureForViewing(Picture targetPicture)
    {
        Picture discardedPic = null;
        if (mTargetPictureForViewing.size() == 1)
            discardedPic = mTargetPictureForViewing.set(0, targetPicture);
        else
            mTargetPictureForViewing.add(targetPicture);

        if (discardedPic != null)
            mDiscardedPictures.add(discardedPic);
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

    public Picture getOriginalPicture()
    {
        return mPictureInDb;
    }

    /**
     * Sets the original picture. It does not replace the picture for viewing.
     * To replace the picture for viewng, call viewOriginalPicture after calling this method.
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

    protected File createFileHandle(File dirPictures) throws IOException
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

    public void discardOriginalPicture()
    {
        if (mPictureInDb != null && !mDiscardedPictures.contains(mPictureInDb))
            mDiscardedPictures.add(mPictureInDb);
    }

    public void discardLastViewedPicture()
    {
        if (getPictureForViewing() != null)
            mDiscardedPictures.add(getPictureForViewing());
    }

    public void setExternalPictureForViewing(String filePath)
    {
        Picture externalPicture = new Picture(filePath);
        setPictureForViewing(externalPicture);
    }

    public boolean isExternalFile(Picture picture)
    {
        return !picture.getPath().contains(mAuthorityPackage);
    }
}
