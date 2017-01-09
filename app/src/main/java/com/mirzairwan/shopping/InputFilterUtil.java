package com.mirzairwan.shopping;

import android.text.InputFilter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mirza Irwan on 8/1/17.
 */

public class InputFilterUtil
{

    public static void setAllCapsInputFilter(EditText et)
    {
        InputFilter[] prefFilters;
        prefFilters = et.getFilters();
        ArrayList<InputFilter> filters = new ArrayList<>(Arrays.asList(prefFilters));
        filters.add(new InputFilter.AllCaps());
        prefFilters = new InputFilter[filters.size()];
        filters.toArray(prefFilters);
        et.setFilters(prefFilters);
    }

}
