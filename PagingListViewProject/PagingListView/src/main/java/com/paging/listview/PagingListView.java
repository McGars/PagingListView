package com.paging.listview;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;


public class PagingListView extends ListView {

	private boolean isHeaderLoader;

	public interface Pagingable {
		void onLoadMoreItems();
	}

	private boolean isLoading;
	private boolean hasMoreItems;
	private Pagingable pagingableListener;
	private LoadingView loadingView;

    private OnScrollListener onScrollListener;

	public PagingListView(Context context) {
		super(context);
		init(null);
	}

	public PagingListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public PagingListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	public boolean isLoading() {
		return this.isLoading;
	}

	public void setIsLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	public void setPagingableListener(Pagingable pagingableListener) {
		this.pagingableListener = pagingableListener;
	}

	public void setHasMoreItems(boolean hasMoreItems) {
		this.hasMoreItems = hasMoreItems;

		if(Build.VERSION.SDK_INT < 11){
			int visible = this.hasMoreItems ? VISIBLE : GONE;
			if(loadingView!=null && loadingView.getVisibility() != visible)
				loadingView.setVisibility(visible);
		} else {
//			int footerCount = getFooterViewsCount();
//			if(!this.hasMoreItems && footerCount > 0){
//				removeFooterView(loadingView);
//			} else if (this.hasMoreItems && footerCount == 0) {
//				addFooterView(loadingView);
//			}
			removeHeaderView(loadingView);
			removeFooterView(loadingView);
			if(this.hasMoreItems){
				if(!isHeaderLoader)
					addFooterView(loadingView);
				else
					addHeaderView(loadingView);
			}
		}
	}

	public boolean hasMoreItems() {
		return this.hasMoreItems;
	}


	public void onFinishLoading(boolean hasMoreItems, List<? extends Object> newItems) {
		setHasMoreItems(hasMoreItems);
		setIsLoading(false);
		if(newItems != null && newItems.size() > 0) {
			ListAdapter adapter = ((HeaderViewListAdapter)getAdapter()).getWrappedAdapter();
			if(adapter instanceof PagingBaseAdapter ) {
				((PagingBaseAdapter)adapter).addMoreItems(newItems);
			}
		}
	}


	private void init(AttributeSet attrs) {

		if(attrs != null) {
			try {
				TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.PaggingListViewDesclareStyle, 0, 0);
				isHeaderLoader  = typedArray.getBoolean(R.styleable.PaggingListViewDesclareStyle_plvHeaderLoader, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		isLoading = false;
        loadingView = new LoadingView(getContext());
		if(isHeaderLoader) {
			addHeaderView(loadingView);
			setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			setStackFromBottom(true);
		} else {
			addFooterView(loadingView);
			setFooterDividersEnabled(false);
		}
		super.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //Dispatch to child OnScrollListener
                if (onScrollListener != null) {
                    onScrollListener.onScrollStateChanged(view, scrollState);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //Dispatch to child OnScrollListener
                if (onScrollListener != null) {
                    onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }

                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                if (!isLoading && hasMoreItems && (
						isHeaderLoader ? firstVisibleItem == 0 :
								lastVisibleItem == totalItemCount)) {
                    if (pagingableListener != null) {
                        isLoading = true;
                        pagingableListener.onLoadMoreItems();
                    }

                }
            }
        });
	}

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        onScrollListener = listener;
    }

	public void setHeaderLoader(boolean isHeader){
		isHeaderLoader = isHeader;
		setHasMoreItems(hasMoreItems);
	}
}
