package com.mirzairwan.shopping;

/**
 * Created by Mirza Irwan on 24/2/17.
 */

import android.util.Log;

import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_BACK;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_CHANGE;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_CREATE_OPTIONS_MENU;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_DB_RESULT;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_DELETE;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_EXIST;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_LEAVE;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_STAY;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_UP;
import static com.mirzairwan.shopping.HistoryItemEditorControl.Event.ON_UPDATE;
import static com.mirzairwan.shopping.HistoryItemEditorControl.State.START;
import static com.mirzairwan.shopping.ItemEditFieldControl.State.ERROR_EMPTY_NAME;

/**
 * It is possible to add actions to a state which are executed on entry to that state. This may
 * seem an economical approach to reducing the amount of code that needs to be written if
 * there are several events arrows entering that state. However, in the long term, actions
 * associated with states can make the software more difficult to maintain. For instance, a
 * new event could be added which terminates at a state with actions. If those actions are not
 * required when the event occurs then a significant change would be required to the state­
 * chart. Furthermore, if the actions associated with a state are changed, it is necessary to
 * ensure that the change is appropriate for all the arrows entering that state. Actions on states
 * are like global variables. They can be very powerful, but they also have the potential to
 * cause many problems.
 * Hint: Use states to control the attributes of user interface items rather than executing
 * actions. All actions should be associated with events.
 * <p>
 * Actions should be associated with events. Example:
 * Insert record into database
 */
public class HistoryItemEditorControl implements ItemControl
{
        private ItemEditorContext mContext;

        private State mCurrentState = START;
        private PriceMgr mPriceMgr;
        private ItemManager mItemManager;
        private ItemEditFieldControl mItemEditFieldControl;
        private String LOG_TAG = HistoryItemEditorControl.class.getSimpleName();
        private PriceEditFieldControl mPriceEditFieldControl;

        public HistoryItemEditorControl(ItemEditorContext context)
        {
                mContext = context;
        }

        public void onExistingItem()
        {
                Log.d(LOG_TAG, mCurrentState + ": ON_EXIST");
                mCurrentState = mCurrentState.transition(ON_EXIST, this);
        }

        public void onChange()
        {
                mCurrentState = mCurrentState.transition(ON_CHANGE, this);
        }

        public void onUp()
        {
                mCurrentState = mCurrentState.transition(ON_UP, this);
        }

        public void onBackPressed()
        {
                mCurrentState = mCurrentState.transition(ON_BACK, this);
        }

        public void onOk()
        {
                mItemEditFieldControl.onValidate();

                if (mItemEditFieldControl.getState() == ERROR_EMPTY_NAME)
                {
                        return;
                }

                mCurrentState = mCurrentState.transition(ON_UPDATE, this);
                mCurrentState = mCurrentState.transition(ON_DB_RESULT, this);
        }

        public void onDelete()
        {
                mCurrentState = mCurrentState.transition(ON_DELETE, this);
                mCurrentState = mCurrentState.transition(ON_DB_RESULT, this);
        }

        public void onCreateOptionsMenu()
        {
                Log.d(LOG_TAG, mCurrentState + ": onCreateOptionsMenu");
                mCurrentState = mCurrentState.transition(ON_CREATE_OPTIONS_MENU, this);
        }

        public void onLeave()
        {
                mCurrentState = mCurrentState.transition(ON_LEAVE, this);
        }

        public void onStay()
        {
                mCurrentState = mCurrentState.transition(ON_STAY, this);
        }

        public void onLoadItemFinished(ItemManager itemManager)
        {
                Log.d(LOG_TAG, mCurrentState + ":  onLoadItemFinished");
                mItemManager = itemManager;
                if (mItemManager.getItem().isInBuyList())
                {
                        mPriceEditFieldControl.onItemIsInShoppingList();
                }
        }

        private void finishItemEditing()
        {
                mContext.finishItemEditing();
        }

        private void warnChangesMade()
        {
                mContext.warnChangesMade();
        }

        protected void delete()
        {
                mContext.delete(mItemManager.getItem().getId());
        }

        private void update()
        {
                mItemEditFieldControl.populateItemFromInputFields();
                mPriceEditFieldControl.populatePriceMgr();
                mContext.update(mItemManager);
        }

        private void postDbProcess()
        {
                mContext.showTransientDbMessage();
        }

        private void setExitTransition()
        {
                mContext.setExitTransition();
        }

        private void setTitle(int stringResId)
        {
                mContext.setTitle(stringResId);
        }

        private void setMenuVisible(int menuResId, boolean b)
        {
                mContext.setMenuVisible(menuResId, b);
        }

        public void setItemEditFieldControl(ItemEditFieldControl itemEditFieldControl)
        {
                mItemEditFieldControl = itemEditFieldControl;
        }

        public void setPriceEditFieldControl(PriceEditFieldControl priceEditFieldControl)
        {
                mPriceEditFieldControl = priceEditFieldControl;
        }

        enum Event
        {
                ON_EXIST, ON_DELETE, ON_UP, ON_BACK, ON_CHANGE, ON_UPDATE, ON_LEAVE, ON_STAY, ON_DB_RESULT,
                ON_CREATE_OPTIONS_MENU
        }

        enum State
        {
                START
                        {
                                public State transition(Event event, HistoryItemEditorControl context)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_EXIST:
                                                        state = UNCHANGE;
                                                        break;
                                                default:
                                                        state = this;
                                        }
                                        state.setAttributes(event, context);
                                        return state;
                                }
                        },

                UNCHANGE
                        {
                                public State transition(Event event, HistoryItemEditorControl context)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_CREATE_OPTIONS_MENU:
                                                        state = UNCHANGE;
                                                        break;
                                                case ON_CHANGE:
                                                        state = CHANGE;
                                                        break;
                                                case ON_DELETE:
                                                        context.delete();
                                                        state = POST_DB_OP;
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                        context.finishItemEditing();
                                                        state = this;
                                                        break;
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, context);
                                        return state;
                                }

                                public void setAttributes(Event event, HistoryItemEditorControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_EXIST:
                                                        control.setTitle(R.string.view_buy_item_details);
                                                        break;
                                                case ON_CREATE_OPTIONS_MENU:
                                                        control.setMenuVisible(R.id.menu_remove_item_from_list, true);
                                                        break;
                                        }
                                }
                        },

                CHANGE
                        {
                                public State transition(Event event, HistoryItemEditorControl context)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_UPDATE:
                                                        context.update();
                                                        state = POST_DB_OP;
                                                        break;
                                                case ON_DELETE:
                                                        context.delete();
                                                        state = POST_DB_OP;
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                        context.warnChangesMade();
                                                        state = WARN;
                                                        break;
                                                case ON_LEAVE:
                                                        context.finishItemEditing();
                                                        state = this;
                                                        break;
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, context);
                                        return state;
                                }

                                public void setAttributes(Event event, HistoryItemEditorControl control)
                                {
                                        control.setMenuVisible(R.id.save_item_details, true);
                                }
                        },

                WARN
                        {
                                public State transition(Event event, HistoryItemEditorControl context)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_LEAVE:
                                                        context.finishItemEditing();
                                                        state = this;
                                                        break;
                                                case ON_STAY:
                                                        state = CHANGE;
                                                        break;
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, context);
                                        return state;
                                }
                        },

                POST_DB_OP
                        {
                                @Override
                                public State transition(Event event, HistoryItemEditorControl context)
                                {
                                        Log.d(this.toString(), "Event:" + event);
                                        State state;
                                        switch (event)
                                        {
                                                case ON_DB_RESULT:
                                                        context.postDbProcess();
                                                        state = this;
                                                        break;
                                                case ON_LEAVE:
                                                        context.finishItemEditing();
                                                        state = this;
                                                        break;
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, context);
                                        return state;
                                }

                                @Override
                                public void setAttributes(Event event, HistoryItemEditorControl context)
                                {
                                        if (event == ON_DELETE)
                                        {
                                                context.setExitTransition();
                                        }
                                }
                        };;

                abstract State transition(Event event, HistoryItemEditorControl itemContext);

                public void setAttributes(Event event, HistoryItemEditorControl itemEditorControl)
                {
                }
        }

        public interface ItemEditorContext extends ItemContext
        {
                void update(ItemManager mItemManager);

                void delete(long itemId);

        }

}
