package com.mirzairwan.shopping;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_BUNDLE_PRICE_ERROR;
import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_BUNDLE_QTY_ONE_OR_LESS;
import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_NEUTRAL;
import static com.mirzairwan.shopping.PriceEditFieldControl.Event.ON_UNIT_PRICE_ERROR;
import static com.mirzairwan.shopping.PriceEditFieldControl.State.NEUTRAL;

/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class PriceEditFieldControl
{
        private TextInputEditText mEtCurrencyCode;
        private ItemContext mItemContext;
        private Context mContext;
        private TextInputEditText mEtBundleQty;
        private TextInputLayout mBundleQtyWrapper;

        private State mState = NEUTRAL;
        private PriceField mUnitPrice;
        private PriceField mBundlePrice;
        private PriceMgr mPriceMgr;

        public PriceEditFieldControl(Context context, TextInputLayout bundleQtyWrapper)
        {
                mContext = context;
                mBundleQtyWrapper = bundleQtyWrapper;
                mEtBundleQty = (TextInputEditText) bundleQtyWrapper.findViewById(R.id.et_bundle_qty);
        }

        public PriceEditFieldControl(ItemContext itemContext)
        {
                mItemContext = itemContext;
                mBundleQtyWrapper = (TextInputLayout) itemContext.findViewById(R.id.bundle_qty_layout);
                mEtBundleQty = (TextInputEditText) itemContext.findViewById(R.id.et_bundle_qty);
                mEtCurrencyCode = (TextInputEditText) itemContext.findViewById(R.id.et_currency_code);
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
                mEtBundleQty.setError(mContext.getString(stringResId));
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

        public void onValidate()
        {
                if (mUnitPrice.isEmpty())
                {
                        mState.transition(ON_UNIT_PRICE_ERROR, this);
                }

                if (mBundlePrice.isEmpty())
                {
                        mState.transition(ON_BUNDLE_PRICE_ERROR, this);
                }
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

                mPriceMgr.setItemPricesForSaving(mUnitPrice.getPrice(), mBundlePrice.getPrice(), getBundleQtyFromInputField());

                return mPriceMgr;
        }

//        private void setUnitPriceError()
//        {
//                mUnitPrice.setError(mContext.getString(R.string.invalid_price));
//        }
//
//        private void setBundlePriceError()
//        {
//                mUnitPrice.setError(mContext.getString(R.string.invalid_price));
//        }

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
