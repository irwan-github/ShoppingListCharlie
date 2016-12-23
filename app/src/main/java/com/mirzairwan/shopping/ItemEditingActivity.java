package com.mirzairwan.shopping;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;

import java.util.ArrayList;
import java.util.List;

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
public class ItemEditingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final String ITEM_URL = "ITEM_URL";
    private static final int ITEM_LOADER_ID = 20;
    private static final int ITEM_PRICE_LOADER_ID = 21;
    private Item item;

    private EditText etName;
    private EditText etBrand;
    private EditText etDescription;
    private EditText etCountryOrigin;
    private EditText etUnitPrice;
    private EditText etBundlePrice;
    private EditText etBundleQty;
    private View.OnTouchListener mOnTouchListener;
    private boolean mItemHaveChanged = false;
    private DaoManager daoManager;
    private List<Price> prices;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editing2);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        setTitle(R.string.view_buy_item_details);

        daoManager = Builder.getDaoManager(this);

        Bundle arg = new Bundle();
        arg.putParcelable(ITEM_URL, uri);
        getLoaderManager().initLoader(ITEM_LOADER_ID, arg, this);
        getLoaderManager().initLoader(ITEM_PRICE_LOADER_ID, arg, this);
    }

    private void populatePricesViews()
    {
        FrameLayout priceContainer = (FrameLayout) findViewById(R.id.price_container);
        etUnitPrice = (EditText) priceContainer.findViewById(R.id.et_unit_price);
        etBundlePrice = (EditText) priceContainer.findViewById(R.id.et_bundle_price);
        etBundleQty = (EditText) priceContainer.findViewById(R.id.et_bundle_qty);

        etUnitPrice.setOnTouchListener(mOnTouchListener);
        etBundlePrice.setOnTouchListener(mOnTouchListener);
        etBundleQty.setOnTouchListener(mOnTouchListener);

        for (Price price : prices) {
            if (price.getPriceType() == Price.Type.UNIT_PRICE)
                etUnitPrice.setText(NumberFormatter.formatToTwoDecimalPlaces(price.getUnitPrice()));

            if (price.getPriceType() == Price.Type.BUNDLE_PRICE) {
                etBundlePrice.setText(NumberFormatter.formatToTwoDecimalPlaces(price.getBundlePrice()));
                etBundleQty.setText(NumberFormatter.formatToTwoDecimalPlaces(price.getBundleQuantity()));
            }
        }

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
            case R.id.menu_remove_item_from_list:
                delete();
                return true;
            case android.R.id.home:
                if (mItemHaveChanged)
                    showUnsavedDialog(new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            NavUtils.navigateUpFromSameTask(ItemEditingActivity.this);
                        }
                    });
                else
                    NavUtils.navigateUpFromSameTask(ItemEditingActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    @Override
    protected void onStart()
    {
        mOnTouchListener = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                mItemHaveChanged = true;
                return false;
            }
        };

        etName = (EditText) findViewById(R.id.et_item_name);
        etBrand = (EditText) findViewById(R.id.et_item_brand);
        etDescription = (EditText) findViewById(R.id.et_item_description);
        etCountryOrigin = (EditText) findViewById(R.id.et_item_country_origin);

        etName.setOnTouchListener(mOnTouchListener);
        etBrand.setOnTouchListener(mOnTouchListener);
        etDescription.setOnTouchListener(mOnTouchListener);
        etCountryOrigin.setOnTouchListener(mOnTouchListener);

        super.onStart();
    }

    @Override
    public void onBackPressed()
    {
        if (mItemHaveChanged)
            showUnsavedDialog(new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            });
        else
            super.onBackPressed();
    }

    private void showUnsavedDialog(DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, onClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void alertRequiredField(int messageId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mandatory field(s)");
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     *
     */
    private void delete()
    {
        daoManager.delete(item);
        finish();
    }

    private void save()
    {
        String itemName;
        if (TextUtils.isEmpty(etName.getText())) {
            alertRequiredField(R.string.mandatory_name);
            etName.requestFocus();
            return;
        } else {
            itemName = etName.getText().toString();
        }

        String itemBrand = etBrand.getText().toString();
        String countryOrigin = etCountryOrigin.getText().toString();
        String itemDescription = etDescription.getText().toString();

        String unitPrice = "0.00";
        if (etUnitPrice != null && !TextUtils.isEmpty(etUnitPrice.getText()))
            unitPrice = etUnitPrice.getText().toString();

        String bundlePrice = "0.00";
        if (etBundlePrice != null && !TextUtils.isEmpty(etBundlePrice.getText()))
            bundlePrice = etBundlePrice.getText().toString();

        String bundleQty = "0.00";
        if (etBundleQty != null && !TextUtils.isEmpty(etBundleQty.getText()))
            bundleQty = etBundleQty.getText().toString();

        item.setName(itemName);
        item.setBrand(itemBrand);
        item.setCountryOrigin(countryOrigin);
        item.setDescription(itemDescription);

        for (Price price : prices) {
            if (price.getPriceType() == BUNDLE_PRICE) {
                price.setBundlePrice(Double.parseDouble(bundlePrice), Double.parseDouble(bundleQty));
            } else {
                price.setUnitPrice(Double.parseDouble(unitPrice));
            }
            item.addPrice(price);
        }


        String msg;
        msg = daoManager.update(item, item.getPrices());

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Populate Item object
     *
     * @param cursor
     */
    private void populateItem(Cursor cursor)
    {
        if (cursor == null)
            throw new IllegalArgumentException("Cursor cannot be null");

        long itemId = 0;
        String itemName = "", itemBrand = "", itemDescription = "", currencyCode = "", countryOrigin = "";

        cursor.moveToFirst();
        itemId = cursor.getLong(cursor.getColumnIndex(ItemsEntry._ID));

        int colNameIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_NAME);
        itemName = cursor.getString(colNameIndex);

        int colBrandIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_BRAND);
        itemBrand = cursor.getString(colBrandIdx);

        int colDescriptionIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_DESCRIPTION);
        itemDescription = cursor.getString(colDescriptionIdx);

        int colCountryOriginIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_COUNTRY_ORIGIN);
        countryOrigin = cursor.getString(colCountryOriginIdx);

        item = new Item(itemId, itemName, itemBrand, countryOrigin, itemDescription, null);
    }

    private void populateItemViews()
    {
        if (item == null)
            throw new IllegalArgumentException("ToBuyItem cannot be null");

        etName.setText(item.getName());
        etBrand.setText(item.getBrand());
        etCountryOrigin.setText(item.getCountryOrigin());
        etDescription.setText(item.getDescription());
    }

    private void clearInputFields()
    {
        etName.setText("");
        etBrand.setText("");
        etCountryOrigin.setText("");
        etDescription.setText("");
        etUnitPrice.setText("");
        etBundlePrice.setText("");
        etBundleQty.setText("");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        String[] projection = null;
        Uri uri;
        Loader<Cursor> loader = null;
        switch (loaderId) {
            case ITEM_LOADER_ID:
                projection = new String[]{
                        ItemsEntry._ID,
                        ItemsEntry.COLUMN_NAME,
                        ItemsEntry.COLUMN_BRAND,
                        ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                        ItemsEntry.COLUMN_DESCRIPTION,
                };

                uri = args.getParcelable(ITEM_URL);
                loader = new CursorLoader(this, uri, projection, null, null, null);
                break;

            case ITEM_PRICE_LOADER_ID:
                projection = new String[]{Contract.PricesEntry._ID,
                        Contract.PricesEntry.COLUMN_PRICE_TYPE_ID,
                        Contract.PricesEntry.COLUMN_PRICE,
                        Contract.PricesEntry.COLUMN_BUNDLE_QTY,
                        Contract.PricesEntry.COLUMN_CURRENCY_CODE,
                        Contract.PricesEntry.COLUMN_SHOP_ID};
                uri = args.getParcelable(ITEM_URL);
                long itemId = ContentUris.parseId(uri);
                String selection = Contract.PricesEntry.COLUMN_ITEM_ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(itemId)};
                loader = new CursorLoader(this, PricesEntry.CONTENT_URI, projection, selection,
                        selectionArgs, null);

                break;

            default:
                throw new IllegalArgumentException("Unsupported query");

        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        int loaderId = loader.getId();
        switch (loaderId) {
            case ITEM_LOADER_ID:
                populateItem(cursor);
                populateItemViews();
                break;
            case ITEM_PRICE_LOADER_ID:
                populatePrices(cursor);
                populatePricesViews();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // If the loader is invalidated, clear out all the data from the input fields.
        clearInputFields();
    }

    private void populatePrices(Cursor cursor)
    {
        prices = new ArrayList<>();

        while (cursor.moveToNext()) {
            int colPriceTypeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE_TYPE_ID);
            int priceTypeVal = cursor.getInt(colPriceTypeIdx);

            int colPriceIdIdx = cursor.getColumnIndex(PricesEntry._ID);
            long priceId = cursor.getLong(colPriceIdIdx);

            int colCurrencyCodeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
            String currencyCode = cursor.getString(colCurrencyCodeIdx);

            int colShopIdIdx = cursor.getColumnIndex(PricesEntry.COLUMN_SHOP_ID);
            long shopId = cursor.getLong(colShopIdIdx);

            int colPriceIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);

            Price price = null;

            if (priceTypeVal == UNIT_PRICE.getType()) {
                double unitPrice = cursor.getDouble(colPriceIdx) / 100;
                price = new Price(priceId, unitPrice, currencyCode, shopId, null);
            }

            if (priceTypeVal == BUNDLE_PRICE.getType()) {
                double bundlePrice = cursor.getDouble(colPriceIdx) / 100;
                int colBundleQtyIdx = cursor.getColumnIndex(PricesEntry.COLUMN_BUNDLE_QTY);
                double bundleQty = cursor.getDouble(colBundleQtyIdx);
                price = new Price(priceId, bundlePrice, bundleQty, currencyCode, shopId, null);
            }
            prices.add(price);
        }

    }


}
