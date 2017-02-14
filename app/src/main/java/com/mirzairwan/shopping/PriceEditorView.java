package com.mirzairwan.shopping;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

/**
 * Created by Mirza Irwan on 14/2/17.
 */
public class PriceEditorView
{
        private LinearLayout mCurrencyCodeLayout;
        private ToggleButton mBtnTogglePrice;
        private AppCompatActivity mActivity;
        private LinearLayout mBundlePriceLayout;

        public PriceEditorView(AppCompatActivity activity)
        {
                mActivity = activity;
                mBundlePriceLayout = (LinearLayout)mActivity.findViewById(R.id.bundle_price_group);
                mBtnTogglePrice = (ToggleButton)mActivity.findViewById(R.id.btn_toggle_price);
                mCurrencyCodeLayout = (LinearLayout)mActivity.findViewById(R.id.currency_code_layout);
                hideOtherViews();
                setupButton();
        }

        private void setupButton()
        {
                mBtnTogglePrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                        {
                                if(isChecked)
                                {
                                        showOtherViews();
                                }
                                else
                                {
                                        hideOtherViews();
                                }
                        }
                });
        }

        private void hideOtherViews()
        {
                mCurrencyCodeLayout.setVisibility(View.GONE);
                mBundlePriceLayout.setVisibility(View.GONE);
        }

        private void showOtherViews()
        {
                mCurrencyCodeLayout.setVisibility(View.VISIBLE);
                mBundlePriceLayout.setVisibility(View.VISIBLE);
        }
}
