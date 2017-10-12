package com.mmdkid.mmdkid.fragments.viewHolders;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.singleton.InternetSingleton;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * Created by LIYADONG on 2017/9/16.
 */

public class VideoMainViewHolder extends ModelViewHolder {

    private CardView mCardView;
    private TextView mTextViewDate;
    private JCVideoPlayerStandard mVideo;
    private Context mContext;

    public VideoMainViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mVideo = (JCVideoPlayerStandard) itemView.findViewById(R.id.videoplayer);
        mContext = itemView.getContext();
    }

    @Override
    public void bindHolder(Model model) {
        if ( model instanceof Content){
            Content content = (Content) model;
            ImageLoader imageLoader =  InternetSingleton.getInstance(mContext).getImageLoader();
            mTextViewDate.setText(content.mCreatedAt);
            mVideo.setUp(content.mVideo
                    , JCVideoPlayerStandard.SCREEN_LAYOUT_LIST, content.mTitle);
            //mVideo.thumbImageView.setImageURI(Uri.parse(content.mImage));
            imageLoader.get(content.mImage,imageLoader.getImageListener(mVideo.thumbImageView,R.drawable.test_icon_default,R.drawable.test_icon_erro));
        }
    }
}
