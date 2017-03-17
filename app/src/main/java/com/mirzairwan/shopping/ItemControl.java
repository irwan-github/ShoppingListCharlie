package com.mirzairwan.shopping;

import android.view.Menu;

import com.mirzairwan.shopping.domain.Picture;
import com.mirzairwan.shopping.domain.PictureMgr;

/**
 * Created by Mirza Irwan on 26/2/17.
 *
 * User's UI-events generated from the buttons and menu of the screen will invoke this interface.
 */

public interface ItemControl
{
        void onChange();

        void onCreateOptionsMenu(Menu menu);

        void onOk();

        void onDelete();

        void onUp();

        void onLeave();

        void onBackPressed();

        void onStay();

        void onCameraAction();

        void onLoadPictureFinished(PictureMgr picture);

        void onCameraResult();

        void onDeletePictureInView();

        void onPickPictureAction();

        void onPickPictureResult(Picture picture);

        void onEnterTransitionEnd();
}
