package com.mmdkid.mmdkid.fragments.viewHolders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.VideoSource;
import com.mmdkid.mmdkid.models.YoukuVideo;
import com.mmdkid.mmdkid.singleton.InternetSingleton;
import com.youku.cloud.player.VideoDefinition;
import com.youku.cloud.player.YoukuPlayerView;

/**
 * Created by LIYADONG on 2017/9/26.
 */

public class VideoYoukuViewHolder extends ModelViewHolder {

    private static final String TAG = "VideoYoukuViewHolder";

    private CardView mCardView;
    private TextView mTextViewDate;
    private YoukuPlayerView mVideo;
    private Context mContext;
    private SimpleDraweeView mImage;
    private ImageView mPlayIcon;
    private TextView mTextViewTitle;
    private LinearLayout mLinearLayoutBottom;

    public VideoYoukuViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mContext = itemView.getContext();
        mVideo = (YoukuPlayerView) itemView.findViewById(R.id.videoplayer);
        mVideo.attachActivity((Activity) mContext);
        mImage = (SimpleDraweeView) itemView.findViewById(R.id.cvContentImage);
        mPlayIcon = (ImageView) itemView.findViewById(R.id.imagePlay);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.tvTitle);
        mLinearLayoutBottom = (LinearLayout) itemView.findViewById(R.id.llVideoBottom);
    }

    @Override
    public void bindHolder(Model model) {
        if ( model instanceof Content){
            final Content content = (Content) model;
            ImageLoader imageLoader =  InternetSingleton.getInstance(mContext).getImageLoader();
            mTextViewDate.setText(content.mCreatedAt);
            mTextViewTitle.setText(content.mTitle);
            mVideo.setPreferVideoDefinition(VideoDefinition.VIDEO_HD);
            mImage.setImageURI(content.mImage);
            mLinearLayoutBottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 打开webview观看视频
                    Intent intent = new Intent(mContext,WebViewActivity.class);
                    intent.putExtra("url",content.getContentUrl());
                    intent.putExtra("model",content);
                    Log.d(TAG,content.getContentUrl());
                    mContext.startActivity(intent);
                }
            });
            //mVideo.playYoukuVideo(vid);//"XMjc2NjA5MjM4NA=="
            //mVideo.thumbImageView.setImageURI(Uri.parse(content.mImage));
            //imageLoader.get(content.mImage,imageLoader.getImageListener(mVideo.thumbImageView,R.drawable.test_icon_default,R.drawable.test_icon_erro));
        }
    }
}
