package com.mirzairwan.shopping;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.mirzairwan.shopping.domain.Item;

import static com.mirzairwan.shopping.ItemDetailsFieldControl.Event.ON_LOAD_ITEM;
import static com.mirzairwan.shopping.ItemDetailsFieldControl.Event.ON_NEXT_AFTER_ITEM_NAME;
import static com.mirzairwan.shopping.ItemDetailsFieldControl.Event.ON_OK;
import static com.mirzairwan.shopping.ItemDetailsFieldControl.State.ERROR_EMPTY_NAME;
import static com.mirzairwan.shopping.ItemDetailsFieldControl.State.NEUTRAL;

/**
 * Created by Mirza Irwan on 27/2/17.
 *
 * Control object to control and coordinate the behaviour of user interface objects pertaining to item details.
 *
 * Responds to the following user or system supplied events:
 * 1. Ok button-click
 * 2. Soft keyboard's .IME_ACTION_NEXT
 * 3. On load item finished call from cursor loader.
 * 4. On reveal more/less details button-click
 * 5. On focus change event
 *
 * The activity can be in one of 2 states:
 * 1. NEUTRAL - No missing mandatory values
 * 2. ERROR_EMPTY_NAME - Missing  item name
 */

public class ItemDetailsFieldControl extends DetailExpander
{
        private TextInputLayout mItemNameWrap;
        private TextInputEditText mEtItemName;
        private TextInputEditText mEtDescription;
        private TextInputEditText mEtCountry;
        private TextInputEditText mEtBrand;
        private ItemContext mItemContext;
        private Item mItem;

        /* Track whether item has missing mandatory value */
        private State mState = NEUTRAL;


        public ItemDetailsFieldControl(ItemContext itemContext)
        {
                super(itemContext);
                mItem = new Item();
                mItemNameWrap = (TextInputLayout) itemContext.findViewById(R.id.item_name_layout);
                mEtItemName = (TextInputEditText) itemContext.findViewById(R.id.et_item_name);
                mEtItemName.setOnEditorActionListener(new ActionListener());
                mEtItemName.setOnFocusChangeListener(new OnFocusChange());

                mEtBrand = (TextInputEditText) itemContext.findViewById(R.id.et_item_brand);
                mEtCountry = (TextInputEditText) itemContext.findViewById(R.id.et_item_country_origin);
                mEtDescription = (TextInputEditText) itemContext.findViewById(R.id.et_item_description);
                mItemContext = itemContext;
        }

        // Get the root view to create a transition
        @Override
        protected int getViewGroupId()
        {
                return R.id.item_details_more;
        }

        @Override
        protected int getToggleButtonId()
        {
                return R.id.btn_toggle_item;
        }

        Item populateItemFromInputFields()
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

        private void setItemNameError(int stringResId)
        {
                mItemNameWrap.setError(mItemContext.getString(stringResId));
        }

        private void setErrorItemNameFieldEnabled(boolean isEnabled)
        {
                mItemNameWrap.setErrorEnabled(isEnabled);
        }

        private void setItemNameError(String errorText)
        {
                mItemNameWrap.setError(errorText);
        }

        private boolean isItemNameEmpty()
        {
                return TextUtils.isEmpty(mEtItemName.getText());
        }

        public void onOk()
        {
                mState = mState.transition(ON_OK, this);
        }

        private boolean onNextAfterItemName()
        {
                boolean isConsumed = false;

                mState = mState.transition(ON_NEXT_AFTER_ITEM_NAME, this);
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

        public boolean isInErrorState()
        {
                return mState == ERROR_EMPTY_NAME;
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
                ON_LOAD_ITEM, ON_OK, ON_NEXT_AFTER_ITEM_NAME
        }

        enum State
        {
                NEUTRAL
                        {
                                @Override
                                State transition(Event event, ItemDetailsFieldControl control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_NEXT_AFTER_ITEM_NAME:
                                                case ON_OK:
                                                        if (control.isItemNameEmpty())
                                                        {
                                                                control.setItemNameError(R.string.mandatory);
                                                                state = ERROR_EMPTY_NAME;
                                                        }
                                                        break;

                                                case ON_LOAD_ITEM:
                                                        control.populateItemInputFields();
                                                        control.invalidateOptionsMenu();
                                                        state = this;
                                                        break;

                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemDetailsFieldControl control)
                                {
                                        control.setErrorItemNameFieldEnabled(false);
                                }
                        },

                ERROR_EMPTY_NAME
                        {
                                @Override
                                State transition(Event event, ItemDetailsFieldControl control)
                                {
                                        State state = this;

                                        switch (event)
                                        {
                                                case ON_OK:
                                                        if (!control.isItemNameEmpty())
                                                        {
                                                                control.setItemNameError(null);
                                                                state = NEUTRAL;
                                                        }

                                                        break;
                                        }

                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemDetailsFieldControl control)
                                {
                                        control.setErrorItemNameFieldEnabled(true);
                                }
                        };

                void setUiOutput(Event event, ItemDetailsFieldControl control)
                {

                }

                abstract State transition(Event event, ItemDetailsFieldControl control);

        }

        private class ActionListener implements TextView.OnEditorActionListener
        {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                        if (v.getId() == R.id.et_item_name && actionId == EditorInfo.IME_ACTION_NEXT)
                        {
                                return onNextAfterItemName();
                        }
                        return false;
                }
        }

        private class OnFocusChange implements View.OnFocusChangeListener
        {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                        v.getId();
                        if (v.getId() == R.id.et_item_name && !hasFocus)
                        {
                                onNextAfterItemName();
                        }
                }
        }


}