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

import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_BUY_QTY_ZERO;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_NEW;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_INVALID_MULTIPLES;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_SELECT_BUNDLE_PRICE;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_SELECT_UNIT_PRICE;
import static com.mirzairwan.shopping.ItemBuyQtyControl.Event.ON_VALID_BUY_QTY;
import static com.mirzairwan.shopping.ItemBuyQtyControl.State.NEUTRAL;
import static com.mirzairwan.shopping.domain.Price.Type.UNIT_PRICE;
import static java.lang.Integer.parseInt;


/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class ItemBuyQtyControl
{
        private ItemContext mItemContext;
        private TextInputLayout mQtyToBuyWrapper;
        private TextInputEditText mEtQtyToBuy;
        private PurchaseManager mPurchaseManager;
        private PriceEditFieldControl mPriceEditFieldControl;
        private RadioGroup mRgPriceTypeChoice;
        private State mState = NEUTRAL;
        private PriceMgr mPriceMgr;

        public ItemBuyQtyControl(ItemContext itemContext)
        {
                mItemContext = itemContext;
                mQtyToBuyWrapper = (TextInputLayout) itemContext.findViewById(R.id.item_quantity_layout);
                mEtQtyToBuy = (TextInputEditText) mQtyToBuyWrapper.findViewById(R.id.et_item_quantity);
                mRgPriceTypeChoice = (RadioGroup) itemContext.findViewById(R.id.price_type_choice);

                OnSelectedPriceChangeListener listener = new OnSelectedPriceChangeListener();

                RadioButton rbUnitPrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_unit_price);
                rbUnitPrice.setOnCheckedChangeListener(listener);

                RadioButton rbBundlePrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_bundle_price);
                rbBundlePrice.setOnCheckedChangeListener(listener);
        }

        private void initializeUi()
        {
                /* Set default quantity to 1. If I set this in xml layout , it will jumble up the number and hint. This did not happen at SDK v24 */
                mEtQtyToBuy.setText("1");
        }


        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mEtQtyToBuy.setOnTouchListener(onTouchListener);
                mItemContext.findViewById(R.id.rb_unit_price).setOnTouchListener(onTouchListener);
                mItemContext.findViewById(R.id.rb_bundle_price).setOnTouchListener(onTouchListener);
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
                selectPriceType(priceType);
        }

        private void populateBuyQuantityField()
        {
                ItemInShoppingList itemInShoppingList = mPurchaseManager.getItemInShoppingList();
                mEtQtyToBuy.setText(String.valueOf(itemInShoppingList.getQuantity()));
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

        /**
         * Check for zero buy quantity
         */
        private void validateUnitBuyQty()
        {
                //mPriceEditFieldControl.onValidateUnitSet();
                if (isQuantityToBuyZero())
                {
                        mState = mState.transition(ON_BUY_QTY_ZERO, this);
                }
                else
                {
                        mState = mState.transition(ON_VALID_BUY_QTY, this);
                }
        }

        /**
         * Check for valid multiples in buy quantity field for bundle pricing
         */
        private void validateBundleBuyQty()
        {
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
                switch(priceType)
                {
                        case UNIT_PRICE:
                                mRgPriceTypeChoice.check(R.id.rb_unit_price);
                                break;
                        case  BUNDLE_PRICE:
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
                if (mPriceEditFieldControl != null)
                {
                        mPriceEditFieldControl.clearBundleQtyError();
                }
        }

        private void clearBuyQtyError()
        {
                mEtQtyToBuy.setError(null);
        }

        private boolean isBuyQuantityValidMultiples()
        {
                if (TextUtils.isEmpty(mEtQtyToBuy.getText()) || TextUtils.isEmpty(mPriceEditFieldControl.getBundleQuantity()))
                {
                        return false;
                }
                String buyQty = mEtQtyToBuy.getText().toString();
                String bundleQty = mPriceEditFieldControl.getBundleQuantity();

                return mPurchaseManager.isBundleQuantityToBuyValid(buyQty, bundleQty);
        }

        public State getErrorState()
        {
                return mState.getParentState();
        }

        public void onValidate()
        {

                int priceTypeChoice = mRgPriceTypeChoice.getCheckedRadioButtonId();

                switch (priceTypeChoice)
                {
                        case R.id.rb_unit_price:
                                mPriceEditFieldControl.onValidateUnitSet();
                                break;
                        case R.id.rb_bundle_price:
                                mPriceEditFieldControl.onValidateBundleSet();
                                break;
                }

                switch (mState)
                {
                        case UNIT_PRICE:
                        case UNIT_BUY_QTY_ERROR:
                                validateUnitBuyQty();
                                break;
                        case BUNDLE_PRICE:
                        case BUNDLE_BUY_QTY_ERROR:
                                validateBundleBuyQty();
                                break;
                }
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
                                selectedPriceType = UNIT_PRICE;
                                break;
                        case R.id.rb_bundle_price:
                                selectedPriceType = Price.Type.BUNDLE_PRICE;
                                break;
                        default:
                                selectedPriceType = UNIT_PRICE;
                }

                Price selectedPrice = mPriceMgr.getSelectedPrice(selectedPriceType);
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
                mState = mState.transition(ON_NEW, this);
                selectPriceType(UNIT_PRICE);
        }


        enum Event
        {
                ON_BUY_QTY_ZERO, ON_SELECT_UNIT_PRICE, ON_SELECT_BUNDLE_PRICE, ON_INVALID_MULTIPLES, ON_VALID_BUY_QTY, ON_NEW;
        }

        enum State
        {
                NEUTRAL(null)
                        {
                                @Override
                                State transition(Event event, ItemBuyQtyControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_NEW:
                                                        state = this;
                                                        break;
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

                                @Override
                                void setUiOutput(Event event, ItemBuyQtyControl control)
                                {
                                        if (event == ON_NEW)
                                        {
                                                control.initializeUi();
                                        }
                                }
                        },

                UNIT_PRICE(null)
                        {
                                @Override
                                State transition(Event event, ItemBuyQtyControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_BUY_QTY_ZERO:
                                                        state = UNIT_BUY_QTY_ERROR;
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

                BUY_ERROR(null)
                        {
                                @Override
                                State transition(Event event, ItemBuyQtyControl control)
                                {
                                        return null;
                                }
                        },

                UNIT_BUY_QTY_ERROR(BUY_ERROR)
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

                BUNDLE_PRICE(null)
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
                                                        state = BUNDLE_BUY_QTY_ERROR;
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

                BUNDLE_BUY_QTY_ERROR(BUY_ERROR)
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

                private State mParentState;

                State(State parentState)
                {
                        mParentState = parentState;
                }

                public State getParentState()
                {
                        return mParentState;
                }

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
