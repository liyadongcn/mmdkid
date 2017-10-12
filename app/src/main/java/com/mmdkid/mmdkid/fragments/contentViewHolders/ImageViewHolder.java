package com.mmdkid.mmdkid.fragments.contentViewHolders;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.adapters.ImageAdapter;

/**
 * Created by LIYADONG on 2017/6/23.
 */

public class ImageViewHolder extends ContentViewHolder {

    public CardView mCardView;
    public TextView mTextViewTitle;
    public TextView mTextViewDate;
    public GridView mImageGridView;
    private Context mContext;

    public ImageViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mImageGridView = (GridView)itemView.findViewById(R.id.gridview);
        mContext = itemView.getContext();
    }

    @Override
    public void bindHolder(Content content) {
        mTextViewTitle.setText(content.mTitle);
        mTextViewDate.setText(content.mCreatedAt);
        mImageGridView.setAdapter(new ImageAdapter(mContext,content.mImageList));
    }
}
