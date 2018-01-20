package com.mmdkid.mmdkid.fragments.viewHolders;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.ImagePost;
import com.mmdkid.mmdkid.models.Model;

/**
 * Created by LIYADONG on 2018/1/11.
 */

public class PublishManageImageViewHolder extends ModelViewHolder {
    private static final String TAG = "PublishManageImageViewHolder";

    public CardView mCardView;
    public TextView mTextViewTitle;
    public TextView mTextViewDate;
    public TextView mTextViewDescription;
    public SimpleDraweeView mImageViewContent;
    public ImageView mDeleteView;
    public Context mContext;

    public PublishManageImageViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mTextViewDescription = (TextView)itemView.findViewById(R.id.tvContentDescription);
        mImageViewContent =(SimpleDraweeView)itemView.findViewById(R.id.cvContentImage);
        mDeleteView = (ImageView) itemView.findViewById(R.id.imageDelete);
    }
    /**
     *  content 数据中image若是字符串，则使用image的值
     *  若image是数组表示多个图像，则使用imageList的首图片作为主图显示
     */
    public void bindHolder(Model model){
        if(model instanceof ImagePost){
            final ImagePost content = (ImagePost) model;
            mTextViewTitle.setText(content.title);
            mTextViewDate.setText(content.created_at);
            mTextViewDescription.setText("阅读 "+content.view_count+"  评论 "+content.comment_count+"  点赞 "+content.thumbsup);
            if(content.imageList==null || content.imageList.isEmpty()){
                mImageViewContent.setVisibility(View.GONE);
            }else{
                mImageViewContent.setVisibility(View.VISIBLE);
                mImageViewContent.setImageURI(content.imageList.get(0));
            }

        }
    }


}
