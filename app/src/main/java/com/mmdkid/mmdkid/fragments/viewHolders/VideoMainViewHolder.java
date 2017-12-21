package com.mmdkid.mmdkid.fragments.viewHolders;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.helper.RelativeDateFormat;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.singleton.InternetSingleton;

import java.text.ParseException;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

/**
 * Created by LIYADONG on 2017/9/16.
 */

public class VideoMainViewHolder extends ModelViewHolder {

    private static final String TAG = "VideoMainViewHolder";

    private CardView mCardView;
    private TextView mTextViewDate;
    private JCVideoPlayerStandard mVideo;
    private Context mContext;
    private LinearLayout mLinearLayoutBottom;

    public VideoMainViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mVideo = (JCVideoPlayerStandard) itemView.findViewById(R.id.videoplayer);
        mContext = itemView.getContext();
        mLinearLayoutBottom = (LinearLayout) itemView.findViewById(R.id.llVideoBottom);
    }

    @Override
    public void bindHolder(Model model) {
        if ( model instanceof Content){
            final Content content = (Content) model;
            ImageLoader imageLoader =  InternetSingleton.getInstance(mContext).getImageLoader();
            try {
                mTextViewDate.setText(RelativeDateFormat.format(content.mCreatedAt));
            } catch (ParseException e) {
                e.printStackTrace();
                mTextViewDate.setText(content.mCreatedAt);
            }
            mVideo.setUp(content.mVideo
                    , JCVideoPlayerStandard.SCREEN_LAYOUT_LIST, content.mTitle);
            //mVideo.thumbImageView.setImageURI(Uri.parse(content.mImage));
            imageLoader.get(content.mImage,imageLoader.getImageListener(mVideo.thumbImageView,R.drawable.test_icon_default,R.drawable.test_icon_erro));
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
        }
    }
}
