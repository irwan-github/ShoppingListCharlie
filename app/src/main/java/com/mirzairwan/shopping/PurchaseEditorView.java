package com.mirzairwan.shopping;

import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Mirza Irwan on 14/2/17.
 */

public class PurchaseEditorView
{
        private ViewGroup mRootView;
        private RadioGroup mPriceTypeChoice;
        private TextView mPriceQuery;
        private ToggleButton mExpandButton;
        private AppCompatActivity mActivity;

        public PurchaseEditorView(AppCompatActivity activity)
        {
                mActivity = activity;
                mExpandButton = (ToggleButton) mActivity.findViewById(R.id.btn_toggle_purchase);
                mPriceQuery = (TextView) mActivity.findViewById(R.id.price_type_query);
                mPriceTypeChoice = (RadioGroup) mActivity.findViewById(R.id.price_type_choice);

                // Get the root view to create a transition
                mRootView = (ViewGroup) mActivity.findViewById(R.id.purchase_details);
                setupButton();
        }

        private void setupButton()
        {
                mExpandButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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
                Transition transition = TransitionInflater.from(mActivity).inflateTransition(R.transition.field_details);
                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(mRootView, transition);

                mPriceQuery.setVisibility(isChecked? View.VISIBLE : View.GONE);
                mPriceTypeChoice.setVisibility(isChecked? View.VISIBLE : View.GONE);
        }


}
