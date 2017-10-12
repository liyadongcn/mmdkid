package com.mmdkid.mmdkid.fragments.viewHolders;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Diary;
import com.mmdkid.mmdkid.models.Model;

/**
 * Created by LIYADONG on 2017/7/26.
 */

public class DiaryListViewHolder extends ModelViewHolder {

    public CardView mCardView;

    public TextView mTextViewDate;
    public TextView mTextViewContent;
    public SimpleDraweeView mImageViewContent;


    public DiaryListViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);

        mTextViewContent = (TextView) itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mImageViewContent =(SimpleDraweeView)itemView.findViewById(R.id.cvContentImage);

    }

    @Override
    public void bindHolder(Model model) {
        if(model instanceof Diary){
            Diary diary = (Diary)model;
            mTextViewContent.setText(diary.mContent);
            mTextViewDate.setText(diary.mDate);
            if(!diary.mImageList.isEmpty()){
                mImageViewContent.setVisibility(View.VISIBLE);
                mImageViewContent.setImageURI(diary.mImageList.get(0));
            }else {
                mImageViewContent.setVisibility(View.GONE);
            }
        }
    }
}
