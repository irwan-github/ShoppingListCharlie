package com.mirzairwan.shopping;

import android.database.Cursor;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ShoppingList;
import com.mirzairwan.shopping.domain.ToBuyItem;

/**
 * Created by Mirza Irwan on 17/12/16.
 */

public class ShoppingCursorList implements ShoppingList
{
    private Cursor mCatalogueCursor;
    private long defaultShopId = 1;

    public ShoppingCursorList()
    {
    }

    public ShoppingCursorList(Cursor cursor)
    {
        mCatalogueCursor = cursor;
    }

//    @Override
//    public ToBuyItem addNewItem(String itemName, String itemBrand, String itemDescription, String countryOrigin, int quantityToBuy, String currencyCode, double unitPrice, double bundlePrice, double bundleQty, Price.Type selectedPriceType)
//    {
//        String msg = "Added to shopping list.";
//        if (quantityToBuy < 1) {
//            throw new IllegalArgumentException("Buy Quantity " + quantityToBuy + " is invalid: it must at least 1");
//        }
//
//        if (currencyCode == null)
//            throw new IllegalArgumentException("Currency code cannot be empty");
//
//        Item item = new Item(itemName);
//        item.setBrand(itemBrand);
//        item.setDescription(itemDescription);
//        item.setCountryOrigin(countryOrigin);
//
//        if (selectedPriceType == null)
//            selectedPriceType = Price.Type.UNIT_PRICE;
//
//        Price unitPriceFinal = new Price(unitPrice, currencyCode, defaultShopId);
//        Price bundlePriceFinal = new Price(bundlePrice, bundleQty, currencyCode, defaultShopId);
//        return addNewItem(item, quantityToBuy, unitPriceFinal, bundlePriceFinal, selectedPriceType);
//
//}

    @Override
    public ToBuyItem addNewItem(Item item, int quantityToBuy, Price selectedPrice)
    {
        return buyItem(item, quantityToBuy, selectedPrice);
    }

    @Override
    public ToBuyItem buyItem(int position)
    {
        mCatalogueCursor.moveToPosition(position);
        long itemid = mCatalogueCursor.getLong(mCatalogueCursor.getColumnIndex(ItemsEntry._ID));
        Item item = new Item("");
        item.setId(itemid);

        long priceId = mCatalogueCursor.getLong(mCatalogueCursor.getColumnIndex(Contract.PricesEntry.ALIAS_ID));
        Price price = new Price(priceId);

        ToBuyItem buyItem = new ToBuyItem(item, 1, price);
        return buyItem;
    }

    @Override
    public ToBuyItem removeItem(int position)
    {
        mCatalogueCursor.moveToPosition(position);
        int colBuyItemIdIdx = mCatalogueCursor.getColumnIndex(ToBuyItemsEntry.ALIAS_ID);
        long buyItemId = mCatalogueCursor.getLong(colBuyItemIdIdx);

        return new ToBuyItem(buyItemId);
    }

    private ToBuyItem buyItem(Item item, int quantityToBuy, Price selectedPrice)
    {
        ToBuyItem buyItem = new ToBuyItem(item, quantityToBuy, selectedPrice);
        return buyItem;
    }

}
