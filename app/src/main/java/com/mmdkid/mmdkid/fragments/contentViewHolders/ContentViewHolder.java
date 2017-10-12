package com.mmdkid.mmdkid.fragments.contentViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mmdkid.mmdkid.models.Content;

/**
 * Created by LIYADONG on 2017/6/23.
 */

public abstract class ContentViewHolder extends RecyclerView.ViewHolder {
    public ContentViewHolder(View itemView) {
        super(itemView);
    }
    public abstract void bindHolder(Content content);
}
