package com.mmdkid.mmdkid.imagepost;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.HomePageActivity;
import com.mmdkid.mmdkid.LoginActivity;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.helper.HtmlUtil;
import com.mmdkid.mmdkid.helper.ProgressDialog;
import com.mmdkid.mmdkid.models.Behavior;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import q.rorbin.badgeview.QBadgeView;


/*
 * Created by Alexander Krol (troy379) on 29.08.16.
 */
public class ImageOverlayView extends RelativeLayout {
    private static final String TAG = "ImageOverlayView";
    private TextView tvDescription;

    private String sharingText;

    private Content content;

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private ImageView mCommentView;
    private User mCurrentUser;
    private ImageView mStarView;
    private LinearLayout mCommentLayout;
    private SimpleDraweeView mUserAvatarView;
    private TextView mUserNameView;
    private TextView mUserDescriptionView;
    private ImageView mShareView;
    private ImageView mThumbsupView;
    private TextView mFollowActionView;

    private boolean mIsStared = false;
    private Behavior mBehaviorStar; // 当前收藏记录
    private Behavior mBehaviorFollow; // 当前关注记录

    public ImageOverlayView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ImageOverlayView(Context context, Content content) {
        super(context);
        this.content = content;
        this.mContext = context;
        init();
    }

    public ImageOverlayView(Context context, com.mmdkid.mmdkid.models.v2.Content content) {
        super(context);
        //this.content = (Content) content;
        this.mContext = context;
        init();
    }

    public ImageOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setDescription(String description) {
        tvDescription.setText(description);
    }

    public void setShareText(String text) {
        this.sharingText = text;
    }

    private void sendShareIntent() {
        /*Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        sendIntent.setType("text/plain");
        getContext().startActivity(sendIntent);*/
        if (this.content != null) {
           /* Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("image/jpg");
            intent.setType("text*//*");
            intent.putExtra(Intent.EXTRA_STREAM, this.content.getContentUrl());
            intent.putExtra(Intent.EXTRA_SUBJECT, "测试标题");
            intent.putExtra(Intent.EXTRA_TEXT, "测试内容");
            getContext().startActivity(Intent.createChooser(intent, "来自xxx"));*/
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("image/jpg");
            try {
                intent.putExtra(Intent.EXTRA_STREAM, new URI(sharingText));//uri为你要分享的图片的uri
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            getContext().startActivity(intent);
        }

    }

    private void sendShareContent() {
        if (this.content != null) {
            UMImage image = new UMImage(getContext(), content.mImageList.get(0));//取第一个图片为分享显示图片
            image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
            String url = content.getContentUrl();
            url = url + "&showIn=wx"; // 通过该标识显示出app下载提示
            UMWeb web = new UMWeb(url);
            web.setTitle(content.mTitle);//标题
            web.setThumb(image);  //缩略图
            String text = HtmlUtil.getTextFromHtml(content.mContent, 20);
            if (text != null && !text.equals("null")) {
                web.setDescription(text);//取最多20字作为描述
            } else {
                web.setDescription((String) getContext().getResources().getText(R.string.share_description_empty));
            }
            new ShareAction((Activity) getContext())
                    //.withText("hello")
                    .withMedia(web)
                    .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ, SHARE_MEDIA.SINA)
                    .setCallback(mShareListener)
                    .open();
        }

    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_image_overlay, this);
        // 图片描述
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        // 分享菜单
        /*view.findViewById(R.id.btnShare).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendShareIntent();
                actionKey(KeyEvent.KEYCODE_BACK);
                sendShareContent();
            }
        });*/
        view.findViewById(R.id.imgClose).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actionKey(KeyEvent.KEYCODE_BACK);
            }
        });
        // 关注
        mFollowActionView = (TextView) findViewById(R.id.tvFollowAction);
        initFollowActionView();
        // 分享
        mShareView = (ImageView) findViewById(R.id.ivShare);
        mShareView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actionKey(KeyEvent.KEYCODE_BACK);
                sendShareContent();
            }
        });
        // 收藏
        mStarView = (ImageView) view.findViewById(R.id.ivStar);
        initStarView();
        mStarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsStared) {
                    // 取消收藏
                    unstar();
                } else {
                    // 收藏
                    star();
                }

            }
        });
        // 点赞
        mThumbsupView = (ImageView) view.findViewById(R.id.ivThumbsup);
        mThumbsupView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点赞
                thumbsup();
            }
        });
        // 评论
        mCommentView = (ImageView) view.findViewById(R.id.ivComment);
        mCommentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点击评论
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra("url", content.getContentCommentUrl());
                intent.putExtra("model", content);
                mContext.startActivity(intent);
            }
        });
        // 显示评论数量
        if (content.mCommentCount != 0) {
            new QBadgeView(mContext)
                    .bindTarget(mCommentView)
                    .setGravityOffset(15, 0, true)
                    .setBadgeGravity(Gravity.END | Gravity.TOP)
                    .setBadgeNumber(content.mCommentCount);
        }
        // 评论区域
        mCommentLayout = (LinearLayout) findViewById(R.id.llComment);
        mCommentLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点击评论
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra("url", content.getContentCommentUrl());
                intent.putExtra("model", content);
                mContext.startActivity(intent);
            }
        });
        // 用户头像
        mUserAvatarView = (SimpleDraweeView) findViewById(R.id.sdvAvatar);
        // 用户昵称
        mUserNameView = (TextView) findViewById(R.id.tvUsername);
        // 用户签名
        mUserDescriptionView = (TextView) findViewById(R.id.tvUserDescription);
        // 初始化用户信息
        initUser();
        // 设置用户头像点击操作
        mUserAvatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (content.mUser != null) {
                    /*Intent intent = new Intent(mContext,WebViewActivity.class);
                    intent.putExtra("url",content.mUser.getUrl());
                    mContext.startActivity(intent);*/
                    Intent intent = new Intent(mContext, HomePageActivity.class);
                    intent.putExtra("model", content.mUser);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    private void thumbsup() {
        App app = (App) mContext.getApplicationContext();
        if (app.isGuest()) {
            // 未登录，弹出登录界面
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
            return;
        } else {
            // 已登录，可以收藏
            mCurrentUser = app.getCurrentUser();
            final Behavior behavior = new Behavior();
            behavior.mModelType = "imagepost";
            behavior.mModelId = content.mModelId;
            behavior.mName = Behavior.BEHAVIOR_THUMBSUP;
            behavior.mUserId = mCurrentUser.mId;
            behavior.save(Model.ACTION_CREATE, mContext, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    // 存储失败
                    Log.d(TAG, "Save a behavior failed. " + error);
                    Toast.makeText(mContext, "点赞失败~", Toast.LENGTH_LONG).show();
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    // 存储成功
                    if (c == Behavior.class && !responseDataList.isEmpty()) {
                        mBehaviorStar = (Behavior) responseDataList.get(0);
                        Log.d(TAG, "Save a behavior success. id is " + mBehaviorStar.mId);
                        Toast.makeText(mContext, "已点赞", Toast.LENGTH_LONG).show();
                        mThumbsupView.setImageDrawable(mContext.getDrawable(R.drawable.thumb_up_outline_red));
                    }
                }
            });
        }
    }

    private void initUser() {
        if (content.mUser != null) {
            mUserNameView.setText(content.mUser.getDisplayName());
            mUserAvatarView.setImageURI(content.mUser.mAvatar);
            if (content.mUser.mSignature == null || content.mUser.mSignature.isEmpty()) {
                mUserDescriptionView.setText(R.string.homepage_no_signature);
            } else {
                mUserDescriptionView.setText(content.mUser.mSignature);
            }
            return;
        }
        if (content.mCreatedBy == 0) return;
        /*User.find(mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 查找用户信息出错
                Log.d(TAG,"Get the create user info failed. " );
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                // 查找用户信息成功
                if (c == User.class && !responseDataList.isEmpty()){
                    content.mUser = (User) responseDataList.get(0);
                    Log.d(TAG,"Get the create user info : " + content.mUser.mId);
                    mUserNameView.setText(content.mUser.getDisplayName());
                    mUserAvatarView.setImageURI(content.mUser.mAvatar);
                }
            }
        }).where("id",String.valueOf(content.mCreatedBy)).all();*/
        User.getUserInfo(content.mCreatedBy, mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 查找用户信息出错
                Log.d(TAG, "Get the create user info failed. ");
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                // 查找用户信息成功
                if (c == User.class && !responseDataList.isEmpty()) {
                    content.mUser = (User) responseDataList.get(0); // 起到缓存的作用
                    Log.d(TAG, "Get the create user info : " + content.mUser.mId);
                    mUserNameView.setText(content.mUser.getDisplayName());
                    mUserAvatarView.setImageURI(content.mUser.mAvatar);
                    if (content.mUser.mSignature == null || content.mUser.mSignature.isEmpty()) {
                        mUserDescriptionView.setText(R.string.homepage_no_signature);
                    } else {
                        mUserDescriptionView.setText(content.mUser.mSignature);
                    }
                }
            }
        });
    }

    // 当前用户取消收藏
    private void unstar() {
        App app = (App) mContext.getApplicationContext();
        if (app.isGuest()) {
            // 未登录，弹出登录界面
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
            return;
        } else {
            // 已登录，可以取消收藏
            mCurrentUser = app.getCurrentUser();
            if (mBehaviorStar == null) return;
            mBehaviorStar.delete(mContext, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    // 取消收藏过程中失败
                    Log.d(TAG, "Delete a star behavior record failed. ");
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    // 删除收藏记录成功
                    Log.d(TAG, "Delete a star behavior record success. ");
                    mBehaviorStar = null;
                    mIsStared = false;
                    mStarView.setImageDrawable(mContext.getDrawable(R.drawable.ic_star_outline_24dp_white));
                }
            });
        }
    }

    private void initStarView() {
        App app = (App) mContext.getApplicationContext();
        if (app.isGuest()) return;
        mCurrentUser = app.getCurrentUser();
        Behavior.find(mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 查找收藏记录出错
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c == Behavior.class && !responseDataList.isEmpty()) {
                    // 找到收藏记录
                    Log.d(TAG, "Find a star behavior record. ");
                    mBehaviorStar = (Behavior) responseDataList.get(0);
                    mStarView.setImageDrawable(mContext.getDrawable(R.drawable.star_yellow));
                    mIsStared = true;
                } else {
                    // 没有该收藏记录
                    Log.d(TAG, "Not find a star behavior record. ");
                    mStarView.setImageDrawable(mContext.getDrawable(R.drawable.ic_star_outline_24dp_white));
                }
            }
        }).where("user_id", Integer.toString(mCurrentUser.mId))
                .where("name", Behavior.BEHAVIOR_STAR)
                .where("model_type", "imagepost")
                .where("model_id", String.valueOf(content.mModelId))
                .all();
    }

    // 收藏当前的imagepost
    private void star() {
        Log.d(TAG, "Start star....");
        App app = (App) mContext.getApplicationContext();
        if (app.isGuest()) {
            // 未登录，弹出登录界面
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
            return;
        } else {
            // 已登录，可以收藏
            mCurrentUser = app.getCurrentUser();
            final Behavior behavior = new Behavior();
            behavior.mModelType = "imagepost";
            behavior.mModelId = content.mModelId;
            behavior.mName = Behavior.BEHAVIOR_STAR;
            behavior.mUserId = mCurrentUser.mId;
            behavior.save(Model.ACTION_CREATE, mContext, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    // 存储失败
                    Log.d(TAG, "Save a behavior failed. " + error);
                    Toast.makeText(mContext, "收藏失败~", Toast.LENGTH_LONG).show();
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    // 存储成功
                    if (c == Behavior.class && !responseDataList.isEmpty()) {
                        mBehaviorStar = (Behavior) responseDataList.get(0);
                        Log.d(TAG, "Save a behavior success. id is " + mBehaviorStar.mId);
                        Toast.makeText(mContext, "已收藏", Toast.LENGTH_LONG).show();
                        mStarView.setImageDrawable(mContext.getDrawable(R.drawable.star_yellow));
                        mIsStared = true;
                    }
                }
            });
        }
    }

    /**
     * 模拟键盘事件方法
     *
     * @param keyCode
     */
    public void actionKey(final int keyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
            Toast.makeText(getContext(), "成功了", Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            showProgressDialog(false);
            Toast.makeText(getContext(), "失败" + t.getMessage(), Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            showProgressDialog(false);
            Toast.makeText(getContext(), "取消了", Toast.LENGTH_LONG).show();

        }
    };

    private void showProgressDialog(boolean show) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        if (show) {
            mProgressDialog.setMessage("请稍后...");
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 初始化关注按钮状态
     */
    private void initFollowActionView() {
        App app = (App) mContext.getApplicationContext();
        if (!app.isGuest()) {
            // 已登录取得当前登录用户
            mCurrentUser = app.getCurrentUser();
        }
        // 设置监听
        mFollowActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentUser != null && content.mCreatedBy != 0) {
                    // 用户已经登录
                    if (mFollowActionView.getText().toString().equals(mContext.getString(R.string.homepage_follow))) {
                        // 关注用户
                        follow(mCurrentUser.mId, content.mCreatedBy);
                    } else {
                        // 取消关注
                        unFollow(mCurrentUser.mId, content.mCreatedBy);
                    }
                } else {
                    // 用户没有登录 弹出登录框
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    mContext.startActivity(intent);
                }
            }
        });
        // 用户是否登录
        if (app.isGuest()) {
            // 用户未登录直接显示关注按钮
            mFollowActionView.setText(R.string.homepage_follow);
            return;
        }

        // 是否为当前用户自己
        if (mCurrentUser != null && mCurrentUser.mId == content.mCreatedBy) {
            mFollowActionView.setVisibility(View.GONE);
            return;
        }
        // 当前用户是否已经关注
        Behavior.find(mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 当前用户未关注
                Log.d(TAG, "Get the error response from server>>>" + error);
                mFollowActionView.setText(R.string.homepage_follow);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (responseDataList.size() >= 1) {
                    // 当前用户已关注
                    mBehaviorFollow = (Behavior) responseDataList.get(0);
                    mFollowActionView.setText(R.string.homepage_followed);
                } else {
                    // 未找到关注记录
                    mFollowActionView.setText(R.string.homepage_follow);
                }

            }
        })
                .where("user_id", String.valueOf(mCurrentUser.mId))
                .where("name", Behavior.BEHAVIOR_FOLLOW)
                .where("model_type", "user")
                .where("model_id", String.valueOf(content.mCreatedBy))
                .all();

    }

    private void follow(int currentUserId,int userId) {
        App app = (App) mContext.getApplicationContext();
        // 用户未登录
        if(app.isGuest()){
            // 显示登录界面
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
            return;
        }
        if (currentUserId==0 || userId==0) return;
        Behavior behavior = new Behavior();
        behavior.mUserId = currentUserId;
        behavior.mName = Behavior.BEHAVIOR_FOLLOW;
        behavior.mModelType = "user";
        behavior.mModelId = userId;
        behavior.save(Model.ACTION_CREATE, mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 创建关注信息出错
                Log.d(TAG,"Create a new follow behavior failed.");
            }
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c==Behavior.class && responseDataList.size()!=0){
                    // 创建关注信息成功
                    Log.d(TAG,"Create a new follow behavior success.");
                    mBehaviorFollow = (Behavior) responseDataList.get(0);
                    mFollowActionView.setText(R.string.homepage_followed);
                }
            }
        });
    }

    private void unFollow(int currentUserId,int userId){
        App app = (App) mContext.getApplicationContext();
        // 用户未登录
        if(app.isGuest()){
            // 用户必须登录才能取消关注
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
            return;
        }
        if (mBehaviorFollow==null) return;
        mBehaviorFollow.delete(mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 取消关注网络操作失败
                Log.d(TAG,"Delete the follow behavior failed.");
            }
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c==Behavior.class && !responseDataList.isEmpty()){
                    // 删除关注成功
                    Log.d(TAG,"Delete the follow behavior success.");
                    mBehaviorFollow = null;
                    mFollowActionView.setText(R.string.homepage_follow);
                }
            }
        });
    }

}
