package com.mirzairwan.shopping;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.domain.Item;

/**
 * Created by Mirza Irwan on 2/1/17.
 */

public class ItemEditingActivity2 extends ItemActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // do extra stuff on your resources, using findViewById on your layout_for_activity1
        setTitle(R.string.view_buy_item_details);
        Intent intent = getIntent();
        Uri uri = intent.getData();

        initLoaders(uri);
        setupPictureToolbar();

    }

    @Override
    protected int getActivityLayout()
    {
        return R.layout.activity_item_editing;
    }

    @Override
    protected void initLoaders(Uri uri)
    {
        Bundle arg = new Bundle();
        arg.putParcelable(ITEM_URI, uri);
        getLoaderManager().initLoader(ITEM_LOADER_ID, arg, this);
        super.initLoaders(uri);
    }

    @Override
    protected void delete()
    {
        if (item.isInBuyList()) {
            alertItemInShoppingList(R.string.item_is_in_shopping_list);
            return;
        }

        String results = daoManager.delete(item, pictureMgr);

        Toast.makeText(this, results, Toast.LENGTH_SHORT).show();

        finish();

    }

    @Override
    protected void save()
    {
        Item item = getItemFromInputField();

        priceMgr.setItemPricesForSaving(item, getUnitPriceFromInputField(), getBundlePriceFromInputField(), getBundleQtyFromInputField());

        String msg = daoManager.update(item, item.getPrices(), pictureMgr);

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        String[] projection = null;
        Uri uri = args.getParcelable(ITEM_URI);
        Loader<Cursor> loader = null;
        itemId = -1;

        switch (loaderId) {
            case ITEM_LOADER_ID:
                projection = new String[]{
                        Contract.ItemsEntry._ID,
                        Contract.ItemsEntry.COLUMN_NAME,
                        Contract.ItemsEntry.COLUMN_BRAND,
                        Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                        Contract.ItemsEntry.COLUMN_DESCRIPTION,
                };
                itemId = ContentUris.parseId(uri);
                loader = new CursorLoader(this, uri, projection, null, null, null);
                break;

            default:
                super.onCreateLoader(loaderId, args);

        }
        return loader;
    }

}
