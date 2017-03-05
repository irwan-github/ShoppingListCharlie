package com.mirzairwan.shopping;

import android.util.Log;

import com.mirzairwan.shopping.domain.Item;

import static com.mirzairwan.shopping.ItemBuyFieldControl.State.BUY_ERROR;
import static com.mirzairwan.shopping.ItemEditFieldControl.State.ERROR_EMPTY_NAME;
import static com.mirzairwan.shopping.PriceEditFieldControl.State.PRICE_ERROR;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_BACK;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_CHANGE;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_CREATE_OPTIONS_MENU;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_DB_RESULT;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_DELETE;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_EDIT;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_INSERT;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_LEAVE;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_NEW;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_STAY;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_UP;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_UPDATE;
import static com.mirzairwan.shopping.ShoppingItemControl.ItemType.EXISTING_ITEM;
import static com.mirzairwan.shopping.ShoppingItemControl.ItemType.NEW_ITEM;
import static com.mirzairwan.shopping.ShoppingItemControl.State.START;


/**
 * Created by Mirza Irwan on 25/2/17.
 */

public class ShoppingItemControl implements ItemControl
{
        private String LOG_TAG = ShoppingItemControl.class.getCanonicalName();
        protected State mCurrentState = START;
        protected ItemType mItemType;
        private ShoppingItemContext mContext;
        private PurchaseManager mPurchaseManager;
        private ItemEditFieldControl mItemEditFieldControl;
        private ItemBuyFieldControl mItemBuyFieldControl;
        private PriceEditFieldControl mPriceEditFieldControl;

        public ShoppingItemControl(ShoppingItemContext context)
        {
                mContext = context;
        }

        @Override
        public void onExistingItem()
        {
                mItemType = EXISTING_ITEM;
                mCurrentState = mCurrentState.transition(ON_EDIT, this);
        }

        public void onNewItem()
        {
                mItemType = NEW_ITEM;
                mCurrentState = mCurrentState.transition(ON_NEW, this);
                mItemBuyFieldControl.onNewItem();
                mPriceEditFieldControl.onNewItem();
        }

        public void setPurchaseManager(PurchaseManager purchaseManager)
        {
                mPurchaseManager = purchaseManager;
        }

        @Override
        public void onChange()
        {
                mCurrentState = mCurrentState.transition(ON_CHANGE, this);
        }

        @Override
        public void onCreateOptionsMenu()
        {
                mCurrentState = mCurrentState.transition(ON_CREATE_OPTIONS_MENU, this);
        }

        @Override
        public void onDelete()
        {
                mCurrentState = mCurrentState.transition(ON_DELETE, this);
                mCurrentState = mCurrentState.transition(ON_DB_RESULT, this);
        }

        @Override
        public void onUp()
        {
                mCurrentState = mCurrentState.transition(ON_UP, this);
        }

        @Override
        public void onLeave()
        {
                mCurrentState = mCurrentState.transition(ON_LEAVE, this);
        }

        @Override
        public void onBackPressed()
        {
                mCurrentState = mCurrentState.transition(ON_BACK, this);
        }

        @Override
        public void onStay()
        {
                mCurrentState = mCurrentState.transition(ON_STAY, this);
        }

        public void onOk()
        {
                mItemEditFieldControl.onValidate();

                if (mItemEditFieldControl.getState() == ERROR_EMPTY_NAME)
                {
                        return;
                }

                mItemBuyFieldControl.onValidate();

                if (mPriceEditFieldControl.getErrorState() == PRICE_ERROR )
                {
                        return;
                }

                if(mItemBuyFieldControl.getErrorState() == BUY_ERROR)
                {
                        return;
                }

                if (mItemType == NEW_ITEM)
                {
                        mCurrentState = mCurrentState.transition(ON_INSERT, this);
                        mCurrentState = mCurrentState.transition(ON_DB_RESULT, this);
                        return;
                }

                if (mItemType == EXISTING_ITEM)
                {
                        mCurrentState = mCurrentState.transition(ON_UPDATE, this);
                        mCurrentState = mCurrentState.transition(ON_DB_RESULT, this);
                        return;
                }
        }

        public void setItemEditFieldControl(ItemEditFieldControl itemEditFieldControl)
        {
                mItemEditFieldControl = itemEditFieldControl;
        }

        private void delete()
        {
                mContext.delete(mPurchaseManager.getItemInShoppingList().getId());
        }

        private void insert()
        {
                Item item = mItemEditFieldControl.populateItemFromInputFields();

                mPurchaseManager.setItem(item);

                mPriceEditFieldControl.populatePriceMgr();

                mItemBuyFieldControl.populatePurchaseMgr();

                mContext.insert(mPurchaseManager);
        }

        private void update()
        {
                Item item = mItemEditFieldControl.populateItemFromInputFields();

                mPurchaseManager.setItem(item);

                mPriceEditFieldControl.populatePriceMgr();

                mItemBuyFieldControl.populatePurchaseMgr();

                mContext.update(mPurchaseManager);
        }

        private void finishItemEditing()
        {
                mContext.finishItemEditing();
        }

        private void warnChangesMade()
        {
                mContext.warnChangesMade();
        }

        private void setMenuVisible(int menuResId, boolean b)
        {
                mContext.setMenuVisible(menuResId, b);
        }

        private void setTitle(int stringResId)
        {
                mContext.setTitle(stringResId);
        }

        private void showDbMessage()
        {
                mContext.showTransientDbMessage();
        }

        private ItemType itemType()
        {
                return mItemType;
        }

        private void setExitTransition()
        {
                mContext.setExitTransition();
        }

        public void setItemBuyQtyFieldControl(ItemBuyFieldControl itemBuyFieldControl)
        {
                mItemBuyFieldControl = itemBuyFieldControl;
        }

        public void setPriceEditFieldControl(PriceEditFieldControl priceEditFieldControl)
        {
                mPriceEditFieldControl = priceEditFieldControl;
        }

        enum Event
        {
                ON_NEW, ON_EDIT, ON_DELETE, ON_UP, ON_BACK, ON_CHANGE, ON_INSERT, ON_UPDATE, ON_LEAVE, ON_LOAD_ITEM, ON_LOAD_PRICE, ON_STAY, ON_DB_RESULT,
                ON_CREATE_OPTIONS_MENU
        }

        enum ItemType
        {
                NEW_ITEM, EXISTING_ITEM
        }

        enum State
        {
                START
                        {
                                public State transition(Event event, ShoppingItemControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_NEW:
                                                case ON_EDIT:
                                                default:
                                                        state = UNCHANGE;
                                        }
                                        return state;
                                }
                        },

                UNCHANGE
                        {
                                public State transition(Event event, ShoppingItemControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_CHANGE:
                                                        state = CHANGE;
                                                        break;
                                                case ON_DELETE:
                                                        control.delete();
                                                        state = POST_DB_OP;
                                                        break;
                                                case ON_CREATE_OPTIONS_MENU:
                                                        state = this;
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                        control.finishItemEditing();
                                                        state = this;
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }

                                public void setAttributes(Event event, ShoppingItemControl control)
                                {
                                        if (control.itemType() == NEW_ITEM)
                                        {
                                                control.setMenuVisible(R.id.menu_remove_item_from_list, false);
                                                control.setTitle(R.string.new_buy_item_title);
                                        }
                                        if (control.itemType() == EXISTING_ITEM)
                                        {
                                                control.setTitle(R.string.view_buy_item_details);
                                                control.setMenuVisible(R.id.menu_remove_item_from_list, true);
                                        }
                                        control.setMenuVisible(R.id.save_item_details, false);
                                }
                        },

                CHANGE
                        {
                                public State transition(Event event, ShoppingItemControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_INSERT:
                                                        control.insert();
                                                        state = POST_DB_OP;
                                                        break;
                                                case ON_UPDATE:
                                                        control.update();
                                                        state = POST_DB_OP;
                                                        break;
                                                case ON_DELETE:
                                                        control.delete();
                                                        state = POST_DB_OP;
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                        control.warnChangesMade();
                                                        state = WARN;
                                                        break;
                                                case ON_LEAVE:
                                                        control.finishItemEditing();
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }

                                public void setAttributes(Event event, ShoppingItemControl context)
                                {
                                        context.setMenuVisible(R.id.save_item_details, true);
                                }
                        },

                WARN
                        {
                                public State transition(Event event, ShoppingItemControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_LEAVE:
                                                        control.finishItemEditing();
                                                        state = this;
                                                        break;
                                                case ON_STAY:
                                                        state = CHANGE;
                                                        break;
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }
                        },

                POST_DB_OP
                        {
                                @Override
                                public State transition(Event event, ShoppingItemControl control)
                                {
                                        Log.d(this.toString(), "Event:" + event);
                                        State state;
                                        switch (event)
                                        {
                                                case ON_DB_RESULT:
                                                        control.showDbMessage();
                                                        state = this;
                                                        break;
                                                case ON_LEAVE:
                                                        control.finishItemEditing();
                                                        state = this;
                                                        break;
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }

                                @Override
                                public void setAttributes(Event event, ShoppingItemControl context)
                                {
                                        if (event == ON_DELETE)
                                        {
                                                context.setExitTransition();
                                        }
                                }
                        };

                public abstract State transition(Event event, ShoppingItemControl control);

                public void setAttributes(Event event, ShoppingItemControl itemControl)
                {
                }

        }


        public interface ShoppingItemContext extends ItemContext
        {
                void delete(long id);

                void update(PurchaseManager mPurchaseManager);

                void insert(PurchaseManager purchaseManager);
        }


}
