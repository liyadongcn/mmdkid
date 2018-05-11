package com.mmdkid.mmdkid.fragments.viewHolders;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Content;
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
    public TextView mTextViewImageCount;
    public SimpleDraweeView mImageViewContent;
    public ImageView mDeleteView;
    public Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PublishManageImageViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mTextViewDescription = (TextView)itemView.findViewById(R.id.tvContentDescription);
        mTextViewImageCount = (TextView)itemView.findViewById(R.id.tvImageCount);
        mImageViewContent =(SimpleDraweeView)itemView.findViewById(R.id.cvContentImage);
        mDeleteView = (ImageView) itemView.findViewById(R.id.imageDelete);
    }
    /**
     *  content 数据中image若是字符串，则使用image的值
     *  若image是数组表示多个图像，则使用imageList的首图片作为主图显示
     */
    public void bindHolder(Model model){
        if(model instanceof Content){
            final Content content = (Content) model;
            mTextViewTitle.setText(content.mTitle);
            mTextViewDate.setText(content.mCreatedAt);
            mTextViewDescription.setText("阅读 "+content.mViewCount+"  评论 "+content.mCommentCount+"  点赞 "+content.mThumbsup);
            if (content.mImageList==null || content.mImageList.isEmpty()){
                //  没用图
                mImageViewContent.setVisibility(View.GONE);
                mTextViewImageCount.setVisibility(View.GONE);
            }else {
                // 有缩略图 使用缩略图
                mImageViewContent.setVisibility(View.VISIBLE);
                mImageViewContent.setImageURI(content.mImageList.get(0));
                mTextViewImageCount.setText(content.mImageList.size()+"图");
            }

        }
    }


}
