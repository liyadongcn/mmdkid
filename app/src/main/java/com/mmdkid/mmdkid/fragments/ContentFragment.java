package com.mmdkid.mmdkid.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.adapters.ModelRecyclerAdapter;
import com.mmdkid.mmdkid.imagepost.ImageOverlayView;
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
import com.youku.cloud.player.VideoDefinition;
import com.youku.cloud.player.YoukuPlayerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.JZMediaManager;
import cn.jzvd.JZUtils;
import cn.jzvd.JZVideoPlayer;



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

    private JZVideoPlayer mCurrentJZVideoPlayer;
    private LinearLayout mCurrentJZPlayerShareToView;
    private TextView mCurrentJZPlayerDescriptionView;

    private YoukuPlayerView mCurrentYoukuPlayerView;
//    private int mCurrentYoukuPlayerPosition;
    private SimpleDraweeView mCurrentYoukuPlayerCoverImage;
    private ImageView mCurrentYoukuPlayerPlayIcon;
    private TextView mCurrentYoukuPlayerTitle;
    private LinearLayout mCurrentYoukuPlayerShareToView;
    private TextView mCurrentYoukuPlayerDescriptionView;

    private boolean mIsPlayingVideo = false;
    private int mVideoPlayingPosition;

    private List<String> mImagePostList; // 当前显示的图片列表
    private String mImageDescription;   // 当前图片的描述
    private ImageOverlayView mOverlayView; // 叠加在图片上的视图


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2="";

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
        mProgressDialog.setMessage(getString(R.string.loading));
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



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
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
        //mAdapter = new ContentRecyclerAdapterDel(fragmentView.getContext(),mDataset);
        mAdapter = new ModelRecyclerAdapter(fragmentView.getContext(),mDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Swipe refresh listener
        mRefreshLayout = (SwipeRefreshLayout)fragmentView.findViewById(R.id.layout_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            public void onRefresh() {
                stopVideoPlayer();
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
                Log.d(TAG,"Click Position is :" + position);
                Model model = mDataset.get(position);
                if (model instanceof Content){
                    Content content = (Content) model;
                    Intent intent;
                    switch (content.mModelType){
                        case Content.TYPE_POST:
                            stopVideoPlayer();
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
                            if (mIsPlayingVideo){
                                // 正在播放视频
                                if (mVideoPlayingPosition != position){
                                    stopVideoPlayer();
                                    if (content.getViewType()== Model.VIEW_TYPE_CONTENT_VIDEO_YOUKU){
                                        // 启动优酷播放
                                        startYoukuPlayer(view,YoukuVideo.getVid(content));
                                    }else {
                                        // 启动JiaoZiVideoPlayer
                                        //mCurrentJZVideoPlayer = view.findViewById(R.id.videoplayer_jiaozi);
                                        startJZVideoPlayer(view);
                                    }
                                    mIsPlayingVideo = true;
                                    mVideoPlayingPosition = position;
                                }
                            }else{
                                // 没有当前视频播放
                                if (content.getViewType()== Model.VIEW_TYPE_CONTENT_VIDEO_YOUKU){
                                    // 启动优酷播放
                                    startYoukuPlayer(view,YoukuVideo.getVid(content));
                                }else {
                                    // 正在使用JiaoZiVideoPlayer
                                    //mCurrentJZVideoPlayer = view.findViewById(R.id.videoplayer_jiaozi);
                                    startJZVideoPlayer(view);
                                }
                                mIsPlayingVideo = true;
                                mVideoPlayingPosition = position;
                            }
                            break;
                        case Content.TYPE_IMAGE:
                            stopVideoPlayer();
                            Log.d(TAG,content.mImageList.toString());
                           /* intent = new Intent(mContext,ImageActivity.class);
                            intent.putExtra(ImageActivity.CONTENT,content);
                            startActivity(intent);*/
                            AnimationDrawable animationDrawable = new AnimationDrawable();
                            Drawable drawable = getResources().getDrawable(R.drawable.loading);
                            if(drawable != null){
                                animationDrawable.addFrame(drawable,100);
                                animationDrawable.setOneShot(false);
                            }
                            GenericDraweeHierarchyBuilder draweeHierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(getResources())
                                    //.setFailureImage(R.drawable.failureDrawable)
                                   // .setProgressBarImage(R.drawable.spinner_gif);
                                    .setProgressBarImage(animationDrawable);
                                    //.setProgressBarImage(new ProgressBarDrawable());

                            //.setPlaceholderImage(R.drawable.placeholderDrawable);
                            mOverlayView = new ImageOverlayView(mContext,content);
                            mImagePostList = content.mImageList;
                            mImageDescription = content.mContent;
                            new ImageViewer.Builder<>(mContext, content.mImageList)
                                    .setStartPosition(0)
                                    .setImageMargin(getContext(),R.dimen.image_margin)
                                    .setImageChangeListener(getImageChangeListener())
                                    .setCustomDraweeHierarchyBuilder(draweeHierarchyBuilder)
                                    .setOverlayView(mOverlayView)
                                    .show();
                            break;
                        default:
                            Toast.makeText(mContext,"Can not show this type content",Toast.LENGTH_SHORT).show();
                            break;
                    }
                }else if(model instanceof Refresh){
                    // 停止当前所有播放
                    stopVideoPlayer();
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
        // 监听视频移入移出可视窗口 若移出窗口就停止播放
        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                int postion = mRecyclerView.getChildLayoutPosition(view);
                if (mIsPlayingVideo && postion == mVideoPlayingPosition){
                    //if (view.getTag()!=null && view.getTag().equals(VideoSource.VIDEO_SOURCE_YOUKU)){
                        // 当优酷视频移出可视时 停止播放
                        /*YoukuPlayerView youkuPlayerView = (YoukuPlayerView) view.findViewById(R.id.videoplayer);
                        SimpleDraweeView youkuPlayerCoverImage = (SimpleDraweeView) view.findViewById(R.id.cvContentImage);
                        ImageView youkuPlayerPlayIcon = (ImageView) view.findViewById(R.id.imagePlay);
                        TextView youkuPlayerTitle = (TextView) view.findViewById(R.id.tvTitle);

                        youkuPlayerView.release();
                        youkuPlayerCoverImage.setVisibility(View.VISIBLE);
                        youkuPlayerPlayIcon.setVisibility(View.VISIBLE);
                        youkuPlayerTitle.setVisibility(View.VISIBLE);
                        mIsPlayingVideo = false;*/
//                        stopVideoPlayer();
//                    }
                    // 停止当前的jiaozivideo播放 已经移出可视区域
                    /*JZVideoPlayer jzvd = view.findViewById(R.id.videoplayer_jiaozi);
                    if (jzvd != null && JZUtils.dataSourceObjectsContainsUri(jzvd.dataSourceObjects, JZMediaManager.getCurrentDataSource())) {
                        JZVideoPlayer.releaseAllVideos();
                        mIsPlayingVideo = false;
                    }*/
                    stopVideoPlayer();
                    mIsPlayingVideo = false;
                }

            }
        });
        //添加分割线
        DividerItemDecoration divider = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        divider.setDrawable(getContext().getDrawable(R.drawable.recyclerview_divider));
        mRecyclerView.addItemDecoration(divider);

        return fragmentView;
    }

    private void startJZVideoPlayer(View view){
        mCurrentJZVideoPlayer = view.findViewById(R.id.videoplayer_jiaozi);
        mCurrentJZPlayerShareToView = view.findViewById(R.id.llShareTo);
        mCurrentJZPlayerDescriptionView =view.findViewById(R.id.cvContentDate);
        mCurrentJZPlayerDescriptionView.setVisibility(View.GONE);
        mCurrentJZPlayerShareToView.setVisibility(View.VISIBLE);
    }

    private void startYoukuPlayer(View view,String vid) {
        Log.d(TAG,"Youku Video Vid is : " + vid);
        mCurrentYoukuPlayerView = (YoukuPlayerView) view.findViewById(R.id.videoplayer);
        mCurrentYoukuPlayerCoverImage = (SimpleDraweeView) view.findViewById(R.id.cvContentImage);
        mCurrentYoukuPlayerPlayIcon = (ImageView) view.findViewById(R.id.imagePlay);
        mCurrentYoukuPlayerTitle = (TextView) view.findViewById(R.id.tvTitle);
        mCurrentYoukuPlayerShareToView = (LinearLayout) view.findViewById(R.id.llShareTo);
        mCurrentYoukuPlayerDescriptionView = (TextView) view.findViewById(R.id.cvContentDate);

        mCurrentYoukuPlayerCoverImage.setVisibility(View.GONE);
        mCurrentYoukuPlayerPlayIcon.setVisibility(View.GONE);
        mCurrentYoukuPlayerTitle.setVisibility(View.GONE);
        mCurrentYoukuPlayerView.setPlayerListener(new MyPlayerListener());
        mCurrentYoukuPlayerView.setShowBackBtn(false);
        mCurrentYoukuPlayerView.setPreferVideoDefinition(VideoDefinition.VIDEO_HD);
        mCurrentYoukuPlayerView.playYoukuVideo(vid);
        mCurrentYoukuPlayerShareToView.setVisibility(View.VISIBLE);
        mCurrentYoukuPlayerDescriptionView.setVisibility(View.GONE);
    }

    private void stopVideoPlayer() {
        // 停止非优酷播放
        if (mCurrentJZVideoPlayer!=null) {
            mCurrentJZPlayerDescriptionView.setVisibility(View.VISIBLE);
            mCurrentJZPlayerShareToView.setVisibility(View.GONE);
            mCurrentJZVideoPlayer.release();
        }
        if (mCurrentYoukuPlayerView!=null){
            // 停止优酷播放
            mCurrentYoukuPlayerView.release();
            mCurrentYoukuPlayerCoverImage.setVisibility(View.VISIBLE);
            mCurrentYoukuPlayerPlayIcon.setVisibility(View.VISIBLE);
            mCurrentYoukuPlayerTitle.setVisibility(View.VISIBLE);
            mCurrentYoukuPlayerShareToView.setVisibility(View.GONE);
            mCurrentYoukuPlayerDescriptionView.setVisibility(View.VISIBLE);
        }
        mIsPlayingVideo = false;
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
        Log.d(TAG,mParam1 +" Fragment onPause");
        if (mCurrentYoukuPlayerView!=null)  mCurrentYoukuPlayerView.onPause();
        //stopVideoPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,mParam1 +" Fragment onResume");
        stopVideoPlayer();

        /*JZVideoPlayer.releaseAllVideos();
        if (mCurrentYoukuPlayerView!=null) {
            mCurrentYoukuPlayerView.release();
            //mCurrentYoukuPlayerView.onResume();
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,mParam1 +" Fragment onStop");
        stopVideoPlayer();
        //if (mCurrentYoukuPlayerView!=null)  mCurrentYoukuPlayerView.release();
        if (JZVideoPlayer.backPress()) {
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,mParam1 +" Fragment onDestroy");
        if (JZVideoPlayer.backPress()) {
            return;
        }
        //if (mCurrentYoukuPlayerView!=null)  mCurrentYoukuPlayerView.onDestroy();
        stopVideoPlayer();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG,mParam1 +" Fragment onDetach");
        if (JZVideoPlayer.backPress()) {
            return;
        }
        if (mCurrentYoukuPlayerView!=null) {
            mCurrentYoukuPlayerView.release();
            mCurrentYoukuPlayerView.onDestroy();
        }
    }

  /*  @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d(TAG,mParam1 +" onHiddenChanged");
        if (hidden) stopVideoPlayer();
        super.onHiddenChanged(hidden);
    }*/

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Log.d(TAG,mParam1 +" setUserVisibleHint");
        if (!isVisibleToUser) stopVideoPlayer();
        super.setUserVisibleHint(isVisibleToUser);
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
            setViewType(responseDataList);
            insertRefresh(responseDataList.size());
            if (mAdapter!=null) mAdapter.notifyDataSetChanged(); // 微博登录调试时增加 否则空指针
            if (mRecyclerView!=null) mRecyclerView.smoothScrollToPosition(0); // 微博登录调试时增加 否则空指针
        }
        mProgressDialog.dismiss();
        if (mRefreshLayout!=null) mRefreshLayout.setRefreshing(false); // 微博登录调试时增加 否则空指针
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
    private void setViewType(ArrayList responseDataList)  {
        for (  Object obj :responseDataList ){
            if ( obj instanceof Content){
                Content content = (Content) obj;
                switch (content.mModelType){
                    case Content.TYPE_POST:
                        if (content.mImage!=null && !content.mImage.isEmpty()){
                            // 文章图片少于三张 则随机使用单图显示或使用有图显示
                            /*java.util.Random random=new java.util.Random();// 定义随机类
                            int result=random.nextInt(5);// 返回[0,5)集合中的整数，注意不包括5
                            if (result == 2){
                                content.setViewType(Model.VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE);
                            }else{
                                content.setViewType(Model.VIEW_TYPE_CONTENT_POST_IMAGE_RIGHT);
                            }*/
                            content.setViewType(getRandomViewType(1));

                        } else if (content.mImageList!=null && content.mImageList.size()<3){
                            // 文章图片少于三张 则随机使用单图显示或使用有图显示
                           /* java.util.Random random=new java.util.Random();// 定义随机类
                            int result=random.nextInt(5);// 返回[0,5)集合中的整数，注意不包括5
                            if (result == 2){
                                content.setViewType(Model.VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE);
                            }else{
                                content.setViewType(Model.VIEW_TYPE_CONTENT_POST_IMAGE_RIGHT);
                            }*/
                            content.setViewType(getRandomViewType(1));
                        }else if (content.mImageList!=null &&  content.mImageList.size()>=3){
                            // 文章图片大于三张 50%的概率显示三图 50%的概率显示单图
                            /*java.util.Random random=new java.util.Random();// 定义随机类
                            int result=random.nextInt(2);// 返回[0,2)集合中的整数，注意不包括5
                            if (result == 0){
                                content.setViewType(Model.VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE);
                            }else{
                                content.setViewType(Model.VIEW_TYPE_CONTENT_POST_IMAGE_RIGHT);
                            }*/
                            content.setViewType(getRandomViewType(3));
                        }

                        break;
                    case Content.TYPE_IMAGE:
                        if (content.mImageList.size() <3){
                            content.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_ONE);
                        }/*else if (content.mImageList.size() >=3 && content.mImageList.size() <4 ) {
                            content.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_THREE);
                        }else if (content.mImageList.size() >=4 && content.mImageList.size() <6 ) {
                            content.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_FOUR);
                        }else if (content.mImageList.size() >=6 && content.mImageList.size() <9 ) {
                            content.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_SIX);
                        }else if (content.mImageList.size() >=9 ){
                            content.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_NINE);
                        }*/else{
                            content.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_THREE);
                        }

                        break;
                    case Content.TYPE_VIDEO:
                        if (content.mSource_name!=null && content.mSource_name.equals(VideoSource.VIDEO_SOURCE_YOUKU)){
                            content.setViewType(Model.VIEW_TYPE_CONTENT_VIDEO_YOUKU);
                        }else {
                            content.setViewType(Model.VIEW_TYPE_CONTENT_VIDEO_MAIN);
                        }
                        break;
                }
            }
        }
    }

    private int getRandomViewType(int imageNum){
        if (imageNum < 3 ){
            // 图片数小于3张
            java.util.Random random=new java.util.Random();// 定义随机类
            int result=random.nextInt(5);// 返回[0,5)集合中的整数，注意不包括5
            if (result == 2){
                return Model.VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE;
            }else{
                return Model.VIEW_TYPE_CONTENT_POST_IMAGE_RIGHT;
            }
        }else{
            // 图片数大于3张
            java.util.Random random=new java.util.Random();// 定义随机类
            int result=random.nextInt(5);// 返回[0,2)集合中的整数，注意不包括5
            if (result == 2){
                return Model.VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE;
            }else{
                return Model.VIEW_TYPE_CONTENT_POST_IMAGE_RIGHT;
            }
        }


    }

    private class MyPlayerListener extends PlayerListener {

    }

    /*
    *   图片浏览监听，可以设置图片描述，以及分享链接
    * */
    private ImageViewer.OnImageChangeListener getImageChangeListener() {
        return new ImageViewer.OnImageChangeListener() {
            @Override
            public void onImageChange(int position) {
                //CustomImage image = images.get(position);
                mOverlayView.setShareText(mImagePostList.get(position));
                mOverlayView.setDescription(String.valueOf(position+1)+"/"+ Integer.toString(mImagePostList.size())
                + " " + mImageDescription);
            }
        };
    }
}
