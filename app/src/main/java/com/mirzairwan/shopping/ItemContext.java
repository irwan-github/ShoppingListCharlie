package com.mirzairwan.shopping;

import com.mirzairwan.shopping.domain.Item;
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

        void populateItemInputFields(Item item);

        void populatePricesInputFields(PriceMgr mPriceMgr);

        void showTransientDbMessage();
}
