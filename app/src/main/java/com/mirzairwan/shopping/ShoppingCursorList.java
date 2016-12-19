package com.mirzairwan.shopping;

import android.database.Cursor;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ShoppingList;
import com.mirzairwan.shopping.domain.ToBuyItem;

import java.util.Currency;

/**
 * Created by Mirza Irwan on 17/12/16.
 */

public class ShoppingCursorList implements ShoppingList
{
    private Cursor mCatalogueCursor;

    public ShoppingCursorList()
    {
    }

    public ShoppingCursorList(Cursor cursor)
    {
        mCatalogueCursor = cursor;
    }

    @Override
    public ToBuyItem createItem(String itemName, String itemBrand, String itemDescription, int quantityToBuy, String currencyCode, double unitPrice, double bundlePrice, double bundleQty, Price.Type selectedPriceType)
    {
        String msg = "Added to shopping list.";
        if (quantityToBuy < 1) {
            throw new IllegalArgumentException("Buy Quantity " + quantityToBuy + " is invalid: it must at least 1");
        }
        Item item = new Item(itemName);
        item.setBrand(itemBrand);
        item.setDescription(itemDescription);

        Currency currency = Currency.getInstance(currencyCode);
        int shopId = 1;

        Price unitPriceFinal = new Price(unitPrice, currency.getCurrencyCode(), shopId);
        item.addPrice(unitPriceFinal);

        Price bundlePriceFinal = new Price(bundlePrice, bundleQty, currency.getCurrencyCode(), shopId);
        item.addPrice(bundlePriceFinal);


        Price selectedPrice;
        if(selectedPriceType == null)
            selectedPriceType = Price.Type.UNIT_PRICE;

        if (selectedPriceType == Price.Type.UNIT_PRICE)
            selectedPrice = unitPriceFinal;
        else
            selectedPrice = bundlePriceFinal;

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

//    public void setCatalogueCursor(Cursor catalogueCursor)
//    {
//        mCatalogueCursor = catalogueCursor;
//    }
}
