package com.mmdkid.mmdkid.fragments.viewHolders;

import android.net.Uri;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.helper.RelativeDateFormat;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;

import java.text.ParseException;

/**
 * Created by LIYADONG on 2017/9/16.
 */

public class PostImageMiddleViewHolder extends ModelViewHolder {

    private static final String TAG = "PostImageMiddleView";

    private CardView mCardView;
    private TextView mTextViewTitle;
    private TextView mTextViewDate;
    private SimpleDraweeView mImageViewContent;

    public PostImageMiddleViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mImageViewContent =(SimpleDraweeView)itemView.findViewById(R.id.cvContentImage);
    }

    /**
     *  content 数据中image若是字符串，则使用image的值
     *  若image是数组表示多个图像，则使用mImageList的首图片作为主图显示
     */
    @Override
    public void bindHolder(Model model) {
        if (model instanceof Content){
            Content content = (Content) model;
            mTextViewTitle.setText(content.mTitle);
            try {
                mTextViewDate.setText(RelativeDateFormat.format(content.mCreatedAt));
            } catch (ParseException e) {
                e.printStackTrace();
                mTextViewDate.setText(content.mCreatedAt);
            }
            if(TextUtils.isEmpty(content.mImage) && content.mImageList!=null  && content.mImageList.isEmpty()){
                mImageViewContent.setVisibility(View.GONE);
            }else if (!TextUtils.isEmpty(content.mImage)){
                Uri uri = Uri.parse(content.mImage);
                if(uri.getScheme()==null){
                    uri = Uri.parse("http:"+content.mImage);
                }
                mImageViewContent.setVisibility(View.VISIBLE);
                mImageViewContent.setImageURI(uri);
                Log.d(TAG,"Title is " + content.mTitle);
                Log.d(TAG,"Image is " + content.mImage);
                Log.d(TAG,"Image URI is " + uri);
                Log.d(TAG,"Image URI scheme is " + uri.getScheme());
            }else if (content.mImageList!=null && !content.mImageList.isEmpty()){
                Uri uri = Uri.parse(content.mImageList.get(0));
                if(uri.getScheme()==null){
                    uri = Uri.parse("http:"+content.mImageList.get(0));
                }
                mImageViewContent.setVisibility(View.VISIBLE);
                mImageViewContent.setImageURI(uri);
            }
        }
    }
}
