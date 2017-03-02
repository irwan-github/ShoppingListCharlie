package com.mirzairwan.shopping;

import com.mirzairwan.shopping.domain.PriceMgr;

/**
 * Created by Mirza Irwan on 26/2/17.
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

        void onLoadPriceFinished(PriceMgr priceMgr);

        void onExistingItem();

        void setItemNameFieldControl(ItemEditFieldControl itemNameFieldControl);

}
