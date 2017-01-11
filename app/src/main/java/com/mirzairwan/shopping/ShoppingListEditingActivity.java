package com.mirzairwan.shopping;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mirzairwan.shopping.data.Contract;
import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ToBuyItem;

import static com.mirzairwan.shopping.domain.Price.Type.BUNDLE_PRICE;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;

/**
 * Created by Mirza Irwan on 2/1/17.
 */

public class ShoppingListEditingActivity extends ItemActivity
{
    private static final String LOG_TAG = ShoppingListEditingActivity.class.getSimpleName();
    private static final String URI_ITEM = "uri"; //Used for saving instant state
    private int actionMode = -1; //Informs the editor whether this activity is creation or updating
    public static final int CREATE_BUY_ITEM_MODE = 1; //use for action mode
    public static final int EDIT_BUY_ITEM_MODE = 2; //use for action mode
    private static final int PURCHASE_ITEM_LOADER_ID = 30;
    private boolean isItemDeleted = false; // After deleting, don't update picture and prices

    private EditText etQty;
    private RadioGroup rgPriceTypeChoice;

    private long defaultShopId = 1;
    private ToBuyItem mToBuyItem;
    private Uri mUriItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, ">>>savedInstantState is " + (savedInstanceState == null? "NULL" : "NOT NULL"));

        etQty = (EditText) findViewById(R.id.et_item_quantity);
        etQty.setOnTouchListener(mOnTouchListener);

        //set touchListener for Radio Group
        rgPriceTypeChoice = (RadioGroup) findViewById(R.id.price_type_choice);
        rgPriceTypeChoice.setOnTouchListener(mOnTouchListener);


        if(savedInstanceState != null) //Restore from previous state
        {
            mUriItem = savedInstanceState.getParcelable(URI_ITEM);
        }
        else
        {
            Intent intent = getIntent();
            mUriItem = intent.getData();
        }

        if (mUriItem == null) {
            setTitle(R.string.new_buy_item_title);
            actionMode = CREATE_BUY_ITEM_MODE; // This flag is used for menu creation and database operation
        } else {
            setTitle(R.string.view_buy_item_details);
            actionMode = EDIT_BUY_ITEM_MODE; //This flag is used for database operation
        }

        initLoaders(mUriItem);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (actionMode == CREATE_BUY_ITEM_MODE) {

            //When adding new item to shopping list, set currency symbol according to current country
            // code preference for Price-related EditText
            etCurrencyCode.setText(FormatHelper.getCurrencyCode(mCountryCode));
            setCurrencySymbol(FormatHelper.getCurrencyCode(mCountryCode));
        }
    }

    private void initLoaders(Uri uri)
    {
        //if no bundle, then the request is for new creation
        if (uri != null) {
            Bundle arg = new Bundle();
            arg.putParcelable(ITEM_URI, uri);

            getLoaderManager().initLoader(PURCHASE_ITEM_LOADER_ID, arg, this);
            //getLoaderManager().restartLoader(PURCHASE_ITEM_LOADER_ID, arg, this);
            super.initPictureLoader(uri, this);
            super.initPriceLoader(uri, this);
        }

    }

    @Override
    protected int getLayoutXml()
    {
        return R.layout.activity_buying_item;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
    {
        String[] projection = null;
        Uri uri;
        Loader<Cursor> loader = null;

        switch (loaderId) {
            case PURCHASE_ITEM_LOADER_ID:
                projection = new String[]{Contract.ToBuyItemsEntry._ID,
                        Contract.ToBuyItemsEntry.COLUMN_ITEM_ID,
                        Contract.ToBuyItemsEntry.COLUMN_QUANTITY,
                        Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED,
                        Contract.ToBuyItemsEntry.COLUMN_SELECTED_PRICE_ID,
                        Contract.ItemsEntry.COLUMN_NAME,
                        Contract.ItemsEntry.COLUMN_BRAND,
                        Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN,
                        Contract.ItemsEntry.COLUMN_DESCRIPTION,
                        Contract.PricesEntry.ALIAS_ID,
                        Contract.PricesEntry.COLUMN_PRICE_TYPE_ID,
                        Contract.PricesEntry.COLUMN_PRICE,
                        Contract.PricesEntry.COLUMN_BUNDLE_QTY,
                        Contract.PricesEntry.COLUMN_CURRENCY_CODE,
                        Contract.PricesEntry.COLUMN_SHOP_ID
                };
                uri = args.getParcelable(ITEM_URI);
                loader = new CursorLoader(this, uri, projection, null, null, null);
                break;

            default:
                loader = super.onCreateLoader(loaderId, args); //Reuse superclass

        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        if (cursor == null || cursor.getCount() < 1)
            return;

        int loaderId = loader.getId();
        switch (loaderId) {

            case PURCHASE_ITEM_LOADER_ID:
                // Proceed with moving to the first row of the cursor for purchase mItem and reading data from it
                // (This should be the only purchase mItem row in the cursor)
                if (cursor.moveToFirst()) {
                    //populateItemDetails(cursor);
                    mItem = createItem(Contract.ToBuyItemsEntry.COLUMN_ITEM_ID, Contract.ItemsEntry.COLUMN_NAME,
                            Contract.ItemsEntry.COLUMN_BRAND, Contract.ItemsEntry.COLUMN_DESCRIPTION,
                            Contract.ItemsEntry.COLUMN_COUNTRY_ORIGIN, cursor);
                    mToBuyItem = createPurchase(cursor);
                    populateItemInputFields(mItem);
                    populatePurchaseView(mToBuyItem);
                }
                break;

            default:
                if (isItemDeleted) //Don't populate prices and pictures
                    return;
                else
                    super.onLoadFinished(loader, cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        super.onLoaderReset(loader);
        clearPurchaseInputFields();
    }

    private ToBuyItem createPurchase(Cursor cursor)
    {
        long buyItemId = cursor.getLong(cursor.getColumnIndex(Contract.ToBuyItemsEntry._ID));

        int colQtyIdx = cursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_QUANTITY);
        int buyQty = cursor.getInt(colQtyIdx);

        int colIsChecked = cursor.getColumnIndex(Contract.ToBuyItemsEntry.COLUMN_IS_CHECKED);
        boolean isItemChecked = cursor.getInt(colIsChecked) > 0;

        int colPriceTypeIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE_TYPE_ID);
        int priceTypeVal = cursor.getInt(colPriceTypeIdx);

        int colPriceIdIdx = cursor.getColumnIndex(Contract.PricesEntry.ALIAS_ID);
        long priceId = cursor.getLong(colPriceIdIdx);

        int colBundleQtyIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_BUNDLE_QTY);
        long bundleQty = cursor.getLong(colBundleQtyIdx);

        int colCurrencyCodeIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_CURRENCY_CODE);
        String currencyCode = cursor.getString(colCurrencyCodeIdx);

        int colShopIdIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_SHOP_ID);
        long shopId = cursor.getLong(colShopIdIdx);

        Price price = null;
        int colPriceIdx = cursor.getColumnIndex(Contract.PricesEntry.COLUMN_PRICE);

        double priceDbl = cursor.getDouble(colPriceIdx) / 100;

        if (priceTypeVal == Price.Type.UNIT_PRICE.getType())
            price = new Price(priceId, priceDbl, currencyCode, shopId, null);

        if (priceTypeVal == Price.Type.BUNDLE_PRICE.getType())
            price = new Price(priceId, priceDbl, bundleQty, currencyCode, shopId, null);


        ToBuyItem toBuyItem = new ToBuyItem(buyItemId, buyQty, price, mItem, null);
        toBuyItem.setCheck(isItemChecked);
        return toBuyItem;


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (actionMode == CREATE_BUY_ITEM_MODE) {
            menu.removeItem(R.id.menu_remove_item_from_list);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void populatePurchaseView(ToBuyItem toBuyItem)
    {
        etQty.setText(String.valueOf(toBuyItem.getQuantity()));
        rgPriceTypeChoice.check(toBuyItem.getSelectedPriceType() == BUNDLE_PRICE ?
                R.id.rb_bundle_price : R.id.rb_unit_price);
    }

    private void clearPurchaseInputFields()
    {
        etQty.setText("");
        rgPriceTypeChoice.clearCheck();
    }

    @Override
    protected void delete()
    {
        isItemDeleted = true; //the onLoaderFinished method will use this flag to decide whether to load prices
        Uri uriDeleteBuyItem = ContentUris.withAppendedId(Contract.ToBuyItemsEntry.CONTENT_URI, mToBuyItem.getId());
        getContentResolver().delete(uriDeleteBuyItem, null, null);
        finish();

    }

    @Override
    protected void save()
    {
        if (!fieldsValidated())
            return;

        Item item = getItemFromInputField();

        String itemQuantity = "1";
        if (TextUtils.isEmpty(etQty.getText()) || Integer.parseInt(etQty.getText().toString()) < 1) {
            alertRequiredField(R.string.message_title, R.string.mandatory_quantity);
            etQty.requestFocus();
            return;
        } else {
            itemQuantity = etQty.getText().toString();
        }

        priceMgr.setItemPricesForSaving(item, getUnitPriceFromInputField(), getBundlePriceFromInputField(), getBundleQtyFromInputField());
        priceMgr.setCurrencyCode(etCurrencyCode.getText().toString());

        String msg;

        if (actionMode == CREATE_BUY_ITEM_MODE) {

            ToBuyItem toBuyItem = shoppingList.addNewItem(item, Integer.parseInt(itemQuantity), priceMgr.getSelectedPrice(getSelectedPriceType()));

            msg = daoManager.insert(toBuyItem, toBuyItem.getItem(), item.getPrices(), mPictureMgr);

        } else //Existing item in the shopping list
        {
            if (mPictureMgr.getItemId() == -1)
                mPictureMgr.setItemId(item.getId());

            mToBuyItem.setItem(item);

            mToBuyItem.setQuantity(Integer.parseInt(itemQuantity));

            mToBuyItem.selectPrice(defaultShopId, getSelectedPriceType());

            msg = daoManager.update(mToBuyItem, item, item.getPrices(), mPictureMgr);
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        finish();

    }

    protected Price.Type getSelectedPriceType()
    {

        int idSelected = rgPriceTypeChoice.getCheckedRadioButtonId();
        Price.Type selectedPriceType = (idSelected == R.id.rb_bundle_price) ?
                Price.Type.BUNDLE_PRICE : UNIT_PRICE;

        return selectedPriceType;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(URI_ITEM, mUriItem);
    }
}
