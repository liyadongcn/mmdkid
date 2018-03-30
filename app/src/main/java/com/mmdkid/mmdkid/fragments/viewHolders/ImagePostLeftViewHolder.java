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
 * Created by LIYADONG on 2018/3/28.
 */

public class ImagePostLeftViewHolder extends ModelViewHolder {
    public CardView mCardView;
    public TextView mTextViewTitle;
    public TextView mTextViewDate;
    private SimpleDraweeView mImage;

    private Context mContext;

    public ImagePostLeftViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mImage = (SimpleDraweeView) itemView.findViewById(R.id.cvContentImage) ;
        mContext = itemView.getContext();
    }

    @Override
    public void bindHolder(Model model) {
        if(model instanceof Content){
            Content content = (Content)model;
            mTextViewTitle.setText(content.mTitle);
            String displayString="";
            try {
                displayString = displayString + content.mAuthor;
                if (content.mCommentCount!=0) displayString = displayString + " " + content.mCommentCount + "评论";
                displayString =  displayString + " "+ RelativeDateFormat.format(content.mCreatedAt);
                mTextViewDate.setText(displayString);
            } catch (ParseException e) {
                e.printStackTrace();
                mTextViewDate.setText(displayString + " "+ content.mCreatedAt);
            }
            mImage.setImageURI(content.mImageList.get(0));

        }

    }
}
