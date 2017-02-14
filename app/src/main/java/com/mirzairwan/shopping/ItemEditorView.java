package com.mirzairwan.shopping;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public class ItemEditorView
{

        private TextInputLayout mEtCountryOriginLayout;
        private TextInputLayout mEtDescriptionLayout;
        private TextInputLayout mEtBrandLayout;
        private ToggleButton mToggleButton;
        private AppCompatActivity mActivity;



        public ItemEditorView(AppCompatActivity activity)
        {
                mActivity = activity;
                mToggleButton = (ToggleButton) mActivity.findViewById(R.id.btn_toggle_item);
                mEtBrandLayout = (TextInputLayout)mActivity.findViewById(R.id.item_brand_layout);
                mEtDescriptionLayout = (TextInputLayout)mActivity.findViewById(R.id.item_description_layout);
                mEtCountryOriginLayout = (TextInputLayout) mActivity.findViewById(R.id.item_country_origin_layout);
                hideOtherViews();
                setupButton();
        }

        private void setupButton()
        {
                mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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
                mEtBrandLayout.setVisibility(View.GONE);
                mEtCountryOriginLayout.setVisibility(View.GONE);
                mEtDescriptionLayout.setVisibility(View.GONE);
        }

        private void showOtherViews()
        {
                mEtBrandLayout.setVisibility(View.VISIBLE);
                mEtCountryOriginLayout.setVisibility(View.VISIBLE);
                mEtDescriptionLayout.setVisibility(View.VISIBLE);
        }


}