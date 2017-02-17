package com.mirzairwan.shopping;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public class ItemEditorExpander
{
        private ViewGroup mRootView;
        private TextInputLayout mEtCountryOriginLayout;
        private TextInputLayout mEtDescriptionLayout;
        private TextInputLayout mEtBrandLayout;
        private ToggleButton mToggleButton;
        private Activity mActivity;

        public ItemEditorExpander(Activity activity)
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
                                if(isChecked)
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

        private void expandLess()
        {
                Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.item_expand_less);

                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);
                mEtBrandLayout.setVisibility(View.GONE);
                mEtCountryOriginLayout.setVisibility(View.GONE);
                mEtDescriptionLayout.setVisibility(View.GONE);
        }

        private void expandMore()
        {
                Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.item_expand_more);

                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);
                mEtBrandLayout.setVisibility(View.VISIBLE );
                mEtCountryOriginLayout.setVisibility(View.VISIBLE);
                mEtDescriptionLayout.setVisibility(View.VISIBLE );
        }


}