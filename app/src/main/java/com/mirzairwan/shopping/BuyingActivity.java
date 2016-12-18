package com.mirzairwan.shopping;

import android.app.Activity;
import android.content.Intent;
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

import com.mirzairwan.shopping.data.DaoManager;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.ShoppingList;
import com.mirzairwan.shopping.domain.ToBuyItem;

import static com.mirzairwan.shopping.ShoppingActivity.CREATE_BUY_ITEM_REQUEST_CODE;

/**
 * Display the buy item details in a screen
 * Create new buy item
 * Update and save buy item details
 * Delete existing buy items
 * <p>
 * Create new buy item uses the NEW_BUY_ITEM_MODE
 * Action modes
 */
public class BuyingActivity extends AppCompatActivity
{


    public static final String IS_MARKED_FOR_DELETION = "IS_MARKED_FOR_DELETION";

    private int actionMode = -1; //Informs the editor whether this activity is creation or updating

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editing);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        //if no bundle, then the request is for new creation
        if (uri != null) {
            setTitle(R.string.update_buy_item_title);
            populateItemDetails();
        } else {
            setTitle(R.string.new_buy_item_title);
            actionMode = CREATE_BUY_ITEM_REQUEST_CODE; // This flag is used for menu creation
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.item_details, menu);
        if (actionMode == CREATE_BUY_ITEM_REQUEST_CODE) {
            menu.removeItem(R.id.menu_remove_item_from_list);
        }
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
                //delete();
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
//    private void delete()
//    {
//        Intent deleteIntent = new Intent();
//        //Get the position of the buy item
//        int buyItemPosition = getBuyItemPosition();
//
//        //Inform caller that user intends to remove buy item from list. Use action_mode to signal this
//        deleteIntent.putExtra(IS_MARKED_FOR_DELETION, true);
//
//        //Put buy item position in a bundle
//        Bundle args = new Bundle();
//        args.putInt(BUY_ITEM_POSITION, buyItemPosition);
//        deleteIntent.putExtra(ARGUMENTS, args);
//        setResult(Activity.RESULT_OK, deleteIntent);
//        finish();
//    }
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
        Price.Type priceType = (idSelected == R.id.rb_bundle_price) ?
                Price.Type.BUNDLE_PRICE : Price.Type.UNIT_PRICE;

        ShoppingList shoppingList = Builder.getShoppingList();
        String currencyCode = "SGD";
        ToBuyItem toBuyItem = shoppingList.addNewItem(itemName, itemBrand, itemDescription,
                Integer.parseInt(itemQuantity), currencyCode,
                Double.parseDouble(unitPrice),
                Double.parseDouble(bundlePrice),
                Double.parseDouble(bundleQty), priceType);

        DaoManager daoManager = Builder.getDaoManager(this);
        String msg = daoManager.insert(toBuyItem, toBuyItem.getItem(),
                toBuyItem.getItem().getPrices());

        setResult(Activity.RESULT_OK);

        Toast.makeText(this, msg, Toast.LENGTH_SHORT);

        finish();
    }

//    private int getBuyItemPosition()
//    {
//        Bundle args = getIntent().getBundleExtra(ARGUMENTS);
//        int buyItemPosition = -1; //Initialize to -1 because assume it is a creation of item
//        if (args != null)
//            buyItemPosition = args.getInt(BUY_ITEM_POSITION); //this is an edit or delete
//        return buyItemPosition;
//    }


    private void populateItemDetails()
    {

//        Bundle buyItemBundle = getIntent().getBundleExtra(ARGUMENTS);
//
//        if (buyItemBundle == null)
//            finish();
//
//        EditText etItemName = (EditText) findViewById(R.id.et_item_name);
//        etItemName.setText(buyItemBundle.getString(BuyItemBundle.ITEM_NAME));
//
//        EditText etItemBrand = (EditText) findViewById(R.id.et_item_brand);
//        etItemBrand.setText(buyItemBundle.getString(BuyItemBundle.ITEM_BRAND));
//
//        EditText etQuantity = (EditText) findViewById(R.id.et_item_quantity);
//        etQuantity.setText(buyItemBundle.getInt(BuyItemBundle.ITEM_QUANTITY) + "");
//
//        EditText etUnitPrice = (EditText) findViewById(R.id.et_unit_price);
//        etUnitPrice.setText(NumberFormatter.formatToTwoDecimalPlaces(
//                                        buyItemBundle.getDouble(BuyItemBundle.UNIT_PRICE))
//                            );
//
//        EditText etBundlePrice = (EditText) findViewById(R.id.et_bundle_price);
//        etBundlePrice.setText(NumberFormatter.formatToTwoDecimalPlaces(
//                buyItemBundle.getDouble(BuyItemBundle.BUNDLE_PRICE))
//                            );
//
//        EditText etBundleQty = (EditText) findViewById(R.id.et_bundle_qty);
//        double bundleQuantity = buyItemBundle.getDouble(BuyItemBundle.BUNDLE_QTY);
//        etBundleQty.setText(bundleQuantity + "");
//
//        EditText etItemDescription = (EditText) findViewById(R.id.et_item_description);
//        etItemDescription.setText(buyItemBundle.getString(BuyItemBundle.ITEM_DESCRIPTION));
//
//        RadioGroup priceTypeChoice = (RadioGroup) findViewById(R.id.price_type_choice);
//        Price.Type priceType = (Price.Type) buyItemBundle.getSerializable(BuyItemBundle.SELECTED_PRICE_TYPE);
//        priceTypeChoice.check(priceType == Price.Type.BUNDLE_PRICE ?
//                R.id.rb_bundle_price : R.id.rb_unit_price);

    }

}
