package com.mirzairwan.shopping;

/**
 * Created by Mirza Irwan on 26/2/17.
 *
 * Control the button and menu of the screen.
 */

public interface ItemControl
{

        void onChange();

        void onCreateOptionsMenu();

        void onOk();

        void onDelete();

        void onUp();

        void onLeave();

        void onBackPressed();

        void onStay();

        void onExistingItem();

}
