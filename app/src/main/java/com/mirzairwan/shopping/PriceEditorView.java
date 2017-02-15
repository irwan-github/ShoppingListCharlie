package com.mirzairwan.shopping;

import android.support.v7.app.AppCompatActivity;
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
public class PriceEditorView
{
        private View mBundleQtyLayout;
        private ViewGroup mRootView;
        private LinearLayout mCurrencyCodeLayout;
        private ToggleButton mBtnTogglePrice;
        private AppCompatActivity mActivity;
        private LinearLayout mBundlePriceLayout;

        public PriceEditorView(AppCompatActivity activity)
        {
                mActivity = activity;
                mBtnTogglePrice = (ToggleButton) mActivity.findViewById(R.id.btn_toggle_price);
                mBundlePriceLayout = (LinearLayout) mActivity.findViewById(R.id.bundle_price_group);
                mBundleQtyLayout = mActivity.findViewById(R.id.bundle_qty_layout);
                mCurrencyCodeLayout = (LinearLayout) mActivity.findViewById(R.id.currency_code_layout);

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

                                showOtherViews(isChecked);
                        }
                });
        }


        protected void showOtherViews(boolean isChecked)
        {
                Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.field_details);
                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);

                mBundlePriceLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                mBundleQtyLayout.setVisibility(isChecked? View.VISIBLE :View.GONE);
                mCurrencyCodeLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
}
