package com.mmdkid.mmdkid.fragments.viewHolders;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;

/**
 * Created by LIYADONG on 2017/7/25.
 */

public class PostImageThreeViewHolder extends ModelViewHolder {
    public CardView mCardView;
    public TextView mTextViewTitle;
    public TextView mTextViewDate;
    private SimpleDraweeView mImage1;
    private SimpleDraweeView mImage2;
    private SimpleDraweeView mImage3;
    //public GridView mImageGridView;
    private Context mContext;

    public PostImageThreeViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mImage1 = (SimpleDraweeView) itemView.findViewById(R.id.sdvImage1) ;
        mImage2 = (SimpleDraweeView) itemView.findViewById(R.id.sdvImage2) ;
        mImage3 = (SimpleDraweeView) itemView.findViewById(R.id.sdvImage3) ;
        //mImageGridView = (GridView)itemView.findViewById(R.id.gridview);
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
                //mImageGridView.setAdapter(new ImageAdapter(mContext, new ArrayList<String>(content.mImageList.subList(0,3)) ));
                mImage1.setImageURI(content.mImageList.get(0));
                mImage2.setImageURI(content.mImageList.get(1));
                mImage3.setImageURI(content.mImageList.get(2));
            }else{
                //mImageGridView.setAdapter(new ImageAdapter(mContext,content.mImageList));
                for (int i =0; i<content.mImageList.size();i++){
                    switch (i){
                        case 0:
                            mImage1.setImageURI(content.mImageList.get(i));
                            break;
                        case 1:
                            mImage2.setImageURI(content.mImageList.get(i));
                            break;
                        case 2:
                            mImage3.setImageURI(content.mImageList.get(i));
                        break;
                    }
                }
            }

        }

    }
}
