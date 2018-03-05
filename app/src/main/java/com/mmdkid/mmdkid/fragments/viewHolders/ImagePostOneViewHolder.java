package com.mmdkid.mmdkid.fragments.viewHolders;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.helper.RelativeDateFormat;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;

import java.text.ParseException;

/**
 * Created by LIYADONG on 2017/7/25.
 */

public class ImagePostOneViewHolder extends ModelViewHolder {
    public CardView mCardView;
    public TextView mTextViewTitle;
    public TextView mTextViewDate;
    private SimpleDraweeView mImage;
    private TextView mTextViewImageCount;
    private Context mContext;

    public ImagePostOneViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mTextViewImageCount = (TextView)itemView.findViewById(R.id.tvImageCount);
        mImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvImage) ;

        mContext = itemView.getContext();
    }

    @Override
    public void bindHolder(Model model) {
        if(model instanceof Content){
            Content content = (Content)model;
            mTextViewTitle.setText(content.mTitle);
            try {
                mTextViewDate.setText(RelativeDateFormat.format(content.mCreatedAt));
            } catch (ParseException e) {
                e.printStackTrace();
                mTextViewDate.setText(content.mCreatedAt);
            }
            mTextViewImageCount.setVisibility(View.GONE);
            if(!content.mImageList.isEmpty()){
                // 显示第一张
                mImage.setImageURI(content.mImageList.get(0));
                if(content.mImageList.size()==1){
                    // 只有一张图片不显示图片数量
                }else{
                    // 多于一张图片则显示图片总数
                    mTextViewImageCount.setVisibility(View.VISIBLE);
                    mTextViewImageCount.setText(content.mImageList.size()+"图");
                }
            }

        }

    }
}
