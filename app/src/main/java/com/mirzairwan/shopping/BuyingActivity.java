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
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ShoppingList;
import com.mirzairwan.shopping.domain.ToBuyItem;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
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
    private static final int ITEM_PRICE_LOADER_ID = 21;
    private int actionMode = -1; //Informs the editor whether this activity is creation or updating
    public static final int CREATE_BUY_ITEM_MODE = 1; //use for startActivityForResult
    public static final int EDIT_BUY_ITEM_MODE = 2; //use for startActivityForResult

    private static final String EDIT_ITEM_URI = "EDIT_ITEM_URI";
    private static final int PURCHASE_ITEM_LOADER_ID = 20;

    private Cursor mCursor;
    private ToBuyItem toBuyItem;
    private long defaultShopId = 1;
    private View.OnTouchListener mOnTouchListener;
    private boolean mItemHaveChanged = false;
    private EditText etName;
    private EditText etBrand;
    private EditText etCountryOrigin;
    private EditText etDescription;
    private EditText etQty;
    private EditText etUnitPrice;
    private EditText etBundlePrice;
    private EditText etBundleQty;
    private RadioGroup rgPriceTypeChoice;
    private Item item;
    private List<Price> prices;
    private long selectedPriceId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buying_item);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        //if no bundle, then the request is for new creation
        if (uri != null) {
            setTitle(R.string.view_buy_item_details);
            Bundle arg = new Bundle();
            arg.putParcelable(EDIT_ITEM_URI, uri);
            getLoaderManager().initLoader(PURCHASE_ITEM_LOADER_ID, arg, this);
            getLoaderManager().initLoader(ITEM_PRICE_LOADER_ID, arg, this);
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
        etCountryOrigin = (EditText) findViewById(R.id.et_item_country_origin);
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
        String countryOrigin = etCountryOrigin.getText().toString();
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
        if (!TextUtils.isEmpty(etBundleQty.getText()))
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

            newBuyItem.getItem().setCountryOrigin(countryOrigin);

            msg = daoManager.insert(newBuyItem, newBuyItem.getItem(), newBuyItem.getItem().getPrices());
        } else //Existing buy item
        {
            toBuyItem.getItem().setName(itemName);
            toBuyItem.getItem().setBrand(itemBrand);
            toBuyItem.getItem().setCountryOrigin(countryOrigin);
            toBuyItem.getItem().setDescription(itemDescription);

            for (Price price : prices)
            {
                if (price.getPriceType() == BUNDLE_PRICE) {
                    price.setBundlePrice(Double.parseDouble(bundlePrice), Double.parseDouble(bundleQty));
                }
                if(price.getPriceType() == UNIT_PRICE)
                {
                    price.setUnitPrice(Double.parseDouble(unitPrice));
                }
                toBuyItem.getItem().addPrice(price);
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
        String itemName = "", itemBrand = "", itemDescription = "", countryOrigin = "";

        itemId = cursor.getLong(cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_ITEM_ID));

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

    private void populatePurchaseDetails(Cursor cursor)
    {
        long buyItemId = cursor.getLong(cursor.getColumnIndex(ToBuyItemsEntry._ID));

        int colQtyIdx = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_QUANTITY);
        int buyQty = cursor.getInt(colQtyIdx);

        int colSelectedPriceIdIdx = cursor.getColumnIndex(ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID);
        selectedPriceId = cursor.getLong(colSelectedPriceIdIdx);

        int colPriceTypeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE_TYPE_ID);
        int priceTypeVal = cursor.getInt(colPriceTypeIdx);

        int colPriceIdIdx = cursor.getColumnIndex(PricesEntry.ALIAS_ID);
        long priceId = cursor.getLong(colPriceIdIdx);

        int colBundleQtyIdx = cursor.getColumnIndex(PricesEntry.COLUMN_BUNDLE_QTY);
        long bundleQty = cursor.getLong(colBundleQtyIdx);

        int colCurrencyCodeIdx = cursor.getColumnIndex(PricesEntry.COLUMN_CURRENCY_CODE);
        String currencyCode = cursor.getString(colCurrencyCodeIdx);

        int colShopIdIdx = cursor.getColumnIndex(PricesEntry.COLUMN_SHOP_ID);
        long shopId = cursor.getLong(colShopIdIdx);

        Price price = null;
        int colPriceIdx = cursor.getColumnIndex(PricesEntry.COLUMN_PRICE);

        double priceDbl = cursor.getDouble(colPriceIdx) / 100;

        if(priceTypeVal == Price.Type.UNIT_PRICE.getType())
            price = new Price(priceId, priceDbl, currencyCode, shopId, null);

        if(priceTypeVal == Price.Type.BUNDLE_PRICE.getType())
            price = new Price(priceId, priceDbl, bundleQty, currencyCode, shopId, null);

        toBuyItem = new ToBuyItem(buyItemId, buyQty, price, item, null);
    }

    private void populateItemViews()
    {
        if (toBuyItem == null)
            throw new IllegalArgumentException("ToBuyItem cannot be null");

        etName.setText(toBuyItem.getItem().getName());
        etBrand.setText(toBuyItem.getItem().getBrand());
        etCountryOrigin.setText(toBuyItem.getItem().getCountryOrigin());
        etDescription.setText(toBuyItem.getItem().getDescription());
    }

    private void populatePrices(Cursor cursor)
    {
        prices = new ArrayList<>();

        while (cursor.moveToNext()) {
            int colPriceTypeIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE_TYPE_ID);
            int priceTypeVal = cursor.getInt(colPriceTypeIdx);

            int colPriceIdIdx = cursor.getColumnIndex(Contract.PricesEntry._ID);
            long priceId = cursor.getLong(colPriceIdIdx);

            int colCurrencyCodeIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
            String currencyCode = cursor.getString(colCurrencyCodeIdx);

            int colShopIdIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_SHOP_ID);
            long shopId = cursor.getLong(colShopIdIdx);

            int colPriceIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE);

            Price price = null;

            if (priceTypeVal == UNIT_PRICE.getType()) {
                double unitPrice = cursor.getDouble(colPriceIdx) / 100;
                price = new Price(priceId, unitPrice, currencyCode, shopId, null);
            }

            if (priceTypeVal == BUNDLE_PRICE.getType()) {
                double bundlePrice = cursor.getDouble(colPriceIdx) / 100;
                int colBundleQtyIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_BUNDLE_QTY);
                double bundleQty = cursor.getDouble(colBundleQtyIdx);
                price = new Price(priceId, bundlePrice, bundleQty, currencyCode, shopId, null);
            }
            prices.add(price);
        }

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

    private void populatePurchaseView()
    {
        etQty.setText(String.valueOf(toBuyItem.getQuantity()));
        rgPriceTypeChoice.check(toBuyItem.getSelectedPriceType() == BUNDLE_PRICE ?
        R.id.rb_bundle_price : R.id.rb_unit_price);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        String[] projection = null;
        Uri uri;
        Loader<Cursor> loader = null;

        switch (loaderId) {
            case PURCHASE_ITEM_LOADER_ID:
                projection = new String[]{ToBuyItemsEntry._ID,
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
                        PricesEntry.COLUMN_SHOP_ID
                };
                uri = args.getParcelable(EDIT_ITEM_URI);
                loader = new CursorLoader(this, uri, projection, null, null, null);
                break;

            case ITEM_PRICE_LOADER_ID:
                projection = new String[]{Contract.PricesEntry._ID,
                        Contract.PricesEntry.COLUMN_PRICE_TYPE_ID,
                        Contract.PricesEntry.COLUMN_PRICE,
                        Contract.PricesEntry.COLUMN_BUNDLE_QTY,
                        Contract.PricesEntry.COLUMN_CURRENCY_CODE,
                        Contract.PricesEntry.COLUMN_SHOP_ID};
                uri = args.getParcelable(EDIT_ITEM_URI);
                long itemId = ContentUris.parseId(uri);
                String selection = Contract.PricesEntry.COLUMN_ITEM_ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(itemId)};
                loader = new CursorLoader(this, Contract.PricesEntry.CONTENT_URI, projection, selection,
                        selectionArgs, null);
                break;

            default:
                throw new IllegalArgumentException("Query not supported");

        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        int loaderId = loader.getId();
        switch (loaderId) {
            case PURCHASE_ITEM_LOADER_ID:
                mCursor = cursor;
                cursor.moveToFirst();
                populateItemDetails(cursor);
                populatePurchaseDetails(cursor);
                populateItemViews();
                populatePurchaseView();
                break;

            case ITEM_PRICE_LOADER_ID:
                populatePrices(cursor);
                populatePricesViews();
                break;

            default:
                throw new IllegalArgumentException("Loader not supported");
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mCursor = null;
    }
}
