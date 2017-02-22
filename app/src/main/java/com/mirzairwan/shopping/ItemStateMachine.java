package com.mirzairwan.shopping;

import static com.mirzairwan.shopping.ItemStateMachine.Event.ON_BACK_PRESSED;
import static com.mirzairwan.shopping.ItemStateMachine.Event.ON_CHANGE;
import static com.mirzairwan.shopping.ItemStateMachine.Event.ON_DELETE;
import static com.mirzairwan.shopping.ItemStateMachine.Event.ON_LEAVE;
import static com.mirzairwan.shopping.ItemStateMachine.Event.ON_SAVE;
import static com.mirzairwan.shopping.ItemStateMachine.Event.ON_SAVE_VALIDATE;
import static com.mirzairwan.shopping.ItemStateMachine.Event.ON_STAY;
import static com.mirzairwan.shopping.ItemStateMachine.Event.ON_UP;
import static com.mirzairwan.shopping.ItemStateMachine.LifecycleState.UNCHANGED;

/**
 * Created by Mirza Irwan on 21/2/17.
 */

public class ItemStateMachine
{
        ItemContext mContext;
        State mState; //New or Existing
        private LifecycleState mModifiedState;

        public ItemStateMachine(ItemContext context, State state)
        {
                mContext = context;
                mState = state;
                mModifiedState = UNCHANGED;
        }

        public void onChange()
        {
                mModifiedState.transition(this, ON_CHANGE);
                mModifiedState.process(this);
        }

        public void onUp()
        {
                mModifiedState.transition(this, ON_UP);
                mModifiedState.process(this);
        }

        public void onBackPressed()
        {
                mModifiedState.transition(this, ON_BACK_PRESSED);
                mModifiedState.process(this);
        }

        public void onProcessDelete()
        {
                mModifiedState.transition(this, ON_DELETE);
                mModifiedState.process(this);
        }

        public void onProcessSave()
        {
                mModifiedState.transition(this, ON_SAVE_VALIDATE);
                mModifiedState.process(this);
        }


        public void onLeave()
        {
                mModifiedState.transition(this, ON_LEAVE);
                mModifiedState.process(this);
        }

        public void onStay()
        {
                mModifiedState.transition(this, ON_STAY);
                mModifiedState.process(this);
        }

        public State getState()
        {
                return mState;
        }

        enum Event
        {
                ON_CHANGE, ON_SAVE_VALIDATE, ON_SAVE, ON_DELETE, ON_UP, ON_STAY, ON_BACK_PRESSED, ON_LEAVE;
        }


        public enum State
        {
                NEW, EXIST
        }

        public enum LifecycleState
        {
                UNCHANGED
                        {
                                @Override
                                public void transition(ItemStateMachine sm, Event event)
                                {
                                        switch (event)
                                        {
                                                case ON_UP:
                                                case ON_BACK_PRESSED:
                                                        sm.mModifiedState = ON_END;
                                                        break;
                                                case ON_DELETE:
                                                        if (sm.mState == State.EXIST)
                                                                sm.mModifiedState = DELETED;
                                                        break;
                                                case ON_CHANGE:
                                                        sm.mModifiedState = CHANGED;
                                                        break;
                                        }
                                }
                        },

                CHANGED
                        {
                                @Override
                                public void transition(ItemStateMachine sm, Event event)
                                {
                                        switch (event)
                                        {
                                                case ON_UP:
                                                case ON_BACK_PRESSED:
                                                        sm.mModifiedState = WARNED;
                                                        break;
                                                case ON_DELETE:
                                                        if (sm.mState == State.EXIST)
                                                                sm.mModifiedState = DELETED;
                                                        break;
                                                case ON_SAVE_VALIDATE:
                                                        sm.mModifiedState = ERROR_VALIDATION;
                                                        break;
                                        }
                                }
                        },

                WARNED
                        {
                                @Override
                                public void transition(ItemStateMachine sm, Event event)
                                {
                                        switch (event)
                                        {
                                                case ON_STAY:
                                                        sm.mModifiedState = CHANGED;
                                                        break;
                                                case ON_LEAVE:
                                                        sm.mModifiedState = ON_END;
                                                        break;
                                        }
                                }

                                @Override
                                public void process(ItemStateMachine sm)
                                {
                                        sm.mContext.warnChangesMade();
                                }
                        },

                ERROR_VALIDATION
                        {
                                @Override
                                public void transition(ItemStateMachine sm, Event event)
                                {
                                        switch (event)
                                        {
                                                case ON_UP:
                                                case ON_BACK_PRESSED:
                                                        sm.mModifiedState = WARNED;
                                                        break;
                                                case ON_DELETE:
                                                        if (sm.mState == State.EXIST)
                                                                sm.mModifiedState = DELETED;
                                                        break;
                                                case ON_SAVE:
                                                {
                                                        switch (sm.mState)
                                                        {
                                                                case NEW:
                                                                        sm.mModifiedState = INSERT;
                                                                        break;
                                                                case EXIST:
                                                                        sm.mModifiedState = UPDATE;
                                                                        break;
                                                        }
                                                }
                                                break;
                                        }
                                }

                                @Override
                                public void process(ItemStateMachine sm)
                                {
                                        if (sm.mContext.areFieldsValid())
                                        {
                                                sm.mModifiedState.transition(sm, ON_SAVE);
                                                sm.mModifiedState.process(sm);

                                        }

                                }
                        },

                INSERT
                        {
                                @Override
                                public void transition(ItemStateMachine sm, Event event)
                                {
                                        switch (event)
                                        {
                                                case ON_LEAVE:
                                                        sm.mModifiedState = ON_END;
                                                        break;
                                        }
                                }

                                @Override
                                public void process(ItemStateMachine sm)
                                {

                                        sm.mContext.insert();
                                        sm.mContext.postDbProcess();

                                }
                        },

                UPDATE
                        {
                                @Override
                                public void transition(ItemStateMachine sm, Event event)
                                {
                                        switch (event)
                                        {
                                                case ON_LEAVE:
                                                        sm.mModifiedState = ON_END;
                                                        break;
                                        }
                                }

                                @Override
                                public void process(ItemStateMachine sm)
                                {

                                        sm.mContext.update();
                                        sm.mContext.postDbProcess();

                                }
                        },

                DELETED
                        {
                                @Override
                                public void transition(ItemStateMachine sm, Event event)
                                {
                                        switch (event)
                                        {
                                                case ON_LEAVE:
                                                        sm.mModifiedState = ON_END;
                                                        break;
                                        }
                                }

                                @Override
                                public void process(ItemStateMachine sm)
                                {
                                        sm.mContext.delete();
                                        sm.mContext.postDbProcess();
                                }
                        },

                ON_END
                        {
                                @Override
                                public void transition(ItemStateMachine sm, Event event)
                                {
                                        /* No transition for this state */
                                }

                                @Override
                                public void process(ItemStateMachine sm)
                                {
                                        sm.mContext.cleanUp();
                                        sm.mContext.finishItemEditing();
                                }
                        };

                public abstract  void transition(ItemStateMachine sm, Event event);

                public void process(ItemStateMachine sm)
                {

                }
        }

        public interface ItemContext
        {
                void finishItemEditing();

                boolean areFieldsValid();

                void postDbProcess();

                void warnChangesMade();

                void update();

                void insert();

                void cleanUp();

                void delete();
        }


}
