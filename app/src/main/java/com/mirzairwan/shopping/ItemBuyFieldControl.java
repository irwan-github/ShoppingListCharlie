package com.mirzairwan.shopping;

import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mirzairwan.shopping.domain.ItemInShoppingList;
import com.mirzairwan.shopping.domain.Price;
import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_BUY_QTY_ZERO;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_INVALID_MULTIPLES;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_LOAD_BUNDLE_PX;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_LOAD_UNIT_PX;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_NEW;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_SELECT_BUNDLE_PRICE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_SELECT_UNIT_PRICE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_VALIDATE;
import static com.mirzairwan.shopping.ItemBuyFieldControl.Event.ON_VALID_BUY_QTY;
import static com.mirzairwan.shopping.ItemBuyFieldControl.State.NEUTRAL;
import static java.lang.Integer.parseInt;


/**
 * Created by Mirza Irwan on 28/2/17.
 */

public class ItemBuyFieldControl
{
        private RadioButton mRbBundlePrice;
        private RadioButton mRbUnitPrice;
        private OnSelectedPriceChangeListener mOnSelectedPriceChangeListener;
        private ItemContext mItemContext;
        private TextInputEditText mEtQtyToBuy;
        private PurchaseManager mPurchaseManager;
        private PriceEditFieldControl mPriceEditFieldControl;
        private RadioGroup mRgPriceTypeChoice;
        private State mState = NEUTRAL;
        private PriceMgr mPriceMgr;

        public ItemBuyFieldControl(ItemContext itemContext)
        {
                mItemContext = itemContext;
                mEtQtyToBuy = (TextInputEditText) itemContext.findViewById(R.id.et_item_quantity);
                mRgPriceTypeChoice = (RadioGroup) itemContext.findViewById(R.id.price_type_choice);

                mOnSelectedPriceChangeListener = new OnSelectedPriceChangeListener();

                mRbUnitPrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_unit_price);
                mRbBundlePrice = (RadioButton) mRgPriceTypeChoice.findViewById(R.id.rb_bundle_price);

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
                Price.Type priceType = mPurchaseManager.getItemInShoppingList().getSelectedPriceType();
                switch (priceType)
                {
                        case UNIT_PRICE:
                                mState = mState.transition(ON_LOAD_UNIT_PX, this);
                                break;
                        case BUNDLE_PRICE:
                                mState = mState.transition(ON_LOAD_BUNDLE_PX, this);
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

                switch (choiceId)
                {
                        case R.id.rb_unit_price:
                        {
                                mState = mState.transition(ON_SELECT_UNIT_PRICE, this);

                        /* Bundle quantity is not needed in unit price calculation. Set the  bundle qty error state to neutral state in order ro proceed */
                                mPriceEditFieldControl.onNeutral();

                                validateUnitBuyQty();

                                break;
                        }

                        case R.id.rb_bundle_price:
                        {
                                mState = mState.transition(ON_SELECT_BUNDLE_PRICE, this);

                                /* Validate bundle quantity */
                                mPriceEditFieldControl.onValidateBundleSet();

                                //validateBundleBuyQty();

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

                mState = mState.transition(ON_VALIDATE, this);
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
        }


        enum Event
        {
                ON_BUY_QTY_ZERO, ON_SELECT_UNIT_PRICE, ON_SELECT_BUNDLE_PRICE, ON_INVALID_MULTIPLES, ON_VALID_BUY_QTY, ON_NEW, ON_VALIDATE, ON_LOAD_UNIT_PX, ON_LOAD_BUNDLE_PX;
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
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
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
                                                case ON_BUY_QTY_ZERO:
                                                        state = UNIT_BUY_QTY_ERROR;
                                                        break;

                                                case ON_VALIDATE:
                                                        if (control.isQuantityToBuyZero())
                                                        {
                                                                state = UNIT_BUY_QTY_ERROR;
                                                        }
                                                        break;

                                                case ON_SELECT_BUNDLE_PRICE:
                                                        if (control.isBuyQtyOneOrLess() || !control.isBuyQuantityValidMultiples())
                                                        {
                                                                state = BUNDLE_BUY_QTY_ERROR;
                                                        }
                                                        else
                                                        {
                                                                state = BUNDLE_PRICE;
                                                        }
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
                                                        break;
                                                default:
                                                        control.clearBuyQtyError();
                                                        control.clearBundleQtyError();
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

                UNIT_BUY_QTY_ERROR(BUY_ERROR)
                        {
                                @Override
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_VALID_BUY_QTY:
                                                        state = UNIT_PRICE;
                                                        break;

                                                case ON_VALIDATE:
                                                        if (!control.isQuantityToBuyZero())
                                                        {
                                                                state = UNIT_PRICE;
                                                        }
                                                        break;
                                                case ON_SELECT_BUNDLE_PRICE:
                                                        if (control.isBuyQtyOneOrLess() || !control.isBuyQuantityValidMultiples())
                                                        {
                                                                state = BUNDLE_BUY_QTY_ERROR;
                                                        }
                                                        else
                                                        {
                                                                state = BUNDLE_PRICE;
                                                        }
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
                                                        if (control.isQuantityToBuyZero())
                                                        {
                                                                state = UNIT_BUY_QTY_ERROR;
                                                        }
                                                        else
                                                        {
                                                                state = UNIT_PRICE;
                                                        }
                                                        break;

                                                case ON_VALIDATE:
                                                        if (control.isBuyQtyOneOrLess() || !control.isBuyQuantityValidMultiples())
                                                        {
                                                                state = BUNDLE_BUY_QTY_ERROR;
                                                        }
                                                        else
                                                        {
                                                                state = BUNDLE_PRICE;
                                                        }
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
                                                        break;
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
                                State transition(Event event, ItemBuyFieldControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_VALIDATE:
                                                        if (control.isBuyQtyOneOrLess() || !control.isBuyQuantityValidMultiples())
                                                        {
                                                                state = BUNDLE_BUY_QTY_ERROR;
                                                        }
                                                        else
                                                        {
                                                                state = BUNDLE_PRICE;
                                                        }
                                                        break;

                                                case ON_SELECT_UNIT_PRICE:
                                                        if (control.isQuantityToBuyZero())
                                                        {
                                                                state = UNIT_BUY_QTY_ERROR;
                                                        }
                                                        else
                                                        {
                                                                state = UNIT_PRICE;
                                                        }
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
