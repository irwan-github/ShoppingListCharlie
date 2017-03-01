package com.mirzairwan.shopping;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import static com.mirzairwan.shopping.ItemEditFieldControl.Event.ON_MISSING_VALUE;
import static com.mirzairwan.shopping.ItemEditFieldControl.Event.ON_VALUE_FILLED;
import static com.mirzairwan.shopping.ItemEditFieldControl.State.NEUTRAL;

/**
 * Created by Mirza Irwan on 27/2/17.
 */

public class ItemEditFieldControl
{
        private ItemContext mItemContext;
        private TextInputEditText mTextInput;
        private Context mContext;
        private TextInputLayout mItemNameWrap;
        private State mState = NEUTRAL;

        public ItemEditFieldControl(Context context, TextInputLayout itemNameWrap, int resIdOfEditText, ItemContext itemContext)
        {
                mItemNameWrap = itemNameWrap;
                mContext = context;
                mTextInput = (TextInputEditText) itemNameWrap.findViewById(resIdOfEditText);
                mTextInput.setOnEditorActionListener(new ActionListener());
                mTextInput.setOnFocusChangeListener(new OnFocusChange());
                mItemContext = itemContext;
        }

        private void showErrorEmptyValue()
        {
                mItemNameWrap.setError(mContext.getString(R.string.mandatory));
        }

        private void hideError()
        {
                mItemNameWrap.setError("");
        }

        private boolean isEmpty()
        {
                return TextUtils.isEmpty(mTextInput.getText());
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
                mTextInput.setOnTouchListener(onTouchListener);
        }

        public String getText()
        {
                return mTextInput.getText().toString();
        }

        public void setText(String itemName)
        {
                mTextInput.setText(itemName);
        }

        public State getState()
        {
                return mState;
        }

        enum Event
        {
                ON_MISSING_VALUE, ON_VALUE_FILLED
        }

        enum State
        {
                NEUTRAL
                        {
                                @Override
                                State transition(Event event, ItemEditFieldControl control)
                                {
                                        State state = this;
                                        if (event == ON_MISSING_VALUE)
                                        {
                                                state = ERROR_EMPTY_NAME;
                                        }
                                        state.setUiOutput(event, control);
                                        return state;
                                }

                                @Override
                                void setUiOutput(Event event, ItemEditFieldControl control)
                                {
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