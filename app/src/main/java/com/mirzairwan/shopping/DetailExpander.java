package com.mirzairwan.shopping;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public abstract class DetailExpander
{
        private ViewGroup mRootView;
        private ToggleButton mToggleButton;
        private Activity mActivity;

        public DetailExpander(Activity activity)
        {
                mActivity = activity;
                mToggleButton = (ToggleButton)mActivity.findViewById(getToggleButtonId());

                // Get the root view to create a transition
                mRootView = (ViewGroup)mActivity.findViewById(getViewGroupId());
                setupButton();
        }

        protected abstract int getViewGroupId();

        protected abstract int getToggleButtonId();

        private void setupButton()
        {
                mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                        {
                                if (isChecked)
                                {
                                        showMore();
                                }
                                else
                                {
                                        showLess();
                                }
                        }
                });
        }

        protected void expandLess()
        {
                Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.item_expand_less);

                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);
                mRootView.setVisibility(View.GONE);
        }

        protected void expandMore()
        {
                Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.item_expand_more);

                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);
                mRootView.setVisibility(View.VISIBLE);
        }

        public void showMore()
        {
                mRootView.setVisibility(View.INVISIBLE);
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


}