package com.mirzairwan.shopping;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
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
    private int actionMode = -1; //Informs the editor whether this activity is creation or updating
    public static final int CREATE_BUY_ITEM_MODE = 1; //use for startActivityForResult
    public static final int EDIT_BUY_ITEM_MODE = 2; //use for startActivityForResult

    private static final String EDIT_ITEM_URI = "EDIT_ITEM_URI";
    private static final int LOADER_ID = 20;

    private Cursor mCursor;
    private ToBuyItem toBuyItem;
    private long defaultShopId = 1;
    private View.OnTouchListener mOnTouchListener;
    private boolean mItemHaveChanged = false;
    private EditText etName;
    private EditText etBrand;
    private EditText etDescription;
    private EditText etQty;
    private EditText etUnitPrice;
    private EditText etBundlePrice;
    private EditText etBundleQty;
    private RadioGroup rgPriceTypeChoice;

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
            Bundle arg = new Bundle();
            arg.putParcelable(EDIT_ITEM_URI, uri);
            getLoaderManager().initLoader(LOADER_ID, arg, this);
            actionMode = EDIT_BUY_ITEM_MODE; //This flag is used for database operation
        } else {
            setTitle(R.string.new_buy_item_title);
            actionMode = CREATE_BUY_ITEM_MODE; // This flag is used for menu creation and database operation
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
        etQty = (EditText) findViewById(R.id.et_item_quantity);
        etUnitPrice = (EditText) findViewById(R.id.et_unit_price);
        etBundlePrice = (EditText) findViewById(R.id.et_bundle_price);
        etBundleQty = (EditText) findViewById(R.id.et_bundle_qty);
        rgPriceTypeChoice = (RadioGroup) findViewById(R.id.price_type_choice);

        etName.setOnTouchListener(mOnTouchListener);
        etBrand.setOnTouchListener(mOnTouchListener);
        etDescription.setOnTouchListener(mOnTouchListener);
        etQty.setOnTouchListener(mOnTouchListener);
        etUnitPrice.setOnTouchListener(mOnTouchListener);
        etBundlePrice.setOnTouchListener(mOnTouchListener);
        etBundleQty.setOnTouchListener(mOnTouchListener);
        rgPriceTypeChoice.setOnTouchListener(mOnTouchListener);

        super.onStart();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (actionMode == CREATE_BUY_ITEM_MODE) {
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
            case R.id.menu_remove_item_from_list:
                delete();
                return true;
            case android.R.id.home:
                if (mItemHaveChanged) {
                    showUnsavedDialog(new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            NavUtils.navigateUpFromSameTask(BuyingActivity.this);
                        }
                    });
                } else
                    NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

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
        builder.setNegativeButton(R.string.keep_editing, null);
        builder.show();
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
        if (!mCursor.moveToNext())
            mCursor.moveToFirst();
        long buyItemId = mCursor.getLong(mCursor.getColumnIndex(ToBuyItemsEntry._ID));
        Uri uriDeleteBuyItem = ContentUris.withAppendedId(ToBuyItemsEntry.CONTENT_URI, buyItemId);
        getContentResolver().delete(uriDeleteBuyItem, null, null);
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
        String itemDescription = etDescription.getText().toString();

        String itemQuantity = "1";
        if (TextUtils.isEmpty(etQty.getText()) || Integer.parseInt(etQty.getText().toString()) < 1) {
            alertRequiredField(R.string.mandatory_quantity);
            etQty.requestFocus();
            return;
        } else {
            itemQuantity = etQty.getText().toString();
        }

        String unitPrice = "0.00";
        if (!TextUtils.isEmpty(etUnitPrice.getText()))
            unitPrice = etUnitPrice.getText().toString();

        String bundlePrice = "0.00";
        if (!TextUtils.isEmpty(etBundlePrice.getText()))
            bundlePrice = etBundlePrice.getText().toString();

        String bundleQty = "0.00";
        if(!TextUtils.isEmpty(etBundleQty.getText()))
            bundleQty = etBundleQty.getText().toString();


        int idSelected = rgPriceTypeChoice.getCheckedRadioButtonId();
        Price.Type selectedPriceType = (idSelected == R.id.rb_bundle_price) ?
                Price.Type.BUNDLE_PRICE : UNIT_PRICE;

        ShoppingList shoppingList = Builder.getShoppingList();

        SharedPreferences prefs = getSharedPreferences(ShoppingActivity.PERSONAL, Activity.MODE_PRIVATE);
        String homeCountryCode = prefs.getString(ShoppingActivity.HOME_COUNTRY_CODE, Locale.getDefault().getCountry());
        Locale homeLocale = new Locale(Locale.getDefault().getLanguage(), homeCountryCode);
        String currencyCode = Currency.getInstance(homeLocale).getCurrencyCode();

        DaoManager daoManager = Builder.getDaoManager(this);

        String msg;
        if (actionMode == CREATE_BUY_ITEM_MODE) {
            ToBuyItem newBuyItem = shoppingList.createItem(itemName, itemBrand, itemDescription,
                    Integer.parseInt(itemQuantity), currencyCode,
                    Double.parseDouble(unitPrice),
                    Double.parseDouble(bundlePrice),
                    Double.parseDouble(bundleQty), selectedPriceType);

            msg = daoManager.insert(newBuyItem, newBuyItem.getItem(),
                    newBuyItem.getItem().getPrices());
        } else //Existing buy item
        {
            toBuyItem.getItem().setName(itemName);
            toBuyItem.getItem().setBrand(itemBrand);
            toBuyItem.getItem().setCountryOrigin("SG");
            toBuyItem.getItem().setDescription(itemDescription);

            for (Price price : toBuyItem.getItem().getPrices()) {
                if (price.getPriceType() == BUNDLE_PRICE) {
                    price.setBundlePrice(Double.parseDouble(bundlePrice), Double.parseDouble(bundleQty));
                } else {
                    price.setUnitPrice(Double.parseDouble(unitPrice));
                }
            }

            toBuyItem.setQuantity(Integer.parseInt(itemQuantity));
            toBuyItem.selectPrice(defaultShopId, selectedPriceType);

            msg = daoManager.update(toBuyItem, toBuyItem.getItem(),
                    toBuyItem.getItem().getPrices());
        }

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
        long buyItemId = 0;
        String itemName = "", itemBrand = "", itemDescription = "", currencyCode = "";
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
                itemName = cursor.getString(colNameIndex);

                int colBrandIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_BRAND);
                itemBrand = cursor.getString(colBrandIdx);

                int colQtyIdx = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
                buyQty = cursor.getInt(colQtyIdx);

                int colDescriptionIdx = cursor.getColumnIndex(ItemsEntry.COLUMN_DESCRIPTION);
                itemDescription = cursor.getString(colDescriptionIdx);

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
                unitPrice = cursor.getDouble(colPriceIdx) / 100;
                Price price = new Price(priceId, unitPrice, currencyCode, shopId, null);
                toBuyItem.getItem().addPrice(price);

            }

            if (priceTypeVal == BUNDLE_PRICE.getType()) {
                bundlePrice = cursor.getDouble(colPriceIdx) / 100;
                int colBundleQtyIdx = cursor.getColumnIndex(PricesEntry.COLUMN_BUNDLE_QTY);
                bundleQty = cursor.getDouble(colBundleQtyIdx);
                Price price = new Price(priceId, bundlePrice, bundleQty, currencyCode, shopId, null);
                toBuyItem.getItem().addPrice(price);
            }

            if (priceId == selectedPriceId) {
                priceType = priceTypeVal == BUNDLE_PRICE.getType() ? BUNDLE_PRICE : UNIT_PRICE;
                toBuyItem.selectPrice(shopId, priceType);

            }

            isItemDetailsPopulated = true;
        }
    }

    private void populateItemViews()
    {
        if (toBuyItem == null)
            throw new IllegalArgumentException("ToBuyItem cannot be null");

        etName.setText(toBuyItem.getItem().getName());
        etBrand.setText(toBuyItem.getItem().getBrand());
        etQty.setText(String.valueOf(toBuyItem.getQuantity()));
        etDescription.setText(toBuyItem.getItem().getDescription());

        for (Price price : toBuyItem.getItem().getPrices()) {
            if (price.getPriceType() == UNIT_PRICE) {
                etUnitPrice.setText(NumberFormatter.formatToTwoDecimalPlaces(
                        price.getUnitPrice()));
            }

            if (price.getPriceType() == BUNDLE_PRICE) {
                etBundlePrice.setText(NumberFormatter.formatToTwoDecimalPlaces(price.getBundlePrice()));
                etBundleQty.setText(NumberFormatter.formatToTwoDecimalPlaces(price.getBundleQuantity()));
            }

        }


        rgPriceTypeChoice.check(toBuyItem.getSelectedPriceType() == BUNDLE_PRICE ?
                R.id.rb_bundle_price : R.id.rb_unit_price);

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

        Uri uri = args.getParcelable(EDIT_ITEM_URI);
        CursorLoader loader = new CursorLoader(this, uri, projection, null, null, null);
        return loader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        mCursor = cursor;
        populateItemDetails(cursor);
        populateItemViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mCursor = null;
    }
}
