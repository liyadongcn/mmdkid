package com.mmdkid.mmdkid.fragments.viewHolders;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Student;
import com.mmdkid.mmdkid.models.User;

/**
 * Created by LIYADONG on 2017/7/21.
 */

public class StudentViewHolder extends ModelViewHolder {

    public CardView mCardView;
    public TextView mTextViewName;
    public SimpleDraweeView mImageViewAvatar;

    public StudentViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewName = (TextView)itemView.findViewById(R.id.tvName);
        mImageViewAvatar =(SimpleDraweeView)itemView.findViewById(R.id.ivAvatar);
    }

    @Override
    public void bindHolder(Model model) {
        if(model instanceof Student){
            Student user = (Student) model;
            mTextViewName.setText(user.mRealname);
            mImageViewAvatar.setImageURI(user.mAvatar);
        }
        if(model instanceof User){
            User user = (User) model;
            mTextViewName.setText(user.getDisplayName());
            mImageViewAvatar.setImageURI(user.mAvatar);
        }
    }
}
