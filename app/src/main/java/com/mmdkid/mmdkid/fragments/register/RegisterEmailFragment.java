package com.mmdkid.mmdkid.fragments.register;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.models.login.Login;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterEmailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisterEmailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterEmailFragment extends Fragment{
    private final String TAG = "RegisterEmailFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    //private EditText mPasswordRepeatView;
    //private EditText mUsernameView;
    private View mProgressView;
    private View mLoginFormView;

    // Status
    private boolean mIsRegistering = false;
    private boolean mTokenValid = false;
    private boolean mIsGettingToken = false;

    // 注册成功后 同步登录到webview的cookies
    private List<String> mCookies;

    public RegisterEmailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterEmailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterEmailFragment newInstance(String param1, String param2) {
        RegisterEmailFragment fragment = new RegisterEmailFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_register_email, container, false);
        // Set up the sign up form.
        //mUsernameView = (EditText) fragmentView.findViewById(R.id.username);
        mEmailView = (AutoCompleteTextView) fragmentView.findViewById(R.id.email);

        mPasswordView = (EditText) fragmentView.findViewById(R.id.password);
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

        //mPasswordRepeatView = (EditText) fragmentView.findViewById(R.id.password_repeat);

        Button mEmailSignInButton = (Button) fragmentView.findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        mLoginFormView = fragmentView.findViewById(R.id.signup_form);
        mProgressView = fragmentView.findViewById(R.id.signup_progress);
        return fragmentView;
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
        //mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        //mPasswordRepeatView.setError(null);

        // Store values at the time of the login attempt.
        //String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        //String password_repeat = mPasswordRepeatView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

       /* if (!TextUtils.isEmpty(password_repeat) && !isPasswordValid(password_repeat)) {
            mPasswordRepeatView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordRepeatView;
            cancel = true;
        }
        // Check the password and the repeat password equals.
        if(!password.equals(password_repeat)){
            mPasswordRepeatView.setError(getString(R.string.error_not_equal_password));
            focusView = mPasswordRepeatView;
            cancel = true;
        }*/

        // Check for a valid username.
       /* if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }*/

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

           registerEmail(email,password);

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
     * 使用邮箱注册新用户
     */
    private void registerEmail(String email,String password){
        if (!isEmailValid(email) || !isPasswordValid(password)){
            Toast.makeText(getContext(), "邮箱或密码错误", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User();
        user.mEmail = email;
        user.mPassword = password;
        user.mRole = User.ROLE_PARENT;
        user.save(User.ACTION_SIGNUP_EMAIL, getContext(), new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Register a user by the email has error :"+ error);
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
                    Log.d(TAG,"user's email is " + user.mEmail);
                    Log.d(TAG,"user's id is " + user.mId);
                    Log.d(TAG,"user's avatar is " + user.mAvatar);
                    Log.d(TAG,"user's nick name is " + user.mNickname);
                    Toast.makeText(getContext(),"邮箱注册成功,正在自动登录",Toast.LENGTH_LONG).show();
                    app.setCurrentUser(user);
                    // 使用新创建的手机账号登录系统
                    //attemptGetToken(mEmailView.getText().toString(),mPasswordView.getText().toString());
                    Login login = new Login(getContext(), mLoginListener);
                    login.start(mEmailView.getText().toString(),mPasswordView.getText().toString());
                }else{
                    Log.d(TAG,"Register a new user by the email,Get right response , but no user info from the server.");
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

     /**
     *  通过用户名密码获得访问Token
     *
     */
   /* private void attemptGetToken(String identity, String password) {
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
                    getCookies(mEmailView.getText().toString(),mPasswordView.getText().toString());

                }
            }
        }).where("username",identity).where("password",password).all();
    }
*/
    /**
     * 使用用户输入的用户名密码模拟登陆web系统获取cookies
     * 在使用webview时加载cookies自动登录web系统
     */
   /* private void getCookies(String identity, String password) {
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
//                .addHeader("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,///;q=0.8")
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
                            showProgress(false);
                        }
                    });
                }
            }
        });
    }*/

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
