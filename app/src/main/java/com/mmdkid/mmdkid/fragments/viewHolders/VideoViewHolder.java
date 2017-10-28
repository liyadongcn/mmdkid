package com.mmdkid.mmdkid.fragments.viewHolders;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.singleton.InternetSingleton;

/**
 * Created by LIYADONG on 2017/7/25.
 */

public class VideoViewHolder extends ModelViewHolder {
    public CardView mCardView;
    public TextView mTextViewDate;
    public TextView mTextViewTitle;

    private SimpleDraweeView mImageViewPoster;

    private Context mContext;

    public VideoViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mImageViewPoster = (SimpleDraweeView) itemView.findViewById(R.id.cvContentImage) ;
        mContext = itemView.getContext();
    }

    @Override
    public void bindHolder(Model model) {
        if (model instanceof Content){
            Content content =(Content)model;
            ImageLoader imageLoader =  InternetSingleton.getInstance(mContext).getImageLoader();
            mTextViewDate.setText(content.mCreatedAt);
            mTextViewTitle.setText(content.mTitle);
            mImageViewPoster.setImageURI(Uri.parse(content.mImage));
           /* mVideo.setUp(content.mVideo
                    , JCVideoPlayerStandard.SCREEN_LAYOUT_LIST, "");*/
            //mVideo.thumbImageView.setImageURI(Uri.parse(content.mImage));
            //imageLoader.get(content.mImage,imageLoader.getImageListener(mVideo.thumbImageView,R.drawable.test_icon_default,R.drawable.test_icon_erro));
        }
    }
}
