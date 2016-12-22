package com.mirzairwan.shopping.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mirzairwan.shopping.data.Contract.Catalogue;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ShoppingList;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentUris.parseId;

/**
 * Created by Mirza Irwan on 9/12/16.
 */

public class ShoppingListProvider extends ContentProvider
{

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI for the items  in items table
     * and its child records in buy_item table
     */
    private static final int ITEMS = 100;

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI for the single item in items table
     */
    private static final int ITEM_ID = 101;

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI for the prices table
     */
    private static final int PRICES = 110;

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI for the single price in prices table
     */
    private static final int PRICE_ID = 111;

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI for the buy_items table
     */
    private static final int BUY_ITEMS = 120;

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI for the single buy_item in buy_items table
     */
    private static final int BUY_ITEM_ID = 121;

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI to gel all buy items record and its parent record in
     * items and prices table
     */
    private static final int SHOPPING_LIST = 122;

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI to get a specific buy item record and its parent record in
     * items and records in prices table for the item.
     */
    private static final int SHOPPING_LIST_ITEMID = 123;

    /**
     * CONTENT_URI matcher code for the content CONTENT_URI for the items table left join buy_items table
     */
    private static final int CATALOGUE = 102;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final Map<String, String> sAllBuyItemsProjectionMap = new HashMap<>();

    private static final Map<String, String> sItemsPricesProjectionMap = new HashMap<>();

    static {

        sAllBuyItemsProjectionMap.put(ItemsEntry.COLUMN_NAME, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_NAME);
        sAllBuyItemsProjectionMap.put(ItemsEntry.COLUMN_BRAND, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_BRAND);
        sAllBuyItemsProjectionMap.put(ItemsEntry.COLUMN_DESCRIPTION, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_DESCRIPTION);
        sAllBuyItemsProjectionMap.put(ItemsEntry.COLUMN_COUNTRY_ORIGIN, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_COUNTRY_ORIGIN);
        sAllBuyItemsProjectionMap.put(ToBuyItemsEntry._ID, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry._ID + " AS " + ToBuyItemsEntry._ID);
        sAllBuyItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_ITEM_ID, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_ITEM_ID);
        sAllBuyItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_QUANTITY, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_QUANTITY);
        sAllBuyItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID);
        sAllBuyItemsProjectionMap.put(ToBuyItemsEntry.COLUMN_IS_CHECKED, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_IS_CHECKED);
        sAllBuyItemsProjectionMap.put(PricesEntry.ALIAS_ID, PricesEntry.TABLE_NAME + "." + PricesEntry._ID + " AS " + PricesEntry.ALIAS_ID);
        sAllBuyItemsProjectionMap.put(PricesEntry.COLUMN_PRICE_TYPE_ID, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_PRICE_TYPE_ID);
        sAllBuyItemsProjectionMap.put(PricesEntry.COLUMN_PRICE, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_PRICE);
        sAllBuyItemsProjectionMap.put(PricesEntry.COLUMN_CURRENCY_CODE, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_CURRENCY_CODE);
        sAllBuyItemsProjectionMap.put(PricesEntry.COLUMN_BUNDLE_QTY, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_BUNDLE_QTY);
        sAllBuyItemsProjectionMap.put(PricesEntry.COLUMN_SHOP_ID, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_SHOP_ID);

        sItemsPricesProjectionMap.put(ItemsEntry._ID, ItemsEntry.TABLE_NAME + "." + ItemsEntry._ID);
        sItemsPricesProjectionMap.put(ItemsEntry.COLUMN_NAME, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_NAME);
        sItemsPricesProjectionMap.put(ItemsEntry.COLUMN_BRAND, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_BRAND);
        sItemsPricesProjectionMap.put(ItemsEntry.COLUMN_DESCRIPTION, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_DESCRIPTION);
        sItemsPricesProjectionMap.put(ItemsEntry.COLUMN_COUNTRY_ORIGIN, ItemsEntry.TABLE_NAME + "." + ItemsEntry.COLUMN_COUNTRY_ORIGIN);
        sItemsPricesProjectionMap.put(PricesEntry.ALIAS_ID, PricesEntry.TABLE_NAME + "." + PricesEntry._ID + " AS " + PricesEntry.ALIAS_ID);
        sItemsPricesProjectionMap.put(PricesEntry.COLUMN_PRICE_TYPE_ID, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_PRICE_TYPE_ID);
        sItemsPricesProjectionMap.put(PricesEntry.COLUMN_PRICE, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_PRICE);
        sItemsPricesProjectionMap.put(PricesEntry.COLUMN_CURRENCY_CODE, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_CURRENCY_CODE);
        sItemsPricesProjectionMap.put(PricesEntry.COLUMN_BUNDLE_QTY, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_BUNDLE_QTY);
        sItemsPricesProjectionMap.put(PricesEntry.COLUMN_SHOP_ID, PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_SHOP_ID);
        sItemsPricesProjectionMap.put(ToBuyItemsEntry.ALIAS_ID, ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry._ID + " AS " + ToBuyItemsEntry.ALIAS_ID);

    }


    static {
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_ITEMS, ITEMS);

        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_ITEMS + "/#", ITEM_ID);

        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_PRICES, PRICES);

        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_PRICES + "/#", PRICE_ID);

        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_BUY_ITEMS, BUY_ITEMS);

        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_BUY_ITEMS + "/#", BUY_ITEM_ID);

        /**
         * CONTENT_URI matcher for all items in the shopping list
         */
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_SHOPPING_LIST, SHOPPING_LIST);

        /**
         * CONTENT_URI matcher for one item in the shopping list
         * Called by buying activity on one existing item
         */
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_SHOPPING_LIST + "/#", SHOPPING_LIST_ITEMID);


        /**
         * CONTENT_URI matcher for all items in the catalogue
         */
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,
                Contract.PATH_CATALOGUE, CATALOGUE);

    }

    private ShoppingListDbHelper mShoppingListDbHelper;


    @Override
    public boolean onCreate()
    {
        mShoppingListDbHelper = new ShoppingListDbHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values)
    {
        int matchCode = sUriMatcher.match(uri);
        Uri resultUri;
        switch (matchCode) {
            case ITEMS:
                resultUri = insertItem(uri, values, null);
                break;
            case PRICES:
                resultUri = insertPrice(uri, values, null);
                break;
            case BUY_ITEMS:
                resultUri = insertBuyItem(uri, values, null);
                break;
            default:
                throw new IllegalArgumentException("Insert of such type is NOT supported for CONTENT_URI" + uri + " >>> match code " + matchCode);
        }
        notifyChange();
        return resultUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        int matchCode = sUriMatcher.match(uri);
        Cursor cursor = null;
        SQLiteDatabase database = null;
        long id = -1;
        switch (matchCode) {
            case ITEMS:
                cursor = queryItems(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case ITEM_ID:
                database = mShoppingListDbHelper.getReadableDatabase();
                id = ContentUris.parseId(uri);
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(id)};
                cursor = database.query(ItemsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRICES:
                database = mShoppingListDbHelper.getReadableDatabase();
                cursor = database.query(PricesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CATALOGUE:
                cursor = getCatalogueAndPrice(uri, projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case SHOPPING_LIST:
                cursor = getBuyItems(uri, projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case SHOPPING_LIST_ITEMID:
                long itemId = ContentUris.parseId(uri);
                selection = PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_ITEM_ID + "=?";
                selectionArgs = new String[]{String.valueOf(itemId)};
                cursor = getBuyItems(uri, projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            default:
                throw new IllegalArgumentException("Query request is NOT supported for " + uri);
        }
        return cursor;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        int result = 0;
        int matchCode = sUriMatcher.match(uri);
        switch (matchCode) {
            case ITEMS:
                result = updateItems(uri, values, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = ItemsEntry._ID + "=?";
                long _id = parseId(uri); //get the database id from CONTENT_URI
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
        notifyChange();
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
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
            case PRICES:
                SQLiteDatabase database = mShoppingListDbHelper.getWritableDatabase();
                deleted = database.delete(PricesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete of this type is NOT supported for " + uri);

        }

        notifyChange();
        return deleted;
    }

    private void notifyChange()
    {
        getContext().getContentResolver().notifyChange(ShoppingList.CONTENT_URI, null);
        getContext().getContentResolver().notifyChange(Catalogue.CONTENT_URI, null);
    }

    private Cursor queryItems(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(ItemsEntry.TABLE_NAME);
        return builder.query(mShoppingListDbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

    }

    private int updatePrices(Uri uri, ContentValues values, String selection,
                             String[] selectionArgs)
    {
        return mShoppingListDbHelper.getWritableDatabase()
                .update(PricesEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private int updateItems(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        int result;
        result = mShoppingListDbHelper.getWritableDatabase().
                update(ItemsEntry.TABLE_NAME, values,
                        selection, selectionArgs);
        return result;
    }

    private int deleteItems(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase database = mShoppingListDbHelper.getWritableDatabase();
        return database.delete(ItemsEntry.TABLE_NAME, selection, selectionArgs);
    }


    /**
     * Get all to buy items in the buy_items table and its parent record in items and prices
     * table
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    private Cursor getBuyItems(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {

//        *           "SELECT items._id AS itemId," +
//        *           "items.name, items.brand, items.country_origin, " +
//        *           "items.description, items.last_updated_on, " +
//        *           "buy_items._id AS dbBuyItemId, buy_items.quantity, " +
//        *           "buy_items.selected_price_id, buy_items.is_checked, buy_items.last_updated_on, " +
//                    "prices.price_type_id, prices.price, prices.currency_code
//        *           "FROM buy_items " +
//        *           "LEFT JOIN items " +
//        *           "ON buy_items._id=items._id " +
//                    "LEFT JOIN prices
//                    "ON buy_items.selected_price_id=prices._id


        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(ToBuyItemsEntry.TABLE_NAME +
                " LEFT JOIN " + ItemsEntry.TABLE_NAME +
                " ON " + ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_ITEM_ID +
                "=" +
                ItemsEntry.TABLE_NAME + "." + ItemsEntry._ID +
                " LEFT JOIN " + PricesEntry.TABLE_NAME +
                " ON " + ToBuyItemsEntry.TABLE_NAME + "." +
                ToBuyItemsEntry.COLUMN_ITEM_ID +
                "=" +
                PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_ITEM_ID
        );

        queryBuilder.setProjectionMap(sAllBuyItemsProjectionMap);

        // Get the database and run the query
        SQLiteDatabase database = mShoppingListDbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);

        return cursor;
    }

    /**
     * Get catalogue item(s) and its prices
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    private Cursor getCatalogueAndPrice(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(ItemsEntry.TABLE_NAME +
                " LEFT JOIN " +
                ToBuyItemsEntry.TABLE_NAME + " ON " +
                ItemsEntry.TABLE_NAME + "." + ItemsEntry._ID + "=" +
                ToBuyItemsEntry.TABLE_NAME + "." + ToBuyItemsEntry.COLUMN_ITEM_ID +
                " LEFT JOIN " +
                PricesEntry.TABLE_NAME + " ON " +
                ItemsEntry.TABLE_NAME + "." + ItemsEntry._ID + "=" +
                PricesEntry.TABLE_NAME + "." + PricesEntry.COLUMN_ITEM_ID
        );

        queryBuilder.setProjectionMap(sItemsPricesProjectionMap);

        // Get the database and run the query
        SQLiteDatabase database = mShoppingListDbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);

        return cursor;
    }

    private Uri insertBuyItem(Uri uri, ContentValues values, SQLiteDatabase database)
    {
        if (database == null)
            database = mShoppingListDbHelper.getWritableDatabase();
        long _id = database.insert(ToBuyItemsEntry.TABLE_NAME, null, values);
        if (_id == -1)
            return null;
        else {
            return ContentUris.withAppendedId(uri, _id);
        }
    }

    private Uri insertPrice(Uri uri, ContentValues values, SQLiteDatabase database)
    {
        if (database == null)
            database = mShoppingListDbHelper.getWritableDatabase();
        long _id = database.insert(PricesEntry.TABLE_NAME, null, values);
        if (_id == -1)
            return null;
        else {
            return ContentUris.withAppendedId(uri, _id);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values, SQLiteDatabase database)
    {
        if (database == null)
            database = mShoppingListDbHelper.getWritableDatabase();
        long _id = database.insert(ItemsEntry.TABLE_NAME, null, values);
        if (_id == -1)
            return null;
        else {
            //getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, _id);
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri)
    {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemsEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown CONTENT_URI " + uri + " with match " + match);
        }
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException
    {
        ContentProviderResult[] results;
        SQLiteDatabase database = mShoppingListDbHelper.getWritableDatabase();
        database.beginTransaction();

        try {
            results = super.applyBatch(operations);
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();//The end of the transaction
        }
        return results;
    }

}
