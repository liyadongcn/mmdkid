package com.mmdkid.mmdkid.fragments.resetPassword;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.util.ArrayList;

import cn.smssdk.SMSSDK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PhoneNumberFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PhoneNumberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhoneNumberFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "PhoneNumberFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText mPhoneView;
    private Button mNextStepView;

    private boolean mIsVerifying = false; // 正在验证手机号码

    private OnFragmentInteractionListener mListener;

    public PhoneNumberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PhoneNumberFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhoneNumberFragment newInstance(String param1, String param2) {
        PhoneNumberFragment fragment = new PhoneNumberFragment();
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
        View fragmentView = (View)inflater.inflate(R.layout.fragment_phone_number, container, false);
        mPhoneView = (EditText) fragmentView.findViewById(R.id.etPhone);
        mNextStepView = (Button) fragmentView.findViewById(R.id.btNextStep);
        mNextStepView.setOnClickListener(this);
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
            case R.id.btNextStep:
                onNextStepButtonClicked();
                break;
        }
    }

    private void onNextStepButtonClicked() {
        // 是否正在验证
        if(mIsVerifying) return ;
        // 检查输入的手机号码格式
        if(!User.isPhoneValid(mPhoneView.getText().toString())){
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            mPhoneView.requestFocus();
            return ;
        }
        // 开始验证
        mIsVerifying = true;
        // 检查手机号是否已经是注册手机号
        User.verifyPhone(mPhoneView.getText().toString(),getActivity(), new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 验证手机号码出错
                Log.d(TAG,"Network wrong during the verifying or user is not exist.");
                mIsVerifying = false;
                mPhoneView.setError(getString(R.string.error_phone_not_exist));
                mPhoneView.requestFocus();
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                mIsVerifying =false;
                if(c==User.class && !responseDataList.isEmpty()){
                    Log.d(TAG,"Phone is valid");
                    // 验证手机号码正确 返回用户信息
                    // 通知ResetPasswordActivity手机号码OK 可以发送验证码
                   mListener.onPhoneVerified(mPhoneView.getText().toString());
                }
            }
        });

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
        void onPhoneVerified(String phone);

    }
}
