package com.mirzairwan.shopping.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Mirza Irwan on 9/12/16.
 */

public final class Contract
{
    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.mirzairwan.shopping";

    /**
     * Use CONTENT_AUTHORITY to create the base of all CONTENT_URI's which activities will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible paths (Will be appended to base content CONTENT_URI for possible CONTENT_URI's)
     */
    public static final String PATH_ITEMS = "items";
    public static final String PATH_BUY_ITEMS = "buy-items";
    public static final String PATH_PRICES = "prices";
    public static final String PATH_PICTURES = "pictures";
    public static final String PATH_SHOPPING_LIST = PATH_BUY_ITEMS + "/" + PATH_ITEMS;
    public static final String PATH_CATALOGUE = PATH_ITEMS + "/" + PATH_BUY_ITEMS;


    public static final class ShoppingList
    {
        /**
         * The content CONTENT_URI to access the shopping list in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SHOPPING_LIST);
    }

    public static final class Catalogue
    {
        /**
         * The CONTENT_URI to access the catalogue in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CATALOGUE);

    }

    public static final class ItemsEntry implements BaseColumns
    {
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single Item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The content CONTENT_URI to access the items data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        public static final String TABLE_NAME = "items";

        //Table joins requires use of alias names for columns with identical names:
        //Alias for _ID column
        public static final String ALIAS_ID = "item_id";

        /**
         * TEXT
         */
        public static final String COLUMN_NAME = "name";

        /**
         * TEXT
         */
        public static final String COLUMN_BRAND = "brand";

        /**
         * TEXT
         */
        public static final String COLUMN_COUNTRY_ORIGIN = "country_origin";

        /**
         * TEXT
         */
        public static final String COLUMN_DESCRIPTION = "description";

        /**
         * INTEGER
         */
        public static final String COLUMN_LAST_UPDATED_ON = "last_updated_on";

        public static final String ALIAS_COLUMN_LAST_UPDATED_ON = "item_last_updated_on";
    }

    public static final class PicturesEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PICTURES);

        public static final String TABLE_NAME = "pictures";

        public static final String COLUMN_ITEM_ID = "item_id";

        public static final String COLUMN_FILE_PATH = "file_path";

        public static final String COLUMN_LAST_UPDATED_ON = "last_updated_on";
    }

    public static final class PricesEntry implements BaseColumns
    {
        /**
         * The content CONTENT_URI to access the item price data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRICES);

        public static final String TABLE_NAME = "prices";

        public static final String ALIAS_ID = "price_id";

        /**
         * INTEGER
         */
        public static final String COLUMN_PRICE_TYPE_ID = "price_type_id";

        /**
         * INTEGER
         */
        public static final String COLUMN_SHOP_ID = "shop_id";

        /**
         * INTEGER
         */
        public static final String COLUMN_PRICE = "price";

        /**
         * INTEGER
         */
        public static final String COLUMN_BUNDLE_QTY = "bundle_qty";

        /**
         * INTEGER
         */
        public static final String COLUMN_ITEM_ID = "item_id";

        /**
         * TEXT
         * Store user's home currenct id
         */
        public static final String COLUMN_CURRENCY_CODE = "currency_code";

        /**
         * INTEGER
         */
        public static final String COLUMN_LAST_UPDATED_ON = "last_updated_on";

    }

    public static final class ToBuyItemsEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "buy_items";

        public static final String ALIAS_ID = "buy_item_id"; //Because all tables use _id as column name

        /**
         * The content CONTENT_URI to access the buy items data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BUY_ITEMS);

        /**
         * INTEGER
         */
        public static final String COLUMN_ITEM_ID = "item_id";


        /**
         * INTEGER
         */
        public static final String COLUMN_QUANTITY = "quantity";

        /**
         * INTEGER
         */
        public static final String COLUMN_SELECTED_PRICE_ID = "selected_price_id";

        /**
         * INTEGER Possible values: 1 for True or 0 for false
         */
        public static final String COLUMN_IS_CHECKED = "is_checked";

        /**
         * INTEGER
         */
        public static final String COLUMN_LAST_UPDATED_ON = "last_updated_on";

        public static final String ALIAS_COLUMN_LAST_UPDATED_ON = "buy_item_last_updated_on";

    }
//

}
