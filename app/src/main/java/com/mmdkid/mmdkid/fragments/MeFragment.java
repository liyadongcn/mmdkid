package com.mmdkid.mmdkid.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.FollowActivity;
import com.mmdkid.mmdkid.HistoryActivity;
import com.mmdkid.mmdkid.LoginActivity;
import com.mmdkid.mmdkid.PublishManageActivity;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.SettingsActivity;
import com.mmdkid.mmdkid.StarActivity;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.models.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "MeFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context mContext;

    private User mCurrentUser;

    private OnFragmentInteractionListener mListener;

    private CardView mSettings;
    private SimpleDraweeView mAvatar;
    private TextView mUsername;
    private TextView mFollower;
    private TextView mFollowing;
    private LinearLayout mStar;
    private LinearLayout mFollow;
    private LinearLayout mHistory;

    private LinearLayout mPublishManage;

    private boolean isUpdatingUserInfo = false;

    public MeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MeFragment newInstance(String param1, String param2) {
        MeFragment fragment = new MeFragment();
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
        mContext = getActivity();

        mCurrentUser = User.loadFromLocal(mContext);
        if(mCurrentUser!=null){
            Log.d(TAG,"Current user is " + mCurrentUser.mUsername);
        }else {
            Log.d(TAG,"There is no user yet! Please go to login. ");
            /*Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);*/
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mSettings = (CardView) view.findViewById(R.id.cvSettings);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SettingsActivity.class);
                startActivity(intent);
            }
        });

        mStar = (LinearLayout) view.findViewById(R.id.llStar);
        mStar.setOnClickListener(this);

        mAvatar = (SimpleDraweeView) view.findViewById(R.id.sdvAvatar);
        mUsername = (TextView) view.findViewById(R.id.tvUsername);
        mFollower = (TextView) view.findViewById(R.id.tvFollower);
        mFollowing = (TextView) view.findViewById(R.id.tvFollowing);

        mFollow = (LinearLayout) view.findViewById(R.id.llFollow);
        mFollow.setOnClickListener(this);

        mHistory = (LinearLayout) view.findViewById(R.id.llHistory);
        mHistory.setOnClickListener(this);

        mPublishManage = (LinearLayout) view.findViewById(R.id.llPublishManage);
        mPublishManage.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"MeFrament is onResume.");
        App app = (App) mContext.getApplicationContext();
        if(!app.isGuest()){
            // 用户已登录
            if (isUpdatingUserInfo){
                app.refreshCurrentUserInfo();
                isUpdatingUserInfo =false;
                // 清除Fresco的缓存
                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                imagePipeline.evictFromMemoryCache(Uri.parse(mCurrentUser.mAvatar));
                imagePipeline.evictFromDiskCache(Uri.parse(mCurrentUser.mAvatar));
                // combines above two lines
                imagePipeline.evictFromCache(Uri.parse(mCurrentUser.mAvatar));
            }
            mCurrentUser = app.getCurrentUser();
            mAvatar.setImageURI(mCurrentUser.mAvatar);
            mAvatar.setOnClickListener(this);
            mUsername.setText(mCurrentUser.getDisplayName());
            mUsername.setOnClickListener(null);
            mFollower.setText(String.valueOf(mCurrentUser.mFollower));
            mFollowing.setText(String.valueOf(mCurrentUser.mFollowing));
            Log.d(TAG,"User display name is " + mCurrentUser.getDisplayName());
            Log.d(TAG,"User nicke name is " + mCurrentUser.mNickname);
            Log.d(TAG,"User user name is " + mCurrentUser.mUsername);
            Log.d(TAG,"User avatar is " + mCurrentUser.mAvatar);
        }else {
            // 用户未登录
            Log.d(TAG,"There is no user yet! Please go to login. ");
            mUsername.setText("请点击登录");
            mUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
            });
            mAvatar.setImageResource(R.drawable.account_off);
            mAvatar.setOnClickListener(null);
            mFollower.setText("");
            mFollowing.setText("");
        }

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
        App app = (App) mContext.getApplicationContext();
        if(app.isGuest()){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            return;
        }
        Intent intent;
        switch (view.getId()){
            case R.id.llStar:
                intent = new Intent(mContext, StarActivity.class);
                startActivity(intent);
                break;
            case R.id.llHistory:
                intent = new Intent(mContext, HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.llFollow:
                intent = new Intent(mContext, FollowActivity.class);
                startActivity(intent);
                break;
            case R.id.sdvAvatar:
                isUpdatingUserInfo = true;
                intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra("url","http://www.mmdkid.cn/index.php?r=user/update&theme=app&id="+mCurrentUser.mId);
                startActivity(intent);
                break;
            case R.id.llPublishManage:
                intent = new Intent(mContext, PublishManageActivity.class);
                startActivity(intent);
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
    }

}
