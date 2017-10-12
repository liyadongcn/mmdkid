package com.mmdkid.mmdkid.fragments.viewHolders;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Diary;
import com.mmdkid.mmdkid.models.Model;

/**
 * Created by LIYADONG on 2017/7/20.
 */

public class DiaryViewHolder extends ModelViewHolder {

    public CardView mCardView;
    public TextView mTextViewName;
    public TextView mTextViewDate;
    public TextView mTextViewContent;
    public SimpleDraweeView mImageViewContent;
    public SimpleDraweeView mImageViewAvatar;

    public DiaryViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewName = (TextView)itemView.findViewById(R.id.tvName);
        mTextViewContent = (TextView) itemView.findViewById(R.id.tvContent);
        mTextViewDate = (TextView)itemView.findViewById(R.id.tvContentDate);
        mImageViewContent =(SimpleDraweeView)itemView.findViewById(R.id.cvContentImage);
        mImageViewAvatar =(SimpleDraweeView)itemView.findViewById(R.id.ivAvatar);
    }

    @Override
    public void bindHolder(Model model) {

        if(model instanceof Diary){
            Diary diary = (Diary)model;
            mTextViewName.setText(diary.mStudent.getDisplayName());
            mTextViewDate.setText(diary.mDate);
            mImageViewAvatar.setImageURI(diary.mStudent.mAvatar);
            if(!diary.mImageList.isEmpty()){
                mImageViewContent.setImageURI(diary.mImageList.get(0));
            }else {
                mTextViewContent.setText(diary.mContent);
            }
        }
    }
}
