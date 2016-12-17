package com.mirzairwan.shopping.data;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mirzairwan.shopping.data.ShoppingListContract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.ItemsEntry;
import com.mirzairwan.shopping.data.ShoppingListContract.PricesEntry;


import java.util.ArrayList;

/**
 * Created by Mirza Irwan on 9/12/16.
 */

public class ShoppingListDbHelper extends SQLiteOpenHelper
{
    public final String LOG_TAG = ShoppingListDbHelper.class.getSimpleName();

    public static final String DATABASE = "shopping_list.db";
    public static final int VERSION = 1;

    public ShoppingListDbHelper(Context context)
    {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // SQL statement to create the items table
        String SQL_CREATE_ITEM_TABLE = "CREATE TABLE " +
                ItemsEntry.TABLE_NAME + " " + "(" +
                ItemsEntry._ID + " " + "INTEGER PRIMARY KEY AUTOINCREMENT," +
                ItemsEntry.COLUMN_NAME + " " + "TEXT NOT NULL," +
                ItemsEntry.COLUMN_BRAND + " " + "TEXT," +
                ItemsEntry.COLUMN_COUNTRY_ORIGIN + " " + "TEXT," +
                ItemsEntry.COLUMN_DESCRIPTION + " " + "TEXT," +
                ItemsEntry.COLUMN_LAST_UPDATED_ON + " " + "INTEGER" +
                ")";

        // SQL statement to create the price table
        String SQL_CREATE_PRICE_TABLE = "CREATE TABLE " +
                PricesEntry.TABLE_NAME + " " + "(" +
                PricesEntry._ID + " " + "INTEGER PRIMARY KEY AUTOINCREMENT," +
                PricesEntry.COLUMN_PRICE_TYPE_ID + " " + "INTEGER NOT NULL," +
                PricesEntry.COLUMN_SHOP_ID + " " + "INTEGER NOT NULL," +
                PricesEntry.COLUMN_PRICE + " " + "INTEGER NOT NULL," +
                PricesEntry.COLUMN_BUNDLE_QTY + " " + "INTEGER," +
                PricesEntry.COLUMN_ITEM_ID + " " + "INTEGER NOT NULL," +
                PricesEntry.COLUMN_CURRENCY_CODE + " " + "TEXT NOT NULL," +
                PricesEntry.COLUMN_LAST_UPDATED_ON + " " + "INTEGER," +
                "UNIQUE(" + PricesEntry.COLUMN_PRICE_TYPE_ID + "," +
                PricesEntry.COLUMN_SHOP_ID + "," + PricesEntry.COLUMN_ITEM_ID + ")," +
                "FOREIGN KEY (" + PricesEntry.COLUMN_ITEM_ID + " ) REFERENCES " +
                ItemsEntry.TABLE_NAME + "(" +
                ItemsEntry._ID + ")" + "" +
                ")";

        // SQL statement to create the buy items table
        String SQL_CREATE_BUY_ITEMS_TABLE = "CREATE TABLE " +
                ToBuyItemsEntry.TABLE_NAME + " " + "(" +
                ToBuyItemsEntry._ID + " " + "INTEGER PRIMARY KEY AUTOINCREMENT," +
                ToBuyItemsEntry.COLUMN_ITEM_ID + " " + "INTEGER NOT NULL," +
                ToBuyItemsEntry.COLUMN_QUANTITY + " " + "INTEGER NOT NULL," +
                ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID + " " + "INTEGER NOT NULL," +
                ToBuyItemsEntry.COLUMN_IS_CHECKED + " " + "INTEGER NOT NULL," +
                ToBuyItemsEntry.COLUMN_LAST_UPDATED_ON + " " + "INTEGER," +
                "FOREIGN KEY (" + ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID + ") REFERENCES " +
                PricesEntry.TABLE_NAME + "(" + PricesEntry._ID + "), " +
                "FOREIGN KEY (" + ToBuyItemsEntry.COLUMN_ITEM_ID + " ) REFERENCES " +
                ItemsEntry.TABLE_NAME + "(" +
                ItemsEntry._ID + ")" + "" +
                ")";

        db.execSQL(SQL_CREATE_ITEM_TABLE);
        db.execSQL(SQL_CREATE_PRICE_TABLE);
        db.execSQL(SQL_CREATE_BUY_ITEMS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }
}
