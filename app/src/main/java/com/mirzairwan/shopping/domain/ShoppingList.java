package com.mirzairwan.shopping.domain;

/**
 * Created by Mirza Irwan on 21/11/16.
 */

public interface ShoppingList
{
    /**
     * Add new item to buy list and catalog. This item is assumed to be not pre-exisitng.
     *
     * @param itemName
     * @param itemBrand
     * @param itemDescription
     * @param quantityToBuy
     * @param unitPrice
     * @param bundlePrice
     * @param bundleQty         Quantity to buy. Value must at least be 1.
     * @param selectedPriceType
     * @return ToBuyItem item to buy
     */
    ToBuyItem createItem(String itemName, String itemBrand, String itemDescription, int quantityToBuy, String currencyCode, double unitPrice, double bundlePrice, double bundleQty, Price.Type selectedPriceType);

    /**
     * Add item from catalogue to "shopping list"
     *
     * @param itemId of item in all items list
     */
    ToBuyItem buyItem(int itemId);

    /**
     * Remove item from shopping list
     * @param position
     * @return
     */
    ToBuyItem removeItem(int position);
}
