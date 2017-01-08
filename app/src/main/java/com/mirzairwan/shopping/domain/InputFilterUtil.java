package com.mirzairwan.shopping.domain;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mirza Irwan on 8/1/17.
 */

public class InputFilterUtil
{

    public static void setInitialCapInputFilter(EditText et)
    {
        InputFilter[] filters;
        filters = et.getFilters();
        ArrayList<InputFilter> arrfilters = new ArrayList<>(Arrays.asList(filters));
        arrfilters.add(new InputFilterUtil.InitialCap());
        filters = new InputFilter[arrfilters.size()];
        arrfilters.toArray(filters);
        et.setFilters(filters);
    }

    public static class InitialCap implements InputFilter
    {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend)
        {
            if(source.length() == 0)
                return null;

            if (Character.isLowerCase(source.charAt(0))) {
                char[] v = new char[end - start];
                TextUtils.getChars(source, start, end, v, 0);
                v[0] = Character.toUpperCase(source.charAt(0));
                String s = new String(v);

                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(s);
                    TextUtils.copySpansFrom((Spanned) source,
                            start, end, null, sp, 0);
                    return sp;
                } else {
                    return s;
                }
            }


            return null; // keep original
        }
    }
}
