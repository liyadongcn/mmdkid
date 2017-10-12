package com.mmdkid.mmdkid.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.adapters.ModelRecyclerAdapter;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Refresh;
import com.mmdkid.mmdkid.models.VideoSource;
import com.mmdkid.mmdkid.models.YoukuVideo;
import com.mmdkid.mmdkid.server.ElasticConnection;
import com.mmdkid.mmdkid.server.ElasticQuery;
import com.mmdkid.mmdkid.server.QueryBuilder;
import com.mmdkid.mmdkid.server.Sort;
import com.stfalcon.frescoimageviewer.ImageViewer;
import com.youku.cloud.player.PlayerListener;
import com.youku.cloud.player.YoukuPlayerView;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentFragment extends Fragment implements ElasticConnection.OnConnectionListener{
    private static final String TAG = "ContentFragment";

    private Context mContext;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout mRefreshLayout;

    private ProgressDialog mProgressDialog;

    //private ArrayList<Content> mDataset;
    private ArrayList<Model> mDataset;
    private ElasticQuery mQuery;
    private ElasticConnection mConnection;
    private Refresh mRefresh = null;

    private YoukuPlayerView mCurrentYoukuPlayerView;
    private int mCurrentYoukuPlayerPosition;
    private SimpleDraweeView mCurrentYoukuPlayerCoverImage;
    private ImageView mCurrentYoukuPlayerPlayIcon;
    private TextView mCurrentYoukuPlayerTitle;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private boolean mIsCreated = false;

    private boolean mIsFetching = false;

    public ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContentFragment newInstance(String param1, String param2) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public boolean isCreated(){
        return mIsCreated;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsCreated = true;
        mContext = getActivity();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // init the data set
        //mDataset = new ArrayList<Content>();
        mDataset = new ArrayList<Model>();

        attemptToGetContent();

       /* mConnection = new ElasticConnection(mContext,mDataset);

        mQuery = new ElasticQuery(mConnection);*/

        //queryData();
        // show the progress dialog
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

    }

    private void attemptToGetContent() {
        Log.d(TAG,"Try to get the content...");
        if(mIsFetching) return;

        mConnection = new ElasticConnection(this);
        mQuery = (ElasticQuery) Content.find(mConnection);
        //mQuery.SetQueryBuilder(getQueryCondition());
        mQuery.setJsonRequest(getQueryRequest());
        mQuery.all();

        mIsFetching = true;

    }

    private JSONObject getQueryRequest(){
        JSONObject request=null;
        if(mParam2.isEmpty()){
            Sort sort = new Sort();
            sort.add("created_at",Sort.SORT_DESC);
            mQuery.setSort(sort);
        }
        switch(mParam1){
            case Content.TYPE_PUSH:
                Log.d(TAG,"参数2：" + mParam2);
                if(mParam2.isEmpty()){
                    request = QueryBuilder.matchAllQuery();
                    Log.d(TAG,"Request is " + request.toString());
                }else{
                    request = QueryBuilder.multiMatchQuery( mParam2, new String[]{"title", "content"});
//                    qb = queryStringQuery(mParam2);
                    Log.d(TAG,"综合 multiMatchQuery is :" + request.toString());
                }
                break;
            case Content.TYPE_HOT:
                request = QueryBuilder.matchAllQuery();
                break;
            case Content.TYPE_VIDEO:
                if(mParam2.isEmpty()){
                    request = QueryBuilder.termQuery("model_type", Content.TYPE_VIDEO);
                    Log.d(TAG,"Request is " + request.toString());
                }else{
                    request = new QueryBuilder().boolQuery()
                            .must(QueryBuilder.termQuery("model_type", Content.TYPE_VIDEO))
                            .must(QueryBuilder.multiMatchQuery( mParam2,new String[]{"title","content"})).getJSONQuery();
                    Log.d(TAG,"Video boolQuery is :" + request.toString());
                }

                break;
            case Content.TYPE_IMAGE:
                if(mParam2.isEmpty()){
                    request = QueryBuilder.termQuery("model_type", Content.TYPE_IMAGE);
                    Log.d(TAG,"Request is " + request.toString());
                }else{
                    request =   new QueryBuilder().boolQuery()
                            .must(QueryBuilder.termQuery("model_type", Content.TYPE_IMAGE))
                            .must(QueryBuilder.multiMatchQuery( mParam2,new String[]{"title","content"})).getJSONQuery();
                    Log.d(TAG,"Image boolQuery is :" + request.toString());
                }
                break;
            case Content.TYPE_POST:
                if(mParam2.isEmpty()){
                    request = QueryBuilder.termQuery("model_type", Content.TYPE_POST);
                    Log.d(TAG,"Request is " + request.toString());
                }else{
                    request =  new QueryBuilder().boolQuery()
                            .must(QueryBuilder.termQuery("model_type", Content.TYPE_POST))
                            .must(QueryBuilder.multiMatchQuery( mParam2,new String[]{"title","content"})).getJSONQuery();
                    Log.d(TAG,"Post boolQuery is :" + request.toString());
                }
                break;
            default:
                request = QueryBuilder.matchAllQuery();
                Log.d(TAG,"Request is " + request.toString());
                break;
        }
        return request;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        //mAdapter = new ContentRecyclerAdapter(fragmentView.getContext(),mDataset);
        mAdapter = new ModelRecyclerAdapter(fragmentView.getContext(),mDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Swipe refresh listener
        mRefreshLayout = (SwipeRefreshLayout)fragmentView.findViewById(R.id.layout_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            public void onRefresh() {
                // 加载更多数据
                if(mQuery.hasMore()){
                    //mConnection.Query(mQuery);
                    getMore();
                }else {
                    // 提示没有更多数可以加载
                    Toast.makeText(mContext, "no more data.", Toast.LENGTH_LONG).show();
                    mRefreshLayout.setRefreshing(false);
                }

            }
        });

        // RecyclerView item 点击监听
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(mContext, mRecyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Model model = mDataset.get(position);
                if (model instanceof Content){
                    Content content = (Content) model;
                    Intent intent;
                    switch (content.mModelType){
                        case Content.TYPE_POST:
                            //Toast.makeText(mContext,"Click "+mDataset.get(position).mTitle,Toast.LENGTH_SHORT).show();
                            intent = new Intent(mContext,WebViewActivity.class);
                        /*String url = "http://10.0.2.2/index.php?r="+mDataset.get(position).mModelType+"/view&id="+mDataset.get(position).mModelId;
                        Toast.makeText(ContentRecyclerViewActivity.this,url,Toast.LENGTH_LONG).show();*/
                            intent.putExtra("url",content.getContentUrl());
                            intent.putExtra("model",content);
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
                            if (content.getViewType()== Model.VIEW_TYPE_CONTENT_VIDEO_YOUKU ){
                                // 停止非优酷播放
                                JCVideoPlayer.releaseAllVideos();
                                if (mCurrentYoukuPlayerView==null){
                                    // 启动优酷播放
                                    mCurrentYoukuPlayerView = (YoukuPlayerView) view.findViewById(R.id.videoplayer);
                                    mCurrentYoukuPlayerCoverImage = (SimpleDraweeView) view.findViewById(R.id.cvContentImage);
                                    mCurrentYoukuPlayerPlayIcon = (ImageView) view.findViewById(R.id.imagePlay);
                                    mCurrentYoukuPlayerTitle = (TextView) view.findViewById(R.id.tvTitle);
                                    mCurrentYoukuPlayerPosition = position;
                                    String vid = new YoukuVideo(content.mVideo).getVid();
                                    Log.d(TAG,"Youku Video Vid is : " + vid);
                                    mCurrentYoukuPlayerCoverImage.setVisibility(View.GONE);
                                    mCurrentYoukuPlayerPlayIcon.setVisibility(View.GONE);
                                    mCurrentYoukuPlayerTitle.setVisibility(View.GONE);
                                    mCurrentYoukuPlayerView.setPlayerListener(new MyPlayerListener());
                                    mCurrentYoukuPlayerView.setShowBackBtn(false);
                                    mCurrentYoukuPlayerView.playYoukuVideo(vid);
                                }else {
                                    if (position != mCurrentYoukuPlayerPosition){
                                        // 停止原先的播放
                                        mCurrentYoukuPlayerView.release();
                                        mCurrentYoukuPlayerCoverImage.setVisibility(View.VISIBLE);
                                        mCurrentYoukuPlayerPlayIcon.setVisibility(View.VISIBLE);
                                        mCurrentYoukuPlayerTitle.setVisibility(View.VISIBLE);
                                        // 启动当前新播放
                                        mCurrentYoukuPlayerView = (YoukuPlayerView) view.findViewById(R.id.videoplayer);
                                        mCurrentYoukuPlayerCoverImage = (SimpleDraweeView) view.findViewById(R.id.cvContentImage);
                                        mCurrentYoukuPlayerPlayIcon = (ImageView) view.findViewById(R.id.imagePlay);
                                        mCurrentYoukuPlayerTitle = (TextView) view.findViewById(R.id.tvTitle);
                                        mCurrentYoukuPlayerPosition = position;
                                        String vid = new YoukuVideo(content.mVideo).getVid();
                                        Log.d(TAG,"Youku Video Vid is : " + vid);
                                        mCurrentYoukuPlayerCoverImage.setVisibility(View.GONE);
                                        mCurrentYoukuPlayerPlayIcon.setVisibility(View.GONE);
                                        mCurrentYoukuPlayerTitle.setVisibility(View.GONE);
                                        mCurrentYoukuPlayerView.setPlayerListener(new MyPlayerListener());
                                        mCurrentYoukuPlayerView.setShowBackBtn(false);
                                        mCurrentYoukuPlayerView.playYoukuVideo(vid);
                                    }
                                }
                            }else {
                                if (mCurrentYoukuPlayerView!=null){
                                    // 停止原先的优酷播放
                                    mCurrentYoukuPlayerView.release();
                                    mCurrentYoukuPlayerCoverImage.setVisibility(View.VISIBLE);
                                    mCurrentYoukuPlayerPlayIcon.setVisibility(View.VISIBLE);
                                    mCurrentYoukuPlayerTitle.setVisibility(View.VISIBLE);
                                }
                            }
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
                }else if(model instanceof Refresh){
                    // 加载更多数据
                    if(mQuery.hasMore()){
                        //mConnection.Query(mQuery);
                        mRefreshLayout.setRefreshing(true);
                        getMore();
                    }else {
                        // 提示没有更多数可以加载
                        Toast.makeText(mContext, "no more data.", Toast.LENGTH_LONG).show();
                        mRefreshLayout.setRefreshing(false);
                    }
                }
                //Content content = mDataset.get(position);

            }

            @Override
            public void onItemLongClick(View view, int position) {
                //Toast.makeText(mContext,"Click "+mDataset.get(position).mContent,Toast.LENGTH_SHORT).show();
            }
        }));

       /* mConnection.setAdapter(mAdapter);
        mConnection.setSwiftRefreshLayout(mRefreshLayout);*/
        //mConnection.setProgressDialog(mProgressDialog);

        return fragmentView;
    }

    private void getMore() {
        if(mIsFetching) return;
        mQuery.all();
        mIsFetching = true;
    }

    public RecyclerView.Adapter getRecyclerViewAdapter(){
        return mAdapter;
    }

    public void update(String keyWord){
        if(mDataset!=null) mDataset.clear();
        mParam2 = keyWord;
        mQuery.setPageFrom(0);
        //mQuery.SetQueryBuilder(getQueryCondition());
        mQuery.setJsonRequest(getQueryRequest());
        mQuery.all();
        mIsFetching = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCurrentYoukuPlayerView!=null)  mCurrentYoukuPlayerView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        JCVideoPlayer.releaseAllVideos();
        if (mCurrentYoukuPlayerView!=null)  mCurrentYoukuPlayerView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCurrentYoukuPlayerView!=null)  mCurrentYoukuPlayerView.release();
        if (JCVideoPlayer.backPress()) {
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (JCVideoPlayer.backPress()) {
            return;
        }
        if (mCurrentYoukuPlayerView!=null)  mCurrentYoukuPlayerView.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (JCVideoPlayer.backPress()) {
            return;
        }

    }


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
        if(c == Content.class && !responseDataList.isEmpty()){
            Log.d(TAG,"Get the content response from the server.");
            mDataset.addAll(0,responseDataList);
            try {
                setViewType(responseDataList);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            insertRefresh(responseDataList.size());
            mAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(0);
        }
        mProgressDialog.dismiss();
        mRefreshLayout.setRefreshing(false);
    }

    //  插入刷新点，用户点击后自动刷新获得新数据
    private void insertRefresh(int position) {
        if (mRefresh == null){
            mRefresh = new Refresh();
            mRefresh.setViewType(Model.VIEW_TYPE_REFRESH);
        }
        mRefresh.mText = getString(R.string.actionn_load_more);
        if (mDataset.contains(mRefresh)) {
            mDataset.remove(mRefresh);
        }
        mDataset.add(position,mRefresh);
    }

    // 根据content的类型设置每种类型的显示样式
    private void setViewType(ArrayList responseDataList) throws URISyntaxException {
        for (  Object obj :responseDataList ){
            if ( obj instanceof Content){
                Content content = (Content) obj;
                switch (content.mModelType){
                    case Content.TYPE_POST:
                        content.setViewType(Model.VIEW_TYPE_CONTENT_POST_MAIN);
                        break;
                    case Content.TYPE_IMAGE:
                        content.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_POST_MAIN);
                        break;
                    case Content.TYPE_VIDEO:
                        if (VideoSource.getSourceName(content.mVideo)!=null &&
                                VideoSource.getSourceName(content.mVideo).equals(VideoSource.VIDEO_SOURCE_YOUKU)){
                            content.setViewType(Model.VIEW_TYPE_CONTENT_VIDEO_YOUKU);
                        }else {
                            content.setViewType(Model.VIEW_TYPE_CONTENT_VIDEO_MAIN);
                        }
                        break;
                }
            }
        }
    }

    private class MyPlayerListener extends PlayerListener {

    }
}
