package com.mirzairwan.shopping;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 *
 * Handles shopping items checked/unchecked events
 */
public interface OnToggleCatalogItemListener
{
    void onToggleItem(boolean isItemChecked, int position);
}
