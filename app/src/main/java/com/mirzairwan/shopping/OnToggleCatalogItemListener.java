package com.mirzairwan.shopping;

/**
 * Created by Mirza Irwan on 6/12/16.
 * Handles shopping items checked/unchecked events
 */
public interface OnToggleCatalogItemListener
{
    void onToggleItem(boolean isItemChecked, int position);
}
