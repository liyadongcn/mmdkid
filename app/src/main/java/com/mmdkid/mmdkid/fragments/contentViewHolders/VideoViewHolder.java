package com.mmdkid.mmdkid.fragments.contentViewHolders;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.singleton.InternetSingleton;

import cn.jzvd.JZVideoPlayerStandard;


/**
 * Created by LIYADONG on 2017/6/23.
 */

public class VideoViewHolder extends ContentViewHolder {

    public CardView mCardView;
    public TextView mTextViewDate;
    public JZVideoPlayerStandard mVideo;

    private Context mContext;

    public VideoViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mVideo = (JZVideoPlayerStandard ) itemView.findViewById(R.id.videoplayer);
        mContext = itemView.getContext();
    }

    @Override
    public void bindHolder(Content content) {
        ImageLoader imageLoader =  InternetSingleton.getInstance(mContext).getImageLoader();
        mTextViewDate.setText(content.mCreatedAt);
        mVideo.setUp(content.mVideo
                , JZVideoPlayerStandard.SCREEN_WINDOW_LIST, content.mTitle);
        //mVideo.thumbImageView.setImageURI(Uri.parse(content.mImage));
        imageLoader.get(content.mImage,imageLoader.getImageListener(mVideo.thumbImageView,R.drawable.test_icon_default,R.drawable.test_icon_erro));
    }
}
