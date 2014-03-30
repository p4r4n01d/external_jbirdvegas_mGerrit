package com.jbirdvegas.mgerrit.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.jbirdvegas.mgerrit.Prefs;
import com.jbirdvegas.mgerrit.R;

public class ThemeableSwipeRefreshLayout extends SwipeRefreshLayout {

    public ThemeableSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        int theme = Prefs.getCurrentThemeID(context);
        // The attributes you want retrieved
        int[] theAttrs = {R.attr.refreshColor1, R.attr.color2, R.attr.color3, R.attr.color4};

        int style;
        if (theme == R.style.Theme_Light) style = R.style.RefreshView;
        else style = R.style.RefreshView_Dark;

        TypedArray a = context.obtainStyledAttributes(style, theAttrs);

        int color1 = a.getColor(0, R.color.card_background);
        int color2 = a.getColor(1, R.color.card_background);
        int color3 = a.getColor(2, R.color.card_background);
        int color4 = a.getColor(3, R.color.card_background);

        setColorScheme(color1, color2, color3, color4);
        a.recycle();
    }


}
