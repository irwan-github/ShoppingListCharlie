package com.mirzairwan.shopping.data;

import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.PictureMgr;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ItemInShoppingList;

import java.io.File;
import java.util.List;

/**
 * Created by Mirza Irwan on 18/12/16.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public interface DaoManager
{
         /**
         *  Create a purchase item listed in the history.
         * Pre-condition: Item and prices must already exist in the history.
         * @param itemId
         * @param priceId
         * @return
         */
        long insert(long itemId, long priceId);

        /**
         * Insert the state of the objects  into the database.
         * The above changes is an atomic transaction which means all database operations must be committed in same transaction.
         *
         * @param buyItem    item in the shopping list
         * @param item       Details of the item
         * @param itemPrices Prices of the item
         * @param pictureMgr Pictures associated with the item
         * @return message
         */
        String insert(ItemInShoppingList buyItem, Item item, List<Price> itemPrices, PictureMgr pictureMgr);

        /**
         * Delete item in the shopping list. Item, pictures and prices  are NOT deleted.
         *
         * @param buyItemId
         * @return The number of rows deleted.
         */
        int delete(long buyItemId);

        /**
         * Update purchase, item, picture and prices details
         * The above changes is an atomic transaction so must be committed in same transaction.
         *
         * @param buyItem
         * @param item
         * @param itemPrices
         * @param pictureMgr tracks item's updated and discarded or replaced pictures
         * @return
         */
        String update(ItemInShoppingList buyItem, Item item, List<Price> itemPrices, PictureMgr pictureMgr);

        /**
         * Save user action of checking the item in the shopping list
         *
         * @param buyItemId primary key of the purchase item in the shopping list
         * @param isChecked indicates whether the item is checked in the shopping list
         * @return
         */
        int update(long buyItemId, boolean isChecked);

        /**
         * Delete item in catalogue and its child records in other tables.
         *
         * @param itemId
         * @param pictureMgr tracks item's updated and discarded or replaced pictures
         * @return
         */
        String delete(long itemId, PictureMgr pictureMgr);

        /**
         * Update item and its child records in other tables.
         * The above changes is an atomic transaction so must be committed in same transaction.
         *
         * @param item
         * @param prices
         * @param pictureMgr tracks item's updated and discarded or replaced pictures
         * @return
         */
        String update(Item item, List<Price> prices, PictureMgr pictureMgr);

        /**
         * Delete photos in the filesystem.
         *
         * @param file item's updated and discarded or replaced pictures
         * @return
         */
        int deleteFileFromFilesystem(File file);

        String deleteCheckedItems();

        /**
         * Delete path of picture in database. This method does not delete file in the filesystem.
         * @param itemId
         * @return
         */
        int deletePictureInDb(long itemId);

}
