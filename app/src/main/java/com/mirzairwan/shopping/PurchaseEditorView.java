package com.mirzairwan.shopping;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public class PurchaseEditorView
{
        private RadioGroup mPriceTypeChoice;
        private TextView mPriceQuery;
        private ToggleButton mExpandButton;
        private AppCompatActivity mActivity;

        public PurchaseEditorView(AppCompatActivity activity)
        {
                mActivity = activity;
                mExpandButton = (ToggleButton)mActivity.findViewById(R.id.btn_toggle_purchase);
                mPriceQuery = (TextView)mActivity.findViewById(R.id.price_type_query);
                mPriceTypeChoice = (RadioGroup)mActivity.findViewById(R.id.price_type_choice);
                hideOtherViews();
                setupButton();
        }

        private void setupButton()
        {
                mExpandButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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
                mPriceQuery.setVisibility(View.GONE);
                mPriceTypeChoice.setVisibility(View.GONE);
        }

        private void showOtherViews()
        {
                mPriceQuery.setVisibility(View.VISIBLE);
                mPriceTypeChoice.setVisibility(View.VISIBLE);
        }


}
