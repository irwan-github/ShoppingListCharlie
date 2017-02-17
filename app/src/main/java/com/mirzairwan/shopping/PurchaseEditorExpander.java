package com.mirzairwan.shopping;

import android.app.Activity;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public class PurchaseEditorExpander extends DetailExpander
{
        public PurchaseEditorExpander(Activity activity)
        {
                super(activity);
        }

        @Override
        protected int getViewGroupId()
        {
                return R.id.purchase_details_more;
        }

        @Override
        protected int getToggleButtonId()
        {
                return R.id.btn_toggle_purchase;
        }

}
