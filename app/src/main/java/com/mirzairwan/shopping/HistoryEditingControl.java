package com.mirzairwan.shopping;

/**
 * Created by Mirza Irwan on 24/2/17.
 */

import android.view.Menu;
import android.view.View;

import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.HistoryEditingControl.ChangeState.UNCHANGE;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_BACK;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_CHANGE;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_CREATE_OPTIONS_MENU;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_DELETE;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_LEAVE;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_LOAD_ITEM;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_OK;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_STAY;
import static com.mirzairwan.shopping.HistoryEditingControl.Event.ON_UP;
import static com.mirzairwan.shopping.HistoryEditingControl.ShoppingListState.TRANSIENT;

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
public class HistoryEditingControl implements ItemControl
{
        private String LOG_TAG = HistoryEditingControl.class.getSimpleName();
        private ItemEditorContext mContext;

        private ItemManager mItemManager;
        private ItemDetailsFieldControl mItemDetailsFieldControl;
        private PriceDetailsFieldControl mPriceDetailsFieldControl;
        private Menu mMenu;

        private ShoppingListState mShoppingListState = TRANSIENT;
        private ChangeState mChangeState = UNCHANGE;

        public HistoryEditingControl(ItemEditorContext context)
        {
                mContext = context;
                mItemDetailsFieldControl = new ItemDetailsFieldControl(context);
                mPriceDetailsFieldControl = new PriceDetailsFieldControl(context);
                mContext.setTitle(R.string.history_item_editing_screen);
        }

        public void onChange()
        {
                mChangeState = mChangeState.transition(ON_CHANGE, this);
        }

        public void onUp()
        {
                mChangeState = mChangeState.transition(ON_UP, this);
        }

        public void onBackPressed()
        {
                mChangeState = mChangeState.transition(ON_BACK, this);
        }

        public void onOk()
        {
                mItemDetailsFieldControl.onOk();
                mPriceDetailsFieldControl.onValidate();

                if (mItemDetailsFieldControl.isInErrorState() || mPriceDetailsFieldControl.isInErrorState())
                {
                        return;
                }

                mShoppingListState = mShoppingListState.transition(ON_OK, this);
        }

        public void onDelete()
        {
                mShoppingListState = mShoppingListState.transition(ON_DELETE, this);
        }

        public void onCreateOptionsMenu(Menu menu)
        {
                mMenu = menu;
                mShoppingListState = mShoppingListState.transition(ON_CREATE_OPTIONS_MENU, this);
                mChangeState = mChangeState.transition(ON_CREATE_OPTIONS_MENU, this);
        }

        public void onLeave()
        {
                mShoppingListState = mShoppingListState.transition(ON_LEAVE, this);
        }

        public void onStay()
        {
                mShoppingListState = mShoppingListState.transition(ON_STAY, this);
        }

        public void onLoadItemFinished(ItemManager itemManager)
        {
                mItemManager = itemManager;
                mShoppingListState = mShoppingListState.transition(ON_LOAD_ITEM, this);
                if (mItemManager.getItem().isInBuyList())
                {
                        mPriceDetailsFieldControl.onItemIsInShoppingList();
                }

                mItemDetailsFieldControl.onLoadItemFinished(mItemManager.getItem());
        }

        private boolean isItemInShoppingList()
        {
                return mItemManager.getItem().isInBuyList();
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

        private void setMenuVisible(int menuResId, boolean isVisible)
        {
                mMenu.findItem(menuResId).setVisible(isVisible);
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mItemDetailsFieldControl.setOnTouchListener(onTouchListener);
                mPriceDetailsFieldControl.setOnTouchListener(onTouchListener);
        }

        public void setPriceMgr(PriceMgr priceMgr)
        {
                mPriceDetailsFieldControl.setPriceMgr(priceMgr);
        }

        public void onLoadPriceFinished(PriceMgr priceMgr)
        {
                mPriceDetailsFieldControl.onLoadFinished(priceMgr);
        }

        public String getCurrencyCode()
        {
                return mPriceDetailsFieldControl.getCurrencyCode();
        }

        private void invalidateOptionsMenu()
        {
                mContext.invalidateOptionsMenu();
        }

        enum Event
        {
                ON_LOAD_ITEM, ON_DELETE, ON_UP, ON_BACK, ON_CHANGE, ON_LEAVE, ON_STAY, ON_CREATE_OPTIONS_MENU, ON_OK
        }

        enum ShoppingListState
        {
                TRANSIENT
                        {
                                @Override
                                ShoppingListState transition(Event event, HistoryEditingControl control)
                                {
                                        ShoppingListState state;
                                        switch (event)
                                        {
                                                case ON_LOAD_ITEM:
                                                        if (control.isItemInShoppingList())
                                                        {
                                                                state = IN_SHOPPING_LIST;
                                                        }
                                                        else
                                                        {
                                                                state = NOT_IN_SHOPPING_LIST;
                                                        }
                                                        break;
                                                default:
                                                        state = NOT_IN_SHOPPING_LIST;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }
                        },

                NOT_IN_SHOPPING_LIST
                        {
                                @Override
                                ShoppingListState transition(Event event, HistoryEditingControl control)
                                {
                                        ShoppingListState shoppingListState;
                                        switch (event)
                                        {
                                                case ON_DELETE:
                                                        control.delete();
                                                        control.setExitTransition();
                                                        control.showDbMessage();
                                                        shoppingListState = this;
                                                        break;

                                                case ON_OK:
                                                        control.update();
                                                        control.showDbMessage();
                                                        shoppingListState = this;
                                                        break;

                                                case ON_LEAVE:
                                                        control.finishItemEditing();

                                                default:
                                                        shoppingListState = this;
                                        }

                                        shoppingListState.setAttributes(event, control);
                                        return shoppingListState;
                                }

                                @Override
                                public void setAttributes(Event event, HistoryEditingControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_LOAD_ITEM:
                                                        control.invalidateOptionsMenu();
                                                        break;
                                                case ON_CREATE_OPTIONS_MENU:
                                                        control.setMenuVisible(R.id.menu_remove_item_from_list, true);
                                                        break;
                                        }
                                }
                        },

                IN_SHOPPING_LIST
                        {
                                @Override
                                ShoppingListState transition(Event event, HistoryEditingControl control)
                                {
                                        ShoppingListState shoppingListState;
                                        switch (event)
                                        {
                                                case ON_OK:
                                                        control.update();
                                                        control.showDbMessage();
                                                        shoppingListState = this;
                                                        break;

                                                case ON_LEAVE:
                                                        control.finishItemEditing();

                                                default:
                                                        shoppingListState = this;
                                        }

                                        shoppingListState.setAttributes(event, control);
                                        return shoppingListState;
                                }
                        };

                abstract ShoppingListState transition(Event event, HistoryEditingControl itemContext);

                public void setAttributes(Event event, HistoryEditingControl itemEditorControl)
                {
                }
        }

        enum ChangeState
        {
                UNCHANGE
                        {
                                public ChangeState transition(Event event, HistoryEditingControl control)
                                {
                                        ChangeState changeState;
                                        switch (event)
                                        {
                                                case ON_CHANGE:
                                                        changeState = CHANGE;
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                        control.finishItemEditing();
                                                default:
                                                        changeState = this;
                                        }

                                        changeState.setAttributes(event, control);
                                        return changeState;
                                }
                        },

                CHANGE
                        {
                                public ChangeState transition(Event event, HistoryEditingControl control)
                                {
                                        ChangeState changeState;
                                        switch (event)
                                        {
                                                case ON_BACK:
                                                case ON_UP:
                                                        control.warnChangesMade();
                                                        changeState = this;
                                                        break;
                                                default:
                                                        changeState = this;
                                        }

                                        changeState.setAttributes(event, control);
                                        return changeState;
                                }

                                public void setAttributes(Event event, HistoryEditingControl control)
                                {
                                        control.setMenuVisible(R.id.save_item_details, true);
                                }
                        };

                abstract ChangeState transition(Event event, HistoryEditingControl itemContext);

                public void setAttributes(Event event, HistoryEditingControl itemEditorControl)
                {
                }
        }

        public interface ItemEditorContext extends ItemContext
        {
                void update(ItemManager mItemManager);

                void delete(long itemId);

        }

}
