package com.mmdkid.mmdkid.fragments.register;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.MainActivity;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.models.login.Login;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.mob.MobSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterPhoneFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisterPhoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterPhoneFragment extends Fragment implements
        View.OnClickListener      {
    private static final String TAG = "RegisterPhoneFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText mPhoneEditText;
    private EditText mCodeEditText;
    private EditText mPasswordEditText;
    private Button mGetCodeButton;
    private Button mRegisterButton;

    private boolean boolShowInDialog = true;
    private EventHandler mEventHandler;

    private static final int TIME_MESSAGE=1;
    private int mTimeOut = 60;

    private boolean mBoolPhoneVerified = false;
    //private boolean mIsLogging = false;

    private View mProgressView;
    private View mLoginFormView;

    // Status
    private boolean mTokenValid = false;
    private boolean mIsGettingToken = false;

    // 注册成功后 同步登录到webview的cookies
    private List<String> mCookies;

    public RegisterPhoneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterPhoneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterPhoneFragment newInstance(String param1, String param2) {
        RegisterPhoneFragment fragment = new RegisterPhoneFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        // 通过代码注册你的AppKey和AppSecret
        MobSDK.init(getContext(), "21b4a5d86b0f4", "928bd431888690ccd317dca6e94d36d7");

        // 如果希望在读取通信录的时候提示用户，可以添加下面的代码，并且必须在其他代码调用之前，否则不起作用；如果没这个需求，可以不加这行代码
        //SMSSDK.setAskPermisionOnReadContact(boolShowInDialog);

        // 创建EventHandler对象
        mEventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message message = new Message();
                message.arg1 = event;
                message.arg2 = result;
                message.obj = data;
                mHandler.sendMessage(message);
            }
        };

        // 注册监听器
        SMSSDK.registerEventHandler(mEventHandler);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            if (event == SMSSDK.RESULT_ERROR){
            // 根据服务器返回的网络错误，给toast提示
                try {
                    Throwable throwable = (Throwable) data;
                    throwable.printStackTrace();
                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");//错误描述
                    int status = object.optInt("status");//错误代码
                    if (status > 0 && !TextUtils.isEmpty(des)) {
                        Toast.makeText(getContext(), des, Toast.LENGTH_SHORT).show();
                        showProgress(false);
                        return;
                    }
                } catch (Exception e) {
                    //do something
                }

            }else {
                // 服务器网络验证成功
                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    // 服务器验证码发送成功  处理你自己的逻辑 data为boolean true为智能验证，false为普通下发短信
                    if (data instanceof Throwable) {
                        Throwable throwable = (Throwable) data;
                        throwable.printStackTrace();
                        JSONObject object = null;
                        try {
                            object = new JSONObject(throwable.getMessage());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String des = object.optString("detail");//错误描述
                        int status = object.optInt("status");//错误代码
                        if (status > 0 && !TextUtils.isEmpty(des)) {
                            Toast.makeText(getContext(), des, Toast.LENGTH_SHORT).show();
                            showProgress(false);
                            mGetCodeButton.setClickable(true);
                            return;
                        }
                    }else{
                        boolean smartVerfication = (boolean) data;
                        if (smartVerfication) {
                            Toast.makeText(getContext(), "智能验证", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "验证码已发送", Toast.LENGTH_SHORT).show();
                            mHandlerText.sendEmptyMessageDelayed(TIME_MESSAGE, 1000);
                        }
                    }

                }else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    // 提交验证码成功,验证通过
                    Toast.makeText(getContext(), "验证码校验成功", Toast.LENGTH_SHORT).show();
                    mBoolPhoneVerified = true;
                    // 通过验证的手机号 注册新用户 并登录系统

                    registerPhone(mPhoneEditText.getText().toString(),mPasswordEditText.getText().toString());
                }
            }
        }
    };

    private Handler mHandlerText =new Handler(){
        public void handleMessage(Message msg) {
            if(msg.what==TIME_MESSAGE){
                if(mTimeOut>0){
                    mGetCodeButton.setText("验证码已发送"+mTimeOut+"秒");
                    mTimeOut--;
                    mHandlerText.sendEmptyMessageDelayed(TIME_MESSAGE, 1000);
                }else{
                    mTimeOut = 60;
                    mGetCodeButton.setClickable(true);
                    mGetCodeButton.setText(R.string.prompt_get_verification_code);
                }
            }else{
                mTimeOut = 60;
                mGetCodeButton.setClickable(true);
                mGetCodeButton.setText(R.string.prompt_get_verification_code);
            }
        };
    };
    /**
     * 使用手机号注册新用户
     */
    private void registerPhone(String phone,String password){
        if (!isPhoneValid(phone) || !isPasswordValid(password)){
            Toast.makeText(getContext(), "手机号码或密码错误", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User();
        user.mCellphone = phone;
        user.mPassword = password;
        user.mRole = User.ROLE_PARENT;
        user.save(User.ACTION_SIGNUP_PHONE, getContext(), new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Register a user by the phone has error :"+ error);
                Toast.makeText(getContext(),"注册错误:"+error,Toast.LENGTH_LONG).show();
                showProgress(false);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                App app = (App)getActivity().getApplication();
                if(!responseDataList.isEmpty()){
                    // 创建账号成功
                    User user = (User) responseDataList.get(0);
                    Log.d(TAG,"register a user is successful.");
                    Log.d(TAG,"user's phone is " + user.mCellphone);
                    Log.d(TAG,"user's id is " + user.mId);
                    Log.d(TAG,"user's avatar is " + user.mAvatar);
                    Log.d(TAG,"user's nick name is " + user.mNickname);
                    Toast.makeText(getContext(),"手机注册成功,正在自动登录",Toast.LENGTH_LONG).show();
                    app.setCurrentUser(user);
                    // 使用新创建的手机账号登录系统
                    //attemptGetToken(mPhoneEditText.getText().toString(),mPasswordEditText.getText().toString());
                    Login login = new Login(getContext(), mLoginListener);
                    login.start(mPhoneEditText.getText().toString(),mPasswordEditText.getText().toString());
                }else{
                    Log.d(TAG,"Create a new user by the phone,Get right response , but no user info from the server.");
                }
            }
        });
    }
    /**
     *  通过用户名密码获得访问Token
     *
     */
    private void attemptGetToken(String identity, String password) {
        if(mIsGettingToken){
            Log.d(TAG,"Is getting token return.");
            return;
        }
        Log.d(TAG,"Try to get the token.");
        mIsGettingToken = true;
        Token.find(getContext(), new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                if(c == Token.class){
                    mIsGettingToken =false;
                    Log.d(TAG,"Get the user token error." + error);
                }
                showProgress(false);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                App app = (App)getActivity().getApplication();
                if(c==Token.class){
                    mIsGettingToken =false;
                    Token token = (Token) responseDataList.get(0);
                    Log.d(TAG,"The access token is :" + token.mAccessToken);
                    mTokenValid = true;
                    token.saveToLocal(getContext());
                }
                if(!mIsGettingToken  && mTokenValid){
                    app.setIsGuest(false);
                    // 同步登录到webview取得登录cookies
                    getCookies(mPhoneEditText.getText().toString(),mPasswordEditText.getText().toString());

                }
            }
        }).where("username",identity).where("password",password).all();
    }

    /**
     * 使用用户输入的用户名密码模拟登陆web系统获取cookies
     * 在使用webview时加载cookies自动登录web系统
     */
    private void getCookies(String identity, String password) {
        String loginUrl = "http://www.mmdkid.cn/index.php?r=site/login";
        final App app = (App) getActivity().getApplication();

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
                getActivity().runOnUiThread(new Runnable() {
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            // 结束注册过程 返回主界面
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });

                }else{
                    Log.d(TAG,"Login the web and get the cookies failed.");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"自动登录web失败",Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    });
                }
            }
        });
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
    public void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_register_phone, container, false);
        mPhoneEditText = (EditText) fragmentView.findViewById(R.id.etPhone);
        mCodeEditText = (EditText)fragmentView.findViewById(R.id.etCode);
        mPasswordEditText = (EditText)fragmentView.findViewById(R.id.etPassword);
        mGetCodeButton = (Button) fragmentView.findViewById(R.id.btGetCode);
        mGetCodeButton.setOnClickListener(this);
        mRegisterButton = (Button) fragmentView.findViewById(R.id.btRegister);
        mRegisterButton.setOnClickListener(this);
        mLoginFormView = fragmentView. findViewById(R.id.signup_form);
        mProgressView = fragmentView.findViewById(R.id.signup_progress);
        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btGetCode:
                // 获取短信验证码
                if (isPhoneValid(mPhoneEditText.getText().toString())){
                    // 发送手机号 取得验证码
                    SMSSDK.getVerificationCode("86",mPhoneEditText.getText().toString());
                    mGetCodeButton.setClickable(false);
                }else{
                    Toast.makeText(getContext(), "请输入正确的手机号码", Toast.LENGTH_LONG).show();
                    mPhoneEditText.requestFocus();
                }
                break;
            case R.id.btRegister:
                // 登录测试
                //attemptGetToken("13501227296","123456");
                // 通过短信验证码注册
                boolean cancel = false;
                View focusView = null;
                if (!isPhoneValid(mPhoneEditText.getText().toString())){
                    mPhoneEditText.setError("请输入正确的手机号码");
                    focusView = mPhoneEditText;
                    cancel = true;
                }
                if (!isCodeValid(mCodeEditText.getText().toString())){
                    mCodeEditText.setError("请输入正确的验证码");
                    focusView = mCodeEditText;
                    cancel = true;
                }
                if (!isPasswordValid(mPasswordEditText.getText().toString())){
                    mPasswordEditText.setError("请输入正确的密码");
                    focusView = mPasswordEditText;
                    cancel = true;

                }

                if (cancel){
                    // 手机号码 验证码 密码 有错误
                    focusView.requestFocus();
                }else{
                    // 发送手机号及验证码
                    showProgress(true);
                    SMSSDK.submitVerificationCode("86", mPhoneEditText.getText().toString(),
                            mCodeEditText.getText().toString());
                }

                break;
        }
    }

    private boolean isPhoneValid(String numString){
        return numString.length()==11 && Utility.isNumeric(numString);
    }

    private boolean isCodeValid(String numString){
        return numString.length()==4 && Utility.isNumeric(numString);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private Login.LoginListener mLoginListener = new Login.LoginListener() {
        @Override
        public void onError(Class c, String error) {
            showProgress(false);
            if (c==Token.class)  Log.d(TAG,"Token Error:"+error);
            if (c==User.class)  Log.d(TAG,"User Error:"+error);
            if (c==null)  Log.d(TAG,"Web Error:"+error);
            //mIsLogging = false;
            Toast.makeText(getContext(),error,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess(Object user, Object token, Object cookies) {
            Log.d(TAG,"User cellphone is :"+((User)user).mCellphone);
            Log.d(TAG,"User email is :"+((User)user).mEmail);
            Log.d(TAG,"User name is :"+((User)user).mUsername);
            Log.d(TAG,"Token is:" + ((Token)token).mAccessToken);
            Log.d(TAG,"Cookies is :" + ((List<String>)cookies));
            App app = (App) getActivity().getApplication();
            ((Token)token).saveToLocal(getContext());
            ((User)user).saveToLocal(getContext());
            app.setCookies(((List<String>)cookies));
            app.setIsGuest(false);
            // 所有登录过程全部成功
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(false);
                    getActivity().finish();
                }
            });
        }
    };
}
