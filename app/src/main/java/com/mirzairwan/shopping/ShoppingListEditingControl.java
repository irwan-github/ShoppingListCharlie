package com.mirzairwan.shopping;

import android.view.Menu;
import android.view.View;

import com.mirzairwan.shopping.domain.Item;
import com.mirzairwan.shopping.domain.PriceMgr;

import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_BACK;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_CHANGE;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_CREATE_OPTIONS_MENU;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_DELETE;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_EDIT;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_LEAVE;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_NEW;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_OK;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_STAY;
import static com.mirzairwan.shopping.ShoppingListEditingControl.Event.ON_UP;
import static com.mirzairwan.shopping.ShoppingListEditingControl.ItemType.TRANSIENT;
import static com.mirzairwan.shopping.ShoppingListEditingControl.State.UNCHANGE;


/**
 * Created by Mirza Irwan on 25/2/17.
 *
 * Matches the UI-events to states.
 * The states will do the corresponding actions and set the properties of the ui
 */

public class ShoppingListEditingControl implements ItemControl
{
        /* Track whether item is new or existing. This is a top level state */
        private ItemType mItemType = TRANSIENT;

        /* Track state of item is changed */
        private State mCurrentState = UNCHANGE;

        /* Control the state of item details */
        private ItemDetailsFieldControl mItemDetailsFieldControl;

        /* Control the state of purchase and pricing details */
        private ItemPurchaseControl mItemPurchaseControl;

        private String LOG_TAG = ShoppingListEditingControl.class.getCanonicalName();
        private ShoppingItemContext mContext;
        private PurchaseManager mPurchaseManager;

        private Menu mMenu;

        public ShoppingListEditingControl(ShoppingItemContext context)
        {
                mContext = context;
                mItemDetailsFieldControl = new ItemDetailsFieldControl(context);
                mItemPurchaseControl = new ItemPurchaseControl(context);
        }

        public void onExistingItem()
        {
                mItemType = mItemType.transition(ON_EDIT, this);
                mCurrentState = mCurrentState.transition(ON_EDIT, this);
        }

        public void onNewItem()
        {
                mItemType = mItemType.transition(ON_NEW, this);
                mCurrentState = mCurrentState.transition(ON_NEW, this);
                mItemPurchaseControl.onNewItem();
        }

        public void setPurchaseManager(PurchaseManager purchaseManager)
        {
                mPurchaseManager = purchaseManager;
                mItemPurchaseControl.setPurchaseManager(purchaseManager);
        }

        @Override
        public void onChange()
        {
                mCurrentState = mCurrentState.transition(ON_CHANGE, this);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu)
        {
                mMenu = menu;
                mItemType = mItemType.transition(ON_CREATE_OPTIONS_MENU, this);
                mCurrentState = mCurrentState.transition(ON_CREATE_OPTIONS_MENU, this);
        }

        public void invalidateOptionsMenu()
        {
                mContext.invalidateOptionsMenu();
        }

        @Override
        public void onDelete()
        {
                mItemType = mItemType.transition(ON_DELETE, this);
        }

        @Override
        public void onUp()
        {
                mCurrentState = mCurrentState.transition(ON_UP, this);
        }

        @Override
        public void onLeave()
        {
                mItemType = mItemType.transition(ON_LEAVE, this);
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
                mItemDetailsFieldControl.onValidate();
                if (mItemDetailsFieldControl.isInErrorState())
                {
                        return;
                }

                mItemPurchaseControl.onValidate();
                if (mItemPurchaseControl.isInErrorState())
                {
                        return;
                }

                mItemType = mItemType.transition(ON_OK, this);
        }

//        public void setItemDetailsFieldControl(ItemDetailsFieldControl itemDetailsFieldControl)
//        {
//                mItemDetailsFieldControl = itemDetailsFieldControl;
//        }

        private void delete()
        {
                mContext.delete(mPurchaseManager.getItemInShoppingList().getId());
        }

        private void insert()
        {
                Item item = mItemDetailsFieldControl.populateItemFromInputFields();

                mPurchaseManager.setItem(item);

                mItemPurchaseControl.populatePurchaseMgr();

                mContext.insert(mPurchaseManager);
        }

        private void update()
        {
                Item item = mItemDetailsFieldControl.populateItemFromInputFields();

                mPurchaseManager.setItem(item);

                mItemPurchaseControl.populatePurchaseMgr();

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

        private void setMenuItemVisibility(int menuId, boolean isVisible)
        {
                mMenu.findItem(menuId).setVisible(isVisible);
        }

        private void setTitle(int stringResId)
        {
                mContext.setTitle(stringResId);
        }

        private void showDbMessage()
        {
                mContext.showTransientDbMessage();
        }

        private void setExitTransition()
        {
                mContext.setExitTransition();
        }

        public void setItemBuyQtyFieldControl(ItemPurchaseControl itemPurchaseControl)
        {
                mItemPurchaseControl = itemPurchaseControl;
        }

        public void setOnTouchListener(View.OnTouchListener onTouchListener)
        {
                mItemDetailsFieldControl.setOnTouchListener(onTouchListener);
                mItemPurchaseControl.setOnTouchListener(onTouchListener);
        }

        public void onLoadFinished(PurchaseManager purchaseManager)
        {
                mPurchaseManager = purchaseManager;
                mItemDetailsFieldControl.onLoadItemFinished(mPurchaseManager.getitem());
                mItemPurchaseControl.onLoadFinished(mPurchaseManager);
        }

        public void onLoadPriceFinished(PriceMgr priceMgr)
        {
                mItemPurchaseControl.onLoadPriceFinished(priceMgr);
        }

        public String getCurrencyCode()
        {
                return mItemPurchaseControl.getCurrencyCode();
        }

        public void setPriceMgr(PriceMgr priceMgr)
        {
                mItemPurchaseControl.setPriceMgr(priceMgr);
        }

        public void onLoaderReset()
        {

        }

        enum Event
        {
                ON_OK, ON_NEW, ON_EDIT, ON_DELETE, ON_UP, ON_BACK, ON_CHANGE, ON_LEAVE, ON_STAY, ON_CREATE_OPTIONS_MENU
        }

        enum ItemType
        {
                TRANSIENT
                        {
                                @Override
                                public ItemType transition(Event event, ShoppingListEditingControl control)
                                {
                                        ItemType state = this;

                                        switch (event)
                                        {
                                                case ON_NEW:
                                                        state = NEW_ITEM;
                                                        break;
                                                case ON_EDIT:
                                                        state = EXISTING_ITEM;
                                                        control.invalidateOptionsMenu();
                                                        break;
                                        }

                                        state.setUiAttibutes(event, control);
                                        return state;
                                }
                        },

                NEW_ITEM
                        {
                                @Override
                                public ItemType transition(Event event, ShoppingListEditingControl control)
                                {
                                        ItemType state = this;
                                        switch(event)
                                        {
                                                case ON_OK:
                                                        control.insert();
                                                        control.showDbMessage();
                                                        break;
                                                case ON_LEAVE:
                                                        control.finishItemEditing();
                                                        state = this;
                                                        break;
                                        }

                                        state.setUiAttibutes(event, control);
                                        return state;
                                }

                                @Override
                                public void setUiAttibutes(Event event, ShoppingListEditingControl control)
                                {
                                        control.setTitle(R.string.new_buy_item_title);
                                }
                        },

                EXISTING_ITEM
                        {
                                @Override
                                public ItemType transition(Event event, ShoppingListEditingControl control)
                                {
                                        ItemType state = this;
                                        switch(event)
                                        {
                                                case ON_CREATE_OPTIONS_MENU:
                                                        state = this;
                                                        break;
                                                case ON_OK:
                                                        control.update();
                                                        control.showDbMessage();
                                                        break;
                                                case ON_DELETE:
                                                        control.delete();
                                                        control.showDbMessage();
                                                        break;
                                                case ON_LEAVE:
                                                        control.finishItemEditing();
                                                        state = this;
                                                        break;
                                        }
                                        state.setUiAttibutes(event, control);
                                        return state;
                                }

                                @Override
                                public void setUiAttibutes(Event event, ShoppingListEditingControl control)
                                {
                                        control.setTitle(R.string.view_buy_item_details);

                                        switch(event)
                                        {

                                                case ON_DELETE:
                                                        control.setExitTransition();
                                                        break;

                                                case ON_CREATE_OPTIONS_MENU:
                                                        control.setMenuItemVisibility(R.id.menu_remove_item_from_list, true);
                                                        break;
                                        }
                                }
                        };

                public abstract ItemType transition(Event event, ShoppingListEditingControl control);

                public void setUiAttibutes(Event event, ShoppingListEditingControl control)
                {

                }
        }

        enum State
        {
                UNCHANGE
                        {
                                public State transition(Event event, ShoppingListEditingControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_CHANGE:
                                                        state = CHANGE;
                                                        control.invalidateOptionsMenu();
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                        control.finishItemEditing();
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }
                        },

                CHANGE
                        {
                                public State transition(Event event, ShoppingListEditingControl control)
                                {
                                        State state;
                                        switch (event)
                                        {
                                                case ON_CREATE_OPTIONS_MENU:
                                                        state = this;
                                                        break;
                                                case ON_BACK:
                                                case ON_UP:
                                                        control.warnChangesMade();
                                                case ON_STAY:
                                                default:
                                                        state = this;
                                        }

                                        state.setAttributes(event, control);
                                        return state;
                                }

                                public void setAttributes(Event event, ShoppingListEditingControl control)
                                {
                                        switch (event)
                                        {
                                                case ON_CREATE_OPTIONS_MENU:
                                                        control.setMenuItemVisibility(R.id.save_item_details, true);
                                                        break;
                                        }
                                }
                        };

                public abstract State transition(Event event, ShoppingListEditingControl control);

                public void setAttributes(Event event, ShoppingListEditingControl itemControl)
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
