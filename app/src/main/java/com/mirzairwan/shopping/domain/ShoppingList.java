package com.mirzairwan.shopping.domain;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public interface ShoppingList
{
    public ItemInShoppingList addNewItem(Item item, int quantityToBuy, Price selectedPrice);

    /**
     * Add item from catalogue to "shopping list"
     *
     * @param itemId of item in catalogue
     */
    ItemInShoppingList buyItem(int itemId);

    /**
     * Remove item from shopping list
     * @param position
     * @return
     */
    ItemInShoppingList removeItem(int position);

}
