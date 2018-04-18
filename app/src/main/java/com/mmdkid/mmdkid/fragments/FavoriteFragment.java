package com.mmdkid.mmdkid.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.adapters.ModelRecyclerAdapter;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Notification;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FavoriteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteFragment extends Fragment {
    private static final String TAG = "FavoriteFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout mRefreshLayout;

    private ProgressDialog mProgressDialog;

    private ArrayList<Model> mDataset;
    private Query mQuery;
    private RESTAPIConnection mConnection;

    private User mCurrentUser;
    private Token mCurrentToken;

    private boolean mIsFetching = false;

    private Context mContext;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate.....");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mContext = getActivity();
        mDataset = new ArrayList<Model>();
    }

    private void initData() {
        mDataset.clear();
        App app = (App)mContext.getApplicationContext();
        if (!app.isGuest()) {
            mCurrentUser = app.getCurrentUser();
            mCurrentToken = app.getCurrentToken();

            mQuery = Notification.find(mContext, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    Log.d(TAG,"Get the error response from the server");
                    mIsFetching = false;
                    mProgressDialog.dismiss();
                    mRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    Log.d(TAG,"Get correct response from the server.");
                    mIsFetching = false;
                    if(c == Notification.class && !responseDataList.isEmpty()){
                        Log.d(TAG,"Get the content response from the server.");
                        for(Object obj : responseDataList){
                            Notification notification = (Notification) obj;
                            if(notification.mModel!=null) mDataset.add(0,(Content) notification.mModel);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    mProgressDialog.dismiss();
                    mRefreshLayout.setRefreshing(false);
                }
            }).where("receiver",Integer.toString(mCurrentUser.mId));
            mQuery.all();
            mIsFetching = true;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume.....");
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView.....");
        // show the progress dialog
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading...");

        if(mIsFetching) {
            mProgressDialog.show();
        }

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_content, container, false);
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

        // Swipe refresh listener
        mRefreshLayout = (SwipeRefreshLayout)fragmentView.findViewById(R.id.layout_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            public void onRefresh() {
                // 加载更多数据
                if(mQuery.hasMore()){
                    //mConnection.Query(mQuery);
                    mQuery.all();
                }else {
                    // 提示没有更多数可以加载
                    Toast.makeText(mContext, getString(R.string.no_more_data), Toast.LENGTH_LONG).show();
                    mRefreshLayout.setRefreshing(false);
                }

            }
        });

        // RecyclerView item 点击监听
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(mContext, mRecyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Content content = (Content) mDataset.get(position);
                Intent intent;
                switch (content.mModelType){
                    case Content.TYPE_POST:
                        //Toast.makeText(mContext,"Click "+mDataset.get(position).mTitle,Toast.LENGTH_SHORT).show();
                        intent = new Intent(mContext,WebViewActivity.class);
                        /*String url = "http://10.0.2.2/index.php?r="+mDataset.get(position).mModelType+"/view&id="+mDataset.get(position).mModelId;
                        Toast.makeText(ContentRecyclerViewActivity.this,url,Toast.LENGTH_LONG).show();*/
                        intent.putExtra("url",content.getContentUrl());
                        Log.d(TAG,content.getContentUrl());
                        String htmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+ "<html><body><h3>"+content.mTitle+"</h3>"+content.mContent+"</body></html>";
                        intent.putExtra("htmlData",htmlData);
                        Log.d(TAG,htmlData);
                        startActivity(intent);
                        break;
                    case Content.TYPE_VIDEO:
                        /*intent = new Intent(mContext,VideoActivity.class);
                        intent.putExtra(VideoActivity.VIDEO_TITLE,content.mTitle);
                        intent.putExtra(VideoActivity.VIDEO_POSTER,content.mImage);
                        intent.putExtra(VideoActivity.VIDEO_URL,"http://mmdkid.cn/uploads/media/1466482403260.mp4");
                        intent.putExtra(VideoActivity.VIDEO_URL,content.mVideo);
                        startActivity(intent);*/
                        /*intent = new Intent(mContext,WebViewActivity.class);
                        intent.putExtra("url",content.getContentUrl());
                        Log.d(LOG_TAG,content.getContentUrl());
                        startActivity(intent);*/

                        break;
                    case Content.TYPE_IMAGE:
                        Log.d(TAG,content.mImageList.toString());
                        /*intent = new Intent(mContext,ImageActivity.class);
                        intent.putStringArrayListExtra(ImageActivity.IMAGE_LIST,content.mImageList);
                        startActivity(intent);*/
                        new ImageViewer.Builder<>(mContext, content.mImageList)
                                .setStartPosition(0)
                                .show();
                        break;
                    default:
                        Toast.makeText(mContext,"Can not show this type content",Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                //Toast.makeText(mContext,"Click "+mDataset.get(position).mContent,Toast.LENGTH_SHORT).show();
            }
        }));

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
        Log.d(TAG,"onAttach.....");
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
        Log.d(TAG,"onDetach.....");
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart.....");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop.....");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause.....");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG,"onDestroyView.....");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy.....");
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
