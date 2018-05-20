package com.mmdkid.mmdkid.fragments.gw;



import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.WebViewActivity;
import com.mmdkid.mmdkid.adapters.ModelRecyclerAdapter;
import com.mmdkid.mmdkid.fragments.RecyclerViewClickListener;

import com.mmdkid.mmdkid.helper.ProgressDialog;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Refresh;
import com.mmdkid.mmdkid.models.gw.Content;
import com.mmdkid.mmdkid.server.ElasticQuery;
import com.mmdkid.mmdkid.server.ElasticsearchConnection;
import com.mmdkid.mmdkid.server.QueryBuilder;
import com.mmdkid.mmdkid.server.Sort;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentFragment extends Fragment {
    private static final String TAG = "GWContentFragment";

    private Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout mRefreshLayout;
    private ProgressDialog mProgressDialog;

    private ArrayList<Model> mDataset;
    private ElasticQuery mQuery;
    private Refresh mRefresh;

    private boolean mIsFetching = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DiscoveryFragment.
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mContext = getActivity();
        mDataset = new ArrayList<Model>();
        initData();

        // show the progress dialog
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.show();
    }

    private void initData() {
        // 建立查询
        mQuery = (ElasticQuery) Content.find(getContext(), new ElasticsearchConnection.OnConnectionListener() {
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
                    Log.d(TAG,"First content title is : " + ((Content)responseDataList.get(0)).mTitle);
                    mDataset.addAll(0,responseDataList);
                    setViewType(responseDataList);
                    insertRefresh(responseDataList.size());
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.smoothScrollToPosition(0);
                }
                mProgressDialog.dismiss();
                mRefreshLayout.setRefreshing(false);
            }
        });
        // 设置查询条件
        mQuery.setJsonRequest(getQueryRequest());
        // 开始查询
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
            case Content.TYPE_POSTS:
                if(mParam2.isEmpty()){
                    request = QueryBuilder.termQuery("model_type", Content.TYPE_POSTS);
                    Log.d(TAG,"Request is " + request.toString());
                }else{
                    request = new QueryBuilder().boolQuery()
                            .must(QueryBuilder.termQuery("model_type", Content.TYPE_POSTS))
                            .must(QueryBuilder.multiMatchQuery( mParam2,new String[]{"title","content"})).getJSONQuery();
                    Log.d(TAG,"Posts boolQuery is :" + request.toString());
                }

                break;
            case Content.TYPE_GOODS:
                if(mParam2.isEmpty()){
                    request = QueryBuilder.termQuery("model_type", Content.TYPE_GOODS);
                    Log.d(TAG,"Request is " + request.toString());
                }else{
                    request =   new QueryBuilder().boolQuery()
                            .must(QueryBuilder.termQuery("model_type", Content.TYPE_GOODS))
                            .must(QueryBuilder.multiMatchQuery( mParam2,new String[]{"title","content"})).getJSONQuery();
                    Log.d(TAG,"Image boolQuery is :" + request.toString());
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
        mAdapter = new ModelRecyclerAdapter(fragmentView.getContext(),mDataset);
        mRecyclerView.setAdapter(mAdapter);

        // RecyclerView item 点击监听
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(mContext, mRecyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Model model = mDataset.get(position);
                if(model instanceof Content){
                    Content content = (Content) model;
                    Intent intent;
                    intent = new Intent(mContext, WebViewActivity.class);
                    intent.putExtra("model", content);
                    intent.putExtra("url", content.getUrl());
                    intent.putExtra("cookies", false);
                    startActivity(intent);

                }else if(model instanceof Refresh){
                    // 加载更多数据
                    if(mQuery.hasMore()){
                        //mConnection.Query(mQuery);
                        mRefreshLayout.setRefreshing(true);
                        getMore();
                    }else {
                        // 提示没有更多数可以加载
                        Toast.makeText(mContext, getString(R.string.no_more_data), Toast.LENGTH_LONG).show();
                        mRefreshLayout.setRefreshing(false);
                    }
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                //Toast.makeText(mContext,"Click "+mDataset.get(position).mContent,Toast.LENGTH_SHORT).show();
            }
        }));

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
                    Toast.makeText(mContext, getString(R.string.no_more_data), Toast.LENGTH_LONG).show();
                    mRefreshLayout.setRefreshing(false);
                }

            }
        });

        //添加分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));

        return fragmentView;
    }

    private void getMore() {
        if(mIsFetching) return;
        mQuery.all();
        mIsFetching = true;
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
                    case Content.TYPE_POSTS:
                        content.setViewType(Model.VIEW_TYPE_GWCONTENT_POST_IMAGE_ON_MIDDLE);
                        break;
                    case Content.TYPE_GOODS:
                        content.setViewType(Model.VIEW_TYPE_GWCONTENT_GOODS_IMAGE_ON_LEFT);
                        break;
                }
            }
        }
    }
}
