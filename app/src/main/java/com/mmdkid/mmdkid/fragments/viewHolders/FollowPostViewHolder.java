package com.mmdkid.mmdkid.fragments.viewHolders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.HomePageActivity;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.helper.HtmlUtil;
import com.mmdkid.mmdkid.helper.ProgressDialog;
import com.mmdkid.mmdkid.helper.RelativeDateFormat;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.singleton.UserInfoLoader;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 关注页面上 文章的式样
 * Created by LIYADONG on 2018/7/9.
 */
public class FollowPostViewHolder extends ModelViewHolder implements View.OnClickListener{
    private static final String TAG = "FollowPostViewHolder";

    public CardView mCardView;
    public TextView mTextViewTitle;
    public TextView mTextViewDate;
    public SimpleDraweeView mImageViewContent;
    public SimpleDraweeView mAvatarView;
    private TextView mUserNameView;
    private TextView mUserDescriptionView;
    private Context mContext;

    private TextView mShareView;
    private TextView mCommentView;
    private TextView mThumbsupView;
    private LinearLayout mUserInfoLlView;
    private LinearLayout mPostLlView;
    private LinearLayout mShareLlView;
    private LinearLayout mCommentLlView;
    private LinearLayout mThumbsupLlView;

    private ProgressDialog mProgressDialog;

    private Model mModel;

    public FollowPostViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mImageViewContent =(SimpleDraweeView)itemView.findViewById(R.id.cvContentImage);
        mAvatarView = (SimpleDraweeView)itemView.findViewById(R.id.sdvAvatar);
        mUserNameView =  (TextView)itemView.findViewById(R.id.tvUsername);
        mUserDescriptionView =  (TextView)itemView.findViewById(R.id.tvUserDescription);

        mShareView = (TextView)itemView.findViewById(R.id.tvShare);
        mCommentView = (TextView)itemView.findViewById(R.id.tvComment);
        mThumbsupView = (TextView)itemView.findViewById(R.id.tvThumbsup);
        // 用户信息区
        mUserInfoLlView = (LinearLayout) itemView.findViewById(R.id.llUserInfo);
        mUserInfoLlView.setOnClickListener(this);
        // 文章区
        mPostLlView = (LinearLayout) itemView.findViewById(R.id.llPost);
        mPostLlView.setOnClickListener(this);
        // 分享区
        mShareLlView = (LinearLayout) itemView.findViewById(R.id.llShare);
        mShareLlView.setOnClickListener(this);
        // 评论区
        mCommentLlView = (LinearLayout) itemView.findViewById(R.id.llComment);
        mCommentLlView.setOnClickListener(this);
        // 点赞区
        mThumbsupLlView = (LinearLayout) itemView.findViewById(R.id.llThumbsup);
        mThumbsupLlView.setOnClickListener(this);
    }
    /**
     *  content 数据中image若是字符串，则使用image的值
     *  若image是数组表示多个图像，则使用mImageList的首图片作为主图显示
     */
    public void bindHolder(Model model){
        mModel = model;
        if(model instanceof Content){
            Content content = (Content)model;
            // 用户头像 昵称 签名
            if (content.mUser!=null){
                Log.d(TAG,"Get the user info from the content model.");
                mAvatarView.setImageURI(content.mUser.mAvatar);
                mUserNameView.setText(content.mUser.getDisplayName());
                mUserDescriptionView.setText(content.mUser.mSignature);
            }else if (content.mCreatedBy!=0){
                // 从缓存或网络获取用户信息
                UserInfoLoader.getInstance(mContext).getUserInfo(content.mCreatedBy,mUserInfoListener);
            }
            // 文章标题
            mTextViewTitle.setText(content.mTitle);
            // 文章发布时间
            try {
                mTextViewDate.setText(content.mAuthor + " "+ RelativeDateFormat.format(content.mCreatedAt));
            } catch (ParseException e) {
                e.printStackTrace();
                mTextViewDate.setText(content.mAuthor + " "+content.mCreatedAt);
            }
            // 文章图片
            if(TextUtils.isEmpty(content.mImage) && content.mImageList!=null && content.mImageList.isEmpty()){
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
            // 文章的统计数据
            mShareView.setText("分享");
            // 文章评论
            if (content.mCommentCount!=0){
                mCommentView.setText(Integer.toString(content.mCommentCount));
            }else{
                mCommentView.setText("评论");
            }
            // 文章点赞
            if (content.mThumbsup!=0){
                mThumbsupView.setText(Integer.toString(content.mThumbsup));
            }
        }
    }

    private UserInfoLoader.UserInfoListener mUserInfoListener = new UserInfoLoader.UserInfoListener() {
        @Override
        public void OnSuccess(User user) {
            if (user!=null) {
                // 从缓存或网络得到用户信息
                Log.d(TAG,"Get the user info from the cache");
                mAvatarView.setImageURI(user.mAvatar);
                mUserNameView.setText(user.getDisplayName());
                mUserDescriptionView.setText(user.mSignature);
                ((Content)mModel).mUser = user;
            }
        }

        @Override
        public void OnFailure(String error) {
            // 获取用户信息错误
            Log.d(TAG,error);
        }

    };

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.llUserInfo:
                // 用户信息区域 点击显示用户主页
                if (mModel!=null && mModel instanceof Content && ((Content)mModel).mUser!=null){
                    intent = new Intent(mContext, HomePageActivity.class);
                    intent.putExtra("model",((Content)mModel).mUser);
                    mContext.startActivity(intent);
                }
                break;
            case R.id.llPost:
                // 文章区域 点击显示文章内容
                if (mModel!=null && mModel instanceof Content ){
                    intent = new Intent(mContext,WebViewActivity.class);
                    intent.putExtra("url",((Content)mModel).getContentUrl());
                    intent.putExtra("model",((Content)mModel));
                    Log.d(TAG,((Content)mModel).getContentUrl());
                    mContext.startActivity(intent);
                }
                break;
            case R.id.llShare:
                // 分享
                share(mModel);
                break;
            case R.id.llComment:
                // 评论
                if (mModel!=null && mModel instanceof Content ){
                    intent = new Intent(mContext,WebViewActivity.class);
                    intent.putExtra("url",((Content)mModel).getContentUrl());
                    intent.putExtra("model",((Content)mModel));
                    Log.d(TAG,((Content)mModel).getContentUrl());
                    mContext.startActivity(intent);
                }
                break;
            case R.id.llThumbsup:
                // 点赞

                break;
        }
    }

    /**
     * 文章分享
     */
    private void share(Model mModel) {
        String url;
        UMImage image;
        UMWeb web = null;
        if(mModel instanceof Content){
            Content content = (Content) mModel;
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
            url = content.getContentUrl()+"&showIn=wx"; // 通过该标识显示出app下载提示
            Log.d(TAG,"Sharing Url is " + url);
            web = new UMWeb(url);
            web.setTitle(content.mTitle);//标题
            web.setThumb(image);  //缩略图
            String text = HtmlUtil.getTextFromHtml(content.mContent,20);
            if (text!=null && !text.equals("null")) {
                web.setDescription(text);//取最多20字作为描述
            }else{
                web.setDescription((String)mContext.getText(R.string.share_description_empty));
            }
            new ShareAction((Activity) mContext)
                    //.withText("hello")
                    .withMedia(web)
                    .setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.SINA)
                    .setCallback(mShareListener)
                    .open();
        }
    }
    /**
     * 文章分享监听
     */
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
    /**
     * 文章分享等待
     */
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
