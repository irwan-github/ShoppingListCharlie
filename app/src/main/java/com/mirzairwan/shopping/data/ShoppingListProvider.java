package com.mirzairwan.shopping.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mirzairwan.shopping.data.ShoppingListContract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.ItemsEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.PricesEntry;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentUris.parseId;

/**
 * Created by Mirza Irwan on 9/12/16.
 */

public class ShoppingListProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the items  in items table
     * and its child records in buy_item table
     */
    private static final int ITEMS = 100;

    /**
     * URI matcher code for the content URI for the single item in items table
     */
    private static final int ITEM_ID = 101;

    /**
     * URI matcher code for the content URI for the items table left join buy_items table
     */
    private static final int ITEMS_JOIN_BUY_ITEMS = 102;

    /**
     * URI matcher code for the content URI for the prices table
     */
    private static final int PRICES = 110;

    /**
     * URI matcher code for the content URI for the single price in prices table
     */
    private static final int PRICE_ID = 111;

    /**
     * URI matcher code for the content URI for the buy_items table
     */
    private static final int BUY_ITEMS = 120;

    /**
     * URI matcher code for the content URI for the single buy_item in buy_items table
     */
    private static final int BUY_ITEM_ID = 121;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final Map<String, String> sAllItemsProjectionMap = new HashMap<>();

    private static final Map<String, String> sBuyItemsProjectionMap = new HashMap<>();

    private static final Map<String, String> sItemsProjectionMap = new HashMap<>();

    static {
        sBuyItemsProjectionMap.put(ToBuyItemsEntry._ID, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry._ID + " AS buy_item_id");
        sBuyItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_QUANTITY, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_QUANTITY);
        sBuyItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID);
        sBuyItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_IS_CHECKED, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_IS_CHECKED);
        sBuyItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_LAST_UPDATED_ON, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_LAST_UPDATED_ON);

        sAllItemsProjectionMap.put(ItemsEntry.ALIAS_ID, ItemsEntry.TABLE_NAME + "." + ItemsEntry._ID + " AS " + ItemsEntry.ALIAS_ID);
        sAllItemsProjectionMap.put(ItemsEntry.COLUMN_NAME, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_NAME);
        sAllItemsProjectionMap.put(ItemsEntry.COLUMN_BRAND, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_BRAND);
        sAllItemsProjectionMap.put(ItemsEntry.COLUMN_DESCRIPTION, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_DESCRIPTION);
        sAllItemsProjectionMap.put(ItemsEntry.COLUMN_COUNTRY_ORIGIN, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_COUNTRY_ORIGIN);
        sAllItemsProjectionMap.put(ItemsEntry.ALIAS_COLUMN_LAST_UPDATED_ON, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_LAST_UPDATED_ON + " AS " + ItemsEntry.ALIAS_COLUMN_LAST_UPDATED_ON);
        sAllItemsProjectionMap.put(ToBuyItemsEntry.ALIAS_ID, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry._ID + " AS " + ToBuyItemsEntry.ALIAS_ID);
        sAllItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_QUANTITY, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_QUANTITY);
        sAllItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID);
        sAllItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_IS_CHECKED, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_IS_CHECKED);
        sAllItemsProjectionMap.put(ToBuyItemsEntry.ALIAS_COLUMN_LAST_UPDATED_ON, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_LAST_UPDATED_ON + " AS " + ToBuyItemsEntry.ALIAS_COLUMN_LAST_UPDATED_ON);

        sItemsProjectionMap.put(ItemsEntry._ID, ItemsEntry.TABLE_NAME + "." + ItemsEntry._ID + " AS " + ItemsEntry.ALIAS_ID);
        sItemsProjectionMap.put(ItemsEntry.COLUMN_NAME, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_NAME);
        sItemsProjectionMap.put(ItemsEntry.COLUMN_BRAND, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_BRAND);
        sItemsProjectionMap.put(ItemsEntry.COLUMN_DESCRIPTION, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_DESCRIPTION);
        sItemsProjectionMap.put(ItemsEntry.COLUMN_COUNTRY_ORIGIN, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_COUNTRY_ORIGIN);
        sItemsProjectionMap.put(ItemsEntry.COLUMN_LAST_UPDATED_ON, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_LAST_UPDATED_ON);
    }


    static {
        sUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY,
                ShoppingListContract.PATH_ITEMS, ITEMS);

        sUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY,
                ShoppingListContract.PATH_ITEMS + "/#", ITEM_ID);

        sUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY,
                ShoppingListContract.PATH_ITEMS + "/" +
                        ShoppingListContract.PATH_BUY_ITEMS, ITEMS_JOIN_BUY_ITEMS);

        sUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY,
                ShoppingListContract.PATH_PRICES, PRICES);

        sUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY,
                ShoppingListContract.PATH_PRICES + "/#", PRICE_ID);

        sUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY,
                ShoppingListContract.PATH_BUY_ITEMS, BUY_ITEMS);

        sUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY,
                ShoppingListContract.PATH_BUY_ITEMS + "/#", BUY_ITEM_ID);

    }

    private ShoppingListDbHelper mShoppingListDbHelper;


    @Override
    public boolean onCreate() {
        mShoppingListDbHelper = new ShoppingListDbHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int matchCode = sUriMatcher.match(uri);
        switch (matchCode) {
            case ITEMS:
                return insertItem(uri, values, null);
            case PRICES:
                return insertPrice(uri, values, null);
            case BUY_ITEMS:
                return insertBuyItem(uri, values, null);
            default:
                throw new IllegalArgumentException("Insert of such type is NOT supported for URI" + uri + " >>> match code " + matchCode);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int matchCode = sUriMatcher.match(uri);
        Cursor cursor = null;
        switch (matchCode) {
            case ITEMS:
                cursor = queryItems(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case ITEMS_JOIN_BUY_ITEMS:
                cursor = getAllItemsAndBuyItems(uri, projection, selection,
                        selectionArgs, sortOrder);
                break;
            case PRICES:
                SQLiteDatabase database = mShoppingListDbHelper.getReadableDatabase();
                cursor = database.query(PricesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BUY_ITEMS:
                //cursor = mShoppingListDbHelper.getReadableDatabase().query(ToBuyItemsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Query request is NOT supported for " + uri);
        }
        return cursor;
    }

    private Cursor queryItems(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(ItemsEntry.TABLE_NAME);
        builder.setProjectionMap(sItemsProjectionMap);
        return builder.query(mShoppingListDbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int result = 0;
        int matchCode = sUriMatcher.match(uri);
        switch (matchCode) {
            case ITEMS:
                result = updateItems(uri, values, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = ItemsEntry._ID + "=?";
                long _id = parseId(uri); //get the database id from URI
                selectionArgs = new String[]{String.valueOf(_id)};
                result = updateItems(uri, values, selection, selectionArgs);
                break;
            case PRICES:
                result = updatePrices(uri, values, selection, selectionArgs);
                break;
            case PRICE_ID:
                selection = PricesEntry._ID + "=?";
                long _idPrice = parseId(uri);
                selectionArgs = new String[]{String.valueOf(_idPrice)};
                result = updatePrices(uri, values, selection, selectionArgs);
                break;
            case BUY_ITEM_ID:
                selection = ToBuyItemsEntry._ID + "=?";
                long _idBuyItem = parseId(uri);
                selectionArgs = new String[]{String.valueOf(_idBuyItem)};
                result = mShoppingListDbHelper.getWritableDatabase().
                        update(ToBuyItemsEntry.TABLE_NAME, values,
                                selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("This type of update is NOT supported for " + uri);
        }
        return result;
    }

    private int updatePrices(Uri uri, ContentValues values, String selection,
                             String[] selectionArgs) {
        return mShoppingListDbHelper.getWritableDatabase()
                .update(PricesEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private int queryPrices(ContentValues values, String selection, String[] selectionArgs) {
        int result;
        result = mShoppingListDbHelper.getWritableDatabase().
                update(PricesEntry.TABLE_NAME, values,
                        selection, selectionArgs);
        return result;
    }

    private int updateItems(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int result;
        result = mShoppingListDbHelper.getWritableDatabase().
                update(ItemsEntry.TABLE_NAME, values,
                        selection, selectionArgs);
        return result;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int matchCode = sUriMatcher.match(uri);
        int deleted;
        long _id;
        switch (matchCode) {
            case ITEMS:
                deleted = deleteItems(uri, selection, selectionArgs);
                break;
            case ITEM_ID:
                _id = ContentUris.parseId(uri);
                selection = "_id=?";
                selectionArgs = new String[]{String.valueOf(_id)};
                deleted = deleteItems(uri, selection, selectionArgs);
                break;
            case BUY_ITEM_ID:
                _id = ContentUris.parseId(uri);
                selection = ToBuyItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(_id)};
                deleted = mShoppingListDbHelper.getWritableDatabase()
                        .delete(ToBuyItemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete of this type is NOT supported for " + uri);


        }
        return deleted;
    }

    private int deleteItems(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mShoppingListDbHelper.getWritableDatabase();
        return database.delete(ItemsEntry.TABLE_NAME, selection, selectionArgs);
    }

    private Cursor queryPrices(Uri uri, String[] projection, String selection,
                               String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase database = mShoppingListDbHelper.getReadableDatabase();
        cursor = database.query(PricesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    /**
     * Get all items in the items table and its child record in buy items
     * table
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    private Cursor getAllItemsAndBuyItems(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

//        *           "SELECT items._id AS itemId," +
//        *           "items.name, items.brand, items.country_origin, " +
//        *           "items.description, items.last_updated_on, " +
//        *           "buy_items._id AS dbBuyItemId, buy_items.quantity, " +
//        *           "buy_items.selected_price_id, buy_items.is_checked, buy_items.last_updated_on " +
//        *           "FROM items " +
//        *           "LEFT JOIN buy_items " +
//        *           "ON items._id=buy_items.item_id " +
//        *           "ORDER BY items.name";

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(ItemsEntry.TABLE_NAME + " LEFT JOIN " +
                ToBuyItemsEntry.TABLE_NAME + " ON " +
                ItemsEntry.TABLE_NAME + "." + ItemsEntry._ID + "=" +
                ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_ITEM_ID);

        queryBuilder.setProjectionMap(sAllItemsProjectionMap);

        // Get the database and run the query
        SQLiteDatabase database = mShoppingListDbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        //cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Uri insertBuyItem(Uri uri, ContentValues values, SQLiteDatabase database) {
        if (database == null)
            database = mShoppingListDbHelper.getWritableDatabase();
        long _id = database.insert(ToBuyItemsEntry.TABLE_NAME, null, values);
        if (_id == -1)
            return null;
        else
            return ContentUris.withAppendedId(uri, _id);
    }

    private Uri insertPrice(Uri uri, ContentValues values, SQLiteDatabase database) {
        if (database == null)
            database = mShoppingListDbHelper.getWritableDatabase();
        long _id = database.insert(PricesEntry.TABLE_NAME, null, values);
        if (_id == -1)
            return null;
        else
            return ContentUris.withAppendedId(uri, _id);
    }

    private Uri insertItem(Uri uri, ContentValues values, SQLiteDatabase database) {
        if (database == null)
            database = mShoppingListDbHelper.getWritableDatabase();
        long _id = database.insert(ItemsEntry.TABLE_NAME, null, values);
        if (_id == -1)
            return null;
        else
            return ContentUris.withAppendedId(uri, _id);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemsEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

//    private Uri insertItem(Uri uri, ContentValues values) {
//        SQLiteDatabase database = mShoppingListDbHelper.getWritableDatabase();
//        long _id = database.insert(ItemsEntry.TABLE_NAME, null, values);
//        if (_id == -1)
//            return null;
//        else
//            return ContentUris.withAppendedId(uri, _id);
//    }

}
