package com.mmdkid.mmdkid.fragments.viewHolders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Refresh;

/**
 * Created by LIYADONG on 2017/9/16.
 */

public class RefreshViewHolder extends ModelViewHolder {
    private static final String TAG = "RefreshViewHolder";

    private LinearLayout mLayout;
    private TextView     mRefreshTextView;

    public RefreshViewHolder(View itemView) {
        super(itemView);
        mLayout = (LinearLayout) itemView.findViewById(R.id.llRrefresh);
        mRefreshTextView = (TextView) itemView.findViewById(R.id.tvRefresh);
    }

    @Override
    public void bindHolder(Model model) {
        if (model instanceof Refresh) {
            Refresh refresh = (Refresh) model;
            mRefreshTextView.setText(refresh.mText);
        }
    }
}
