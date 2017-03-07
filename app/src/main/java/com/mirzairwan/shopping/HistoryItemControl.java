package com.mirzairwan.shopping;

/**
 * Created by Mirza Irwan on 24/2/17.
 */

import android.util.Log;
import android.view.Menu;

import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_BACK;
import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_CHANGE;
import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_CREATE_OPTIONS_MENU;
import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_DELETE;
import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_EXIST;
import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_LEAVE;
import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_STAY;
import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_UP;
import static com.mirzairwan.shopping.HistoryItemControl.Event.ON_UPDATE;
import static com.mirzairwan.shopping.HistoryItemControl.State.UNCHANGE;

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
public class HistoryItemControl implements ItemControl
{
        private String LOG_TAG = HistoryItemControl.class.getSimpleName();
        private ItemEditorContext mContext;
        private State mCurrentState = UNCHANGE;
        private ItemManager mItemManager;
        private ItemDetailsFieldControl mItemDetailsFieldControl;
        private PriceDetailsFieldControl mPriceDetailsFieldControl;
        private Menu mMenu;

        public HistoryItemControl(ItemEditorContext context)
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
                mItemDetailsFieldControl.onValidate();
                mPriceDetailsFieldControl.onValidate();

                if (mItemDetailsFieldControl.isInErrorState() || mPriceDetailsFieldControl.isInErrorState())
                {
                        return;
                }

                mCurrentState = mCurrentState.transition(ON_UPDATE, this);
        }

        public void onDelete()
        {
                mCurrentState = mCurrentState.transition(ON_DELETE, this);
        }

        public void onCreateOptionsMenu(Menu menu)
        {
                Log.d(LOG_TAG, mCurrentState + ": onCreateOptionsMenu");
                mMenu = menu;
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
                        mPriceDetailsFieldControl.onItemIsInShoppingList();
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
                mItemDetailsFieldControl.populateItemFromInputFields();
                mPriceDetailsFieldControl.populatePriceMgr();
                mContext.update(mItemManager);
        }

        private void showDbMessage()
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

        private void setMenuVisible(int menuResId, boolean isVisible)
        {
                mMenu.findItem(menuResId).setVisible(isVisible);
        }

        public void setItemDetailsFieldControl(ItemDetailsFieldControl itemDetailsFieldControl)
        {
                mItemDetailsFieldControl = itemDetailsFieldControl;
        }

        public void setPriceDetailsFieldControl(PriceDetailsFieldControl priceDetailsFieldControl)
        {
                mPriceDetailsFieldControl = priceDetailsFieldControl;
        }

        enum Event
        {
                ON_EXIST, ON_DELETE, ON_UP, ON_BACK, ON_CHANGE, ON_UPDATE, ON_LEAVE, ON_STAY,
                ON_CREATE_OPTIONS_MENU
        }

        enum State
        {
                UNCHANGE
                        {
                                public State transition(Event event, HistoryItemControl control)
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
                                                        control.delete();
                                                        control.showDbMessage();
                                                        state = this;
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                case ON_LEAVE:
                                                        control.finishItemEditing();
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }

                                public void setAttributes(Event event, HistoryItemControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_EXIST:
                                                        control.setTitle(R.string.view_buy_item_details);
                                                        break;
                                                case ON_CREATE_OPTIONS_MENU:
                                                        control.setMenuVisible(R.id.menu_remove_item_from_list, true);
                                                        break;
                                                case  ON_DELETE:
                                                        control.setExitTransition();
                                                        break;
                                        }
                                }
                        },

                CHANGE
                        {
                                public State transition(Event event, HistoryItemControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_UPDATE:
                                                        control.update();
                                                        control.showDbMessage();
                                                        state = this;
                                                        break;
                                                case ON_DELETE:
                                                        control.delete();
                                                        control.showDbMessage();
                                                        state = this;
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                        control.warnChangesMade();
                                                        state = this;
                                                        break;
                                                case ON_LEAVE:
                                                        control.finishItemEditing();
                                                case ON_STAY:
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }

                                public void setAttributes(Event event, HistoryItemControl control)
                                {
                                        control.setMenuVisible(R.id.save_item_details, true);

                                        if (event == ON_DELETE)
                                        {
                                                control.setExitTransition();
                                        }
                                }
                        };

                abstract State transition(Event event, HistoryItemControl itemContext);

                public void setAttributes(Event event, HistoryItemControl itemEditorControl)
                {
                }
        }

        public interface ItemEditorContext extends ItemContext
        {
                void update(ItemManager mItemManager);

                void delete(long itemId);

        }

}
