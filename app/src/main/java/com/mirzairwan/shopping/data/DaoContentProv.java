package com.mirzairwan.shopping.data;


import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.mirzairwan.shopping.data.ShoppingListContract.ItemsEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.PricesEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.ToBuyItemsEntry;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ToBuyItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mirza Irwan on 18/12/16.
 */

public class DaoContentProv implements DaoManager
{
    private static final String LOG_TAG = DaoContentProv.class.getSimpleName();
    private Context mContext;

    public DaoContentProv(Context context)
    {
        mContext = context;
    }

    @Override
    public String insert(ToBuyItem buyItem, Item item, List<Price> itemPrices)
    {
        Log.d(LOG_TAG, "Save domain object graph");
        String msg = "";
        ContentProviderResult[] result;
        Date updateTime = new Date();

        ContentValues itemValues = new ContentValues();
        itemValues = getItemContentValues(item, itemValues);

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder itemBuilder =
                ContentProviderOperation.newInsert(ItemsEntry.CONTENT_URI);

        ContentProviderOperation itemInsertOp = itemBuilder.withValues(itemValues).build();

        ops.add(itemInsertOp);

        for (int j = 0; j < itemPrices.size(); ++j)
        {
            Price price = itemPrices.get(j);
            ContentProviderOperation.Builder priceBuilder =
                    ContentProviderOperation.newInsert(PricesEntry.CONTENT_URI);

            long itemId = -1; //The item id does not exist at this point.
            ContentValues priceContentValues = getPriceContentValues(price, itemId, updateTime, null);

            priceBuilder = priceBuilder.withValues(priceContentValues).
                    withValueBackReference(PricesEntry.COLUMN_ITEM_ID, 0);

            ops.add(priceBuilder.build());
        }

        ContentProviderOperation.Builder buyItemBuilder =
                ContentProviderOperation.newInsert(ToBuyItemsEntry.CONTENT_URI);

        buyItemBuilder = buyItemBuilder.withValues(getBuyItemContentValues(buyItem, updateTime))
                .withValueBackReference(ToBuyItemsEntry.COLUMN_ITEM_ID, 0);

        for(int k = 0; k < itemPrices.size(); ++k)
        {
            Price price = itemPrices.get(k);
            Price.Type selectedPriceType = buyItem.getSelectedPriceType();
            if(selectedPriceType == price.getPriceType())
                buyItemBuilder = buyItemBuilder
                        .withValueBackReference(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, k + 1);
        }

        ContentProviderOperation opBuyItem = buyItemBuilder.build();
        ops.add(opBuyItem);

        try {
            result = mContext.getContentResolver()
                    .applyBatch(ShoppingListContract.CONTENT_AUTHORITY, ops);
            msg = result.toString();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        return msg;
    }

    private ContentValues getItemContentValues(Item item, ContentValues values) {
        if (values == null)
            values = new ContentValues();
        values.put(ItemsEntry.COLUMN_NAME, item.getName());
        values.put(ItemsEntry.COLUMN_BRAND, item.getBrand());
        values.put(ItemsEntry.COLUMN_COUNTRY_ORIGIN, item.getCountryOrigin());
        values.put(ItemsEntry.COLUMN_DESCRIPTION, item.getDescription());

        return values;
    }

    private ContentValues getPriceContentValues(Price price, long itemId, Date updateTime, ContentValues values) {
        ContentValues priceValues = values;
        if (priceValues == null)
            priceValues = new ContentValues();


        if (price.getPriceType() == Price.Type.UNIT_PRICE) {
            priceValues.put(PricesEntry.COLUMN_PRICE, (long) (price.getUnitPrice() * 100));
            priceValues.put(PricesEntry.COLUMN_PRICE_TYPE_ID, Price.Type.UNIT_PRICE.getType());
        } else {
            priceValues.put(PricesEntry.COLUMN_PRICE, (long) (price.getBundlePrice() * 100));
            priceValues.put(PricesEntry.COLUMN_BUNDLE_QTY,
                    (long) (price.getBundleQuantity() * 100));
            priceValues.put(PricesEntry.COLUMN_PRICE_TYPE_ID, Price.Type.BUNDLE_PRICE.getType());
        }

        int shopId = 1;
        priceValues.put(PricesEntry.COLUMN_SHOP_ID, shopId);

        if (itemId > 0)
            priceValues.put(PricesEntry.COLUMN_ITEM_ID, itemId);

        priceValues.put(PricesEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());

        priceValues.put(PricesEntry.COLUMN_CURRENCY_CODE, price.getCurrencyCode());
        return priceValues;
    }

    private ContentValues getBuyItemContentValues(ToBuyItem buyItem, Date updateTime) {
        ContentValues buyItemValues = new ContentValues();
        if(buyItem.getItem().getId() > 0)
            buyItemValues.put(ToBuyItemsEntry.COLUMN_ITEM_ID, buyItem.getItem().getId());
        buyItemValues.put(ToBuyItemsEntry.COLUMN_QUANTITY, buyItem.getQuantity());
        if(buyItem.getSelectedPrice().getId() > 0)
            buyItemValues.put(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID,
                                    buyItem.getSelectedPrice().getId());
        buyItemValues.put(ToBuyItemsEntry.COLUMN_IS_CHECKED, buyItem.isChecked());
        buyItemValues.put(ToBuyItemsEntry.COLUMN_LAST_UPDATED_ON, updateTime.getTime());
        return buyItemValues;
    }
}
