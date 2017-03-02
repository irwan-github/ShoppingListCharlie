package com.mirzairwan.shopping;

import android.util.Log;

import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.ItemBuyQtyControl.State.BUNDLE_BUY_QUANTITY_ERROR;
import static com.mirzairwan.shopping.ItemBuyQtyControl.State.UNIT_BUY_QUANTITY_ERROR;
import static com.mirzairwan.shopping.ItemEditFieldControl.State.ERROR_EMPTY_NAME;
import static com.mirzairwan.shopping.PriceEditFieldControl.State.BUNDLE_QTY_ERROR;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_BACK;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_CHANGE;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_CREATE_OPTIONS_MENU;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_DB_RESULT;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_DELETE;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_EDIT;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_INSERT;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_LEAVE;
import static com.mirzairwan.shopping.ShoppingItemControl.Event.ON_LOAD_PRICE;
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
        protected State mCurrentState = START;
        protected ItemType mItemType;
        protected PriceMgr mPriceMgr;
        private ItemEditFieldControl mItemEditFieldControl;
        private ShoppingItemContext mContext;
        private PurchaseManager mPurchaseManager;
        private String LOG_TAG = ShoppingItemControl.class.getCanonicalName();
        private ItemBuyQtyControl mItemBuyQtyControl;
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

        @Override
        public void onLoadPriceFinished(PriceMgr priceMgr)
        {
                mPriceMgr = priceMgr;
                mCurrentState = mCurrentState.transition(ON_LOAD_PRICE, this);
        }

        public void onOk()
        {
                mItemEditFieldControl.onValidate();
                if (mItemEditFieldControl.getState() == ERROR_EMPTY_NAME)
                {
                        return;
                }

                mItemBuyQtyControl.onValidate();
                if (mPriceEditFieldControl.getState() == BUNDLE_QTY_ERROR || mItemBuyQtyControl.getState() == UNIT_BUY_QUANTITY_ERROR
                        || mItemBuyQtyControl.getState() == BUNDLE_BUY_QUANTITY_ERROR)
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

        @Override
        public void setItemNameFieldControl(ItemEditFieldControl itemEditFieldControl)
        {
                mItemEditFieldControl = itemEditFieldControl;
        }

        private void populateItemInputFields()
        {
                //Item item = mPurchaseManager.getitem();
                //mContext.populateItemInputFields(item);
        }

        private void populatePricesInputFields()
        {
                mContext.populatePricesInputFields(mPriceMgr);
        }

        private void delete()
        {
                mContext.delete(mPurchaseManager.getItemInShoppingList().getId());
        }

        private void insert()
        {
                Item item = mItemEditFieldControl.populateItemFromInputFields();
                mPurchaseManager.setItem(item);

                String itemQuantity = mItemBuyQtyControl.getQuantity();
                mPurchaseManager.getItemInShoppingList().setQuantity(Integer.parseInt(itemQuantity));

                mPriceEditFieldControl.populatePriceMgr();

                mContext.insert(mPurchaseManager);
        }

        private void update()
        {
                Item item = mItemEditFieldControl.populateItemFromInputFields();
                mPurchaseManager.setItem(item);

                String itemQuantity = mItemBuyQtyControl.getQuantity();
                mPurchaseManager.getItemInShoppingList().setQuantity(Integer.parseInt(itemQuantity));

                mPriceEditFieldControl.populatePriceMgr();

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

        public void setItemBuyQtyFieldControl(ItemBuyQtyControl itemBuyQtyControl)
        {
                mItemBuyQtyControl = itemBuyQtyControl;
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
