package com.mirzairwan.shopping.data;

import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ToBuyItem;

import java.util.List;

/**
 * Created by Mirza Irwan on 17/12/16.
 */

public interface DaoManager
{
    /**
     * Insert the state of the entire objects that is referenced
     * by the BuyItem object in the domain object graph into the database.
     * The above changes to the database must be committed in a same single transaction.
     * @param buyItem item in the shopping list
     * @param item Details of the item
     * @param itemPrices Prices of the item
     * @return message
     */
    String insert(ToBuyItem buyItem, Item item, List<Price> itemPrices);
}
