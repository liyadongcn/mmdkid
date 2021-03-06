package com.mmdkid.mmdkid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.helper.HtmlUtil;
import com.mmdkid.mmdkid.helper.ProgressDialog;
import com.mmdkid.mmdkid.models.ActionLog;
import com.mmdkid.mmdkid.models.Behavior;
import com.mmdkid.mmdkid.models.Comment;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Diary;
import com.mmdkid.mmdkid.models.Goods;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.mmdkid.mmdkid.singleton.ActionLogs;

import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.ArrayList;
import java.util.List;
/**
 * 可加载的内容包括content
 * content的评论内容
 */
public class WebViewActivity extends AppCompatActivity {

    private final static String TAG = "WebViewActivity";

    private User mCurrentUser;  // 当前用户
    private Token mCurrentToken;
    private Model mModel;
    private User mUser;         // 当前内容发布者

    private SimpleDraweeView mUserAvatarView;// 内容发布者头像
    private TextView mUserNameView;          // 内容发布者名字
    private TextView mUserDescriptionView;  // 内容发布者个人签名

    private WebView mWebView;
    private ContentLoadingProgressBar mProgressBar;
    private LinearLayout mCommentForm;
    private EditText mCommentView;
    private TextView   mSendButton;
    private ImageView mStarView;
    private ImageView mThumbupView;
    private ImageView mShareView;

    private boolean mIsStared=false;
    private Behavior mBehaviorStar; // 当前收藏记录

    private List<String> mCookies;
    private boolean mUsingCookies = true;

    private ProgressDialog mProgressDialog;

    // 使webview可以打开上载按钮
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessage5;
    public static final int FILECHOOSER_RESULTCODE = 5173;
    public static final int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 5174;

    // 指明当前webview显示一个广告
    public static final int SHOW_TYPE_ADVERTISEMENT = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Let's display the progress in the activity title bar, like the
        // browser app does.
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mUsingCookies = getIntent().getBooleanExtra("cookies",true);
        // 内容对象content content的类型又包括：文章、图片、视频；评论页，只包含对内容的评论；
        mModel = (Model) getIntent().getSerializableExtra("model");
        // 当前登录用户的信息
        getCurrentLoginUserInfo();
        // 内容加载进度条
        mProgressBar = (ContentLoadingProgressBar) findViewById(R.id.progressBar);
        // 评论区域
        mCommentForm = (LinearLayout) findViewById(R.id.llCommentForm);
        if (mModel == null || mModel instanceof com.mmdkid.mmdkid.models.gw.Content ){
            mCommentForm.setVisibility(View.GONE);
        }else {
            mCommentForm.setVisibility(View.VISIBLE);
        }
        // 评论编辑
        mCommentView = (EditText) findViewById(R.id.comment) ;
        // 评论发送
        mSendButton = (TextView) findViewById(R.id.tvPublish);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComment();
            }
        });
        // 收藏
        mStarView = (ImageView) findViewById(R.id.ivStar);
        initStarView();
        mStarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsStared){
                    // 取消收藏
                    unstar();
                }else{
                    // 收藏
                    star();
                }

            }
        });
        // 分享
        mShareView = (ImageView) findViewById(R.id.ivShare);
        mShareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareOnSelection();
            }
        });
        // 点赞
        mThumbupView = (ImageView) findViewById(R.id.ivThumbup);
        mThumbupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thumbUp();
            }
        });
        // 登录用户的cookies信息
        if(mCookies!=null && !mCookies.isEmpty() && mUsingCookies){
            startWebView(mCookies);
        }else{
            //getCookie("liyadong","123456");
            startWebView(null);
        }

        // 用户头像
        mUserAvatarView = (SimpleDraweeView) findViewById(R.id.sdvAvatar);
        // 用户昵称
        mUserNameView = (TextView) findViewById(R.id.tvUsername);
        // 用户签名
        mUserDescriptionView = (TextView) findViewById(R.id.tvUserDescription);
        // 初始化用户信息
        //showUser();

    }


    private void initStarView()
    {
        if (mModel == null || mModel instanceof com.mmdkid.mmdkid.models.gw.Content ) return;
        App app = (App) getApplicationContext();
        if (app.isGuest()) return ;
        mCurrentUser = app.getCurrentUser();
        Behavior.find(this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 查找收藏记录出错
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c == Behavior.class && !responseDataList.isEmpty()){
                    // 找到收藏记录
                    Log.d(TAG,"Find a star behavior record. ");
                    mBehaviorStar = (Behavior) responseDataList.get(0);
                    mStarView.setImageDrawable(getDrawable(R.drawable.star_yellow));
                    mIsStared =true;
                }
                else{
                    // 没有该收藏记录
                    Log.d(TAG,"Not find a star behavior record. ");
                    mStarView.setImageDrawable(getDrawable(R.drawable.ic_star_outline_gray));
                }
            }
        }).where("user_id",Integer.toString(mCurrentUser.mId))
                .where("name",Behavior.BEHAVIOR_STAR)
                .where("model_type",((Content)mModel).mModelType)
                .where("model_id", String.valueOf(((Content)mModel).mModelId))
                .all();
    }
    // 收藏当前内容
    private void star() {
        Log.d(TAG,"Start star....");
        App app = (App) getApplicationContext();
        if (app.isGuest()){
            // 未登录，弹出登录界面
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }else {
            // 已登录，可以收藏
            mCurrentUser = app.getCurrentUser();
            final Behavior behavior = new Behavior();
            behavior.mModelType =((Content)mModel).mModelType;
            behavior.mModelId = ((Content)mModel).mModelId;
            behavior.mName = Behavior.BEHAVIOR_STAR;
            behavior.mUserId = mCurrentUser.mId;
            behavior.save(Model.ACTION_CREATE,this, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    // 存储失败
                    Log.d(TAG,"Save a behavior failed. " + error);
                    Toast.makeText(WebViewActivity.this,"收藏失败~",Toast.LENGTH_LONG).show();
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    // 存储成功
                    if(c == Behavior.class && !responseDataList.isEmpty()){
                        mBehaviorStar = (Behavior) responseDataList.get(0);
                        Log.d(TAG,"Save a behavior success. id is " + mBehaviorStar.mId);
                        Toast.makeText(WebViewActivity.this,"已收藏",Toast.LENGTH_LONG).show();
                        mStarView.setImageDrawable(getDrawable(R.drawable.star_yellow));
                        mIsStared = true;
                    }
                }
            });
        }
    }
    // 取消收藏当前内容
    private void unstar() {
        App app = (App) getApplicationContext();
        if (app.isGuest()){
            // 未登录，弹出登录界面
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }else {
            // 已登录，可以取消收藏
            mCurrentUser = app.getCurrentUser();
            if (mBehaviorStar == null) return;
            mBehaviorStar.delete(this, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    // 取消收藏过程中失败
                    Log.d(TAG,"Delete a star behavior record failed. ");
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    // 删除收藏记录成功
                    Log.d(TAG,"Delete a star behavior record success. ");
                    mBehaviorStar = null;
                    mIsStared =false;
                    Toast.makeText(WebViewActivity.this,"已取消收藏",Toast.LENGTH_LONG).show();
                    mStarView.setImageDrawable(getDrawable(R.drawable.ic_star_outline_gray));
                }
            });
        }
    }
    private void setUserOnToolbarVisibility(int visibility){
        // 用户头像
        mUserAvatarView.setVisibility(visibility);
        // 用户昵称
        mUserNameView.setVisibility(visibility);
        // 用户签名
        mUserDescriptionView.setVisibility(visibility);
    }
    private void showUserOnToolbar() {
        // 浏览器浏览非content数据
        if (!(mModel instanceof Content)) return;
        final Content content = (Content) mModel;
        if (content.mSource_name!=null && !content.mSource_name.isEmpty()) return; // 转载内容不显示创建者
        if (content.mUser != null){
            setUserOnToolbarVisibility(View.VISIBLE);
            mUserNameView.setText(content.mUser.getDisplayName());
            mUserAvatarView.setImageURI(content.mUser.mAvatar);
            if(content.mUser.mSignature==null || content.mUser.mSignature.isEmpty()){
                mUserDescriptionView.setText(R.string.homepage_no_signature);
            }else{
                mUserDescriptionView.setText(content.mUser.mSignature);
            }
            // 设置用户头像点击操作
            mUserAvatarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((Content)mModel).mUser!= null){
                    /*Intent intent = new Intent(mContext,WebViewActivity.class);
                    intent.putExtra("url",content.mUser.getUrl());
                    mContext.startActivity(intent);*/
                        Intent intent = new Intent(WebViewActivity.this, HomePageActivity.class);
                        intent.putExtra("model",((Content)mModel).mUser);
                        startActivity(intent);
                    }
                }
            });
            return;
        }
        if (content.mCreatedBy == 0) return;
        User.getUserInfo(content.mCreatedBy,this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 查找用户信息出错
                Log.d(TAG,"Get the create user info failed. " );
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                // 查找用户信息成功
                if (c == User.class && !responseDataList.isEmpty()){
                    content.mUser = (User) responseDataList.get(0); // 起到缓存的作用
                    Log.d(TAG,"Get the create user info : " + content.mUser.mId);
                    setUserOnToolbarVisibility(View.VISIBLE);
                    mUserNameView.setText(content.mUser.getDisplayName());
                    mUserAvatarView.setImageURI(content.mUser.mAvatar);
                    if(content.mUser.mSignature==null || content.mUser.mSignature.isEmpty()){
                        mUserDescriptionView.setText(R.string.homepage_no_signature);
                    }else{
                        mUserDescriptionView.setText(content.mUser.mSignature);
                    }
                    // 设置用户头像点击操作
                    mUserAvatarView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (((Content)mModel).mUser!= null){
                                Intent intent = new Intent(WebViewActivity.this, HomePageActivity.class);
                                intent.putExtra("model",((Content)mModel).mUser);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog(boolean show){
        if (mProgressDialog==null){
            mProgressDialog = new ProgressDialog(this);
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

    /**
     *  当前登录用户的登录信息
     */
    private void getCurrentLoginUserInfo() {
        App app = (App) getApplicationContext();
        mCurrentUser = app.getCurrentUser();
        mCurrentToken = app.getCurrentToken();
        mCookies = app.getCookies();
    }

    /**
     *  发表评论
     */
    private void sendComment() {
        if (mCurrentUser == null){
            // 登录才能发表评论
            Intent intent = new Intent(WebViewActivity.this,LoginActivity.class);
            startActivity(intent);
            return;
        }

        String modelType = null;
        int modelId = 0;
        if(mModel == null){
            return ;
        }else if (mModel instanceof Content){
            modelType = ((Content) mModel).mModelType;
            modelId = ((Content) mModel).mModelId;
        }else if (mModel instanceof Diary){
            modelType = "diary";
            modelId =((Diary) mModel).mId;
        }else{
            return;
        }

        // Reset errors.
        mCommentView.setError(null);

        String commentString = mCommentView.getText().toString();

        if (TextUtils.isEmpty(commentString) || !isCommentValid(commentString)){
            mCommentView.setError(getString(R.string.error_invalid_comment));
            mCommentView.requestFocus();
            return;
        }

        Comment comment = new Comment();
        comment.mCreated_by = mCurrentUser.mId;
        comment.mUpdated_by = mCurrentUser.mId;
        comment.mContent = commentString;
        comment.mModel_type = modelType;
        comment.mModel_id = modelId;
        comment.save(Model.ACTION_CREATE, this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Save the comment failed.");
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c == Comment.class && !responseDataList.isEmpty()){
                    Log.d(TAG,"Send the comment success.");
                    Log.d(TAG,"The comment is " + ((Comment)responseDataList.get(0)).toString());
                    mCommentView.setText(null);
                    mCommentView.clearFocus();
                    Toast.makeText(WebViewActivity.this,getString(R.string.send_comment_success)
                            ,Toast.LENGTH_SHORT).show();
                    mWebView.reload();
                }
            }
        });

    }

    private boolean isCommentValid(String comment) {
        return comment.length()>2;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //友盟Session启动、App使用时长等基础数据统计
        MobclickAgent.onResume(this);
        //开始记录一个用户使用log
        ActionLogs.getInstance(this).start(ActionLog.ACTION_VIEW,mModel);
        // 刷新登录信息
        getCurrentLoginUserInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //友盟Session启动、App使用时长等基础数据统计
        MobclickAgent.onPause(this);
        //结束记录一个用户使用log
        ActionLogs.getInstance(this).stop();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_webview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                int showType = getIntent().getIntExtra("showType",0);
                if (showType == SHOW_TYPE_ADVERTISEMENT){
                    // 当前显示的是广告页 返回时启动主程序
                    Intent intent = new Intent(WebViewActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }else{
                    finish();
                    break;
                }

            case R.id.action_share:
                // 分享当前内容
                shareOnSelection();
                break;
                
        }
        return super.onOptionsItemSelected(item);
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
            Toast.makeText(WebViewActivity.this,"成功了",Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            showProgressDialog(false);
            Toast.makeText(WebViewActivity.this,"失败"+t.getMessage(),Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            showProgressDialog(false);
            Toast.makeText(WebViewActivity.this,"取消了",Toast.LENGTH_LONG).show();

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startWebView(List<String> cookies) {

        mWebView = (WebView) findViewById(R.id.webViewDetail);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setJavaScriptEnabled(true);

        //showProgressDialog();

        final Activity activity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000);
//                if (progress == 100 ) mProgressDialog.dismiss();
//                mProgressDialog.setProgress(progress);
                if (progress == 100) mProgressBar.hide();
                mProgressBar.setProgress(progress);
            }

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                this.openFileChooser(uploadMsg, "*/*");
            }

            // For Android >= 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType) {
                this.openFileChooser(uploadMsg, acceptType, null);
            }

            // For Android >= 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
            }

            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                if (mUploadMessage5 != null) {
                    mUploadMessage5.onReceiveValue(null);
                    mUploadMessage5 = null;
                }
                mUploadMessage5 = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent,
                            FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
                } catch (ActivityNotFoundException e) {
                    mUploadMessage5 = null;
                    return false;
                }
                return true;
            }
        });
        //webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String htmlData = intent.getStringExtra("htmlData");

        mWebView.setWebViewClient(new WebViewClient(){
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Oh no! " + description);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                //Toast.makeText(activity, "Oh no! " + error.toString(), Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Oh no! " +  error.toString());
            }

            // 处理网页内连接的点击
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                Log.d(TAG,"Url is clicked :"+uri.toString());
                Log.d(TAG,"Url scheme :"+uri.getScheme());
                Log.d(TAG,"Url path :"+uri.getPath());
                Log.d(TAG,"Url host :"+uri.getHost());
                Log.d(TAG,"Url encodedQuery :"+uri.getEncodedQuery());
                if (uri.getScheme().equals("http") && uri.getHost().equals("www.mmdkid.cn") && uri.getEncodedQuery().contains("r=user%2Fshow-me")) {
                    // 点击用户头像
                    showUserHomePage();
                    //表示告诉系统我们已经拦截了URL并做处理，不需要再触发系统默认的行为
                    return true;
                }
                if (uri.getScheme().equals("http") && uri.getHost().equals("www.mmdkid.cn") && uri.getEncodedQuery().contains("r=post%2Fshare-wx")) {
                    // 点击分享到微信
                    share(SHARE_MEDIA.WEIXIN);
                    //表示告诉系统我们已经拦截了URL并做处理，不需要再触发系统默认的行为
                    return true;
                }if (uri.getScheme().equals("http") && uri.getHost().equals("www.mmdkid.cn") && uri.getEncodedQuery().contains("r=post%2Fshare-circle")) {
                    // 点击分享到朋友圈
                    share(SHARE_MEDIA.WEIXIN_CIRCLE);
                    //表示告诉系统我们已经拦截了URL并做处理，不需要再触发系统默认的行为
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, request);
            }
        });


        if (TextUtils.isEmpty(url) ) {
            //mWebView.loadData("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+"<html><body>You scored <b>192</b> points.</body></html>","text/html;charset=UTF-8",null);
            Toast.makeText(this, "No Url,show htmldata " , Toast.LENGTH_SHORT).show();
            //mWebView.loadDataWithBaseURL(null,"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+"<html><body>You scored <b>192</b> points.</body></html>","text/html","UTF-8",null);
            mWebView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
        } else {
            //Log.d(TAG,url);
            if(cookies!=null && !cookies.isEmpty()){
                //url = "http://www.mmdkid.cn/index.php?r=site/login";
                //url = "http://10.0.3.2/index.php?r=diary/view&theme=app&id=44";
                CookieSyncManager.createInstance(this);
                CookieManager cookieManager = CookieManager.getInstance();
                //cookieManager.removeAllCookies(null);
                cookieManager.removeAllCookie();
                cookieManager.setAcceptCookie(true);
                //cookieManager.removeSessionCookies(null);//移除
                //cookieManager.setAcceptThirdPartyCookies(mWebView,true);
                cookieManager.setAcceptFileSchemeCookies(true);

                for (String cookie : mCookies){
                    cookieManager.setCookie(url, cookie , new ValueCallback<Boolean>() {
                        @Override
                        public void onReceiveValue(Boolean aBoolean) {
                            if(aBoolean==true){
                                Log.d(TAG,"Setting a cookie success.");
                            }else{
                                Log.d(TAG,"Setting a cookie fail.");
                            }
                        }
                    });//cookies是在HttpClient中获得的cookie
                    Log.d(TAG,"Set one cookie " + cookie);
                    //break;
                }
                //cookieManager.flush();
                Log.d(TAG,"Get the cookie manager cookie " + cookieManager.getCookie(url));

                CookieSyncManager.getInstance().sync();
            }
            Log.d(TAG,"Loading url " + url);
            mWebView.loadUrl(url);
        }

    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        UMShareAPI.get(this).onActivityResult(requestCode,resultCode,intent);
        // 用于浏览器上载文件 若没有webview不能上传文件
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            if (null == mUploadMessage5) {
                return;
            }
            mUploadMessage5.onReceiveValue(WebChromeClient.FileChooserParams
                    .parseResult(resultCode, intent));
            mUploadMessage5 = null;
        }
    }
    private void showUserHomePage(){
        // 浏览器浏览非content数据
        if (!(mModel instanceof Content)) return;
        final Content content = (Content) mModel;
        /*if (content.mSource_name!=null && !content.mSource_name.isEmpty()){
            Log.d(TAG,"Content source name :" + content.mSource_name);
            return; // 转载内容不显示创建者
        }*/
        if (content.mUser != null){
            Intent intent = new Intent(WebViewActivity.this, HomePageActivity.class);
            intent.putExtra("model",((Content)mModel).mUser);
            startActivity(intent);
            return;
        }
        if (content.mCreatedBy == 0) return;
        User.getUserInfo(content.mCreatedBy,this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 查找用户信息出错
                Log.d(TAG,"Get the create user info failed. " );
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                // 查找用户信息成功
                if (c == User.class && !responseDataList.isEmpty()){
                    content.mUser = (User) responseDataList.get(0); // 起到缓存的作用
                    Log.d(TAG,"Get the create user info : " + content.mUser.mId);
                    Intent intent = new Intent(WebViewActivity.this, HomePageActivity.class);
                    intent.putExtra("model", content.mUser);
                    startActivity(intent);
                }
            }
        });
    }
    /**
     * 指定分享的渠道，直接分享
     */
    private void share(SHARE_MEDIA shareMedia){
        String url;
        UMImage image;
        UMWeb web = null;
        url = getIntent().getStringExtra("url");
        if(mModel instanceof Content){
            Content content = (Content) mModel;
            if (content.mImage!=null && !content.mImage.isEmpty() && !content.mImage.equalsIgnoreCase("null")){
                // 使用image字段
                image = new UMImage(WebViewActivity.this, HtmlUtil.getUrl(content.mImage));//网络图片
            }else if(content.mImageList!=null && !content.mImageList.isEmpty()){
                // 使用第一张图片
                image = new UMImage(WebViewActivity.this,  HtmlUtil.getUrl(content.mImageList.get(0)));//网络图片
            }else{
                // 使用默认图标
                image = new UMImage(WebViewActivity.this,R.mipmap.ic_launcher);
            }

            image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
            url = url+"&showIn=wx"; // 通过该标识显示出app下载提示
            Log.d(TAG,"Sharing Url is " + url);
            web = new UMWeb(url);
            web.setTitle(content.mTitle);//标题
            web.setThumb(image);  //缩略图
            String text = HtmlUtil.getTextFromHtml(content.mContent,20);
            if (text!=null && !text.equals("null")) {
                web.setDescription(text);//取最多20字作为描述
            }else{
                web.setDescription((String) getResources().getText(R.string.share_description_empty));
            }
        }

        new ShareAction(WebViewActivity.this)
                .setPlatform(shareMedia)//传入平台
                .withMedia(web)//分享内容
                .setCallback(mShareListener)//回调监听器
                .share();
    }
    /**
     * 分享当前内容，显示分享面板，用户选择分享渠道
     */
    private void shareOnSelection(){
        // 分享当前页面内容
        //Log.d(TAG,"Model is " + ((Content)mModel).mImage);
        String url;
        UMImage image;
        UMWeb web = null;

        if(mModel instanceof Content){
            Content content = (Content) mModel;
            url = content.getContentUrl();
            if (content.mImage!=null && !content.mImage.isEmpty() && !content.mImage.equalsIgnoreCase("null")){
                // 使用image字段
                image = new UMImage(WebViewActivity.this, HtmlUtil.getUrl(content.mImage));//网络图片
            }else if(content.mImageList!=null && !content.mImageList.isEmpty()){
                // 使用第一张图片
                image = new UMImage(WebViewActivity.this,  HtmlUtil.getUrl(content.mImageList.get(0)));//网络图片
            }else{
                // 使用默认图标
                image = new UMImage(WebViewActivity.this,R.mipmap.ic_launcher);
            }

            image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
            url = url+"&showIn=wx"; // 通过该标识显示出app下载提示
            Log.d(TAG,"Sharing Url is " + url);
            web = new UMWeb(url);
            web.setTitle(content.mTitle);//标题
            web.setThumb(image);  //缩略图
            String text = HtmlUtil.getTextFromHtml(content.mContent,20);
            if (text!=null && !text.equals("null")) {
                web.setDescription(text);//取最多20字作为描述
            }else{
                web.setDescription((String) getResources().getText(R.string.share_description_empty));
            }
        }
        if (mModel instanceof Goods) {
            url = getIntent().getStringExtra("url");
            Goods goods = (Goods) mModel;
            web = new UMWeb(url);
            web.setTitle(goods.title);//标题
            if (goods.imageList!= null && !goods.imageList.isEmpty() ){
                image = new UMImage(WebViewActivity.this, goods.imageList.get(0));//网络图片
                image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
                web.setThumb(image);  //缩略图
            }
            if (goods.editorComment!=null && !goods.editorComment.equals("null")) {
                //取最多20字作为描述
                web.setDescription(goods.editorComment.length()>20? goods.editorComment.substring(0,20):goods.editorComment) ;
            }else{
                web.setDescription((String) getResources().getText(R.string.share_description_empty));
            }
        }
        if (mModel instanceof com.mmdkid.mmdkid.models.gw.Content){
            url = getIntent().getStringExtra("url");
            com.mmdkid.mmdkid.models.gw.Content gwContent = (com.mmdkid.mmdkid.models.gw.Content)mModel;
            web = new UMWeb(url);
            web.setTitle(gwContent.mTitle);//标题
            if (gwContent.mImage!=null && !gwContent.mImage.isEmpty()){
                image = new UMImage(WebViewActivity.this, gwContent.mImage);//网络图片
                image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
                web.setThumb(image);  //缩略图
            }else if (gwContent.mImageList!= null && !gwContent.mImageList.isEmpty() ){
                image = new UMImage(WebViewActivity.this, gwContent.mImageList.get(0));//网络图片
                image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
                web.setThumb(image);  //缩略图
            }
            web.setDescription((String) getResources().getText(R.string.share_description_empty));
                   /* if (goods.editorComment!=null && !goods.editorComment.equals("null")) {
                        //取最多20字作为描述
                        web.setDescription(goods.editorComment.length()>20? goods.editorComment.substring(0,20):goods.editorComment) ;
                    }else{
                        web.setDescription("");
                    }*/
        }
        new ShareAction(WebViewActivity.this)
                //.withText("hello")
                .withMedia(web)
                .setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.SINA)
                .setCallback(mShareListener)
                .open();
    }
    /**
     * 点赞当前内容
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void thumbUp() {
        if(mModel instanceof Content){
            Content content = (Content)mModel;
            mWebView.loadUrl(content.getThumbsupUrl());
            Toast.makeText(this,"已点赞",Toast.LENGTH_LONG).show();
            mThumbupView.setImageDrawable(getDrawable(R.drawable.thumb_up_outline_red));
        }
    }
}
