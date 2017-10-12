package com.mmdkid.mmdkid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.Auth;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * A login screen that offers login via email/password.
 * 登录过程
 * 1、根据用户名密码取得用户访问Token
 * 2、根据Token信息取得用户的用户信息
 * 3、使用用户名密码模拟web登录，取得网站的访问cookie
 */
public class LoginActivity extends AppCompatActivity implements RESTAPIConnection.OnConnectionListener,OnClickListener {

    private static final String TAG = "LoginActivity" ;

    private static final String FORGOT_PASSWORD_URL = "http://www.mmdkid.cn/index.php?r=site/request-password-reset&theme=app";

    private boolean mIsLogging = false;
    private boolean mIsGettingToken = false;
    private boolean mIsGettingUserInfo = false;
    private boolean mTokenValid = false;
    private boolean mUserValid = false;

    private Token mToken;
    private User  mUser;
    private String mIdentity;
    private String mPassword;
    private List<String> mCookies;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mIdentityView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mRegisterView;
    private TextView mForgetPasswordView;

    // 第三方登录
    private ImageView mQQLogin;
    private ImageView mSinaLogin;
    private ImageView mWxLogin;
    private UMShareAPI mShareAPI;



    public class ShareUserInfo {
        public String uid;
        public String name;
        public String iconurl;
        public String gender;
        public String city;
        public String province;
    };
    private ShareUserInfo mShareUserInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mEmailView.setVisibility(View.GONE);

        mIdentityView = (EditText) findViewById(R.id.identity);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mRegisterView = (TextView) findViewById(R.id.register);
        mRegisterView.setOnClickListener(this);
        mForgetPasswordView = (TextView) findViewById(R.id.tvForgetPassword);
        mForgetPasswordView.setOnClickListener(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        UMShareAPI.get(LoginActivity.this).setShareConfig(config);

        mQQLogin = (ImageView)findViewById(R.id.qqLogin);
        mQQLogin.setOnClickListener(this);

        mSinaLogin = (ImageView)findViewById(R.id.sinaLogin);
        mSinaLogin.setOnClickListener(this);

        mWxLogin = (ImageView)findViewById(R.id.wxLogin);
        mWxLogin.setOnClickListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.qqLogin:
                // do the qq login
                Log.d(TAG,"QQ login view is clicked.");
                mShareAPI = UMShareAPI.get(LoginActivity.this);
                mShareAPI.getPlatformInfo(LoginActivity.this, SHARE_MEDIA.QQ, umAuthListener);
                break;
            case R.id.sinaLogin:
                // do the sina weibo login
                Log.d(TAG,"Sina weibo login view is clicked.");
                mShareAPI = UMShareAPI.get(LoginActivity.this);
                mShareAPI.getPlatformInfo(LoginActivity.this, SHARE_MEDIA.SINA, umAuthListener);
                //UmengTool.getSignature(this);
                break;
            case R.id.wxLogin:
                // 微信登录
                Log.d(TAG,"Weixin login view is clicked.");
                mShareAPI = UMShareAPI.get(LoginActivity.this);
                mShareAPI.getPlatformInfo(LoginActivity.this, SHARE_MEDIA.WEIXIN, umAuthListener);
                break;
            case R.id.tvForgetPassword:
                // 忘记密码
                intent = new Intent(LoginActivity.this,WebViewActivity.class);
                intent.putExtra("url",FORGOT_PASSWORD_URL);
                startActivity(intent);
                break;
            case R.id.register:
                // 注册新用户
                intent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
                finish();
            default:
        }
    }

    /*
    * 通过第三方提供的用户信息 查找本地的关联账号
    * */
    private void findAuth(final ShareUserInfo userInfo, final String source){
        Log.d(TAG,"Try to find the related auth.");
        Auth.find(LoginActivity.this, new RESTAPIConnection.OnConnectionListener() {
                    @Override
                    public void onErrorRespose(Class c, String error) {
                        Log.d(TAG,"Can not get the auth " + error);
                    }
                    @Override
                    public void onResponse(Class c, ArrayList responseDataList) {
                        if(!responseDataList.isEmpty()){
                            // 本地已经有关联的账号
                            Log.d(TAG,"Get the auth from the server ");
                            Auth  auth = (Auth) responseDataList.get(0);
                            Log.d(TAG,"Auth source is " + auth.mSource);
                            Log.d(TAG,"Auth source id is " + auth.mSourceId);
                            Log.d(TAG,"Auth user id is " + auth.mUserId);
                            Log.d(TAG,"Auth user name is " + auth.mUsername);
                            Log.d(TAG,"Auth user secret is " + auth.mSecret);
                            //  使用关联账号登录
                            mIdentity = auth.mUsername;
                            mPassword = auth.mSecret;
                            attemptGetToken(mIdentity,mPassword);
                        }else{
                            // 本地没有关联的账号 需要创建一个关联账号
                            Log.d(TAG,"Get right response from the server.but it is empty.");
                            Log.d(TAG,"There is no related user. Try to auto create a  new user.");
                            createAutoUser(userInfo,source);
                        }
                    }
                }).where("source", source).where("source_id",userInfo.uid).all();
    }
    /*
    * 本地没有关联账号时，系统将自动创建一个账号并关联
    * */
    private void createAutoUser(final ShareUserInfo userInfo, final String source) {
        User user = new User();
        user.mAvatar = userInfo.iconurl;
        user.mNickname = userInfo.name;
        user.mRole = User.ROLE_PARENT;
        user.mPassword = Utility.getRandomString(8);
        mPassword = user.mPassword;
        user.save(User.ACTION_AUTO_SIGNUP, LoginActivity.this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"auto create a new user for the share user has error "+ error);
                Toast.makeText(LoginActivity.this,"auto create a user erro",Toast.LENGTH_LONG).show();
                showProgress(false);
            }
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if(!responseDataList.isEmpty()){
                    // 创建关联账号成功
                    User user = (User) responseDataList.get(0);
                    Log.d(TAG,"auto create a user is successful.");
                    Log.d(TAG,"auto user's name is " + user.mUsername);
                    Log.d(TAG,"auto user's id is " + user.mId);
                    Log.d(TAG,"auto user's avatar is " + user.mAvatar);
                    Log.d(TAG,"auto user's nick name is " + user.mNickname);
                    // 使用新创建的用户名和用户秘密登录系统
                    mIdentity = user.mUsername;
                    // 创建关联信息记录
                    createAuth(user.mId,userInfo,source,mPassword);
                }else{
                    Log.d(TAG,"Auto create a new user ,Get right response , but no user info from the server.");
                }
            }
        });
    }
    /*
    * 使用第三方用户信息以及新创建的关联账号
    * 创建一个关联记录
    * */
    private void createAuth(int userId,ShareUserInfo userInfo,String source,String password){
        Auth auth = new Auth();
        auth.mUserId = userId;
        auth.mSource = source;
        auth.mSourceId = userInfo.uid;
        auth.mSecret = password;
        auth.save(Model.ACTION_CREATE, LoginActivity.this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Create a new auth on the server error.");
                Toast.makeText(LoginActivity.this,"Create a auth error.",Toast.LENGTH_LONG).show();
                showProgress(false);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if(!responseDataList.isEmpty()){
                    Log.d(TAG,"Create a new auth on the server");
                    Auth  auth = (Auth) responseDataList.get(0);
                    Log.d(TAG,"Auth source is " + auth.mSource);
                    Log.d(TAG,"Auth source id is " + auth.mSourceId);
                    Log.d(TAG,"Auth user id is " + auth.mUserId);
                    Log.d(TAG,"Auth user secret is " + auth.mSecret);
                    // 使用新创建的关联账号登录系统
                    attemptGetToken(mIdentity,mPassword);
                }else{
                    Log.d(TAG,"Get right response from the server,but has error to create a new auth.");
                }
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mIsLogging) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mIdentity = mIdentityView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(mPassword) && !isPasswordValid(mPassword)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid identity, if the user entered one.
        if (!TextUtils.isEmpty(mIdentity) && !isIdentityValid(mIdentity)) {
            mIdentityView.setError(getString(R.string.error_invalid_identity));
            focusView = mIdentityView;
            cancel = true;
        }

        // Check for a valid email address.
       /* if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

           /*
           * 通用登录过程，删除本地缓存的用户信息及该用户相关的Token信息
           * 重新申请用户的访问Token
           * */
            mIsLogging = true;
            mTokenValid = false;
            mUserValid = false;
            /*
            * 清除本地目前缓存的用户信息
            * */
            App app = (App) getApplicationContext();
            app.setCurrentUser(null);
            /*
            * 使用用户唯一标识获取授权Token，该标识可以是用户名、手机号、邮箱号
            * */
            attemptGetToken(mIdentity,mPassword);
        }
    }

    /*
    *  通过有效的Token获得登录用户的信息
    * */
    private void attemptGetUser(String identity,String accessToken) {
        if(mIsGettingUserInfo) return;
        mIsGettingUserInfo = true;
        RESTAPIConnection connection = new RESTAPIConnection(this);
        connection.ACCESS_TOKEN = accessToken;
        User.find(connection).where("user_name",identity).all();
    }
    /*
    *  通过用户名密码获得访问Token
    *
    * */
    private void attemptGetToken(String identity, String password) {
        if(mIsGettingToken) return;
        mIsGettingToken = true;
        RESTAPIConnection connection = new RESTAPIConnection(this);
        Token.find(connection).where("username",identity).where("password",password).all();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean isIdentityValid(String identity){

        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onErrorRespose(Class c, String error) {
        showProgress(false);
        Log.d(TAG,"Get erro from the server.");
        Log.d(TAG,error);
        if(c==Token.class){
            mIsGettingToken = false;
            mTokenValid = false;
            mIdentityView.setError(getString(R.string.error_invalid_login));
        }
        if(c==User.class){
            mIsGettingUserInfo = false;
            mUserValid =false;
        }

        mIsLogging = false;

        /*
        * 获取Token或者用户信息错误，清除本地缓存的token信息及用户信息
        * */
        App app = (App) getApplicationContext();
        app.setCurrentUser(null);
    }

    @Override
    public void onResponse(Class c, ArrayList responseDataList) {

        Log.d(TAG,"Get response from the server.");
        if(c==Token.class){
            mToken = (Token) responseDataList.get(0);
            Log.d(TAG,"The access token is :" + mToken.mAccessToken);
            mTokenValid = true;
            mIsGettingToken = false;
            mToken.saveToLocal(this);
            Log.d(TAG,"Try to get the user info by the access token.");
            attemptGetUser(mIdentity,mToken.mAccessToken);
        }
        if(c==User.class){
            if(!responseDataList.isEmpty()){
                mUser = (User)responseDataList.get(0);
                Log.d(TAG,"The user information is :" + mUser.mUsername + mUser.mAvatar);
                mUserValid = true;
                mUser.saveToLocal(this);
                App app = (App)getApplicationContext();
                app.setIsGuest(false);
            }else{
                Log.d(TAG,"There is no this user" );
                mUserValid =false;
            }
            mIsGettingUserInfo = false;
        }
        if(!mIsGettingUserInfo && !mIsGettingToken && mUserValid && mTokenValid){
            Log.d(TAG,"Login Success!");
            Log.d(TAG,"User is " + mUser.mUsername);
            Log.d(TAG,"AccessToken is " + mToken.mAccessToken);
            // 模拟登陆web系统取得cookies信息
            getCookies(mIdentity,mPassword);
        }

    }



    /*
    * 第三方授权监听，若成功将返回第三方提供的用户信息
    * */
    private UMAuthListener umAuthListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
            //授权开始的回调
            Log.d(TAG,"Authorize start");

        }
        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            showProgress(true);
            // 第三方授权成功 返回用户信息
            Toast.makeText(getApplicationContext(), "Authorize succeed", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Authorize succeed");
            Log.d(TAG,"The platform is " + platform.toSnsPlatform().mKeyword);
            Log.d(TAG,data.get("uid"));
            Log.d(TAG,data.get("name"));
            Log.d(TAG,data.get("gender"));
            Log.d(TAG,data.get("iconurl"));
            Log.d(TAG,data.get("city"));
            Log.d(TAG,data.get("province"));
            Toast.makeText(getApplicationContext(),data.get("name") , Toast.LENGTH_SHORT).show();
            mShareUserInfo = new ShareUserInfo();
            mShareUserInfo.city = data.get("city");
            mShareUserInfo.province = data.get("province");
            mShareUserInfo.uid = data.get("uid");
            mShareUserInfo.name = data.get("name");
            mShareUserInfo.iconurl = data.get("iconurl");
            mShareUserInfo.gender = data.get("gender");

            // 使用第三方提供的用户信息 查找本地该用户关联账号信息
            findAuth(mShareUserInfo,platform.toSnsPlatform().mKeyword);
        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            Toast.makeText( getApplicationContext(), "Authorize fail", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Authorize fail");
            showProgress(false);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {
            Toast.makeText( getApplicationContext(), "Authorize cancel", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Authorize cancel");
            showProgress(false);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"Get the activity result");
        showProgress(true);
    }

    /**
     * 使用用户输入的用户名密码模拟登陆web系统获取cookies
     * 在使用webview时加载cookies自动登录web系统
     */
    private void getCookies(String identity, String password) {
        String loginUrl = "http://www.mmdkid.cn/index.php?r=site/login";
        final App app = (App) getApplicationContext();

        //step 1: 同样的需要创建一个OkHttpClick对象
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .followRedirects(false) // 禁用OkHttp的自动重定向功能 否则cookie信息不完整 没有_identity信息 不能完成自动登录的功能
                .build();

        //step 2: 创建  FormBody.Builder
        FormBody formBody = new FormBody.Builder()
                .add("LoginForm[username]",identity)
                .add("LoginForm[password]",password)
                .add("LoginForm[rememberMe]","1")
                .add("login-button","")
                .build();

        //step 3: 创建请求
        okhttp3.Request request = new okhttp3.Request.Builder().url(loginUrl)
//                .addHeader("User-Agent","Mozilla/5.0 (Linux; Android 6.0; Custom Phone - 6.0.0 - API 23 - 768x1280 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.0.0 Mobile Safari/537.36")
//                .addHeader("cache-control","max-age=0")
//                .addHeader("accept-encoding","gzip, deflate")
//                .addHeader("referer","http://10.0.3.2/index.php?r=site%2Flogin")
//                .addHeader("origin","http://10.0.3.2")
//                .addHeader("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
//                .addHeader("accept-language","zh-CN,zh;q=0.8")
                .post(formBody)
                .build();

        //step 4： 建立联系 创建Call对象
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // TODO: 17-1-4  请求失败
                Log.d(TAG,"Login the web and get the failure response.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                // TODO: 17-1-4 请求成功
                //获得响应头
                Headers headers =  response.headers();//response.headers();
                //遍历响应头信息
                String cookies = "";
                for (int i = 0; i < headers.size(); i++){
                    Log.d(TAG,headers.name(i) + "--->" + headers.value(i));
                }
                Log.d(TAG,response.body().string());
                if ( !headers.values("Set-Cookie").isEmpty()){
                    // 登录成功 获取Response Cookies
                    // 若模拟登录失败 则返回的Response中没有Set-Cookie信息
                    mCookies =  headers.values("Set-Cookie");
                    // 将Cookie保存得到SharedPreference中
                    app.setCookies(mCookies);
                    // 所有登录过程全部成功
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            finish();
                        }
                    });

                }else{
                    Log.d(TAG,"Login the web and get the cookies failed.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                        }
                    });
                }
            }
        });
    }
}

