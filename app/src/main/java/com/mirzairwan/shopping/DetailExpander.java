package com.mirzairwan.shopping;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import static com.mirzairwan.shopping.DetailExpander.Event.BUTTON_CLICK;
import static com.mirzairwan.shopping.DetailExpander.Event.ON_INITIALIZE_CONTRACT;
import static com.mirzairwan.shopping.DetailExpander.State.CONTRACT;
import static com.mirzairwan.shopping.DetailExpander.State.NEUTRAL;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public abstract class DetailExpander implements CompoundButton.OnCheckedChangeListener
{
        private ItemContext mItemContext;
        private ViewGroup mRootView;
        private ToggleButton mToggleButton;
        private Activity mActivity;
        private State mState = NEUTRAL;

        public DetailExpander(Activity activity, Event initialEvent)
        {
                mActivity = activity;
                mToggleButton = (ToggleButton) mActivity.findViewById(getToggleButtonId());

                // Get the root view to create a transition
                mRootView = (ViewGroup) mActivity.findViewById(getViewGroupId());
                setupButton();
                mState = mState.transition(initialEvent, this);
        }

        public DetailExpander(Activity activity)
        {
                mActivity = activity;
                mToggleButton = (ToggleButton) mActivity.findViewById(getToggleButtonId());

                // Get the root view to create a transition
                mRootView = (ViewGroup) mActivity.findViewById(getViewGroupId());
                setupButton();
                mState = mState.transition(ON_INITIALIZE_CONTRACT, this);
        }

        public DetailExpander(ItemContext itemContext)
        {
                mState = CONTRACT;
                mItemContext = itemContext;
                mToggleButton = (ToggleButton) itemContext.findViewById(getToggleButtonId());

                // Get the root view to create a transition
                mRootView = (ViewGroup) itemContext.findViewById(getViewGroupId());
                setupButton();
        }

        protected abstract int getViewGroupId();

        protected abstract int getToggleButtonId();

        private void setupButton()
        {
                mToggleButton.setOnCheckedChangeListener(this);
        }

        /* User events delegated to state machine */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
                buttonClick();
        }

        private void buttonClick()
        {
                mState = mState.transition(BUTTON_CLICK, this);
        }

        private void showVisible(boolean isVisible)
        {
                mRootView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }

        /**
         * Ui Action methods for version Kitkat and below
         */
        private void expandLess()
        {
                //Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.item_expand_less);
                Transition transition = mItemContext.inflateTransition(R.transition.item_expand_less);

                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);
                mRootView.setVisibility(View.GONE);
        }

        /**
         * Ui Action methods for version Kitkat and below
         */
        private void expandMore()
        {
                Transition transition = mItemContext.inflateTransition(R.transition.item_expand_more);

                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);
                mRootView.setVisibility(View.VISIBLE);
        }

        /**
         * Ui Action methods
         */
        public void showMore()
        {
                //mRootView.setVisibility(View.INVISIBLE);
                // get the center for the clipping circle
                int cx = mRootView.getWidth() / 2;
                int cy = mRootView.getHeight() / 2;
                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);
                // create the animator for this view (the start radius is zero)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {

                        Animator anim = ViewAnimationUtils.createCircularReveal(mRootView, cx, cy, 0, finalRadius);

                        anim.addListener(new AnimatorListenerAdapter()
                        {
                                @Override
                                public void onAnimationStart(Animator animation)
                                {
                                        // make the view visible and start the animation
                                        super.onAnimationStart(animation);
                                        mRootView.setVisibility(View.VISIBLE);
                                }
                        });

                        anim.start();
                }
                else
                {
                        expandMore();
                }

        }

        /**
         * Ui Action methods
         */
        private void showLess()
        {
                // previously visible view
                // get the center for the clipping circle
                int cx = mRootView.getWidth() / 2;
                int cy = mRootView.getHeight() / 2;

                // get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(cx, cy);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                        // create the animation (the final radius is zero)
                        Animator anim = ViewAnimationUtils.createCircularReveal(mRootView, cx, cy, initialRadius, 0);

                        // make the view invisible when the animation is done
                        anim.addListener(new AnimatorListenerAdapter()
                        {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                        super.onAnimationEnd(animation);
                                        mRootView.setVisibility(View.GONE);
                                }
                        });

                        // start the animation
                        anim.start();
                }
                else
                {
                        expandLess();
                }
        }

        enum Event
        {
                ON_INITIALIZE_EXPAND, ON_INITIALIZE_CONTRACT, BUTTON_CLICK
        }

        enum State
        {
                NEUTRAL
                        {
                                @Override
                                State transition(Event event, DetailExpander control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case ON_INITIALIZE_EXPAND:
                                                        state = EXPAND;
                                                        break;
                                                default:
                                                        state = CONTRACT;
                                        }
                                        state.showUiOuput(event, control);
                                        return state;
                                }

                                @Override
                                void showUiOuput(Event event, DetailExpander control)
                                {
                                        switch (event)
                                        {
                                                case ON_INITIALIZE_EXPAND:
                                                        control.showVisible(true);
                                                        break;
                                                default:
                                                        control.showVisible(false);
                                        }
                                }
                        },

                CONTRACT
                        {
                                @Override
                                State transition(Event event, DetailExpander control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case BUTTON_CLICK:
                                                        state = EXPAND;
                                                        break;
                                        }

                                        state.showUiOuput(event, control);
                                        return state;
                                }

                                @Override
                                void showUiOuput(Event event, DetailExpander control)
                                {
                                        switch (event)
                                        {
                                                case ON_INITIALIZE_CONTRACT:
                                                        control.showVisible(false);
                                                        break;
                                                default:
                                                        control.showLess();
                                        }
                                }
                        },

                EXPAND
                        {
                                @Override
                                State transition(Event event, DetailExpander control)
                                {
                                        State state = this;
                                        switch (event)
                                        {
                                                case BUTTON_CLICK:
                                                        state = CONTRACT;
                                                        break;
                                        }
                                        state.showUiOuput(event, control);
                                        return state;
                                }

                                @Override
                                void showUiOuput(Event event, DetailExpander control)
                                {
                                        switch (event)
                                        {
                                                case ON_INITIALIZE_EXPAND:
                                                        control.showVisible(true);
                                                        break;
                                                default:
                                                        control.showMore();
                                        }

                                }
                        };

                abstract State transition(Event event, DetailExpander control);

                void showUiOuput(Event event, DetailExpander control)
                {

                }

        }


}