package com.fima.cardsui.objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.fima.cardsui.Utils;
import com.jbirdvegas.mgerrit.R;

public abstract class Card extends AbstractCard {

    // handle swiping on a per card bases
    protected boolean mSwipable = false;

    public boolean isSwipable() {
        return mSwipable;
    }

    public Card setSwipableCard(boolean swipable) {
        this.mSwipable = swipable;
        return this;
    }

    public interface OnCardSwiped {
        public void onCardSwiped(Card card, View layout);
    }

    private OnCardSwiped onCardSwipedListener;
    private OnClickListener mListener;
    protected View mCardLayout;

    public Card() {

    }

    public Card(String title) {
        this.title = title;
    }

    public Card(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }

    public Card(String title, int image) {
        this.title = title;
        this.image = image;
    }

    public Card(String title, String desc, int image) {
        this.title = title;
        this.desc = desc;
        this.image = image;
    }

    public Card(String titlePlay, String description, String color,
                String titleColor, Boolean hasOverflow, Boolean isClickable) {

        this.titlePlay = titlePlay;
        this.description = description;
        this.color = color;
        this.titleColor = titleColor;
        this.hasOverflow = hasOverflow;
        this.isClickable = isClickable;
    }

    @Override
    public View getView(Context context, View convertView, boolean swipable) {
        View view = LayoutInflater.from(context).inflate(getCardLayout(), null);
        mCardLayout = view;
        try {
            ((FrameLayout) view.findViewById(R.id.cardContent))
                    .addView(getCardContent(context));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        setLayoutParams(context, view);
        return view;
    }

    @Override
    public View getView(Context context, View convertView) {
        return getView(context, convertView, false);
    }

    public View getViewLast(Context context) {

        View view = LayoutInflater.from(context).inflate(getLastCardLayout(),
                null);
        mCardLayout = view;
        try {
            ((FrameLayout) view.findViewById(R.id.cardContent))
                    .addView(getCardContent(context));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        setLayoutParams(context, view);
        return view;
    }

    public View getViewFirst(Context context) {

        View view = LayoutInflater.from(context).inflate(getFirstCardLayout(),
                null);
        mCardLayout = view;
        try {
            ((FrameLayout) view.findViewById(R.id.cardContent))
                    .addView(getCardContent(context));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        setLayoutParams(context, view);
        return view;
    }

    private void setLayoutParams(Context context, View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int bottom = Utils.convertDpToPixelInt(context, 12);
        lp.setMargins(0, 0, 0, bottom);

        view.setLayoutParams(lp);
    }

    public abstract View getCardContent(Context context);

    public OnClickListener getClickListener() {
        return mListener;
    }

    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public void OnSwipeCard() {
        if (onCardSwipedListener != null)
            onCardSwipedListener.onCardSwiped(this, mCardLayout);
        // TODO: find better implementation to get card-object's used content
        // layout (=> implementing getCardContent());
    }

    public OnCardSwiped getOnCardSwipedListener() {
        return onCardSwipedListener;
    }

    public void setOnCardSwipedListener(OnCardSwiped onEpisodeSwipedListener) {
        this.onCardSwipedListener = onEpisodeSwipedListener;
    }

    protected int getCardLayout() {
        return R.layout.item_card;
    }

    protected int getLastCardLayout() {
        return R.layout.item_card_empty_last;
    }

    protected int getFirstCardLayout() {
        return R.layout.item_play_card_empty_first;
    }

}
