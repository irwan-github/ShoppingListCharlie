package com.mirzairwan.shopping;

import android.transition.Transition;
import android.view.View;

import com.mirzairwan.shopping.domain.Picture;

import java.io.File;

/**
 * Created by Mirza Irwan on 26/2/17.
 */

public interface ItemContext
{
        void warnChangesMade();

        void finishItemEditing();

        void setExitTransition();

        void setTitle(int stringResId);

        void showTransientDbMessage();

        View findViewById(int resId);

        String getString(int stringResId);

        void invalidateOptionsMenu();

        Transition inflateTransition(int transitionResId);

        String getDefaultCountryCode();

        void removeUnsavedPicturesFromApp();

        void setPictureMenuItemEnabled(int menuId, boolean enabled);

        void setPictureMenuItemVisible(int menuId, boolean visible);

        void showSoftKeyboard(View view);

        void startSnapShotActivity(File file);

        void setPictureView(Picture picture);

        File getExternalFilesDir(String environment);

        void startPickPictureActivity();
}
