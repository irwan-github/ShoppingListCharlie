package com.mirzairwan.shopping;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;

import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.PriceDetailsFieldControl.Event.ON_ITEM_IS_IN_SHOPPUNG_LIST;
import static com.mirzairwan.shopping.PriceDetailsFieldControl.Event.ON_VALIDATE_BUNDLE_QTY;
import static com.mirzairwan.shopping.PriceDetailsFieldControl.Event.ON_VALIDATE_CURRENCY_CODE;
import static com.mirzairwan.shopping.PriceDetailsFieldControl.State.CURRENCY_CODE_ERROR;
import static com.mirzairwan.shopping.PriceDetailsFieldControl.State.NEUTRAL;
import static com.mirzairwan.shopping.PriceDetailsFieldControl.State.PRICE_ERROR;
import static com.mirzairwan.shopping.R.id.et_bundle_price;

/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class PriceDetailsFieldControl extends DetailExpander
{
        private ItemContext mItemContext;
        private TextInputEditText mEtCurrencyCode;
        //private TextInputEditText mEtBundleQty;
        private QuantityPicker mEtBundleQty;
        private PriceField mUnitPrice;
        private PriceField mBundlePrice;
        private PriceMgr mPriceMgr;

        /* State for bundle quantity field */
        private State mBundleQtyState = NEUTRAL;

        /* State for currency code field */
        private State mCurrencyCodeState = NEUTRAL;

        public PriceDetailsFieldControl(ItemContext itemContext)
        {
                super(itemContext);
                mItemContext = itemContext;

                View viewBundleQty = itemContext.findViewById(R.id.qp_bundle_qty);
                mEtBundleQty = new QuantityPicker(viewBundleQty, 2);

                mEtBundleQty.setVisibility(View.VISIBLE);
                mEtBundleQty.setHint(mItemContext.getString(R.string.bundle_quantity_txt));
                mEtCurrencyCode = (TextInputEditText) itemContext.findViewById(R.id.et_currency_code);

                TextInputLayout etUnitPrice = (TextInputLayout) itemContext.findViewById(R.id.unit_price_layout);
                mUnitPrice = new PriceField(etUnitPrice, itemContext.getString(R.string.unit_price_txt), R.id.et_unit_price);

                TextInputLayout etBundlePrice = (TextInputLayout) itemContext.findViewById(R.id.bundle_price_layout);
                mBundlePrice = new PriceField(etBundlePrice, itemContext.getString(R.string.bundle_price_txt), et_bundle_price);

                mEtCurrencyCode.setText(FormatHelper.getCurrencyCode(itemContext.getDefaultCountryCode()));
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

        private boolean isBundleQuantityOneOrLess()
        {
                String bundleQty = mEtBundleQty.getText().toString();
                if (TextUtils.isEmpty(mEtBundleQty.getText()))
                {
                        return false;
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

        public boolean isInErrorState()
        {
                return (mCurrencyCodeState.getParentState() == PRICE_ERROR || mBundleQtyState.getParentState() == PRICE_ERROR);
        }

        public void onValidate()
        {
                mBundleQtyState = mBundleQtyState.transition(ON_VALIDATE_BUNDLE_QTY, this);
                mCurrencyCodeState = mCurrencyCodeState.transition(ON_VALIDATE_CURRENCY_CODE, this);
        }

        public void setPriceMgr(PriceMgr priceMgr)
        {
                mPriceMgr = priceMgr;
        }

        public void onLoadFinished(PriceMgr priceMgr)
        {
                mPriceMgr = priceMgr;
                String currencyCode = mPriceMgr.getUnitPrice().getCurrencyCode();
                mEtCurrencyCode.setText(currencyCode);
                mUnitPrice.setPrice(currencyCode, mPriceMgr.getUnitPriceForDisplay());
                mBundlePrice.setPrice(currencyCode, mPriceMgr.getBundlePriceForDisplay());

                int bundleQuantity = mPriceMgr.getBundlePrice().getBundleQuantity();
                mEtBundleQty.setQuantity(bundleQuantity <= 1 ? 2 : bundleQuantity);

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
                mEtBundleQty.setQuantity(2);
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

        public String getCurrencyCode()
        {
                return mEtCurrencyCode.getText().toString();
        }

        public State onValidateCurrencyCode()
        {
                State state;
                if (isCurrencyCodeEmpty())
                {
                        setCurrencyCodeError(R.string.empty_currency_code_msg);
                        state = CURRENCY_CODE_ERROR;
                }
                else if (!isCurrencyCodeValid())
                {
                        setCurrencyCodeError(R.string.invalid_currency_code_msg);
                        state = CURRENCY_CODE_ERROR;
                }
                else
                {
                        clearCurrencyCodeError();
                        state = NEUTRAL;
                }
                return state;
        }

        enum Event
        {
                ON_NEUTRAL, ON_ITEM_IS_IN_SHOPPUNG_LIST, ON_VALIDATE_CURRENCY_CODE, ON_VALIDATE_BUNDLE_QTY, ON_VALIDATE;
        }

        enum State
        {
                NEUTRAL(null)
                        {
                                @Override
                                State transition(Event event, PriceDetailsFieldControl control)
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
                                                        state = control.onValidateCurrencyCode();
                                                        break;
                                        }

                                        state.setUiOutput(event, control);

                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, PriceDetailsFieldControl control)
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
                                State transition(Event event, PriceDetailsFieldControl control)
                                {
                                        State state = this;
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, PriceDetailsFieldControl control)
                                {
                                        control.setBundleQuantityEnabled(false);
                                        control.setCurrencyCodeEnabled(false);
                                        control.setPriceFieldsEnabled(false);
                                }
                        },

                PRICE_ERROR(null)
                        {
                                @Override
                                State transition(Event event, PriceDetailsFieldControl control)
                                {
                                        return this;
                                }
                        },

                BUNDLE_QTY_ERROR(PRICE_ERROR)
                        {
                                @Override
                                State transition(Event event, PriceDetailsFieldControl control)
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
                                void setUiOutput(Event event, PriceDetailsFieldControl control)
                                {
                                        control.showBundleQtyError(R.string.invalid_bundle_quantity_one);
                                }
                        },

                CURRENCY_CODE_ERROR(PRICE_ERROR)
                        {
                                @Override
                                State transition(Event event, PriceDetailsFieldControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_VALIDATE_CURRENCY_CODE:
                                                        state = control.onValidateCurrencyCode();
                                                        break;

                                                default:
                                                        state = this;
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }
                        };

                private State parentState;

                State(State parentState)
                {
                        this.parentState = parentState;
                }

                abstract State transition(Event event, PriceDetailsFieldControl control);

                void setUiOutput(Event event, PriceDetailsFieldControl control)
                {

                }

                State getParentState()
                {
                        return parentState;
                }

        }


}
