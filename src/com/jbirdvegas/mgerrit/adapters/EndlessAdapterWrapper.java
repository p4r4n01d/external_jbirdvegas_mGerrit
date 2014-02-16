package com.jbirdvegas.mgerrit.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.jbirdvegas.mgerrit.R;

import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for another adapter. This is a merge of the CommonsWare's
 *  AdapterWrapper and EndlessAdapter libraries of which neither support
 *  CursorAdapters.
 */
public abstract class EndlessAdapterWrapper extends BaseAdapter
    implements AbsListView.OnScrollListener {

    private BaseAdapter wrapped;
    private Context mContext;

    private int mPendingResource = -1;
    private View mPendingView;
    private boolean mLoadingMoreData = false;
    private boolean mIsMoreData = true;

    public EndlessAdapterWrapper(Context context, BaseAdapter wrapped) {
        this(context, wrapped, R.layout.loading_placeholder);
    }

    /**
     * Constructor wrapping a supplied Adapter and
     * providing a id for a pending view.
     */
    public EndlessAdapterWrapper(Context context, BaseAdapter wrapped, int pendingResource) {
        this.wrapped = wrapped;
        this.mContext = context;
        this.mPendingResource = pendingResource;
    }

    public abstract void loadData();

    public void startDataLoading() {
        mLoadingMoreData = true;
    }

    public void finishedDataLoading(boolean moreData) {
        mIsMoreData = moreData;
        mPendingView = null;
        mLoadingMoreData = false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position >= wrapped.getCount()) {
            return(false);
        }

        return wrapped.isEnabled(position);
    }

    @Override
    public int getCount() {
        // Add an extra item for the pending row
        return wrapped.getCount() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position >= wrapped.getCount()) {
            return null;
        }

        return wrapped.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        if (position >= wrapped.getCount()) {
            return -1;
        }

        return wrapped.getItemId(position);
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView.setVisibility(View.VISIBLE);
        if (position >= wrapped.getCount()) {
            if (!mLoadingMoreData || !mIsMoreData) {
                convertView.setVisibility(View.GONE);
                return convertView;
            } else if (mPendingView == null && mPendingResource != -1) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                return inflater.inflate(mPendingResource, null);
            } else {
                return mPendingView;
            }
        } else {
            return wrapped.getView(position, convertView, parent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= wrapped.getCount()) return IGNORE_ITEM_VIEW_TYPE;
        return wrapped.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        // Add another view type for the placeholder
        return wrapped.getViewTypeCount() + 1;
    }

    @Override
    public boolean isEmpty() {
        return !mLoadingMoreData && wrapped.isEmpty();
    }

    @Override
    public void notifyDataSetChanged() {
        wrapped.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        mPendingView = null;
        mLoadingMoreData = false;
        wrapped.notifyDataSetInvalidated();
    }

    /**
     * Returns the ListAdapter that is wrapped by the endless
     * logic.
     */
    protected BaseAdapter getWrappedAdapter() {
        return wrapped;
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (listView.getLastVisiblePosition() >= listView.getCount()) {
                startDataLoading();
                loadData();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // Not used.
    }
}
