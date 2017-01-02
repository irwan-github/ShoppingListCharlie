package com.mirzairwan.shopping.domain;

/**
 * Created by Mirza Irwan on 21/11/16.
 */

public interface ShoppingList
{
//    /**
//     * Add new item to shopping list and catalog. This item is assumed to be not pre-exisitng.
//     *
//     * @param itemName
//     * @param itemBrand
//     * @param itemDescription
//     * @param quantityToBuy
//     * @param unitPrice
//     * @param bundlePrice
//     * @param bundleQty Quantity to buy. Value must at least be 1.
//     * @param selectedPriceType
//     * @return ToBuyItem item to buy
//     */
//    ToBuyItem addNewItem(String itemName, String itemBrand, String itemDescription, String countryOrigin, int quantityToBuy, String currencyCode, double unitPrice, double bundlePrice, double bundleQty, Price.Type selectedPriceType);

    public ToBuyItem addNewItem(Item item, int quantityToBuy, Price selectedPrice);

    /**
     * Add item from catalogue to "shopping list"
     *
     * @param itemId of item in catalogue
     */
    ToBuyItem buyItem(int itemId);

    /**
     * Remove item from shopping list
     * @param position
     * @return
     */
    ToBuyItem removeItem(int position);

}
