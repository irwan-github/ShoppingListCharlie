package com.mirzairwan.shopping;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mirzairwan.shopping.domain.ItemInShoppingList;
import com.mirzairwan.shopping.domain.Price;

import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_BUY_QTY_ZERO;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_INVALID_MULTIPLES;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_SELECT_BUNDLE_PRICE;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_SELECT_UNIT_PRICE;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_VALID_BUY_QTY;
import static com.mirzairwan.shopping.ItemBuyQtyControl.State.NEUTRAL;
import static java.lang.Integer.parseInt;


/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class ItemBuyQtyControl
{
        private Context mContext;
        private TextInputLayout mQtyToBuyWrapper;
        private TextInputEditText etQtyToBuy;
        private PurchaseManager mPurchaseManager;
        private PriceEditFieldControl mPriceEditFieldControl;
        private RadioGroup mRgPriceTypeChoice;
        private State mState = NEUTRAL;

        public ItemBuyQtyControl(Context context, TextInputLayout qtyToBuyWrapper, RadioGroup rgPriceTypeChoice)
        {
                mContext = context;
                mQtyToBuyWrapper = qtyToBuyWrapper;
                etQtyToBuy = (TextInputEditText) mQtyToBuyWrapper.findViewById(R.id.et_item_quantity);
                mRgPriceTypeChoice = rgPriceTypeChoice;

                OnSelectedPriceChangeListener listener = new OnSelectedPriceChangeListener();

                RadioButton rbUnitPrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_unit_price);
                rbUnitPrice.setOnCheckedChangeListener(listener);

                RadioButton rbBundlePrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_bundle_price);
                rbBundlePrice.setOnCheckedChangeListener(listener);
        }

        public void setPriceEditFieldControl(PriceEditFieldControl priceEditFieldControl)
        {
                mPriceEditFieldControl = priceEditFieldControl;
        }

        public void setPurchaseManager(PurchaseManager purchaseManager)
        {
                mPurchaseManager = purchaseManager;
        }

        public void onLoadFinished()
        {
                populateBuyQuantityField();

                Price.Type priceType = mPurchaseManager.getItemInShoppingList().getSelectedPriceType();
                if (priceType == Price.Type.UNIT_PRICE)
                {
                        selectPriceType(R.id.rb_unit_price);
                        return;
                }

                if (priceType == Price.Type.BUNDLE_PRICE)
                {
                        selectPriceType(R.id.rb_bundle_price);
                }
        }

        private void populateBuyQuantityField()
        {
                ItemInShoppingList itemInShoppingList = mPurchaseManager.getItemInShoppingList();
                etQtyToBuy.setText(String.valueOf(itemInShoppingList.getQuantity()));
        }

        private boolean isQuantityToBuyZero()
        {
                String quantityToBuy = etQtyToBuy.getText().toString();

                if (TextUtils.isEmpty(quantityToBuy) || parseInt(quantityToBuy) < 1)
                {
                        return true;
                }
                else
                {
                        return false;
                }
        }

        private void validateUnitBuyQty()
        {
                if (isQuantityToBuyZero())
                {
                        mState = mState.transition(ON_BUY_QTY_ZERO, this);
                }
                else
                {
                        mState = mState.transition(ON_VALID_BUY_QTY, this);
                }
        }

        private void validateBundleBuyQty()
        {
                mPriceEditFieldControl.onValidateBundleQty();

                if (isBuyQtyOneOrLess() || !isBuyQuantityValidMultiples())
                {
                        mState = mState.transition(ON_INVALID_MULTIPLES, this);
                }
                else
                {
                        mState = mState.transition(ON_VALID_BUY_QTY, this);
                }
        }

        private void onSelectPriceType(int choiceId)
        {
                Log.d("onCheckedChanged", "onSelectPriceType: " + String.valueOf(choiceId));
                if (choiceId == R.id.rb_unit_price)
                {
                        mState = mState.transition(ON_SELECT_UNIT_PRICE, this);

                        /* Bundle quantity is not needed in unit price calculation. Set the  bundle qty error state to neutral state in order ro proceed */
                        mPriceEditFieldControl.onNeutral();

                        validateUnitBuyQty();

                        return;
                }

                if (choiceId == R.id.rb_bundle_price)
                {
                        mState = mState.transition(ON_SELECT_BUNDLE_PRICE, this);

                        validateBundleBuyQty();

                }
        }

        private boolean isBuyQtyOneOrLess()
        {
                String bundleQtyToBuy = etQtyToBuy.getText().toString();
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

        private void selectPriceType(int choiceId)
        {
                Log.d("onCheckedChanged", "selectPriceType: " + String.valueOf(choiceId));
                mRgPriceTypeChoice.check(choiceId);
        }

        public void selectPriceType(Price.Type priceType)
        {
                Log.d("onCheckedChanged", "selectPriceType: " + priceType);
                if (priceType == Price.Type.UNIT_PRICE)
                {
                        mRgPriceTypeChoice.check(R.id.rb_unit_price);
                }

                if (priceType == Price.Type.BUNDLE_PRICE)
                {
                        mRgPriceTypeChoice.check(R.id.rb_bundle_price);
                }
        }

        private void setErrorQuantity(int stringResId)
        {
                etQtyToBuy.setError(mContext.getString(stringResId));
        }

        private void clearBundleQtyError()
        {
                mPriceEditFieldControl.clearBundleQtyError();
        }

        private void clearBuyQtyError()
        {
                etQtyToBuy.setError(null);
        }

        private boolean isBuyQuantityValidMultiples()
        {
                if (TextUtils.isEmpty(etQtyToBuy.getText()) || TextUtils.isEmpty(mPriceEditFieldControl.getBundleQuantity()))
                {
                        return false;
                }
                String buyQty = etQtyToBuy.getText().toString();
                String bundleQty = mPriceEditFieldControl.getBundleQuantity();

                return mPurchaseManager.isBundleQuantityToBuyValid(buyQty, bundleQty);
        }

        public State getState()
        {
                return mState;
        }

        public void onValidate()
        {
                switch (mState)
                {
                        case UNIT_PRICE:
                        case UNIT_BUY_QUANTITY_ERROR:
                                validateUnitBuyQty();
                                break;
                        case BUNDLE_PRICE:
                        case BUNDLE_BUY_QUANTITY_ERROR:
                                validateBundleBuyQty();
                                break;
                }
        }

        public String getQuantity()
        {
                return etQtyToBuy.getText().toString();
        }


        enum Event
        {
                ON_BUY_QTY_ZERO, ON_SELECT_UNIT_PRICE, ON_SELECT_BUNDLE_PRICE, ON_INVALID_MULTIPLES, ON_VALID_BUY_QTY;
        }

        enum State
        {
                NEUTRAL
                        {
                                @Override
                                State transition(Event event, ItemBuyQtyControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_SELECT_UNIT_PRICE:
                                                        state = UNIT_PRICE;
                                                        break;

                                                case ON_SELECT_BUNDLE_PRICE:
                                                        state = BUNDLE_PRICE;
                                                        break;
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }
                        },

                UNIT_PRICE
                        {
                                @Override
                                State transition(Event event, ItemBuyQtyControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_BUY_QTY_ZERO:
                                                        state = UNIT_BUY_QUANTITY_ERROR;
                                                        break;

                                                case ON_SELECT_BUNDLE_PRICE:
                                                        state = BUNDLE_PRICE;
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyQtyControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_SELECT_UNIT_PRICE:
                                                        control.clearBuyQtyError();
                                                        control.clearBundleQtyError();
                                                        break;
                                        }
                                }
                        },

                UNIT_BUY_QUANTITY_ERROR
                        {
                                @Override
                                State transition(Event event, ItemBuyQtyControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALID_BUY_QTY:
                                                        state = UNIT_PRICE;
                                                        break;

                                                case ON_SELECT_BUNDLE_PRICE:
                                                        state = BUNDLE_PRICE;
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyQtyControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_BUY_QTY_ZERO:
                                                        control.setErrorQuantity(R.string.invalid_buy_quantity_zero);
                                                        break;
                                        }
                                }
                        },

                BUNDLE_PRICE
                        {
                                @Override
                                State transition(Event event, ItemBuyQtyControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_SELECT_UNIT_PRICE:
                                                        state = UNIT_PRICE;
                                                        break;

                                                case ON_INVALID_MULTIPLES:
                                                        state = BUNDLE_BUY_QUANTITY_ERROR;
                                                        break;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyQtyControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_VALID_BUY_QTY:
                                                        control.clearBuyQtyError();
                                                        break;
                                        }

                                }
                        },

                BUNDLE_BUY_QUANTITY_ERROR
                        {
                                State state;

                                @Override
                                State transition(Event event, ItemBuyQtyControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_INVALID_MULTIPLES:
                                                        state = this;
                                                        break;

                                                case ON_VALID_BUY_QTY:
                                                        state = BUNDLE_PRICE;
                                                        break;

                                                case ON_SELECT_UNIT_PRICE:
                                                        state = UNIT_PRICE;
                                                        break;
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemBuyQtyControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_INVALID_MULTIPLES:
                                                        control.setErrorQuantity(R.string.invalid_multiple_buy_quantity_bundle);
                                                        break;
                                        }

                                }
                        };

                void setUiOutput(Event event, ItemBuyQtyControl control)
                {

                }

                abstract State transition(Event event, ItemBuyQtyControl control);
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
