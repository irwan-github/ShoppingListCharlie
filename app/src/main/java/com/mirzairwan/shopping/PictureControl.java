package com.mirzairwan.shopping;

import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.PictureMgr;

import java.io.File;
import java.io.IOException;

import static com.mirzairwan.shopping.PictureControl.Event.ON_CAMERA_ACTION;
import static com.mirzairwan.shopping.PictureControl.Event.ON_CAMERA_RESULT;
import static com.mirzairwan.shopping.PictureControl.Event.ON_DELETE_PICTURE;
import static com.mirzairwan.shopping.PictureControl.Event.ON_LOAD_PICTURE;
import static com.mirzairwan.shopping.PictureControl.Event.ON_PICK_PICTURE_ACTION;
import static com.mirzairwan.shopping.PictureControl.Event.ON_PICK_PICTURE_RESULT;
import static com.mirzairwan.shopping.PictureControl.PictureState.NO_PICTURE;

/**
 * Created by Mirza Irwan on 15/3/17.
 */

public class PictureControl
{
        private PictureState mPictureState = NO_PICTURE;
        private ImageView mImageViewItem;
        private ItemContext mItemContext;
        private PictureMgr mPictureMgr;

        public PictureControl(ItemContext context)
        {
                mItemContext = context;
                mImageViewItem = (ImageView) context.findViewById(R.id.img_item);
        }

        public void setPictureMgr(PictureMgr pictureMgr)
        {
                mPictureMgr = pictureMgr;
        }

        public void onLoadFinished(PictureMgr pictureMgr)
        {
                mPictureMgr = pictureMgr;
                mPictureState = mPictureState.transition(ON_LOAD_PICTURE, this);
        }

        private void setPictureMenuItemEnabled(int menuResId, boolean enabled)
        {
                mItemContext.setPictureMenuItemEnabled(menuResId, enabled);
        }

        public void onCameraAction()
        {
                mPictureState = mPictureState.transition(ON_CAMERA_ACTION, this);
        }

        public void onCameraResult()
        {
                mPictureState = mPictureState.transition(ON_CAMERA_RESULT, this);
        }

        private void processPicture()
        {
                mItemContext.setPictureView(mPictureMgr.getNewPicture());
        }

        private void startSnapShotActivity()
        {
                File itemPicFile;
                try
                {
                        /*
                         * This method returns a standard location for saving pictures and videos which are associated with your application.
                         * If your application is uninstalled, any files saved in this location are removed.
                         * Security is not enforced for files in this location and other applications may read, change and delete them.
                         * However, DAC also states that beginning with Android 4.4, the permission is no longer required because the directory is not accessible by other apps ....
                         * On Nexus 5, the storage directory returned is "/storage/emulated/0/Android/data/com.mirzairwan.shopping/files/Pictures"
                         */
                        File externalFilesDir = mItemContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                        /*
                                File for use with ACTION_VIEW intents.
                                 File path is /storage/emulated/0/Android/data/com.mirzairwan.shopping/files/Pictures/Item__***_***_-***.jpg
                        */
                        itemPicFile = PictureUtil.createFileHandle(externalFilesDir);
                        mPictureMgr.setNewPicture(itemPicFile);
                }
                catch(IOException e)
                {
                        e.printStackTrace();
                        //Toast.makeText(this, "Photo file cannot be created. Aborting camera operation", Toast.LENGTH_SHORT).show();
                        return;
                }

                mItemContext.startSnapShotActivity(itemPicFile);
        }

        private void setPictureMenuItemVisible(int menuResId, boolean visible)
        {
                mItemContext.setPictureMenuItemVisible(menuResId, visible);
        }

        public void onDeletePictureInView()
        {
                mPictureState = mPictureState.transition(ON_DELETE_PICTURE, this);
        }

        private void setDbPictureInView()
        {
                mItemContext.setPictureView(mPictureMgr.getPictureInDb());
        }

        private void setNewPictureInView()
        {
                mItemContext.setPictureView(mPictureMgr.getNewPicture());
        }

        /**
         * Invoked when user clicks on delete button in picture toolbar
         * if picture's path exist in the database, the picture file is not deleted from filesystem. It is only marked for deletion. It will be deleted from filesystem when
         * update operation is performed.
         * If the picture does not exist in database and picture is written to storage memory by this app, the image file is deleted.
         * If the currently view picture is not the database picture, the database picture will be used
         * for viewing if exist.
         */
        private void deletePictureInView()
        {
                String path = mImageViewItem.getTag().toString();

                if (mPictureMgr.isPictureInDb(path))
                {
                        mItemContext.setPictureView(null);
                        mPictureMgr.setPictureInDbForDelete(true);
                }
                else
                {
                        /* Delete from filesystem */
                        mPictureMgr.deleteNewPicture();
                        Picture pictureInDb = mPictureMgr.getPictureInDb();
                        mItemContext.setPictureView(pictureInDb);
                }
        }

        public void onPickPictureAction()
        {
                mPictureState = mPictureState.transition(ON_PICK_PICTURE_ACTION, this);
        }

        public void onPickPictureResult(Picture picture)
        {
                mPictureMgr.setNewPicture(picture);
                mPictureState = mPictureState.transition(ON_PICK_PICTURE_RESULT, this);
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {

        }

        public PictureMgr getPictureMgr()
        {
                return mPictureMgr;
        }

        enum Event
        {
                ON_LOAD_PICTURE, ON_CAMERA_ACTION, ON_CAMERA_RESULT, ON_DELETE_PICTURE, ON_PICK_PICTURE_ACTION, ON_PICK_PICTURE_RESULT
        }

        enum PictureState
        {
                NO_PICTURE
                        {
                                @Override
                                PictureState transition(Event event, PictureControl control)
                                {
                                        PictureState state = this;
                                        switch (event)
                                        {
                                                case ON_LOAD_PICTURE:
                                                        control.setDbPictureInView();
                                                        state = PICTURE_EXIST;
                                                        break;
                                                case ON_CAMERA_ACTION:
                                                        control.startSnapShotActivity();
                                                        break;
                                                case ON_PICK_PICTURE_ACTION:
                                                        control.startPickPictureActivity();
                                                        break;
                                                case ON_CAMERA_RESULT:
                                                        control.processPicture();
                                                        state = PICTURE_EXIST;
                                                        break;
                                                case ON_PICK_PICTURE_RESULT:
                                                        control.setNewPictureInView();
                                                        state = PICTURE_EXIST;
                                                        break;
                                                default:
                                                        state = NO_PICTURE;
                                        }

                                        state.setUiAttribute(event, control);
                                        return state;
                                }

                                @Override
                                void setUiAttribute(Event event, PictureControl control)
                                {
                                        control.setPictureMenuItemEnabled(R.id.remove_picture, false);
                                        control.setPictureMenuItemVisible(R.id.remove_picture, false);
                                }
                        },

                PICTURE_EXIST
                        {
                                PictureState state = this;

                                @Override
                                PictureState transition(Event event, PictureControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_CAMERA_ACTION:
                                                        control.startSnapShotActivity();
                                                        break;
                                                case ON_CAMERA_RESULT:
                                                        control.processPicture();
                                                        state = PICTURE_EXIST;
                                                        break;
                                                case ON_PICK_PICTURE_ACTION:
                                                        control.startPickPictureActivity();
                                                        break;
                                                case ON_PICK_PICTURE_RESULT:
                                                        control.setNewPictureInView();
                                                        state = PICTURE_EXIST;
                                                        break;
                                                case ON_DELETE_PICTURE:
                                                        control.deletePictureInView();
                                                        state = NO_PICTURE;
                                                        break;
                                                default:
                                                        state = PICTURE_EXIST;
                                        }

                                        state.setUiAttribute(event, control);
                                        return state;
                                }

                                void setUiAttribute(Event event, PictureControl control)
                                {
                                        control.setPictureMenuItemEnabled(R.id.remove_picture, true);
                                        control.setPictureMenuItemVisible(R.id.remove_picture, true);
                                }
                        };

                void setUiAttribute(Event event, PictureControl control)
                {

                }

                abstract PictureState transition(Event event, PictureControl control);
        }



        private void startPickPictureActivity()
        {
                mItemContext.startPickPictureActivity();
        }
}