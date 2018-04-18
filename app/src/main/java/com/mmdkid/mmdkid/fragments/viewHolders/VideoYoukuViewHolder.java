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
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.helper.HtmlUtil;
import com.mmdkid.mmdkid.helper.ProgressDialog;
import com.mmdkid.mmdkid.helper.RelativeDateFormat;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.VideoSource;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.youku.cloud.player.YoukuPlayerView;

import java.text.ParseException;

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
    private TextView mTextViewNumber;
    private LinearLayout mShareToView;
    private SimpleDraweeView mWeChatView;
    private SimpleDraweeView mWxCircleView;
    private ProgressDialog mProgressDialog;

    public VideoYoukuViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mContext = itemView.getContext();
//        mVideo = (YoukuPlayerView) itemView.findViewById(R.id.videoplayer);
//        mVideo.attachActivity((Activity) mContext);
        mImage = (SimpleDraweeView) itemView.findViewById(R.id.cvContentImage);
        mPlayIcon = (ImageView) itemView.findViewById(R.id.imagePlay);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.tvTitle);
        mLinearLayoutBottom = (LinearLayout) itemView.findViewById(R.id.llVideoBottom);
        mTextViewNumber = (TextView) itemView.findViewById(R.id.tvCommentNum);
        itemView.setTag(VideoSource.VIDEO_SOURCE_YOUKU);

        mShareToView = (LinearLayout) itemView.findViewById(R.id.llShareTo);
        mWeChatView =(SimpleDraweeView) itemView.findViewById(R.id.sdvWeChat);
        mWxCircleView =(SimpleDraweeView) itemView.findViewById(R.id.sdvWxCircle);
    }

    @Override
    public void bindHolder(Model model) {
        if ( model instanceof Content){
            final Content content = (Content) model;
            //ImageLoader imageLoader =  InternetSingleton.getInstance(mContext).getImageLoader();
            try {
                mTextViewDate.setText(content.mAuthor + " "+ RelativeDateFormat.format(content.mCreatedAt));
            } catch (ParseException e) {
                e.printStackTrace();
                mTextViewDate.setText(content.mAuthor + " "+ content.mCreatedAt);
            }
            mTextViewTitle.setText(content.mTitle);
//            mVideo.setPreferVideoDefinition(VideoDefinition.VIDEO_HD);
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
            if (content.mViewCount !=0){
                mTextViewNumber.setText(Utility.getNumberString(content.mViewCount)+"次播放");
            }


            mWeChatView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Log.d(TAG,"WeChat clicked.");
                   share(content,SHARE_MEDIA.WEIXIN);

               }
            });

            mWxCircleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG,"WxCircle clicked.");
                    share(content,SHARE_MEDIA.WEIXIN_CIRCLE);
                }
            });

            mShareToView.setVisibility(View.GONE);
        }
    }

    private void share(Content content,SHARE_MEDIA shareMedia){
        String url;
        UMImage image;
        UMWeb web = null;
        if (content.mImage!=null && !content.mImage.isEmpty() && !content.mImage.equalsIgnoreCase("null")){
            // 使用image字段
            image = new UMImage(mContext, HtmlUtil.getUrl(content.mImage));//网络图片
        }else if(content.mImageList!=null && !content.mImageList.isEmpty()){
            // 使用第一张图片
            image = new UMImage(mContext,  HtmlUtil.getUrl(content.mImageList.get(0)));//网络图片
        }else{
            // 使用默认图标
            image = new UMImage(mContext,R.mipmap.ic_launcher);
        }

        image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
        url = content.getContentUrl();
        url = url+"&showIn=wx"; // 通过该标识显示出app下载提示
        Log.d(TAG,"Sharing Url is " + url);
        web = new UMWeb(url);
        web.setTitle(content.mTitle);//标题
        web.setThumb(image);  //缩略图
        String text = HtmlUtil.getTextFromHtml(content.mContent,20);
        if (text!=null && !text.equals("null")) {
            web.setDescription(text);//取最多20字作为描述
        }else{
            web.setDescription((String) mContext.getText(R.string.share_description_empty));
        }
        new ShareAction((Activity) mContext)
                //.withText("hello")
                .withMedia(web)
                .setPlatform(shareMedia)
                .setCallback(mShareListener)
                .share();
    }

    private UMShareListener mShareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {
            showProgressDialog(true);
        }

        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            showProgressDialog(false);
            Toast.makeText(mContext,"成功了",Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            showProgressDialog(false);
            Toast.makeText(mContext,"失败"+t.getMessage(),Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            showProgressDialog(false);
            Toast.makeText(mContext,"取消了",Toast.LENGTH_LONG).show();

        }
    };

    private void showProgressDialog(boolean show){
        if (mProgressDialog==null){
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        if (show){
            mProgressDialog.setMessage("请稍后...");
            mProgressDialog.show();
        }else {
            mProgressDialog.dismiss();
        }
    }
}
