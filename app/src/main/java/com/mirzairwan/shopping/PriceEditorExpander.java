package com.mirzairwan.shopping;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

/**
 * Created by Mirza Irwan on 14/2/17.
 */
public class PriceEditorExpander
{
        private TextInputLayout mBundleQtyLayout;
        private ViewGroup mRootView;
        private TextInputLayout mCurrencyCodeLayout;
        private ToggleButton mBtnTogglePrice;
        private Activity mActivity;
        private LinearLayout mBundlePriceLayout;

        public PriceEditorExpander(Activity activity)
        {
                mActivity = activity;
                mBtnTogglePrice = (ToggleButton) mActivity.findViewById(R.id.btn_toggle_price);
                mBundlePriceLayout = (LinearLayout) mActivity.findViewById(R.id.bundle_price_group);
                mBundleQtyLayout = (TextInputLayout)mActivity.findViewById(R.id.bundle_qty_layout);
                mCurrencyCodeLayout = (TextInputLayout) mActivity.findViewById(R.id.currency_code_layout);

                // Get the root view to create a transition
                mRootView = (ViewGroup) mActivity.findViewById(R.id.prices_layout);
                setupButton();
        }

        private void setupButton()
        {
                mBtnTogglePrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                        {
                                if (isChecked)
                                {
                                        expandMore();
                                }
                                else
                                {
                                        expandLess();
                                }
                        }
                });
        }

        protected void expandLess()
        {
                Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.item_expand_less);

                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);
                mBundlePriceLayout.setVisibility(View.GONE);
                mBundleQtyLayout.setVisibility(View.GONE);
                mCurrencyCodeLayout.setVisibility(View.GONE);
        }

        protected void expandMore()
        {
                Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.item_expand_more);

                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);
                mBundlePriceLayout.setVisibility(View.VISIBLE);
                mBundleQtyLayout.setVisibility(View.VISIBLE);
                mCurrencyCodeLayout.setVisibility(View.VISIBLE);
        }
}
