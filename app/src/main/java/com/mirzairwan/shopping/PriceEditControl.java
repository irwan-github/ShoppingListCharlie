package com.mirzairwan.shopping;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

import static com.mirzairwan.shopping.PriceEditControl.Event.ON_BUNDLE_QTY_ONE_OR_LESS;
import static com.mirzairwan.shopping.PriceEditControl.Event.ON_NEUTRAL;
import static com.mirzairwan.shopping.PriceEditControl.State.NEUTRAL;

/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class PriceEditControl
{
        private Context mContext;
        private TextInputEditText mEtBundleQty;
        private TextInputLayout mBundleQtyWrapper;
        private State mState = NEUTRAL;

        public PriceEditControl(Context context, TextInputLayout bundleQtyWrapper)
        {
                mContext = context;
                mBundleQtyWrapper = bundleQtyWrapper;
                mEtBundleQty = (TextInputEditText) bundleQtyWrapper.findViewById(R.id.et_bundle_qty);
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

        enum Event
        {
                ON_BUNDLE_QTY_ONE_OR_LESS, ON_NEUTRAL
        }

        enum State
        {
                NEUTRAL
                        {
                                @Override
                                State transition(Event event, PriceEditControl control)
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
                                void setUiOutput(Event event, PriceEditControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_NEUTRAL:
                                                        control.clearBundleQtyError();
                                                        break;
                                        }
                                }
                        },

                BUNDLE_QTY_ERROR
                        {
                                @Override
                                State transition(Event event, PriceEditControl control)
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
                                void setUiOutput(Event event, PriceEditControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_BUNDLE_QTY_ONE_OR_LESS:
                                                        control.showBundleQtyError(R.string.invalid_bundle_quantity_one);
                                                        break;
                                        }
                                }
                        };


                abstract State transition(Event event, PriceEditControl control);

                void setUiOutput(Event event, PriceEditControl control)
                {

                }

        }


}
