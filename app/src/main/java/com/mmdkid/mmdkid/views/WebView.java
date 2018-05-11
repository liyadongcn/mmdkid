package com.mmdkid.mmdkid.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 没有使用，使用后加载事件失效
 */

public class WebView extends android.webkit.WebView {
    private OnScrollChangedListener mOnScrollChangedListener;
 
    public WebView(final Context context) {
        super(context);
    }
 
    public WebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }
 
    public WebView(final Context context, final AttributeSet attrs,
                             final int defStyle) {
        super(context, attrs, defStyle);
    }
 
    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl,
                                   final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
 
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScroll(l - oldl, t - oldt);
        }
    }
 
    public OnScrollChangedListener getOnScrollChangedListener() {
        return mOnScrollChangedListener;
    }
 
    public void setOnScrollChangedListener(
            final OnScrollChangedListener OnScrollChangedListener) {
        mOnScrollChangedListener = OnScrollChangedListener;
    }
 
    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public static interface OnScrollChangedListener {
        public void onScroll(int dx, int dy);
    }
}