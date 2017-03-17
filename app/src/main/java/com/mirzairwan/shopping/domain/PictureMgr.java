package com.mirzairwan.shopping.domain;

import com.mirzairwan.shopping.data.DaoManager;

import java.io.File;

/**
 * Created by Mirza Irwan on 12/3/17.
 */

public class PictureMgr
{
        private static String mAuthorityPackage = "Android/data/com.mirzairwan.shopping/files/Pictures";
        private DaoManager mDaoContentProv;

        /* Currently stored in database */
        private Picture mPictureInDb;

        /* New Picture */
        private Picture mNewPicture;

        private long mItemId = -1;
        private boolean mIsPictureinDbForDelete = false;

        public PictureMgr(DaoManager daoContentProv)
        {
                mDaoContentProv = daoContentProv;
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
        public void setNewPicture(File newPictureFile)
        {
                /* Delete existing picture except original picture.*/
                if (mNewPicture != null && mNewPicture.getId() == -1)
                {
                        deleteNewPicture();
                }

                if (newPictureFile != null)
                {
                        mNewPicture = new Picture(newPictureFile);
                }
                else
                {
                        mNewPicture = null;
                }
        }

        /**
         * The current new picture will replaced by the new picture. The current new picture will be deleted from the filesystem.
         *
         * @param newPicture will be the picture to replace exisiting new picture
         */
        public void setNewPicture(Picture newPicture)
        {
                if (mNewPicture != null && mNewPicture.getId() == -1)
                {
                        deleteNewPicture();
                }

                mNewPicture = newPicture;
        }

        public void deleteNewPicture()
        {
                /* Only delete file owned by this app */
                if (!isExternalFile(mNewPicture))
                {
                        int deletedInFs = mDaoContentProv.deleteFileFromFilesystem(mNewPicture.getFile());
                }

                mNewPicture = null;
        }

        public boolean isPictureInDb(String path)
        {
                return path.equals(mPictureInDb.getPath());
        }

        /**
         * @return Picture stored in database
         */
        public Picture getPictureInDb()
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
        public void setDbPicture(Picture pictureInDb)
        {
                mPictureInDb = pictureInDb;
        }

        /**
         * Get picture for viewing. If target picture for viewing is empty, then original picture
         * is used. However, if original picture was deleted, then null will be returned
         *
         * @return
         */
        public Picture getNewPicture()
        {
                return mNewPicture;
        }

        public void setPictureInDbForDelete(boolean isDelete)
        {
                mIsPictureinDbForDelete = isDelete;
        }

        public boolean isPictureInDbToBeDeleted()
        {
                return mIsPictureinDbForDelete;
        }
}
