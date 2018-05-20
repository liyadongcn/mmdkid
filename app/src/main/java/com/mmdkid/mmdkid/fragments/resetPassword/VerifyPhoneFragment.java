package com.mmdkid.mmdkid.fragments.resetPassword;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
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

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.mob.MobSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VerifyPhoneFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VerifyPhoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerifyPhoneFragment extends Fragment implements View.OnClickListener{
    private final static String TAG = "VerifyPhoneFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "phone";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mPhone;
    private String mParam2;

    private EventHandler mEventHandler;
    private static final int TIME_MESSAGE=1;
    private int mTimeOut = 60;

    private EditText mCodeEditText;
    private EditText mPasswordEditText;
    private Button mGetCodeButton;
    private Button mResetButton;
    private View mProgressView;
    private View mResetFormView;

    private OnFragmentInteractionListener mListener;

    public VerifyPhoneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VerifyPhoneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VerifyPhoneFragment newInstance(String param1, String param2) {
        VerifyPhoneFragment fragment = new VerifyPhoneFragment();
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
            mPhone = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // 通过代码注册你的AppKey和AppSecret
        MobSDK.init(getContext(), "21b4a5d86b0f4", "928bd431888690ccd317dca6e94d36d7");
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
                    // 通过验证的手机号 修改该用户的用户密码
                    resetPassword();
                }
            }
        }
    };

    private void resetPassword() {
        // 手机号已经通过短信验证
        // 密码格式也已经验证
        // 通过手机号及新密码修改数据库密码
        User.resetPasswordByPhone(mPhone, mPasswordEditText.getText().toString(), getActivity(), new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                showProgress(false);
                Log.d(TAG,"Network error or change password error>>>"+error);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c==User.class && !responseDataList.isEmpty()){
                    Log.d(TAG,"Change the password by phone success.");
                    showProgress(false);
                    if(mListener!=null) mListener.onResetPasswordSuccess();
                }
            }
        });
    }

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
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mResetFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mResetFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mResetFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mResetFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_verify_phone, container, false);
        mCodeEditText = (EditText)fragmentView.findViewById(R.id.etCode);
        mPasswordEditText = (EditText)fragmentView.findViewById(R.id.etPassword);
        mGetCodeButton = (Button) fragmentView.findViewById(R.id.btGetCode);
        mGetCodeButton.setOnClickListener(this);
        mResetButton = (Button) fragmentView.findViewById(R.id.btReset);
        mResetButton.setOnClickListener(this);
        mResetFormView = fragmentView. findViewById(R.id.llResetForm);
        mProgressView = fragmentView.findViewById(R.id.reset_progress);
        // 发送手机号 取得验证码
        if (mPhone!=null && !mPhone.isEmpty()){
            SMSSDK.getVerificationCode("86",mPhone);
            mGetCodeButton.setClickable(false);
        }
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
                if (mPhone!=null && !mPhone.isEmpty()){
                    // 发送手机号 取得验证码
                    SMSSDK.getVerificationCode("86",mPhone);
                    mGetCodeButton.setClickable(false);
                }else{
                    Toast.makeText(getContext(), "没有手机号码", Toast.LENGTH_LONG).show();
                    mCodeEditText.requestFocus();
                }
                break;
            case R.id.btReset:

                // 通过短信验证码注册
                boolean cancel = false;
                View focusView = null;

                if (!User.isCodeValid(mCodeEditText.getText().toString())){
                    mCodeEditText.setError("请输入正确的验证码");
                    focusView = mCodeEditText;
                    cancel = true;
                }
                if (!User.isPasswordValid(mPasswordEditText.getText().toString())){
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
                    SMSSDK.submitVerificationCode("86", mPhone,
                            mCodeEditText.getText().toString());
                }

                break;
        }
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
        void onResetPasswordSuccess();
    }
}
