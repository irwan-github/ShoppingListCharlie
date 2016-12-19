package com.mirzairwan.shopping;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ShoppingList;
import com.mirzairwan.shopping.domain.ToBuyItem;

import java.util.Currency;
import java.util.Locale;

import static com.mirzairwan.shopping.ShoppingActivity.CREATE_BUY_ITEM_REQUEST_CODE;
import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;

/**
 * Display the buy item details in a screen
 * Create new buy item
 * Update and save buy item details
 * Delete existing buy items
 * <p>
 * Create new buy item uses the NEW_BUY_ITEM_MODE
 * Action modes
 */
public class BuyingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final String ITEM_ID = "ITEM_ID";
    private static final int LOADER_ID = 20;

    private int actionMode = -1; //Informs the editor whether this activity is creation or updating
    private Cursor mCursor;

    private ToBuyItem toBuyItem;
    private long defaultShopId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editing);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        //if no bundle, then the request is for new creation
        if (uri != null) {
            setTitle(R.string.view_buy_item_details);
            //populateItemDetails(uri);
            Bundle arg = new Bundle();
            //arg.putLong(ITEM_ID, ContentUris.parseId(uri));
            arg.putParcelable(ITEM_ID, uri);
            getLoaderManager().initLoader(LOADER_ID, arg, this);
        } else {
            setTitle(R.string.new_buy_item_title);
            actionMode = CREATE_BUY_ITEM_REQUEST_CODE; // This flag is used for menu creation
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (actionMode == CREATE_BUY_ITEM_REQUEST_CODE) {
            menu.removeItem(R.id.menu_remove_item_from_list);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.item_details, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {

        switch (menuItem.getItemId()) {
            case R.id.save_item_details:
                save();
                return true;
            case R.id.menu_remove_item_from_list: //Do nothing. Let originator handle it
                Loader<Object> loader = getLoaderManager().getLoader(LOADER_ID);
                delete();
            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    private void alertRequiredField(int messageId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mandatory field(s)");
        builder.setMessage(messageId);
        builder.setPositiveButton("OK", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     *
     */
    private void delete()
    {
        if(!mCursor.moveToNext())
            mCursor.moveToFirst();
        long buyItemId = mCursor.getLong(mCursor.getColumnIndex(ToBuyItemsEntry._ID));
        Uri uriDeleteBuyItem = ContentUris.withAppendedId(ToBuyItemsEntry.CONTENT_URI, buyItemId);
        getContentResolver().delete(uriDeleteBuyItem, null, null);
        finish();
    }

    private void save()
    {
        EditText etName = (EditText) findViewById(R.id.et_item_name);
        String itemName;

        if (TextUtils.isEmpty(etName.getText())) {
            alertRequiredField(R.string.mandatory_name);
            etName.requestFocus();
            return;
        } else {
            itemName = etName.getText().toString();
        }


        String itemBrand = ((EditText) findViewById(R.id.et_item_brand)).getText().toString();
        String itemDescription = ((EditText) findViewById(R.id.et_item_description)).getText().toString();
        EditText etQty = (EditText) findViewById(R.id.et_item_quantity);
        String itemQuantity;

        if (TextUtils.isEmpty(etQty.getText()) || Integer.parseInt(etQty.getText().toString()) < 1) {
            alertRequiredField(R.string.mandatory_quantity);
            etQty.requestFocus();
            return;
        } else {
            itemQuantity = etQty.getText().toString();
        }

        String unitPrice = ((EditText) findViewById(R.id.et_unit_price)).getText().toString();
        String bundlePrice = ((EditText) findViewById(R.id.et_bundle_price)).getText().toString();
        String bundleQty = ((EditText) findViewById(R.id.et_bundle_qty)).getText().toString();
        int idSelected = ((RadioGroup) findViewById(R.id.price_type_choice)).getCheckedRadioButtonId();
        Price.Type selectedPriceType = (idSelected == R.id.rb_bundle_price) ?
                Price.Type.BUNDLE_PRICE : UNIT_PRICE;

        ShoppingList shoppingList = Builder.getShoppingList();

        SharedPreferences prefs = getSharedPreferences(ShoppingActivity.PERSONAL, Activity.MODE_PRIVATE);
        String homeCountryCode  = prefs.getString(ShoppingActivity.HOME_COUNTRY_CODE, Locale.getDefault().getCountry());
        Locale homeLocale = new Locale(Locale.getDefault().getLanguage(), homeCountryCode);
        String currencyCode = Currency.getInstance(homeLocale).getCurrencyCode();

        DaoManager daoManager = Builder.getDaoManager(this);

        String msg;
        if(actionMode == CREATE_BUY_ITEM_REQUEST_CODE) {
            ToBuyItem newBuyItem = shoppingList.createItem(itemName, itemBrand, itemDescription,
                    Integer.parseInt(itemQuantity), currencyCode,
                    Double.parseDouble(unitPrice),
                    Double.parseDouble(bundlePrice),
                    Double.parseDouble(bundleQty), selectedPriceType);

            msg = daoManager.insert(newBuyItem, newBuyItem.getItem(),
                    newBuyItem.getItem().getPrices());
        }
        else //Existing buy item
        {
            toBuyItem.getItem().setName(itemName);
            toBuyItem.getItem().setBrand(itemBrand);
            toBuyItem.getItem().setCountryOrigin("SG");
            toBuyItem.getItem().setDescription(itemDescription);

            for( Price price : toBuyItem.getItem().getPrices())
            {
                if(price.getPriceType() == BUNDLE_PRICE)
                {
                    price.setBundlePrice(Double.parseDouble(bundlePrice), Double.parseDouble(bundleQty));
                }
                else
                {
                    price.setUnitPrice(Double.parseDouble(unitPrice));
                }
            }

            toBuyItem.setQuantity(Integer.parseInt(itemQuantity));
            toBuyItem.selectPrice(defaultShopId, selectedPriceType);

            msg = daoManager.update(toBuyItem, toBuyItem.getItem(),
                    toBuyItem.getItem().getPrices());
        }

        setResult(Activity.RESULT_OK);

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        finish();
    }


    private void populateItemDetails(Cursor cursor)
    {

        if (cursor == null)
            throw new IllegalArgumentException("Cursor cannot be null");

        long itemId = 0;
        long buyItemId = 0;
        String itemName = "", itemBrand = "", itemDescription = "", currencyCode="";
        int buyQty = 0;
        double unitPrice = 0, bundlePrice = 0, bundleQty = 0;
        Price.Type priceType;


        boolean isItemDetailsPopulated = false; //Multiple identical item detail records will be retrieved due to item having more than one price
                                                //So the first pass in the loop will populate item details. Skip the populating the item details in
                                                //subsequent pass
        while (cursor.moveToNext()) {
            if (!isItemDetailsPopulated) {

                itemId = cursor.getLong(cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_ITEM_ID));
                buyItemId = cursor.getLong(cursor.getColumnIndex(ToBuyItemsEntry._ID));

                int colNameIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
                EditText etItemName = (EditText) findViewById(R.id.et_item_name);
                itemName = cursor.getString(colNameIndex);
                etItemName.setText(itemName);


                int colBrandIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_BRAND);
                EditText etItemBrand = (EditText) findViewById(R.id.et_item_brand);
                itemBrand = cursor.getString(colBrandIdx);
                etItemBrand.setText(itemBrand);

                int colQtyIdx = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
                EditText etQuantity = (EditText) findViewById(R.id.et_item_quantity);
                buyQty = cursor.getInt(colQtyIdx);
                etQuantity.setText(String.valueOf(buyQty));

                int colDescriptionIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_DESCRIPTION);
                EditText etItemDescription = (EditText) findViewById(R.id.et_item_description);
                itemDescription = cursor.getString(colDescriptionIdx);
                etItemDescription.setText(itemDescription);

                Item item = new Item(itemId, itemName, itemBrand, "SG", itemDescription, null);
                toBuyItem = new ToBuyItem(buyItemId);
                toBuyItem.setItem(item);
                toBuyItem.setQuantity(buyQty);

            }

            int colPriceTypeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE_TYPE_ID);
            int priceTypeVal = cursor.getInt(colPriceTypeIdx);

            int colPriceIdIdx = cursor.getColumnIndex(PricesEntry.ALIAS_ID);
            long priceId = cursor.getLong(colPriceIdIdx);


            int colSelectedPriceIdIdx = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID);
            long selectedPriceId = cursor.getLong(colSelectedPriceIdIdx);

            int colCurrencyCodeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
            currencyCode = cursor.getString(colCurrencyCodeIdx);

            int colShopIdIdx = cursor.getColumnIndex(PricesEntry.COLUMN_SHOP_ID);
            long shopId = cursor.getLong(colShopIdIdx);

            int colPriceIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);

            if (priceTypeVal == UNIT_PRICE.getType()) {

                EditText etUnitPrice = (EditText) findViewById(R.id.et_unit_price);
                unitPrice = cursor.getDouble(colPriceIdx)/100;
                etUnitPrice.setText(NumberFormatter.formatToTwoDecimalPlaces(
                        unitPrice));

                Price price = new Price(priceId, unitPrice, currencyCode, shopId, null);
                toBuyItem.getItem().addPrice(price);

            }

            if (priceTypeVal == BUNDLE_PRICE.getType()) {

                EditText etBundlePrice = (EditText) findViewById(R.id.et_bundle_price);
                bundlePrice = cursor.getDouble(colPriceIdx)/100;
                etBundlePrice.setText(NumberFormatter.formatToTwoDecimalPlaces(bundlePrice));

                int colBundleQtyIdx = cursor.getColumnIndex(PricesEntry.COLUMN_BUNDLE_QTY);
                bundleQty = cursor.getDouble(colBundleQtyIdx);
                EditText etBundleQty = (EditText) findViewById(R.id.et_bundle_qty);
                etBundleQty.setText(NumberFormatter.formatToTwoDecimalPlaces(bundleQty));

                Price price = new Price(priceId, bundlePrice, bundleQty, currencyCode, shopId, null);
                toBuyItem.getItem().addPrice(price);
            }

            if(priceId == selectedPriceId)
            {
                RadioGroup priceTypeChoice = (RadioGroup) findViewById(R.id.price_type_choice);

                priceType = priceTypeVal == BUNDLE_PRICE.getType() ? BUNDLE_PRICE : UNIT_PRICE;

                priceTypeChoice.check(priceTypeVal == BUNDLE_PRICE.getType() ?
                                        R.id.rb_bundle_price : R.id.rb_unit_price);

                toBuyItem.selectPrice(shopId, priceType);

            }

            isItemDetailsPopulated = true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String[] projection = new String[]{ToBuyItemsEntry._ID,
                ToBuyItemsEntry.COLUMN_ITEM_ID,
                ToBuyItemsEntry.COLUMN_QUANTITY,
                ToBuyItemsEntry.COLUMN_IS_CHECKED,
                ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID,
                ItemsEntry.COLUMN_NAME,
                ItemsEntry.COLUMN_BRAND,
                ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                ItemsEntry.COLUMN_DESCRIPTION,
                PricesEntry.ALIAS_ID,
                PricesEntry.COLUMN_PRICE_TYPE_ID,
                PricesEntry.COLUMN_PRICE,
                PricesEntry.COLUMN_BUNDLE_QTY,
                PricesEntry.COLUMN_CURRENCY_CODE,
                PricesEntry.COLUMN_SHOP_ID};

        Uri uri = args.getParcelable(ITEM_ID);
        CursorLoader loader = new CursorLoader(this, uri, projection, null, null, null);
        return loader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        mCursor = cursor;
        populateItemDetails(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mCursor = null;
    }
}
