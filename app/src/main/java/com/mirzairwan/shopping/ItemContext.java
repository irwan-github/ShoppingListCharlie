package com.mirzairwan.shopping;

import android.transition.Transition;
import android.view.View;

/**
 * Created by Mirza Irwan on 26/2/17.
 */

public interface ItemContext
{
        void warnChangesMade();

        void finishItemEditing();

        void setExitTransition();

        //boolean areFieldsValid();

        void setMenuVisible(int id, boolean isVisible);

        void setTitle(int resourceId);

        void showTransientDbMessage();

        View findViewById(int resId);

        String getString(int resId);

        void invalidateOptionsMenu();

        Transition inflateTransition(int isExpand);
}
