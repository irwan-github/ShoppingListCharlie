package com.mirzairwan.shopping.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 * <p>
 * Helper class to track all the pictures taken for an item.
 * Use the class to decide which pictures to delete and which one to save on the database
 * and the filesystem.
 */

public class PictureMgr implements Parcelable
{
        public static final Creator<PictureMgr> CREATOR = new Creator<PictureMgr>()
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
        private static final String SHOPPING_LIST_PICS = "Item_";
        private static String mAuthorityPackage = "Android/data/com.mirzairwan.shopping/files/Pictures";
        private Picture mPictureInDb; //Currently stored in database

        //Currently shown to the user. For this implementation, only one picture is allowed.
        private List<Picture> mTargetPictureForViewing = new ArrayList<>();

        private List<Picture> mDiscardedPictures = new ArrayList<>(); //To be deleted from filesystem
        private long mItemId = -1;

        public PictureMgr()
        {

        }

        private PictureMgr(Parcel in)
        {
                mItemId = in.readLong();
                in.readTypedList(mTargetPictureForViewing, Picture.CREATOR);
                in.readTypedList(mDiscardedPictures, Picture.CREATOR);
                mPictureInDb = in.readParcelable(getClass().getClassLoader());
                mAuthorityPackage = in.readString();
        }

        /**
         * Create a collision-resistant file name
         * @param dirPictures
         * @return
         * @throws IOException
         */
        public static File createFileHandle(File dirPictures) throws IOException
        {
                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                String picFilename = SHOPPING_LIST_PICS + "_" + timeStamp + "_";

                //Get file handle
                File filePicture = File.createTempFile(picFilename, ".jpg", dirPictures);

                return filePicture;
        }

        public static boolean isExternalFile(Picture picture)
        {
                return !picture.getPath().contains(mAuthorityPackage);
        }

        public long getItemId()
        {
                return mItemId;
        }

        public void setItemId(long id)
        {
                mItemId = id;
        }

        /**
         * The current picture will replaced by the new picture.
         * If the current picture is different from newPicture and if it's path is not associated with
         * the item stored in database, the current picture will be put in the discarded list.
         * Original file (exist in database records) will never be put in discarded list in this method
         * because it is NOT known whether user want to save the new picture.
         * If not different, the target picture will be ignored.
         *
         * @param newPictureFile
         */
        public void setPictureForViewing(File newPictureFile)
        {
                if (newPictureFile != null)
                {
                        setPictureForViewing(new Picture(newPictureFile));
                }
        }

        private boolean isOriginalPicture(Picture discardedPic)
        {
                if (mPictureInDb != null && mPictureInDb.getPath().equals(discardedPic.getPath()))
                {
                        return true;
                }
                else
                {
                        return false;
                }
        }

        private boolean isSameAsCurrentViewedPicture(Picture targetPicture)
        {
                Picture currentViewedPicture = null;
                if (mTargetPictureForViewing.size() == 0)
                {
                        return false;
                }
                else
                {
                        currentViewedPicture = mTargetPictureForViewing.get(0);
                }

                if (currentViewedPicture.getPath().equals(targetPicture.getPath()))
                {
                        return true;
                }
                else
                {
                        return false;
                }
        }

        public boolean hasDiscardedInternalPictures()
        {
                Iterator<Picture> iteratorDiscardPics = mDiscardedPictures.iterator();
                boolean hasInternalPic = false;
                while (iteratorDiscardPics.hasNext())
                {

                        Picture discardedPicture = iteratorDiscardPics.next();

                        if (!isExternalFile(discardedPicture) && discardedPicture.getFile() != null)
                        {
                                hasInternalPic = true;
                                break;
                        }
                }
                return hasInternalPic;
        }

        /**
         * Get picture for viewing. If target picture for viewing is empty, then original picture
         * is used. However, if original picture was deleted, then null will be returned
         *
         * @return
         */
        public Picture getPictureForViewing()
        {
                if (mTargetPictureForViewing.size() > 0)
                {
                        return mTargetPictureForViewing.get(0);
                }
                else
                {
                        return mPictureInDb;
                }
        }

        /**
         * The current picture will replaced by the new picture.
         * If the current picture is different from newPicture and if it's path is not associated with
         * the item stored in database, the current picture will be put in the discarded list.
         * Original file (exist in database records) will never be put in discarded list in this method
         * because it is NOT known whether user want to save the new picture.
         *
         * @param newPicture will be the picture to be used for viewing
         */
        public void setPictureForViewing(Picture newPicture)
        {
                if (newPicture == null)
                {
                        return;
                }

                if (isSameAsCurrentViewedPicture(newPicture))
                {
                        return;
                }

                Picture discardedPic = null;

                if (mTargetPictureForViewing.size() == 1)
                {
                        discardedPic = mTargetPictureForViewing.set(0, newPicture);
                }
                else
                {
                        mTargetPictureForViewing.add(newPicture);
                }

                if (discardedPic != null && !isOriginalPicture(discardedPic))
                {
                        mDiscardedPictures.add(discardedPic);
                }
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
         * Original picture, if exist,  wll be the pictute target picture and removed from discarded
         * pile. The current target picture is put in discarded file.
         */
        public void setViewOriginalPicture()
        {
                if (mPictureInDb == null)
                {
                        return;
                }
                setPictureForViewing(mPictureInDb);
                if (mDiscardedPictures.contains(mPictureInDb))
                {
                        mDiscardedPictures.remove(mPictureInDb);
                }
        }

        /**
         * Move original picture (path stored in database) to discarded list if it was NOT already in.
         * Remove original picture from current viewed picture
         */
        public void discardOriginalPicture()
        {
                if (mPictureInDb != null && !mDiscardedPictures.contains(mPictureInDb))
                {
                        mDiscardedPictures.add(mPictureInDb);
                }
                if (mPictureInDb != null && mTargetPictureForViewing.contains(mPictureInDb))
                {
                        mTargetPictureForViewing.remove(mPictureInDb);
                }
                mPictureInDb = null;
        }

        /**
         * Invoked when user clicks on delete button in picture toolbar
         * Move picture to discarded list if it was NOT already in, Original picture will also be
         * put in the discarded list
         * The current picture for viewing is removed.
         * If current picture is original picture, then original picture will not exist anymore.
         */
        public Picture discardCurrentPictureInView()
        {
                Picture pictureForViewing = getPictureForViewing();
                if (pictureForViewing != null && !mDiscardedPictures.contains(pictureForViewing) && !isOriginalPicture(pictureForViewing))
                {
                        mDiscardedPictures.add(pictureForViewing);
                        mTargetPictureForViewing.remove(pictureForViewing);
                }
                if (isOriginalPicture(pictureForViewing))
                {
                        discardOriginalPicture();
                }

                return pictureForViewing;
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
}
