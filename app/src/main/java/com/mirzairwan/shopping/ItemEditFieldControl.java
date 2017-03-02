package com.mirzairwan.shopping;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.mirzairwan.shopping.domain.Item;

import static com.mirzairwan.shopping.ItemEditFieldControl.Event.ON_LOAD_ITEM;
import static com.mirzairwan.shopping.ItemEditFieldControl.Event.ON_MISSING_VALUE;
import static com.mirzairwan.shopping.ItemEditFieldControl.Event.ON_VALUE_FILLED;
import static com.mirzairwan.shopping.ItemEditFieldControl.State.NEUTRAL;

/**
 * Created by Mirza Irwan on 27/2/17.
 */

public class ItemEditFieldControl
{
        private TextInputLayout mItemNameWrap;
        private TextInputEditText mEtItemName;
        private TextInputEditText mEtDescription;
        private TextInputEditText mEtCountry;
        private TextInputEditText mEtBrand;

        private ItemContext mItemContext;

        private State mState = NEUTRAL;
        private Item mItem;

        public ItemEditFieldControl(ItemContext itemContext)
        {
                mItem = new Item();
                mItemNameWrap = (TextInputLayout)itemContext.findViewById(R.id.item_name_layout);
                mEtItemName = (TextInputEditText) itemContext.findViewById(R.id.et_item_name);
                mEtItemName.setOnEditorActionListener(new ActionListener());
                mEtItemName.setOnFocusChangeListener(new OnFocusChange());

                mEtBrand = (TextInputEditText)itemContext.findViewById(R.id.et_item_brand);
                mEtCountry = (TextInputEditText)itemContext.findViewById(R.id.et_item_country_origin);
                mEtDescription = (TextInputEditText)itemContext.findViewById(R.id.et_item_description);
                mItemContext = itemContext;
        }

        protected Item populateItemFromInputFields()
        {
                String itemName = mEtItemName.getText().toString();
                String itemBrand = mEtBrand.getText().toString();
                String countryOrigin = mEtCountry.getText().toString();
                String itemDescription = mEtDescription.getText().toString();

                mItem.setName(itemName);
                mItem.setBrand(itemBrand);
                mItem.setCountryOrigin(countryOrigin);
                mItem.setDescription(itemDescription);

                return mItem;
        }


        private void showErrorEmptyValue()
        {
                mItemNameWrap.setError(mItemContext.getString(R.string.mandatory));
        }

        private void hideError()
        {
                mItemNameWrap.setError("");
        }

        private boolean isEmpty()
        {
                return TextUtils.isEmpty(mEtItemName.getText());
        }

        public void onValidate()
        {
                if (isEmpty())
                {
                        mState = mState.transition(ON_MISSING_VALUE, this);
                }
                else
                {
                        mState = mState.transition(ON_VALUE_FILLED, this);
                }
        }

        private boolean onActionNext()
        {
                boolean isConsumed = false;
                if (isEmpty())
                {
                        mState = mState.transition(ON_MISSING_VALUE, this);
                        isConsumed = true;
                }
                else
                {
                        mState = mState.transition(ON_VALUE_FILLED, this);
                }

                return isConsumed;
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mEtItemName.setOnTouchListener(onTouchListener);
                mEtBrand.setOnTouchListener(onTouchListener);
                mEtDescription.setOnTouchListener(onTouchListener);
                mEtCountry.setOnTouchListener(onTouchListener);
        }

        public String getText()
        {
                return mEtItemName.getText().toString();
        }

        public void setText(String itemName)
        {
                mEtItemName.setText(itemName);
        }

        public State getState()
        {
                return mState;
        }

        private void populateItemInputFields()
        {
                mEtItemName.setText(mItem.getName());
                mEtBrand.setText(mItem.getBrand());
                mEtCountry.setText(mItem.getCountryOrigin());
                mEtDescription.setText(mItem.getDescription());
        }

        private void invalidateOptionsMenu()
        {
                mItemContext.invalidateOptionsMenu();
        }

        public void onLoadItemFinished(Item item)
        {
                mItem = item;
                mState = mState.transition(ON_LOAD_ITEM, this);
        }

        enum Event
        {
                ON_MISSING_VALUE, ON_VALUE_FILLED, ON_LOAD_ITEM
        }

        enum State
        {
                NEUTRAL
                        {
                                @Override
                                State transition(Event event, ItemEditFieldControl control)
                                {
                                        State state = this;
                                        switch(event)
                                        {
                                                case ON_MISSING_VALUE:
                                                        state = ERROR_EMPTY_NAME;
                                                        break;

                                                case ON_LOAD_ITEM:
                                                        state = this;
                                                        break;

                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemEditFieldControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_LOAD_ITEM:
                                                        control.populateItemInputFields();
                                                        control.invalidateOptionsMenu();
                                                        break;
                                        }
                                        control.hideError();
                                }
                        },

                ERROR_EMPTY_NAME
                        {
                                @Override
                                State transition(Event event, ItemEditFieldControl control)
                                {
                                        State state = this;
                                        if (event == ON_VALUE_FILLED)
                                        {
                                                state = NEUTRAL;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemEditFieldControl control)
                                {
                                        control.showErrorEmptyValue();
                                }
                        };

                void setUiOutput(Event event, ItemEditFieldControl control)
                {

                }

                abstract State transition(Event event, ItemEditFieldControl control);

        }

        private void setMenuVisible(int menuId, boolean isVisible)
        {
                mItemContext.setMenuVisible(menuId, isVisible);
        }

        private class ActionListener implements TextView.OnEditorActionListener
        {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                        if (actionId == EditorInfo.IME_ACTION_NEXT)
                        {
                                return onActionNext();
                        }
                        return false;
                }
        }

        private class OnFocusChange implements View.OnFocusChangeListener
        {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                        if (!hasFocus)
                        {
                                onActionNext();
                        }
                }
        }


}