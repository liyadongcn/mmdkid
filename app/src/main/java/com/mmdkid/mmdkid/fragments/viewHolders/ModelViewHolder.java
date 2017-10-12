package com.mmdkid.mmdkid.fragments.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mmdkid.mmdkid.models.Model;

/**
 * Created by LIYADONG on 2017/7/23.
 */

public abstract class ModelViewHolder extends RecyclerView.ViewHolder {
    public ModelViewHolder(View itemView) {
        super(itemView);
    }
    public abstract void bindHolder(Model model);
}
