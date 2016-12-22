package com.mirzairwan.shopping;

import android.app.LoaderManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;

import java.util.List;

import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;

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
    private static final int LOADER_ID = 20;
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
    private Button btnPrices;
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

        btnPrices = (Button) findViewById(R.id.btn_prices);
        btnPrices.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (prices == null) {
                    prices = daoManager.getItemPrice(item.getId());
                    populatePrices();
                    btnPrices.setVisibility(View.INVISIBLE);

                }
            }
        });

        Bundle arg = new Bundle();
        arg.putParcelable(ITEM_URL, uri);
        getLoaderManager().initLoader(LOADER_ID, arg, this);


    }

    private void populatePrices()
    {
        View pricesView = null;
        FrameLayout priceContainer = (FrameLayout) findViewById(R.id.price_container);
        priceContainer.setVisibility(View.VISIBLE);
        pricesView = getLayoutInflater().inflate(R.layout.price_editing, priceContainer, true);
        etUnitPrice = (EditText) pricesView.findViewById(R.id.et_unit_price);
        etBundlePrice = (EditText) pricesView.findViewById(R.id.et_bundle_price);
        etBundleQty = (EditText) pricesView.findViewById(R.id.et_bundle_qty);

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

            item.addPrice(price);
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
        //if item is in shopping list, do not delete
        if (item.isInBuyList()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.item_is_in_shopping_list);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();

                }
            });
            builder.show();
            return;
        }

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

        for (Price price : item.getPrices()) {
            if (price.getPriceType() == BUNDLE_PRICE) {
                price.setBundlePrice(Double.parseDouble(bundlePrice), Double.parseDouble(bundleQty));
            } else {
                price.setUnitPrice(Double.parseDouble(unitPrice));
            }
        }

        String msg;
        msg = daoManager.update(item, item.getPrices());

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Populate ToBuyItem and Item object
     *
     * @param cursor
     */
    private void populateItemDetails(Cursor cursor)
    {
        if (cursor == null)
            throw new IllegalArgumentException("Cursor cannot be null");

        long itemId = 0;
        String itemName = "", itemBrand = "", itemDescription = "", currencyCode = "", countryOrigin = "";

        while (cursor.moveToNext()) {

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String[] projection = new String[]{
                ItemsEntry._ID,
                ItemsEntry.COLUMN_NAME,
                ItemsEntry.COLUMN_BRAND,
                ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                ItemsEntry.COLUMN_DESCRIPTION,
        };

        Uri uri = args.getParcelable(ITEM_URL);
        CursorLoader loader = new CursorLoader(this, uri, projection, null, null, null);
        return loader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        populateItemDetails(cursor);
        populateItemViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }
}
