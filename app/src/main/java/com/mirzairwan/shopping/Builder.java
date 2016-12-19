package com.mirzairwan.shopping;

import android.content.Context;
import android.database.Cursor;

import com.mirzairwan.shopping.data.DaoContentProv;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.ShoppingList;

/**
 * Created by Mirza Irwan on 18/12/16.
 */

public class Builder
{
    static DaoManager getDaoManager(Context context)
    {
        return new DaoContentProv(context);
    }

    public static ShoppingList getShoppingList(Cursor c)
    {
        return new ShoppingCursorList(c);
    }

    public static ShoppingList getShoppingList()
    {
        return new ShoppingCursorList();
    }
}
