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

        void setTitle(int stringResId);

        void showTransientDbMessage();

        View findViewById(int resId);

        String getString(int stringResId);

        void invalidateOptionsMenu();

        Transition inflateTransition(int transitionResId);

        String getDefaultCountryCode();
}