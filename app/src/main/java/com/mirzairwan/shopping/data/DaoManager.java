package com.mirzairwan.shopping.data;

import com.mirzairwan.shopping.domain.PictureMgr;
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
     * Create a purchase item listed in the catalog.
     * Pre-condition: Item and prices must already exist in the database.
     * @param buyItem
     * @return long primary key of item in shopping list
     */
    long insert(ToBuyItem buyItem);

    /**
     * Insert the state of the entire objects that is referenced
     * by the BuyItem object in the domain object graph into the database.
     * The above changes is an atomic transaction so must be committed in same transaction.
     * @param buyItem item in the shopping list
     * @param item Details of the item
     * @param itemPrices Prices of the item
     * @return message
     */
    String insert(ToBuyItem buyItem, Item item, List<Price> itemPrices, PictureMgr pictureMgr);

    /**
     * Delete item in the shopping list. Item, pictures and prices in catalogue is NOT deleted.
     * @param buyItem
     * @return The number of rows deleted.
     */
    int delete(ToBuyItem buyItem);

    /**
     * Update purchase, item, picture and prices details
     * The above changes is an atomic transaction so must be committed in same transaction.
     * @param buyItem
     * @param item
     * @param itemPrices
     * @param pictureMgr tracks item's updated and discarded or replaced pictures
     * @return
     */
    String update(ToBuyItem buyItem, Item item, List<Price> itemPrices, PictureMgr pictureMgr);

    /**
     * Save user action of checking the item in the shopping list
     * @param buyItemId primary key of the purchase item in the shopping list
     * @param isChecked indicates whether the item is checked in the shopping list
     * @return
     */
    String update(long buyItemId, boolean isChecked);

    /**
     * Delete item in catalogue and its child records in other tables.
     * @param item
     * @param pictureMgr tracks item's updated and discarded or replaced pictures
     * @return
     */
    String delete(Item item, PictureMgr pictureMgr);

    /**
     * Update item and its child records in other tables.
     * The above changes is an atomic transaction so must be committed in same transaction.
     * @param item
     * @param prices
     * @param pictureMgr tracks item's updated and discarded or replaced pictures
     * @return
     */
    String update(Item item, List<Price> prices, PictureMgr pictureMgr);

    /**
     * Delete photos in the filesystem.
     * @param pictureMgr tracks item's updated and discarded or replaced pictures
     * @return
     */
    String cleanUpDiscardedPictures(PictureMgr pictureMgr);

}
