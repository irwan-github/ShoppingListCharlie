package com.mirzairwan.shopping;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mirzairwan.shopping.domain.ItemInShoppingList;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_CURRENCY_CODE_EMPTY;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_INVALID_BUNDLE_QTY;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_INVALID_CURRENCY_CODE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_INVALID_MULTIPLES;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_LOAD_BUNDLE_PX;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_LOAD_PX_FINISHED;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_LOAD_UNIT_PX;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_NEW;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_SELECT_BUNDLE_PRICE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_SELECT_UNIT_PRICE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_UNIT_BUY_QTY_ERROR;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_VALIDATE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_VALIDATE_CURRENCY_CODE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_VALID_BUNDLE_QTY;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_VALID_BUY_QTY;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_VALID_CURRENCY_CODE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_VALID_MULTIPLES;
import static com.mirzairwan.shopping.ItemBuyFieldControl.State.BUY_ERROR;
import static com.mirzairwan.shopping.ItemBuyFieldControl.State.NEUTRAL;
import static com.mirzairwan.shopping.ItemBuyFieldControl.State.NEUTRAL_CURRENCY_CODE;
import static java.lang.Integer.parseInt;


/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class ItemBuyFieldControl
{
        private final String mDefaultCurrencyCode;
        private ItemContext mItemContext;
        private RadioButton mRbBundlePrice;
        private RadioButton mRbUnitPrice;
        private OnSelectedPriceChangeListener mOnSelectedPriceChangeListener;

        private TextInputEditText mEtQtyToBuy;
        private PurchaseManager mPurchaseManager;
        private RadioGroup mRgPriceTypeChoice;

        private PriceMgr mPriceMgr;

        private PriceField mUnitPrice;
        private PriceField mBundlePrice;

        private TextInputEditText mEtCurrencyCode;
        private TextInputEditText mEtBundleQty;

        private State mPriceSelectState = NEUTRAL;
        private State mBundleQtyState = NEUTRAL;
        private State mBuyQtyState = NEUTRAL;
        private State mCurrencyCodeState = NEUTRAL_CURRENCY_CODE;

        public ItemBuyFieldControl(ItemContext itemContext, String currencyCode)
        {
                mItemContext = itemContext;
                mDefaultCurrencyCode = currencyCode;
                mEtQtyToBuy = (TextInputEditText) itemContext.findViewById(R.id.et_item_quantity);
                mRgPriceTypeChoice = (RadioGroup) itemContext.findViewById(R.id.price_type_choice);

                mOnSelectedPriceChangeListener = new OnSelectedPriceChangeListener();

                mRbUnitPrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_unit_price);
                mRbBundlePrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_bundle_price);

                TextInputLayout etUnitPrice = (TextInputLayout) itemContext.findViewById(R.id.sl_unit_price_layout);
                mUnitPrice = new PriceField(etUnitPrice, itemContext.getString(R.string.unit_price_txt), R.id.sl_et_unit_price);

                TextInputLayout etBundlePrice = (TextInputLayout) itemContext.findViewById(R.id.sl_bundle_price_layout);
                mBundlePrice = new PriceField(etBundlePrice, itemContext.getString(R.string.bundle_price_txt), R.id.sl_et_bundle_price);

                mEtBundleQty = (TextInputEditText) itemContext.findViewById(R.id.sl_et_bundle_qty);
                mEtCurrencyCode = (TextInputEditText) itemContext.findViewById(R.id.et_currency_code);
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mEtQtyToBuy.setOnTouchListener(onTouchListener);
                mItemContext.findViewById(R.id.rb_unit_price).setOnTouchListener(onTouchListener);
                mItemContext.findViewById(R.id.rb_bundle_price).setOnTouchListener(onTouchListener);
                mUnitPrice.setOnTouchListener(onTouchListener);
                mBundlePrice.setOnTouchListener(onTouchListener);
                mEtBundleQty.setOnTouchListener(onTouchListener);
                mEtCurrencyCode.setOnTouchListener(onTouchListener);
        }

        public void setPurchaseManager(PurchaseManager purchaseManager)
        {
                mPurchaseManager = purchaseManager;
        }

        public void onLoadFinished()
        {
                Price.Type priceType = mPurchaseManager.getItemInShoppingList().getSelectedPriceType();
                switch (priceType)
                {
                        case UNIT_PRICE:
                                mPriceSelectState = mPriceSelectState.transition(ON_LOAD_UNIT_PX, this);
                                break;
                        case BUNDLE_PRICE:
                                mPriceSelectState = mPriceSelectState.transition(ON_LOAD_BUNDLE_PX, this);
                                break;
                }
        }

        private void setDefaultBuyQuantityFields()
        {
                /* Set default quantity to 1. If I set this in xml layout , it will jumble up the number and hint. This did not happen at SDK v24 */
                mEtQtyToBuy.setText("1");
                selectPriceType(Price.Type.UNIT_PRICE);
                mRbUnitPrice.setOnCheckedChangeListener(mOnSelectedPriceChangeListener);
                mRbBundlePrice.setOnCheckedChangeListener(mOnSelectedPriceChangeListener);
                mEtCurrencyCode.setText(mDefaultCurrencyCode);
        }

        private void populateBuyQuantityFields()
        {
                ItemInShoppingList itemInShoppingList = mPurchaseManager.getItemInShoppingList();
                mEtQtyToBuy.setText(String.valueOf(itemInShoppingList.getQuantity()));

                Price.Type priceType = mPurchaseManager.getItemInShoppingList().getSelectedPriceType();
                selectPriceType(priceType);

                mRbUnitPrice.setOnCheckedChangeListener(mOnSelectedPriceChangeListener);
                mRbBundlePrice.setOnCheckedChangeListener(mOnSelectedPriceChangeListener);
        }

        private void populateSelectedUnitPriceFields()
        {
                Price price = mPurchaseManager.getItemInShoppingList().getSelectedPrice();
                String formattedUnitPx = FormatHelper.formatToTwoDecimalPlaces(price.getUnitPrice());
                String currencyCode = price.getCurrencyCode();
                mUnitPrice.setPrice(currencyCode, formattedUnitPx);
                mEtCurrencyCode.setText(currencyCode);

                if (mPriceMgr != null)
                {
                        populateOtherPriceField(Price.Type.BUNDLE_PRICE);
                }
        }

        private void populateSelectedBundlePriceFields()
        {
                Price price = mPurchaseManager.getItemInShoppingList().getSelectedPrice();
                String formattedBundlePx = FormatHelper.formatToTwoDecimalPlaces(price.getBundlePrice());
                mBundlePrice.setPrice(price.getCurrencyCode(), formattedBundlePx);
                int bundleQuantity = price.getBundleQuantity();
                mEtBundleQty.setText(String.valueOf(bundleQuantity));
                mEtCurrencyCode.setText(price.getCurrencyCode());

                if (mPriceMgr != null)
                {
                        populateOtherPriceField(Price.Type.UNIT_PRICE);
                }
        }

        private void populateOtherPriceField(Price.Type priceType)
        {
                switch (priceType)
                {
                        case UNIT_PRICE:
                                Price unitPriceObj = mPriceMgr.getUnitPrice();
                                double unitPrice = unitPriceObj.getUnitPrice();
                                String formattedUnitPx = FormatHelper.formatToTwoDecimalPlaces(unitPrice);
                                mUnitPrice.setPrice(unitPriceObj.getCurrencyCode(), formattedUnitPx);
                                break;
                        case BUNDLE_PRICE:
                                Price bundlePriceObj = mPriceMgr.getBundlePrice();
                                double bundlePrice = bundlePriceObj.getBundlePrice();
                                String formattedBundlePx = FormatHelper.formatToTwoDecimalPlaces(bundlePrice);
                                mBundlePrice.setPrice(bundlePriceObj.getCurrencyCode(), formattedBundlePx);
                                mEtBundleQty.setText(String.valueOf(bundlePriceObj.getBundleQuantity()));
                                break;
                }
        }

        private boolean isQuantityToBuyZero()
        {
                String quantityToBuy = mEtQtyToBuy.getText().toString();

                if (TextUtils.isEmpty(quantityToBuy) || parseInt(quantityToBuy) < 1)
                {
                        return true;
                }
                else
                {
                        return false;
                }
        }

        public void setPriceMgr(PriceMgr priceMgr)
        {
                mPriceMgr = priceMgr;
        }

        private void onSelectPriceType(int choiceId)
        {
                Log.d("onCheckedChanged", "onSelectPriceType: " + String.valueOf(choiceId));

                switch (choiceId)
                {
                        case R.id.rb_unit_price:
                        {
                                mPriceSelectState = mPriceSelectState.transition(ON_SELECT_UNIT_PRICE, this);
                                break;
                        }

                        case R.id.rb_bundle_price:
                        {
                                mPriceSelectState = mPriceSelectState.transition(ON_SELECT_BUNDLE_PRICE, this);
                                break;

                        }
                }
        }

        private boolean isBuyQtyOneOrLess()
        {
                String bundleQtyToBuy = mEtQtyToBuy.getText().toString();
                if (TextUtils.isEmpty(bundleQtyToBuy))
                {
                        return true;
                }

                int nBundleQtyToBuy = Integer.parseInt(bundleQtyToBuy);
                if (nBundleQtyToBuy <= 1)
                {
                        return true;
                }
                else
                {
                        return false;
                }
        }

        void selectPriceType(Price.Type priceType)
        {
                Log.d("onCheckedChanged", "selectPriceType: " + priceType);
                switch (priceType)
                {
                        case UNIT_PRICE:
                                mRgPriceTypeChoice.check(R.id.rb_unit_price);
                                break;
                        case BUNDLE_PRICE:
                                mRgPriceTypeChoice.check(R.id.rb_bundle_price);
                                break;
                        default:
                                mRgPriceTypeChoice.check(R.id.rb_unit_price);
                }
        }

        private void setErrorQuantity(int stringResId)
        {
                mEtQtyToBuy.setError(mItemContext.getString(stringResId));
        }

        private void clearBundleQtyError()
        {
                mEtBundleQty.setError(null);
        }

        private void clearBuyQtyError()
        {
                mEtQtyToBuy.setError(null);
        }

        private boolean isBuyQuantityValidMultiples()
        {
                if (TextUtils.isEmpty(mEtQtyToBuy.getText()) || TextUtils.isEmpty(mEtBundleQty.getText()))
                {
                        return false;
                }
                String buyQty = mEtQtyToBuy.getText().toString();
                String bundleQty = mEtBundleQty.getText().toString();

                return mPurchaseManager.isBundleQuantityToBuyValid(buyQty, bundleQty);
        }

        public State getErrorState()
        {
                if (mBuyQtyState.getParentState() == BUY_ERROR)
                {
                        return mBuyQtyState.getParentState();
                }
                else if (mBundleQtyState.getParentState() == BUY_ERROR)
                {
                        return mBundleQtyState.getParentState();
                }
                else if (mCurrencyCodeState.getParentState() == BUY_ERROR)
                {
                        return mCurrencyCodeState.getParentState();
                }
                else
                {
                        return null;
                }
        }

        private boolean isCurrencyCodeEmpty()
        {
                return TextUtils.isEmpty(mEtCurrencyCode.getText());
        }

        private boolean isCurrencyCodeValid()
        {
                String newCurrencyCode = mEtCurrencyCode.getText().toString();

                boolean isNotEmpty = !TextUtils.isEmpty(newCurrencyCode);

                return isNotEmpty && FormatHelper.isValidCurrencyCode(newCurrencyCode);
        }

        public void onValidate()
        {
                mPriceSelectState = mPriceSelectState.transition(ON_VALIDATE, this);
                mCurrencyCodeState = mCurrencyCodeState.transition(ON_VALIDATE_CURRENCY_CODE, this);
        }

        public void populatePurchaseMgr()
        {
                String itemQuantity = mEtQtyToBuy.getText().toString();
                mPurchaseManager.getItemInShoppingList().setQuantity(Integer.parseInt(itemQuantity));

                Price.Type selectedPriceType;
                int optionId = mRgPriceTypeChoice.getCheckedRadioButtonId();

                switch (optionId)
                {
                        case R.id.rb_unit_price:
                                selectedPriceType = Price.Type.UNIT_PRICE;
                                break;
                        case R.id.rb_bundle_price:
                                selectedPriceType = Price.Type.BUNDLE_PRICE;
                                break;
                        default:
                                selectedPriceType = Price.Type.UNIT_PRICE;
                }

                mPriceMgr.setCurrencyCode(mEtCurrencyCode.getText().toString());

                String unitPrice = mUnitPrice.getPrice();
                String bundlePrice = mBundlePrice.getPrice();
                String bundleQtyFromInputField = mEtBundleQty.getText().toString();
                mPriceMgr.setItemPricesForSaving(unitPrice, bundlePrice, bundleQtyFromInputField);

                Price selectedPrice = mPriceMgr.getSelectedPrice(selectedPriceType);
                mPurchaseManager.setPriceMgr(mPriceMgr);
                mPurchaseManager.getItemInShoppingList().setSelectedPrice(selectedPrice);
        }

        public void onLoaderReset()
        {
                clearPurchaseInputFields();
        }

        private void clearPurchaseInputFields()
        {
                mEtQtyToBuy.setText("");
                mRgPriceTypeChoice.clearCheck();
        }

        public void onNewItem()
        {
                mPriceSelectState = mPriceSelectState.transition(ON_NEW, this);
        }

        public String getCurrencyCode()
        {
                return mPurchaseManager.getItemInShoppingList().getSelectedPrice().getCurrencyCode();
        }

        public void onLoadPriceFinished(PriceMgr priceMgr)
        {
                mPriceMgr = priceMgr;
                mPriceSelectState = mPriceSelectState.transition(ON_LOAD_PX_FINISHED, this);
        }

        private void setCurrencyCodeError(int stringResId)
        {
                mEtCurrencyCode.setError(mItemContext.getString(stringResId));
        }

        private boolean isBundleQuantityOneOrLess()
        {
                String bundleQty = mEtBundleQty.getText().toString();
                if (TextUtils.isEmpty(mEtBundleQty.getText()))
                {
                        return true;
                }

                int nBundleQtyToBuy = Integer.parseInt(bundleQty);
                if (nBundleQtyToBuy <= 1)
                {
                        return true;
                }
                else
                {
                        return false;
                }
        }

        private void setBuyQtyState(Event event)
        {
                mBuyQtyState = mBuyQtyState.transition(event, this);
        }

        private void setBundleQtyState(Event event)
        {
                mBundleQtyState = mBundleQtyState.transition(event, this);
        }

        private void clearCurrencyCodeError()
        {
                mEtCurrencyCode.setError(null);
        }

        private void setBundleQtyError(int stringResId)
        {
                mEtBundleQty.setError(mItemContext.getString(stringResId));
        }

        private void onValidateBuyQtyMultiples()
        {
                if (isBuyQtyOneOrLess() || !isBuyQuantityValidMultiples())
                {
                        mBuyQtyState = mBuyQtyState.transition(ON_INVALID_MULTIPLES, this);
                }
                else
                {
                        mBuyQtyState = mBuyQtyState.transition(ON_VALID_MULTIPLES, this);
                }

        }

        private void onValidateBuyQtyZero()
        {
                if (isQuantityToBuyZero())
                {
                        mBuyQtyState = mBuyQtyState.transition(ON_UNIT_BUY_QTY_ERROR, this);
                }
                else
                {
                        mBuyQtyState = mBuyQtyState.transition(ON_VALID_BUY_QTY, this);
                }
        }

        private void onValidateCurrencyCode()
        {
                if (isCurrencyCodeEmpty())
                {
                        mCurrencyCodeState = mCurrencyCodeState.transition(ON_CURRENCY_CODE_EMPTY, this);
                }
                else if (!isCurrencyCodeValid())
                {
                        mCurrencyCodeState = mCurrencyCodeState.transition(ON_INVALID_CURRENCY_CODE, this);
                }
                else
                {
                        mCurrencyCodeState = mCurrencyCodeState.transition(ON_VALID_CURRENCY_CODE, this);
                }
        }

        private void onValidateBundleQty()
        {
                if (isBundleQuantityOneOrLess())
                {
                        mBundleQtyState = mBundleQtyState.transition(ON_INVALID_BUNDLE_QTY, this);
                }
                else
                {
                        mBundleQtyState = mBundleQtyState.transition(ON_VALID_BUNDLE_QTY, this);
                }
        }

        enum Event
        {
                ON_SELECT_UNIT_PRICE, ON_SELECT_BUNDLE_PRICE, ON_INVALID_MULTIPLES, ON_VALID_BUY_QTY, ON_NEW,
                ON_VALIDATE, ON_LOAD_UNIT_PX, ON_LOAD_BUNDLE_PX, ON_LOAD_PX_FINISHED, ON_UNIT_BUY_QTY_ERROR, ON_CURRENCY_CODE_EMPTY,
                ON_VALIDATE_CURRENCY_CODE, ON_VALID_MULTIPLES,
                ON_INVALID_BUNDLE_QTY, ON_VALID_BUNDLE_QTY, ON_INVALID_CURRENCY_CODE, ON_VALID_CURRENCY_CODE;
        }

        enum State
        {
                NEUTRAL(null)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_NEW:
                                                        state = UNIT_PRICE;
                                                        break;
                                                case ON_LOAD_UNIT_PX:
                                                        state = UNIT_PRICE;
                                                        break;

                                                case ON_LOAD_BUNDLE_PX:
                                                        state = BUNDLE_PRICE;
                                                        break;

                                                case ON_UNIT_BUY_QTY_ERROR:
                                                        state = UNIT_BUY_QTY_ERROR;
                                                        break;

                                                case ON_INVALID_MULTIPLES:
                                                        state = BUNDLE_BUY_QTY_ERROR;
                                                        break;

                                                case ON_INVALID_BUNDLE_QTY:
                                                        state = BUNDLE_QTY_ERROR;
                                                        break;

                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_VALID_BUY_QTY:
                                                        control.clearBuyQtyError();
                                                        break;

                                                case ON_VALID_BUNDLE_QTY:
                                                        control.clearBundleQtyError();
                                                        break;
                                        }
                                }
                        },

                NEUTRAL_CURRENCY_CODE(null)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALIDATE_CURRENCY_CODE:
                                                        if (control.isCurrencyCodeEmpty())
                                                        {
                                                                state = CURRENCY_CODE_EMPTY;
                                                        }
                                                        else if (!control.isCurrencyCodeValid())
                                                        {
                                                                state = CURRENCY_CODE_INVALID;
                                                        }
                                                        break;
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        control.clearCurrencyCodeError();
                                }
                        },

                UNIT_PRICE(null)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_LOAD_PX_FINISHED:
                                                        state = this;
                                                        break;

                                                //Called by database op
                                                case ON_VALIDATE:
                                                        control.onValidateBuyQtyZero();
                                                        break;

                                                case ON_SELECT_BUNDLE_PRICE:
                                                        state = BUNDLE_PRICE;
                                                        control.onValidateBundleQty();
                                                        control.onValidateBuyQtyMultiples();

                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_NEW:
                                                        control.setDefaultBuyQuantityFields();
                                                        break;
                                                case ON_LOAD_UNIT_PX:
                                                        control.populateBuyQuantityFields();
                                                        control.populateSelectedUnitPriceFields();
                                                        break;
                                                case ON_LOAD_PX_FINISHED:
                                                        control.populateOtherPriceField(Price.Type.BUNDLE_PRICE);
                                                        break;
                                                default:
                                                        break;
                                        }
                                }
                        },

                BUY_ERROR(null)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        return null;
                                }
                        },

                CURRENCY_CODE_EMPTY(BUY_ERROR)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = NEUTRAL_CURRENCY_CODE;
                                        switch (event)
                                        {
                                                case ON_VALIDATE_CURRENCY_CODE:
                                                        if (control.isCurrencyCodeEmpty())
                                                        {
                                                                state = CURRENCY_CODE_EMPTY;
                                                        }
                                                        else if (!control.isCurrencyCodeValid())
                                                        {
                                                                state = CURRENCY_CODE_INVALID;
                                                        }
                                                        else
                                                        {
                                                                state = NEUTRAL_CURRENCY_CODE;
                                                        }
                                                        break;

                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        control.setCurrencyCodeError(R.string.empty_currency_code_msg);
                                }
                        },


                CURRENCY_CODE_INVALID(BUY_ERROR)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALIDATE_CURRENCY_CODE:
                                                        if (control.isCurrencyCodeEmpty())
                                                        {
                                                                state = CURRENCY_CODE_EMPTY;
                                                        }
                                                        else if (!control.isCurrencyCodeValid())
                                                        {
                                                                state = CURRENCY_CODE_INVALID;
                                                        }
                                                        else
                                                        {
                                                                state = NEUTRAL_CURRENCY_CODE;
                                                        }
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        control.setCurrencyCodeError(R.string.invalid_currency_code_msg);
                                }
                        },

                UNIT_BUY_QTY_ERROR(BUY_ERROR)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALID_BUY_QTY:
                                                {
                                                        state = NEUTRAL;
                                                        break;
                                                }

                                                case ON_SELECT_BUNDLE_PRICE:
                                                        control.onValidateBuyQtyMultiples();
                                                        break;

                                                case ON_INVALID_MULTIPLES:
                                                        state = BUNDLE_BUY_QTY_ERROR;
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        control.setErrorQuantity(R.string.invalid_buy_quantity_zero);
                                }
                        },

                BUNDLE_PRICE(null)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {

                                                case ON_SELECT_UNIT_PRICE:
                                                        state = UNIT_PRICE;
                                                        control.onValidateBuyQtyZero();
                                                        break;

                                                case ON_LOAD_PX_FINISHED:
                                                        state = this;
                                                        break;

                                                case ON_VALIDATE:
                                                        control.onValidateBuyQtyMultiples();
                                                        control.onValidateBundleQty();
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_LOAD_BUNDLE_PX:
                                                        control.populateBuyQuantityFields();
                                                        control.populateSelectedBundlePriceFields();
                                                        break;
                                                case ON_LOAD_PX_FINISHED:
                                                        control.populateOtherPriceField(Price.Type.UNIT_PRICE);
                                                        break;
                                                case ON_VALID_BUY_QTY:
                                                        control.clearBuyQtyError();
                                                        break;
                                        }

                                }
                        },

                BUNDLE_QTY_ERROR(BUY_ERROR)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALID_BUNDLE_QTY:
                                                        state = NEUTRAL;
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        control.setBundleQtyError(R.string.invalid_bundle_quantity_one);
                                }
                        },

                BUNDLE_BUY_QTY_ERROR(BUY_ERROR)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {

                                                case ON_SELECT_UNIT_PRICE:
                                                        control.onValidateBuyQtyZero();
                                                        break;

                                                case ON_VALID_BUY_QTY:
                                                        state = NEUTRAL;
                                                        break;

                                                case ON_UNIT_BUY_QTY_ERROR:
                                                        state = UNIT_BUY_QTY_ERROR;
                                                        break;
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyFieldControl control)
                                {
                                        control.setErrorQuantity(R.string.invalid_multiple_buy_quantity_bundle);
                                }
                        };

                private State mParentState;

                State(State parentState)
                {
                        mParentState = parentState;
                }

                public State getParentState()
                {
                        return mParentState;
                }

                void setUiOutput(Event event, ItemBuyFieldControl control)
                {

                }

                abstract State transition(Event event, ItemBuyFieldControl control);
        }

        class OnSelectedPriceChangeListener implements CompoundButton.OnCheckedChangeListener
        {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                        int checkedId = buttonView.getId();

                        if (isChecked)
                        {
                                Log.d("onCheckedChanged", String.valueOf(checkedId));
                                Log.d("onCheckedChanged", "Unit Price : " + String.valueOf(R.id.rb_unit_price));
                                Log.d("onCheckedChanged", "Bundle Price : " + String.valueOf(R.id.rb_bundle_price));
                                onSelectPriceType(checkedId);
                        }
                }
        }


}
