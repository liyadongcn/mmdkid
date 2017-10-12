package com.mmdkid.mmdkid.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.DiaryListActivity;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.UserGroupPostListActivity;
import com.mmdkid.mmdkid.adapters.ModelRecyclerAdapter;
import com.mmdkid.mmdkid.models.Diary;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Student;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.models.UserGroup;
import com.mmdkid.mmdkid.models.UserGroupMap;
import com.mmdkid.mmdkid.models.UserRelationship;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TodayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TodayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodayFragment extends Fragment {
    private static final String TAG = "TodayFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Context mContext;

    private ProgressDialog mProgressDialog;

    private ArrayList<Model> mDataset;
    private Query mQuery;
    private RESTAPIConnection mConnection;

    private User mCurrentUser;
    private Token mCurrentToken;
    private ArrayList<Student> mCurrentUserChildren;

    private boolean mIsFetching = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TodayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TodayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TodayFragment newInstance(String param1, String param2) {
        TodayFragment fragment = new TodayFragment();
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
        mDataset = new ArrayList<Model>();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // show the progress dialog
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading...");

        if(mIsFetching) {
            mProgressDialog.show();
        }

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_today, container, false);
        // use the recycler view
        mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.rvContent);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(fragmentView.getContext());
        //mLayoutManager = new GridLayoutManager(this,2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ModelRecyclerAdapter(fragmentView.getContext(),mDataset);
        mRecyclerView.setAdapter(mAdapter);

        // RecyclerView item 点击监听
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(mContext, mRecyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Model model = mDataset.get(position);
                if(model instanceof Student){
                    Intent intent = new Intent(mContext, DiaryListActivity.class);
                    intent.putExtra("model",model);
                    startActivity(intent);
                }
                if(model instanceof UserGroup){
                    Intent intent = new Intent(mContext, UserGroupPostListActivity.class);
                    intent.putExtra("model",model);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //Toast.makeText(mContext,"Click "+mDataset.get(position).mContent,Toast.LENGTH_SHORT).show();
            }
        }));

        initData();

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume.....");

    }

    private void initData() {
        mDataset.clear();
        App app = (App)mContext.getApplicationContext();
        if (!app.isGuest()) {
            mCurrentUser = app.getCurrentUser();
            mCurrentToken = app.getCurrentToken();
            mCurrentUserChildren = new ArrayList<Student>();

            // 获取当前用户的所有kid
            mQuery = UserRelationship.find(mContext, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    Log.d(TAG,"Get the error response from the server");
                    mIsFetching = false;
                    mProgressDialog.dismiss();

                }

                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    Log.d(TAG,"Get correct response from the server.");
                    mIsFetching = false;
                    if(c == UserRelationship.class && !responseDataList.isEmpty()){
                        Log.d(TAG,"Get the content response from the server.");
                        for(Object obj : responseDataList){
                            UserRelationship userRelationship = (UserRelationship) obj;
                            Student model;
                            if(userRelationship.mChild!=null) {
                                model = (Student) userRelationship.mChild;
                                model.mParentId = mCurrentUser.mId;
                                model.mRelationship = userRelationship.mRelationship;
                                model.setViewType(Model.VIEW_TYPE_USER);
                                mCurrentUserChildren.add(model);
                                Log.d(TAG,"Get one child named " + model.mRealname + " " + model.mNickname );
                                Log.d(TAG,"Get one child avatar " + model.mAvatar  );
                                getChildTodayDiary(model);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    mProgressDialog.dismiss();

                }
            }).where("user1_id",Integer.toString(mCurrentUser.mId))
                    //.where("relationship",Integer.toString(UserRelationship.USER_RELATIONSHIP_MOTHER))
                    .where("status",Integer.toString(UserRelationship.STATUS_ACTIVE));
            mQuery.all();
            mIsFetching = true;

            // 获取当前用户所有的朋友圈及班级
            UserGroupMap.find(mContext, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    Log.d(TAG,"Get the error response for the user groups from the server");
                    mIsFetching = false;
                    mProgressDialog.dismiss();
                }

                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    if(c == UserGroupMap.class && !responseDataList.isEmpty()){
                        Log.d(TAG,"Get the user groups response from the server.");
                        for(Object obj : responseDataList){
                            UserGroupMap userGroupMap = (UserGroupMap) obj;
                            UserGroup userGroup;
                            if(userGroupMap.mModelGroup!=null) {
                                userGroup = (UserGroup) userGroupMap.mModelGroup;
                                userGroup.setViewType(Model.VIEW_TYPE_USER_GROUP);
                                Log.d(TAG,"Get one user group named " + userGroup.mName  );
                                mDataset.add(userGroup);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    mProgressDialog.dismiss();
                }
            }).where("expand","model_user_group")
                    .where("user_id",Integer.toString(mCurrentUser.mId))
                    .where("status",Integer.toString(UserGroupMap.STATUS_ACTIVE))
                    .all();
        }

    }

    private void getChildTodayDiary(final Student child) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateNowStr = sdf.format(d);

        Diary.find(mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Get diary error. ");
            }
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                Log.d(TAG,"Get correct response from the server.");
                if(c== Diary.class && !responseDataList.isEmpty()){
                    for(Object obj : responseDataList){
                        Diary diary = (Diary) obj;
                        diary.setViewType(Model.VIEW_TYPE_DIARY);
                        mDataset.add(diary);
                        Log.d(TAG,"Get the diary of student " + diary.mStudent.mRealname);
                        Log.d(TAG,"The student avatar is " + diary.mStudent.mAvatar);
                        Log.d(TAG,"The content is " + diary.mContent);
                    }
                }else{
                    Log.d(TAG,"The student " + child.mRealname + " no today diary.");
                    mDataset.add(child);
                }
                mAdapter.notifyDataSetChanged();
            }
        }).where("user_id",Integer.toString(child.mId))
            .where("date",dateNowStr).where("expand","image,student").all();
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
