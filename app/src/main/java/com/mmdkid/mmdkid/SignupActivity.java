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
import android.widget.TextView;

import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * A login screen that offers login via email/password.
 */
public class SignupActivity extends AppCompatActivity  implements RESTAPIConnection.OnConnectionListener{

    private static final String TAG = "SignupActivity";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordRepeatView;
    private EditText mUsernameView;
    private View mProgressView;
    private View mLoginFormView;

    // Status
    private boolean mIsRegistering = false;
    private boolean mTokenValid = false;
    private boolean mIsGettingToken = false;

    // 注册成功后 同步登录到webview的cookies
    private List<String> mCookies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set up the sign up form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        mPasswordRepeatView = (EditText) findViewById(R.id.password_repeat);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        mLoginFormView = findViewById(R.id.signup_form);
        mProgressView = findViewById(R.id.signup_progress);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {
        if (mIsRegistering ) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordRepeatView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password_repeat = mPasswordRepeatView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password_repeat) && !isPasswordValid(password_repeat)) {
            mPasswordRepeatView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordRepeatView;
            cancel = true;
        }
        // Check the password and the repeat password equals.
        if(!password.equals(password_repeat)){
            mPasswordRepeatView.setError(getString(R.string.error_not_equal_password));
            focusView = mPasswordRepeatView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user sign up attempt.
            showProgress(true);

            RESTAPIConnection connection = new RESTAPIConnection(this);
            User.signup(connection,username,email,password,password_repeat,User.ROLE_PARENT);

        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 3;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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

        if(c == User.class){
            mIsRegistering = false;
            Log.d(TAG,"Register a new user error " + error);
        }

        if(c == Token.class){
            mIsGettingToken =false;
            Log.d(TAG,"Get the user token error." + error);
        }
        showProgress(false);
    }

    @Override
    public void onResponse(Class c, ArrayList responseDataList) {
        App app = (App)getApplicationContext();
        if(c == User.class){
            mIsRegistering = false;
            if(responseDataList.isEmpty()){
                Log.d(TAG,"Register a new user with no user info.");
            }else{
                User user = (User) responseDataList.get(0);
                Log.d(TAG,"Register a new user success.");
                Log.d(TAG,"User avatar is " + user.mAvatar);
                app.setCurrentUser(user);
                Log.d(TAG,"Try to get the user token.");
                attemptGetToken(mUsernameView.getText().toString(),mPasswordView.getText().toString());
            }
        }
        if(c==Token.class){
            mIsGettingToken =false;
            Token token = (Token) responseDataList.get(0);
            Log.d(TAG,"The access token is :" + token.mAccessToken);
            mTokenValid = true;
            token.saveToLocal(this);
        }
        if(!mIsGettingToken && !mIsRegistering && mTokenValid){
            app.setIsGuest(false);

            // 同步登录到webview取得登录cookies
            getCookies(mUsernameView.getText().toString(),mPasswordView.getText().toString());

        }

    }

    private void attemptGetToken(String identity, String password) {
        if(mIsGettingToken) return;
        mIsGettingToken = true;
        RESTAPIConnection connection = new RESTAPIConnection(this);
        Token.find(connection).where("username",identity).where("password",password).all();
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
                            // 结束注册过程 返回主界面
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
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

