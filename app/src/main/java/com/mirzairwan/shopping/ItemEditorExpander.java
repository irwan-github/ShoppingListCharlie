package com.mirzairwan.shopping;

import android.app.Activity;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public class ItemEditorExpander extends DetailExpander
{
        public ItemEditorExpander(Activity activity)
        {
                super(activity);
        }

        // Get the root view to create a transition
        @Override
        protected int getViewGroupId()
        {
                return R.id.item_details_more;
        }

        @Override
        protected int getToggleButtonId()
        {
                return R.id.btn_toggle_item;
        }

}