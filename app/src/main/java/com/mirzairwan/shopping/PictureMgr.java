package com.mirzairwan.shopping;

import com.mirzairwan.shopping.domain.Picture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mirza Irwan on 29/12/16.
 */

public class PictureMgr
{
    private Picture mPictureInDb; //Currently stored in database
    private List<Picture> mPictures = new ArrayList<>(); //Currently shown to the user. For this implementation, only one picture is allowed.
    private List<Picture> mDiscardedPictures = new ArrayList<>(); //To be deleted from filesystem
    private long itemId = -1;

    public PictureMgr()
    {

    }

    public PictureMgr(Picture pictureInDb)
    {
//        mPictureInDb = pictureInDb;
//        if (mPictures.size() == 1)
//            mPictures.set(0, mPictureInDb);
//        else
//            mPictures.add(pictureInDb);
        setOriginalPicture(pictureInDb);
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
     * @param targetPicture
     */

    public void setPictureForViewing(Picture targetPicture)
    {
        Picture discardedPic = null;
        if (mPictures.size() == 1)
            discardedPic = mPictures.set(0, targetPicture);
        else
            mPictures.add(targetPicture);

        if (discardedPic != null)
            mDiscardedPictures.add(discardedPic);
    }


    public Picture getPictureForViewing()
    {
        if (mPictures.size() > 0)
            return mPictures.get(0);
        else
            return null;
    }

    public List<Picture> getPictureForSaving()
    {
        return mPictures;
    }


    public List<Picture> getDiscardedPictures()
    {
        return mDiscardedPictures;
    }

    public Picture getOriginalPicture()
    {
        return mPictureInDb;
    }


    public void setOriginalPicture(Picture pictureInDb)
    {
//        mPictureInDb = pictureInDb;

        mPictureInDb = pictureInDb;
        if (mPictures.size() == 1)
            mPictures.set(0, mPictureInDb);
        else
            mPictures.add(pictureInDb);

    }

    /**
     * Original file wll be the pictute target picture and removed from discarded pile
     */
    public void resetToOriginalPicture()
    {
        if (mPictureInDb == null)
            return;
        setPictureForViewing(mPictureInDb);
        mDiscardedPictures.remove(mPictureInDb);
    }

}
