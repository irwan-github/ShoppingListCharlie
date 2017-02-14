package com.mirzairwan.shopping;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public class ItemEditorView
{

        private ViewGroup mRootView;
        private TextInputLayout mEtCountryOriginLayout;
        private TextInputLayout mEtDescriptionLayout;
        private TextInputLayout mEtBrandLayout;
        private ToggleButton mToggleButton;
        private AppCompatActivity mActivity;


        public ItemEditorView(AppCompatActivity activity)
        {
                mActivity = activity;
                mToggleButton = (ToggleButton) mActivity.findViewById(R.id.btn_toggle_item);
                mEtBrandLayout = (TextInputLayout) mActivity.findViewById(R.id.item_brand_layout);
                mEtDescriptionLayout = (TextInputLayout) mActivity.findViewById(R.id.item_description_layout);
                mEtCountryOriginLayout = (TextInputLayout) mActivity.findViewById(R.id.item_country_origin_layout);

                // Get the root view to create a transition
                mRootView = (ViewGroup) mActivity.findViewById(R.id.activity_item_editing);

                setupButton();
        }

        private void setupButton()
        {
                mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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

                mEtBrandLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                mEtCountryOriginLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                mEtDescriptionLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }


}