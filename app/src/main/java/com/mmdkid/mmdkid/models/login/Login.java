package com.mmdkid.mmdkid.models.login;

import android.content.Context;
import android.util.Log;

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
 * Created by LIYADONG on 2017/11/17.
 */

public class Login {
    private static final String TAG = "Login";

    private User mUser;
    private Token mToken;
    private List<String> mCookies;
    private String mIdentity;
    private String mPassword;
    private LoginListener mListener;
    private Context mContext;
    private boolean mIsLoginWeb = true;

    public Login(Context context, LoginListener listener){
        mContext = context;
        mListener = listener;
    }

    public void setLoginWeb(boolean loginWeb){
        mIsLoginWeb = loginWeb;
    }

    /**
     *  通过用户唯一标识（手机、email、用户名）密码登录
     *  适用于首次登录 或注册后首次登录
     */
    public void start(String identity, String password){
        if (identity.isEmpty() || password.isEmpty()){
            if (mListener!=null){
                mListener.onError(null,"用户识别码或秘密不能为空");
                return;
            }
        }
        mIdentity = identity;
        mPassword = password;
        getToken(mIdentity,mPassword);
    }

    /**
     *  通过用户访问token登录 主要用于用户再次访问时自动登录
     */
    public void  startWithToken(String identity,Token token){
        mIdentity = identity;
        mToken = token;
        getUser(mIdentity,token.mAccessToken);
    }

    /**
     *  通过用户唯一是识别码（手机号、邮箱或者是用户名）及密码获得访问Token
     *
     */
    private void getToken(final String identity, String password) {
        Log.d(TAG,"Try to get the token.");
        Token.find(mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                if(c == Token.class){
                    Log.d(TAG,"Get the user token error." + error);
                    if (mListener!=null) mListener.onError(c,error);
                }
            }
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if(c==Token.class){
                    // 获取访问Token成功
                    Token token = (Token) responseDataList.get(0);
                    Log.d(TAG,"The access token is :" + token.mAccessToken);
                    mToken = token;
                    // 使用Token获取用户信息
                    getUser(identity,mToken.mAccessToken);
                }
            }
        }).where("username",identity).where("password",password).all();
    }

    /*
  *  通过有效的Token获得登录用户的信息
  * */
    private void getUser(final String identity, String accessToken) {
        RESTAPIConnection connection = new RESTAPIConnection(mContext);
        connection.ACCESS_TOKEN = accessToken;
        User.find(mContext, accessToken,new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                if(c == User.class){
                    Log.d(TAG,"Get the user infromation error." + error);
                    if (mListener!=null) mListener.onError(c,error);
                }
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if(c==User.class){
                    // 获取用户信息成功
                    User user = (User) responseDataList.get(0);
                    Log.d(TAG,"The user cellphone is :" + user.mCellphone);
                    Log.d(TAG,"The user email is :" + user.mEmail);
                    Log.d(TAG,"The user signature is :" + user.mSignature);
                    mUser = user;
                    // 模拟登录web
                    if (mIsLoginWeb){
                        // 需要登录web
                        getCookies(identity,mPassword);
                    }else{
                        // 不需要登录web 整个登录过程结束 目前只用于MainActivity的自动登录过程
                        // token只要不过期就认为web登录未过期cookies有效，因为token的有效期短于
                        // cookies的有效期
                        if (mListener!=null) mListener.onSuccess(mUser,mToken,null);
                    }

                }
            }
        }).where("user_name",identity).all();
    }

    /**
     * 使用用户输入的用户名密码模拟登陆web系统获取cookies
     * 在使用webview时加载cookies自动登录web系统
     */
    private void getCookies(String identity, String password) {
        String loginUrl = "http://www.mmdkid.cn/index.php?r=site/login";

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
                if (mListener!=null) mListener.onError(null,"登录浏览器网络连接失败");
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
                    // 整个登录过程成功
                    if (mListener!=null) mListener.onSuccess(mUser,mToken,mCookies);
                }else{
                    Log.d(TAG,"Login the web and get the cookies failed.");
                    if (mListener!=null) mListener.onError(null,"登录浏览器失败");
                }
            }
        });
    }


    public interface LoginListener {
        void onError(Class c, String error);
        void onSuccess(Object user, Object token,Object cookies);
    }
}
