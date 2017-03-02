package com.mirzairwan.shopping;

import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.View;

import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_BUNDLE_QTY_ONE_OR_LESS;
import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_NEUTRAL;
import static com.mirzairwan.shopping.PriceEditFieldControl.State.NEUTRAL;

/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class PriceEditFieldControl
{
        private TextInputEditText mEtCurrencyCode;
        private ItemContext mItemContext;
        private TextInputEditText mEtBundleQty;
        private State mState = NEUTRAL;
        private PriceField mUnitPrice;
        private PriceField mBundlePrice;
        private PriceMgr mPriceMgr;

        public PriceEditFieldControl(ItemContext itemContext, SharedPreferences sharedPrefs)
        {
                mItemContext = itemContext;
                mEtBundleQty = (TextInputEditText) itemContext.findViewById(R.id.et_bundle_qty);
                mEtCurrencyCode = (TextInputEditText) itemContext.findViewById(R.id.et_currency_code);

                String mSettingsCountryCode = sharedPrefs.getString(mItemContext.getString(R.string.user_country_pref), null);
                mEtCurrencyCode.setText(FormatHelper.getCurrencyCode(mSettingsCountryCode));
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

        public State getState()
        {
                return mState;
        }

        public void onValidateBundleQty()
        {
                if (isBundleQuantityOneOrLess())
                {
                        mState = mState.transition(ON_BUNDLE_QTY_ONE_OR_LESS, this);
                }
                else
                {
                        mState = mState.transition(ON_NEUTRAL, this);
                }

        }

        public void onNeutral()
        {
                mState = mState.transition(ON_NEUTRAL, this);
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

        enum Event
        {
                ON_BUNDLE_QTY_ONE_OR_LESS, ON_NEUTRAL, ON_BUNDLE_PRICE_ERROR, ON_UNIT_PRICE_ERROR
        }

        enum State
        {
                NEUTRAL
                        {
                                @Override
                                State transition(Event event, PriceEditFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_BUNDLE_QTY_ONE_OR_LESS:
                                                        state = BUNDLE_QTY_ERROR;
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
                                                        break;
                                        }
                                }
                        },

//                PRICE_ERROR
//                        {
//                                @Override
//                                State transition(Event event, PriceEditFieldControl control)
//                                {
//                                        return this;
//                                }
//
//                                @Override
//                                void setUiOutput(Event event, PriceEditFieldControl control)
//                                {
//                                        switch (event)
//                                        {
//                                                case ON_UNIT_PRICE_ERROR:
//                                                        control.setUnitPriceError();
//                                                        break;
//                                                case ON_BUNDLE_PRICE_ERROR:
//                                                        control.setBundlePriceError();
//                                        }
//                                }
//                        },

                BUNDLE_QTY_ERROR
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
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, PriceEditFieldControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_BUNDLE_QTY_ONE_OR_LESS:
                                                        control.showBundleQtyError(R.string.invalid_bundle_quantity_one);
                                                        break;
                                        }
                                }
                        };


                abstract State transition(Event event, PriceEditFieldControl control);

                void setUiOutput(Event event, PriceEditFieldControl control)
                {

                }

        }




}
