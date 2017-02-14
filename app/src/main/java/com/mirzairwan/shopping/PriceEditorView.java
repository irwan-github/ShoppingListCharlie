package com.mirzairwan.shopping;

import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
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
        private ViewGroup mRootView;
        private LinearLayout mCurrencyCodeLayout;
        private ToggleButton mBtnTogglePrice;
        private AppCompatActivity mActivity;
        private LinearLayout mBundlePriceLayout;

        public PriceEditorView(AppCompatActivity activity)
        {
                mActivity = activity;
                mBundlePriceLayout = (LinearLayout) mActivity.findViewById(R.id.bundle_price_group);
                mBtnTogglePrice = (ToggleButton) mActivity.findViewById(R.id.btn_toggle_price);
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


        private void showOtherViews(boolean isChecked)
        {
                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, new ChangeBounds());
                mCurrencyCodeLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                mBundlePriceLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
}
