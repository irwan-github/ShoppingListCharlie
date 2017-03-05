package com.mirzairwan.shopping;

import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.View;

import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_ITEM_IS_IN_SHOPPUNG_LIST;
import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_NEUTRAL;
import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_VALIDATE_BUNDLE_QTY;
import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_VALIDATE_CURRENCY_CODE;
import static com.mirzairwan.shopping.PriceEditFieldControl.State.NEUTRAL;

/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class PriceEditFieldControl extends DetailExpander
{
        private TextInputEditText mEtCurrencyCode;
        private ItemContext mItemContext;
        private TextInputEditText mEtBundleQty;

        private PriceField mUnitPrice;
        private PriceField mBundlePrice;
        private PriceMgr mPriceMgr;

        /* State for bundle quantity field */
        private State mBundleQtyState = NEUTRAL;

        /* State forcurrency code field */
        private State mCurrencyCodeState = NEUTRAL;

        public PriceEditFieldControl(ItemContext itemContext, SharedPreferences sharedPrefs)
        {
                super(itemContext);
                mItemContext = itemContext;
                mEtBundleQty = (TextInputEditText) itemContext.findViewById(R.id.et_bundle_qty);
                mEtCurrencyCode = (TextInputEditText) itemContext.findViewById(R.id.et_currency_code);

                String mSettingsCountryCode = sharedPrefs.getString(mItemContext.getString(R.string.user_country_pref), null);
                mEtCurrencyCode.setText(FormatHelper.getCurrencyCode(mSettingsCountryCode));
        }

        @Override
        protected int getViewGroupId()
        {
                return R.id.price_details_more;
        }

        @Override
        protected int getToggleButtonId()
        {
                return R.id.btn_toggle_price;
        }

        public void setUnitPrice(PriceField unitPrice)
        {
                mUnitPrice = unitPrice;
        }

        public void setBundlePrice(PriceField bundlePrice)
        {
                mBundlePrice = bundlePrice;
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

        private void showBundleQtyError(int stringResId)
        {
                mEtBundleQty.setError(mItemContext.getString(stringResId));
        }

        public void clearBundleQtyError()
        {
                mEtBundleQty.setError(null);
        }

        public String getBundleQuantity()
        {
                return mEtBundleQty.getText().toString();
        }

        public State getBundleQtyState()
        {
                return mBundleQtyState;
        }

        public State getErrorState()
        {
                if (mBundleQtyState.getParentState() != null)
                {
                        return mBundleQtyState.getParentState();
                }

                if (mCurrencyCodeState.getParentState() != null)
                {
                        return mCurrencyCodeState.getParentState();
                }

                return null;
        }

        /**
         * Validate bundle quantity field.
         */
        public void onValidateBundleSet()
        {
                mBundleQtyState = mBundleQtyState.transition(ON_VALIDATE_BUNDLE_QTY, this);
                mCurrencyCodeState = mCurrencyCodeState.transition(ON_VALIDATE_CURRENCY_CODE, this);
        }

        public void onValidateUnitSet()
        {
                mCurrencyCodeState = mCurrencyCodeState.transition(ON_VALIDATE_CURRENCY_CODE, this);
        }

        public void onNeutral()
        {
                mBundleQtyState = mBundleQtyState.transition(ON_NEUTRAL, this);
        }

        public void setPriceMgr(PriceMgr priceMgr)
        {
                mPriceMgr = priceMgr;
        }

        public void onLoadFinished()
        {
                String currencyCode = mPriceMgr.getUnitPrice().getCurrencyCode();
                mEtCurrencyCode.setText(currencyCode);
                mUnitPrice.setPrice(currencyCode, mPriceMgr.getUnitPriceForDisplay());
                mBundlePrice.setPrice(currencyCode, mPriceMgr.getBundlePriceForDisplay());
                mEtBundleQty.setText(String.valueOf(mPriceMgr.getBundlePrice().getBundleQuantity()));
                mUnitPrice.setCurrencySymbolInPriceHint(currencyCode);
                mBundlePrice.setCurrencySymbolInPriceHint(currencyCode);
        }

        protected String getBundleQtyFromInputField()
        {
                String bundleQty;
                bundleQty = "0";
                if (mEtBundleQty != null && !TextUtils.isEmpty(mEtBundleQty.getText()))
                {
                        bundleQty = mEtBundleQty.getText().toString();
                }
                return bundleQty;
        }

        public PriceMgr populatePriceMgr()
        {
                mPriceMgr.setCurrencyCode(mEtCurrencyCode.getText().toString());

                String unitPrice = mUnitPrice.getPrice();
                String bundlePrice = mBundlePrice.getPrice();
                String bundleQtyFromInputField = getBundleQtyFromInputField();
                mPriceMgr.setItemPricesForSaving(unitPrice, bundlePrice, bundleQtyFromInputField);

                return mPriceMgr;
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mEtCurrencyCode.setOnTouchListener(onTouchListener);
                mUnitPrice.setOnTouchListener(onTouchListener);
                mBundlePrice.setOnTouchListener(onTouchListener);
                mEtBundleQty.setOnTouchListener(onTouchListener);
        }

        public void onLoaderReset()
        {
                clearPriceInputFields();
        }

        private void clearPriceInputFields()
        {
                mUnitPrice.clear();
                mBundlePrice.clear();
                mEtBundleQty.setText("");
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

        private void setBundleQuantityEnabled(boolean enabled)
        {
                mEtBundleQty.setEnabled(enabled);
        }

        public void onItemIsInShoppingList()
        {
                mBundleQtyState = mBundleQtyState.transition(ON_ITEM_IS_IN_SHOPPUNG_LIST, this);
        }

        private void setCurrencyCodeError(int stringResId)
        {
                mEtCurrencyCode.setError(mItemContext.getString(stringResId));
        }

        private void setCurrencyCodeEnabled(boolean isEnabled)
        {
                mEtCurrencyCode.setEnabled(isEnabled);
        }

        private void setPriceFieldsEnabled(boolean isEnabled)
        {
                mUnitPrice.setEnabled(isEnabled);
                mBundlePrice.setEnabled(isEnabled);
        }

        private void clearCurrencyCodeError()
        {
                mEtCurrencyCode.setError(null);
        }

        public void onNewItem()
        {

        }

        enum Event
        {
                ON_NEUTRAL, ON_ITEM_IS_IN_SHOPPUNG_LIST, ON_VALIDATE_CURRENCY_CODE, ON_VALIDATE_BUNDLE_QTY
        }

        enum State
        {
                NEUTRAL(null)
                        {
                                @Override
                                State transition(Event event, PriceEditFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_ITEM_IS_IN_SHOPPUNG_LIST:
                                                        state = ITEM_IN_SHOPPING_LIST;
                                                        break;
                                                case ON_VALIDATE_BUNDLE_QTY:
                                                        if (control.isBundleQuantityOneOrLess())
                                                        {
                                                                state = BUNDLE_QTY_ERROR;
                                                        }
                                                        break;
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
                                void setUiOutput(Event event, PriceEditFieldControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_NEUTRAL:
                                                        control.clearBundleQtyError();
                                                        control.clearCurrencyCodeError();
                                                        break;
                                        }
                                }
                        },

                ITEM_IN_SHOPPING_LIST(null)
                        {
                                @Override
                                State transition(Event event, PriceEditFieldControl control)
                                {
                                        State state = this;
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, PriceEditFieldControl control)
                                {
                                        control.setBundleQuantityEnabled(false);
                                        control.setCurrencyCodeEnabled(false);
                                        control.setPriceFieldsEnabled(false);
                                }
                        },

                PRICE_ERROR(null)
                        {
                                @Override
                                State transition(Event event, PriceEditFieldControl control)
                                {
                                        return this;
                                }
                        },

                BUNDLE_QTY_ERROR(PRICE_ERROR)
                        {
                                @Override
                                State transition(Event event, PriceEditFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_NEUTRAL:
                                                        state = NEUTRAL;
                                                        break;
                                                case ON_VALIDATE_BUNDLE_QTY:
                                                        if (!control.isBundleQuantityOneOrLess())
                                                        {
                                                                state = NEUTRAL;
                                                        }
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, PriceEditFieldControl control)
                                {
                                        control.showBundleQtyError(R.string.invalid_bundle_quantity_one);
                                }
                        },

                CURRENCY_CODE_EMPTY(PRICE_ERROR)
                        {
                                @Override
                                State transition(Event event, PriceEditFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALIDATE_BUNDLE_QTY:
                                                        if (control.isBundleQuantityOneOrLess())
                                                        {
                                                                state = BUNDLE_QTY_ERROR;
                                                        }
                                                        break;
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
                                                                state = NEUTRAL;
                                                        }
                                                        break;
                                        }

                                        state.setUiOutput(event, control);

                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, PriceEditFieldControl control)
                                {
                                        control.setCurrencyCodeError(R.string.empty_currency_code_msg);
                                }
                        },
                CURRENCY_CODE_INVALID(PRICE_ERROR)
                        {
                                @Override
                                State transition(Event event, PriceEditFieldControl control)
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
                                                                state = NEUTRAL;
                                                        }
                                                        break;
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, PriceEditFieldControl control)
                                {
                                        control.setCurrencyCodeError(R.string.invalid_currency_code_msg);
                                }

                        };

                private State parentState;

                State(State parentState)
                {
                        this.parentState = parentState;
                }

                abstract State transition(Event event, PriceEditFieldControl control);

                void setUiOutput(Event event, PriceEditFieldControl control)
                {

                }

                State getParentState()
                {
                        return parentState;
                }

        }


}
