package com.mirzairwan.shopping;

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

    @Override
    public ToBuyItem addNewItem(String itemName, String itemBrand, String itemDescription, int quantityToBuy, String currencyCode, double unitPrice, double bundlePrice, double bundleQty, Price.Type selectedPriceType)
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

    private ToBuyItem buyItem(Item item, int quantityToBuy, Price selectedPrice)
    {
        ToBuyItem buyItem = new ToBuyItem(item, quantityToBuy, selectedPrice);
        return buyItem;
    }
}
