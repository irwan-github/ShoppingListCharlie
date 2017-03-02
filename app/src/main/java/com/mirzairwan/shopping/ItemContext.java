package com.mirzairwan.shopping;

import android.view.View;

import com.mirzairwan.shopping.domain.PriceMgr;

/**
 * Created by Mirza Irwan on 26/2/17.
 */

public interface ItemContext
{
        void warnChangesMade();

        void finishItemEditing();

        void setExitTransition();

        boolean areFieldsValid();

        void setMenuVisible(int id, boolean isVisible);

        void setTitle(int resourceId);

        void populatePricesInputFields(PriceMgr mPriceMgr);

        void showTransientDbMessage();

        View findViewById(int resId);

        String getString(int resId);

        void invalidateOptionsMenu();
}
