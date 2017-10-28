package com.mmdkid.mmdkid.fragments.viewHolders;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.adapters.ImageAdapter;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;

import java.util.ArrayList;

/**
 * Created by LIYADONG on 2017/7/25.
 */

public class ImagePostViewHolder extends ModelViewHolder {
    public CardView mCardView;
    public TextView mTextViewTitle;
    public TextView mTextViewDate;
    public GridView mImageGridView;
    private Context mContext;

    public ImagePostViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mImageGridView = (GridView)itemView.findViewById(R.id.gridview);
        mContext = itemView.getContext();
    }

    @Override
    public void bindHolder(Model model) {
        if(model instanceof Content){
            Content content = (Content)model;
            mTextViewTitle.setText(content.mTitle);
            mTextViewDate.setText(content.mCreatedAt);
            if(content.mImageList.size()>3){
                // 最多只显示前三张图片作为封面
                mImageGridView.setAdapter(new ImageAdapter(mContext, new ArrayList<String>(content.mImageList.subList(0,3)) ));
            }else{
                mImageGridView.setAdapter(new ImageAdapter(mContext,content.mImageList));
            }

        }

    }
}
