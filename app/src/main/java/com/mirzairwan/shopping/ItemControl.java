package com.mirzairwan.shopping;

import android.view.Menu;

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
}
