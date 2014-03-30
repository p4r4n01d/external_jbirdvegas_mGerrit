package com.jbirdvegas.mgerrit.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.jbirdvegas.mgerrit.R;

public class ThemeableSwipeRefreshLayout extends SwipeRefreshLayout {

    public ThemeableSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThemeableSwipeRefreshLayout,
                0, R.style.RefreshView);

        int color1 = a.getColor(R.styleable.ThemeableSwipeRefreshLayout_color1, R.color.card_background);
        int color2 = a.getColor(R.styleable.ThemeableSwipeRefreshLayout_color2, R.color.card_background);
        int color3 = a.getColor(R.styleable.ThemeableSwipeRefreshLayout_color3, R.color.card_background);
        int color4 = a.getColor(R.styleable.ThemeableSwipeRefreshLayout_color4, R.color.card_background);

        setColorScheme(color1, color2, color3, color4);
        a.recycle();
    }


}
