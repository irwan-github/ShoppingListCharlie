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
import android.text.TextUtils;
import android.view.Menu;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.data.Contract.ItemsEntry;
import com.mirzairwan.shopping.data.Contract.PricesEntry;
import com.mirzairwan.shopping.data.Contract.ToBuyItemsEntry;
import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ToBuyItem;

import java.util.ArrayList;
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
public class BuyingActivity extends ItemEditingActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private int actionMode = -1; //Informs the editor whether this activity is creation or updating
    public static final int CREATE_BUY_ITEM_MODE = 1; //use for startActivityForResult
    public static final int EDIT_BUY_ITEM_MODE = 2; //use for startActivityForResult

    private static final String EDIT_ITEM_URI = "EDIT_ITEM_URI";
    private static final int PURCHASE_ITEM_LOADER_ID = 30;
    private static final int ITEM_PRICE_LOADER_ID = 31;

    private Cursor mCursor;
    private ToBuyItem toBuyItem;

    private long defaultShopId = 1;

    private EditText etQty;
    private RadioGroup rgPriceTypeChoice;

    private long selectedPriceId;
    private long buyItemId;

    private boolean isDeleting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buying_item);
        setPurchaseItemTitle();
    }

    private void setPurchaseItemTitle()
    {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri == null) {
            setTitle(R.string.new_buy_item_title);
            actionMode = CREATE_BUY_ITEM_MODE; // This flag is used for menu creation and database operation
        } else
            setTitle(R.string.view_buy_item_details);
    }

    @Override
    protected void initLoaders()
    {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        //if no bundle, then the request is for new creation
        if (uri != null) {
            Bundle arg = new Bundle();
            arg.putParcelable(EDIT_ITEM_URI, uri);
            getLoaderManager().initLoader(PURCHASE_ITEM_LOADER_ID, arg, this);
            getLoaderManager().initLoader(ITEM_PRICE_LOADER_ID, arg, this);
            actionMode = EDIT_BUY_ITEM_MODE; //This flag is used for database operation
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        etQty = (EditText) findViewById(R.id.et_item_quantity);
        etQty.setOnTouchListener(mOnTouchListener);

        rgPriceTypeChoice = (RadioGroup) findViewById(R.id.price_type_choice);
        rgPriceTypeChoice.setOnTouchListener(mOnTouchListener);


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
    protected void delete()
    {
        isDeleting = true;
        Uri uriDeleteBuyItem = ContentUris.withAppendedId(ToBuyItemsEntry.CONTENT_URI, buyItemId);
        getContentResolver().delete(uriDeleteBuyItem, null, null);
        finish();
    }

    protected Price.Type getSelectedPriceType()
    {

        int idSelected = rgPriceTypeChoice.getCheckedRadioButtonId();
        Price.Type selectedPriceType = (idSelected == R.id.rb_bundle_price) ?
                Price.Type.BUNDLE_PRICE : UNIT_PRICE;

        return selectedPriceType;
    }

    protected Price getSelectedPrice()
    {
        Price.Type selectedPriceType = getSelectedPriceType();
        Price selectedPrice = null;

        for (Price price : mPrices) {
            if (selectedPriceType == price.getPriceType())
                selectedPrice = price;
        }

        return selectedPrice;
    }


    @Override
    protected void populatePricesForSaving(String unitPriceDbl, String bundlePriceDbl, String bundleQtyDbl)
    {
        SharedPreferences prefs = getSharedPreferences(ShoppingActivity.PERSONAL, Activity.MODE_PRIVATE);
        String homeCountryCode = prefs.getString(ShoppingActivity.HOME_COUNTRY_CODE, Locale.getDefault().getCountry());
        Locale homeLocale = new Locale(Locale.getDefault().getLanguage(), homeCountryCode);
        String currencyCode = Currency.getInstance(homeLocale).getCurrencyCode();

        mPrices = new ArrayList<>();
        Price unitPrice = new Price(Double.parseDouble(unitPriceDbl), currencyCode, defaultShopId);
        mPrices.add(unitPrice);
        Price bundlePrice = new Price(Double.parseDouble(bundlePriceDbl), Double.parseDouble(bundleQtyDbl), currencyCode, defaultShopId);
        mPrices.add(bundlePrice);
    }

    @Override
    protected void save()
    {
        getItemFromUserInput();

        String itemQuantity = "1";
        if (TextUtils.isEmpty(etQty.getText()) || Integer.parseInt(etQty.getText().toString()) < 1) {
            alertRequiredField(R.string.mandatory_quantity);
            etQty.requestFocus();
            return;
        } else {
            itemQuantity = etQty.getText().toString();
        }

        DaoManager daoManager = Builder.getDaoManager(this);
        String msg;

        if (actionMode == CREATE_BUY_ITEM_MODE) {

            populatePricesForSaving(getUnitPriceFromUserInput(), getBundlePriceFromUserInput(), getBundleQtyFromUserInput());

            Price selectedPrice = getSelectedPrice();

            toBuyItem = new ToBuyItem(item, Integer.parseInt(itemQuantity), selectedPrice);

            msg = daoManager.insert(toBuyItem, toBuyItem.getItem(), mPrices);
        } else //Existing buy item
        {
            super.populatePricesForSaving(getUnitPriceFromUserInput(), getBundlePriceFromUserInput(), getBundleQtyFromUserInput());

            toBuyItem.setQuantity(Integer.parseInt(itemQuantity));

            toBuyItem.selectPrice(defaultShopId, getSelectedPriceType());

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
        buyItemId = cursor.getLong(cursor.getColumnIndex(ToBuyItemsEntry._ID));

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

        if (priceTypeVal == Price.Type.UNIT_PRICE.getType())
            price = new Price(priceId, priceDbl, currencyCode, shopId, null);

        if (priceTypeVal == Price.Type.BUNDLE_PRICE.getType())
            price = new Price(priceId, priceDbl, bundleQty, currencyCode, shopId, null);

        toBuyItem = new ToBuyItem(buyItemId, buyQty, price, item, null);
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
                // Proceed with moving to the first row of the cursor for purchase item and reading data from it
                // (This should be the only purchase item row in the cursor)
                if (cursor.moveToFirst()) {
                    populateItemDetails(cursor);
                    populatePurchaseDetails(cursor);
                    populateItemViews();
                    populatePurchaseView();
                }
                break;

            case ITEM_PRICE_LOADER_ID:
                if (!isDeleting) {
                    populatePrices(cursor);
                    populatePricesViews();
                }
                break;

            default:
                throw new IllegalArgumentException("Loader not supported");
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // If the loader is invalidated, clear out all the data from the input fields.
        clearItemInputFields();
        clearPriceInputFields();
        clearPurchaseInputFields();
    }

    private void clearPurchaseInputFields()
    {
        etQty.setText("");
        rgPriceTypeChoice.clearCheck();
    }
}
