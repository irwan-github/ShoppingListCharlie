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

import static com.mirzairwan.shopping.ItemPurchaseControl.Event.ON_LOAD_BUNDLE_PX;
import static com.mirzairwan.shopping.ItemPurchaseControl.Event.ON_LOAD_PX_FINISHED;
import static com.mirzairwan.shopping.ItemPurchaseControl.Event.ON_LOAD_UNIT_PX;
import static com.mirzairwan.shopping.ItemPurchaseControl.Event.ON_NEW;
import static com.mirzairwan.shopping.ItemPurchaseControl.Event.ON_SELECT_BUNDLE_PRICE;
import static com.mirzairwan.shopping.ItemPurchaseControl.Event.ON_SELECT_UNIT_PRICE;
import static com.mirzairwan.shopping.ItemPurchaseControl.Event.ON_VALIDATE;
import static com.mirzairwan.shopping.ItemPurchaseControl.State.BUY_ERROR;
import static com.mirzairwan.shopping.ItemPurchaseControl.State.NEUTRAL;
import static com.mirzairwan.shopping.ItemPurchaseControl.State.NEUTRAL_BUY_QTY;
import static com.mirzairwan.shopping.ItemPurchaseControl.State.NEUTRAL_CURRENCY_CODE;


/**
 * Created by Mirza Irwan on 28/2/17.
 *
 * Respond to the following UI-events:
 * 1. Ok button-click
 * 2. Select options button.
 * 3. Add/Reduce buy quantity button-clicks.
 * 4. Add/Reduce bundle quantity button-clicks
 *
 * When a user supplies any of the above events to a user interface object, the event handler is made to call a corresponding method in this control object.
 * The method uses the current state to determine which state transition should occur and thus which action(s) should be executed.
 *
 * For instance, when a user clicks on the  Add/Reduce buy quantity button, the activity can be in one of 2 states:
 * 1. Unit Price
 * 2. Bundle Price
 *
 *  The  concurrent states will be defined by the following state variables:
 * 1. mPriceSelectState
 * 2. mBuyQtyValidState
 * 3. mCurrencyCodeState
 */

public class ItemPurchaseControl
{
        private QuantityPicker mQpBuyQty;
        private String mDefaultCurrencyCode;
        private TextInputLayout mEtCurrencyCodeWrap;

        private ItemContext mItemContext;
        private RadioButton mRbBundlePrice;
        private RadioButton mRbUnitPrice;
        private OnSelectedPriceChangeListener mOnSelectedPriceChangeListener;

        private PurchaseManager mPurchaseManager;
        private RadioGroup mRgPriceTypeChoice;

        private PriceMgr mPriceMgr;

        private PriceField mUnitPrice;
        private PriceField mBundlePrice;

        private TextInputEditText mEtCurrencyCode;
        private QuantityPicker mQpBundleQty;

        /* Controls which price is selected for the item. Based on the price type, the appropriate UI widgets will be displayed. Certain events will cause the
              the fields to be auto populated from the database. */
        private State mPriceSelectState = NEUTRAL;

        /* Controls whether the buy quantity field is in an error state or valid state. */
        private State mBuyQtyValidState = NEUTRAL_BUY_QTY;

        /* Controls whether the currency code field is in an error state or valid state. */
        private State mCurrencyCodeState = NEUTRAL_CURRENCY_CODE;

        public ItemPurchaseControl(ItemContext itemContext)
        {
                String mSettingsCountryCode = itemContext.getDefaultCountryCode();
                mDefaultCurrencyCode = FormatHelper.getCurrencyCode(mSettingsCountryCode);

                mItemContext = itemContext;
                mRgPriceTypeChoice = (RadioGroup) itemContext.findViewById(R.id.price_type_choice);

                mOnSelectedPriceChangeListener = new OnSelectedPriceChangeListener();

                mRbUnitPrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_unit_price);
                mRbBundlePrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_bundle_price);

                TextInputLayout etUnitPrice = (TextInputLayout) itemContext.findViewById(R.id.sl_unit_price_layout);
                mUnitPrice = new PriceField(etUnitPrice, itemContext.getString(R.string.unit_price_txt), R.id.sl_et_unit_price);

                TextInputLayout etBundlePrice = (TextInputLayout) itemContext.findViewById(R.id.sl_bundle_price_layout);
                mBundlePrice = new PriceField(etBundlePrice, itemContext.getString(R.string.bundle_price_txt), R.id.sl_et_bundle_price);

                mQpBundleQty = new QuantityPicker(itemContext.findViewById(R.id.sl_bundle_qty), 2);
                mQpBundleQty.setHint(itemContext.getString(R.string.bundle_quantity_txt));
                setBundleQtyListeners();

                mEtCurrencyCodeWrap = (TextInputLayout) itemContext.findViewById(R.id.sl_currency_code_layout);
                mEtCurrencyCode = (TextInputEditText) mEtCurrencyCodeWrap.findViewById(R.id.et_currency_code);

                mQpBuyQty = new QuantityPicker(itemContext.findViewById(R.id.sl_buy_qty));
                mQpBuyQty.setHint(itemContext.getString(R.string.quantity_label));
                mQpBuyQty.setVisibility(View.VISIBLE);
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mQpBuyQty.setOnTouchListener(onTouchListener);
                mItemContext.findViewById(R.id.rb_unit_price).setOnTouchListener(onTouchListener);
                mItemContext.findViewById(R.id.rb_bundle_price).setOnTouchListener(onTouchListener);
                mUnitPrice.setOnTouchListener(onTouchListener);
                mBundlePrice.setOnTouchListener(onTouchListener);
                mQpBundleQty.setOnTouchListener(onTouchListener);
                mEtCurrencyCode.setOnTouchListener(onTouchListener);
        }

        public void setPurchaseManager(PurchaseManager purchaseManager)
        {
                mPurchaseManager = purchaseManager;
        }

        /**
         * This event is triggered by a retrieval of records from the database. This event will be called before onLoadPriceFinished.
         * @param purchaseManager
         */
        public void onLoadFinished(PurchaseManager purchaseManager)
        {
                mPurchaseManager = purchaseManager;
                Price.Type priceType = this.mPurchaseManager.getItemInShoppingList().getSelectedPriceType();
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

        /**
         * This event is triggered by a retrieval of records from the database. This event will be called before onLoadPriceFinished.
         * @param priceMgr
         */
        public void onLoadPriceFinished(PriceMgr priceMgr)
        {
                mPriceMgr = priceMgr;
                mPriceSelectState = mPriceSelectState.transition(ON_LOAD_PX_FINISHED, this);
        }

        private void setDefaultBuyQuantityFields()
        {
                /* Set default quantity to 1. If I set this in xml layout , it will jumble up the number and hint. This did not happen at SDK v24 */
                mQpBuyQty.setQuantity(1);
                selectPriceType(Price.Type.UNIT_PRICE);
                mRbUnitPrice.setOnCheckedChangeListener(mOnSelectedPriceChangeListener);
                mRbBundlePrice.setOnCheckedChangeListener(mOnSelectedPriceChangeListener);
                mEtCurrencyCode.setText(mDefaultCurrencyCode);
        }

        private void populateBuyQuantityFields()
        {
                ItemInShoppingList itemInShoppingList = mPurchaseManager.getItemInShoppingList();
                mQpBuyQty.setQuantity(itemInShoppingList.getQuantity());

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
                mQpBundleQty.setQuantity(bundleQuantity);
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

                                int bundleQuantity = bundlePriceObj.getBundleQuantity();
                                mQpBundleQty.setQuantity(bundleQuantity <= 1? 2 : bundleQuantity);
                                break;
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
                String bundleQtyToBuy = mQpBuyQty.getText();
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

        private void setBuyQtyError(int stringResId)
        {
                mQpBuyQty.setError(mItemContext.getString(stringResId));
        }

        private void clearBuyQtyError()
        {
                mQpBuyQty.setError(null);
        }

        private boolean isBuyQuantityValidMultiples()
        {
                if (TextUtils.isEmpty(mQpBuyQty.getText()) || TextUtils.isEmpty(mQpBuyQty.getText()))
                {
                        return false;
                }
                String buyQty = mQpBuyQty.getText().toString();
                String bundleQty = mQpBundleQty.getText().toString();

                return mPurchaseManager.isBundleQuantityToBuyValid(buyQty, bundleQty);
        }

        private State getErrorState()
        {
                if (mBuyQtyValidState.getParentState() == BUY_ERROR)
                {
                        return mBuyQtyValidState.getParentState();
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

        public boolean isInErrorState()
        {
                return getErrorState() == BUY_ERROR;
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
                mCurrencyCodeState = mCurrencyCodeState.transition(ON_VALIDATE, this);
        }

        public void populatePurchaseMgr()
        {
                String itemQuantity = mQpBuyQty.getText().toString();
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
                String bundleQtyFromInputField = mQpBundleQty.getText().toString();
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
                mQpBuyQty.setQuantity(1);
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

        private void setCurrencyCodeError(int stringResId)
        {
                mEtCurrencyCode.setError(mItemContext.getString(stringResId));
        }

        private void clearCurrencyCodeError()
        {
                mEtCurrencyCode.setError(null);
        }

        private boolean isValidBuyQtyMultiples()
        {
                if (isBuyQtyOneOrLess() || !isBuyQuantityValidMultiples())
                {
                        return false;
                }
                else
                {
                        return true;
                }
        }

        private void setCurrencyCodeVisibility(int visible)
        {
                mEtCurrencyCodeWrap.setVisibility(visible);
                mEtCurrencyCode.setVisibility(visible);
        }

        private void setUnitPriceVisibility(int visibility)
        {
                mUnitPrice.setVisibility(visibility);
        }

        private void setBundlePriceVisibility(int visibility)
        {
                mBundlePrice.setVisibility(visibility);
        }

        private void setBundleQtyVisibility(int visibility)
        {
                mQpBundleQty.setVisibility(visibility);
        }

        private void setBuyQtyErrorVisibility(int visibility)
        {
                mQpBuyQty.setErrorVisibility(visibility);
        }

        private void setBuyUnitQtyListeners()
        {
                mQpBuyQty.setDefaultListeners();
        }

        private void setBuyBundleQtyListeners()
        {
                mQpBuyQty.setOnClickListenerForDown(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                int bundleQty = mQpBundleQty.getQuantity();
                                int buyQty = mQpBuyQty.getQuantity();

                                if (buyQty < bundleQty)
                                {
                                        /* Do not decrease buy quantity */
                                        return;
                                }

                                int remainder = buyQty % bundleQty;
                                if (remainder == 0 && buyQty != bundleQty)
                                {
                                        buyQty -= bundleQty;
                                }
                                else
                                {
                                        int quotient = buyQty / bundleQty;
                                        buyQty = bundleQty * (quotient);
                                }
                                mQpBuyQty.setQuantity(buyQty);
                                mBuyQtyValidState = mBuyQtyValidState.transition(Event.ON_VALIDATE, ItemPurchaseControl.this);
                        }
                });

                mQpBuyQty.setOnClickListenerForUp(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                int bundleQty = mQpBundleQty.getQuantity();
                                int buyQty = mQpBuyQty.getQuantity();
                                int remainder = buyQty % bundleQty;
                                if (remainder == 0)
                                {
                                        buyQty += bundleQty;
                                }
                                else if (buyQty < bundleQty)
                                {
                                        buyQty = bundleQty;
                                }
                                else
                                {
                                        int quotient = buyQty / bundleQty;
                                        buyQty = bundleQty * (quotient + 1);
                                }
                                mQpBuyQty.setQuantity(buyQty);
                                mBuyQtyValidState = mBuyQtyValidState.transition(Event.ON_VALIDATE, ItemPurchaseControl.this);
                        }
                });
        }

        private void setBundleQtyListeners()
        {
                View.OnClickListener bundleQtyUpListener = new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                int quantity = mQpBundleQty.getQuantity();
                                mQpBundleQty.setQuantity(++quantity);
                                mBuyQtyValidState = mBuyQtyValidState.transition(Event.ON_VALIDATE, ItemPurchaseControl.this);
                        }
                };

                View.OnClickListener bundleQtyDownListener = new View.OnClickListener()
                {
                        @Override
                        public void onClick(View v)
                        {
                                int quantity = mQpBundleQty.getQuantity();
                                if (quantity > 2)
                                {
                                        mQpBundleQty.setQuantity(--quantity);
                                        mBuyQtyValidState = mBuyQtyValidState.transition(Event.ON_VALIDATE, ItemPurchaseControl.this);
                                }
                        }
                };

                mQpBundleQty.setOnClickListenerForDown(bundleQtyDownListener);
                mQpBundleQty.setOnClickListenerForUp(bundleQtyUpListener);
        }

        enum Event
        {
                ON_SELECT_UNIT_PRICE, ON_SELECT_BUNDLE_PRICE, ON_NEW, ON_LOAD_UNIT_PX, ON_LOAD_BUNDLE_PX, ON_LOAD_PX_FINISHED, ON_VALIDATE;
        }

        enum State
        {
                NEUTRAL(null)
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_NEW:
                                                        control.setDefaultBuyQuantityFields();
                                                        state = UNIT_PRICE;
                                                        break;

                                                case ON_LOAD_UNIT_PX:
                                                        control.populateBuyQuantityFields();
                                                        control.populateSelectedUnitPriceFields();
                                                        state = UNIT_PRICE;
                                                        break;

                                                case ON_LOAD_BUNDLE_PX:
                                                        control.populateBuyQuantityFields();
                                                        control.populateSelectedBundlePriceFields();
                                                        state = BUNDLE_PRICE;
                                                        break;

                                                default:
                                                        state = this;

                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                        },

                NEUTRAL_BUY_QTY(null)
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        State state = this;
                                        switch(event)
                                        {
                                                case ON_VALIDATE:
                                                case ON_SELECT_BUNDLE_PRICE:
                                                        if (!control.isValidBuyQtyMultiples())
                                                                state = BUY_QTY_ERROR;
                                                        else
                                                                state = this;
                                                        break;
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemPurchaseControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_VALIDATE:
                                                        control.clearBuyQtyError();
                                                        control.setBuyQtyErrorVisibility(View.GONE);
                                                        break;
                                        }
                                }

                        },

                NEUTRAL_CURRENCY_CODE(null)
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALIDATE:
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
                                void setUiOutput(Event event, ItemPurchaseControl control)
                                {
                                        control.clearCurrencyCodeError();
                                }
                        },

                UNIT_PRICE(null) /* Used by mPriceSelectState*/
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_LOAD_PX_FINISHED:
                                                        control.populateOtherPriceField(Price.Type.BUNDLE_PRICE);
                                                        state = this;
                                                        break;

                                                case ON_SELECT_BUNDLE_PRICE:
                                                        control.validateBuyQty();
                                                        state = BUNDLE_PRICE;
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemPurchaseControl control)
                                {
                                        control.setBundlePriceVisibility(View.GONE);
                                        control.setBundleQtyVisibility(View.GONE);
                                        control.setUnitPriceVisibility(View.VISIBLE);
                                        control.setCurrencyCodeVisibility(View.VISIBLE);
                                        control.setBuyUnitQtyListeners();
                                        control.setBuyQtyErrorVisibility(View.GONE);

                                        switch (event)
                                        {
                                                default:
                                                        break;
                                        }
                                }
                        },

                BUY_ERROR(null)
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        return null;
                                }
                        },

                CURRENCY_CODE_EMPTY(BUY_ERROR)
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        State state = NEUTRAL_CURRENCY_CODE;
                                        switch (event)
                                        {
                                                case ON_VALIDATE:
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
                                void setUiOutput(Event event, ItemPurchaseControl control)
                                {
                                        control.setCurrencyCodeError(R.string.empty_currency_code_msg);
                                }
                        },

                CURRENCY_CODE_INVALID(BUY_ERROR)
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALIDATE:
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
                                void setUiOutput(Event event, ItemPurchaseControl control)
                                {
                                        control.setCurrencyCodeError(R.string.invalid_currency_code_msg);
                                }
                        },

                BUNDLE_PRICE(null) /* Used by mPriceSelectState*/
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALIDATE:
                                                        control.validateBuyQty();
                                                        break;

                                                case ON_SELECT_UNIT_PRICE:
                                                        control.neutralizeBuyQty();
                                                        state = UNIT_PRICE;
                                                        break;

                                                case ON_LOAD_PX_FINISHED:
                                                        control.populateOtherPriceField(Price.Type.UNIT_PRICE);
                                                        state = this;
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemPurchaseControl control)
                                {
                                        control.setUnitPriceVisibility(View.GONE);
                                        control.setBundlePriceVisibility(View.VISIBLE);
                                        control.setBundleQtyVisibility(View.VISIBLE);
                                        control.setCurrencyCodeVisibility(View.VISIBLE);
                                        control.setBuyBundleQtyListeners();
                                }
                        },

                BUY_QTY_ERROR(BUY_ERROR)  /* This state is used by mBuyQtyValidState */
                        {
                                @Override
                                State transition(Event event, ItemPurchaseControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALIDATE:
                                                        if(control.isValidBuyQtyMultiples())
                                                                state = NEUTRAL_BUY_QTY;
                                                        break;

                                                case ON_SELECT_UNIT_PRICE:
                                                        state = NEUTRAL_BUY_QTY;
                                                        break;

                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemPurchaseControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_VALIDATE:
                                                case ON_SELECT_BUNDLE_PRICE:
                                                        control.setBuyQtyError(R.string.invalid_multiple_buy_quantity_bundle);
                                                        control.setBuyQtyErrorVisibility(View.VISIBLE);
                                                        break;
                                        }
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

                void setUiOutput(Event event, ItemPurchaseControl control)
                {

                }

                abstract State transition(Event event, ItemPurchaseControl control);
        }

        private void neutralizeBuyQty()
        {
                mBuyQtyValidState = mBuyQtyValidState.transition(ON_SELECT_UNIT_PRICE, this);
        }

        private void validateBuyQty()
        {
                mBuyQtyValidState = mBuyQtyValidState.transition(ON_VALIDATE, this);
        }

        class OnSelectedPriceChangeListener implements CompoundButton.OnCheckedChangeListener
        {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                        int checkedId = buttonView.getId();

                        if (isChecked)
                        {
                                onSelectPriceType(checkedId);
                        }
                }
        }


}
