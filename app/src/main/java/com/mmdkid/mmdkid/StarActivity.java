package com.mmdkid.mmdkid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mmdkid.mmdkid.adapters.ModelRecyclerAdapter;
import com.mmdkid.mmdkid.fragments.RecyclerViewClickListener;
import com.mmdkid.mmdkid.models.Behavior;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

public class StarActivity extends AppCompatActivity {
    private static final String TAG ="StarActivity";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initData();

        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        App app = (App)getApplication();
        if (app.isGuest()) finish();
        mCurrentUser = app.getCurrentUser();
        mCurrentToken = app.getCurrentToken();

        mDataset = new ArrayList<Model>();
        mQuery = Behavior.find(this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Get the error response from the server");
                mProgressDialog.dismiss();
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                Log.d(TAG,"Get correct response from the server.");

                if(c == Behavior.class && !responseDataList.isEmpty()){
                    Log.d(TAG,"Get the content response from the server.");
                    for(Object obj : responseDataList){
                        Behavior behavior = (Behavior)obj;
                        if(behavior.mModel!=null){
                            switch (behavior.mModelType){
                                case Content.TYPE_POST:
                                    behavior.mModel.setViewType(Model.VIEW_TYPE_CONTENT_POST);
                                    break;
                                case Content.TYPE_IMAGE:
                                    behavior.mModel.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_POST);
                                    break;
                                case Content.TYPE_VIDEO:
                                    behavior.mModel.setViewType(Model.VIEW_TYPE_CONTENT_VIDEO);
                            }
                            mDataset.add((Content) behavior.mModel);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
                mProgressDialog.dismiss();
                mRefreshLayout.setRefreshing(false);
            }
        }).where("user_id",Integer.toString(mCurrentUser.mId))
                .where("name",Behavior.BEHAVIOR_STAR)
                .where("expand","model_content");
        mQuery.all();
    }

    private void initView() {
        // show the progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // use the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.rvContent);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        //mLayoutManager = new GridLayoutManager(this,2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ModelRecyclerAdapter(this,mDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Swipe refresh listener
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.layout_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            public void onRefresh() {
                // 加载更多数据
                if(mQuery.hasMore()){
                    //mConnection.Query(mQuery);
                    mQuery.all();
                }else {
                    // 提示没有更多数可以加载
                    Toast.makeText(StarActivity.this, "no more data.", Toast.LENGTH_LONG).show();
                    mRefreshLayout.setRefreshing(false);
                }

            }
        });

        // RecyclerView item 点击监听
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this, mRecyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Content content = (Content)mDataset.get(position);
                Intent intent;
                switch (content.mModelType){
                    case Content.TYPE_POST:
                        //Toast.makeText(mContext,"Click "+mDataset.get(position).mTitle,Toast.LENGTH_SHORT).show();
                        intent = new Intent(StarActivity.this,WebViewActivity.class);
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
                        // 打开webview观看视频
                        intent = new Intent(StarActivity.this,WebViewActivity.class);
                        intent.putExtra("url",content.getContentUrl());
                        intent.putExtra("model",content);
                        Log.d(TAG,content.getContentUrl());
                        startActivity(intent);

                        break;
                    case Content.TYPE_IMAGE:
                        Log.d(TAG,content.mImageList.toString());
                        /*intent = new Intent(mContext,ImageActivity.class);
                        intent.putStringArrayListExtra(ImageActivity.IMAGE_LIST,content.mImageList);
                        startActivity(intent);*/
                        new ImageViewer.Builder<>(StarActivity.this, content.mImageList)
                                .setStartPosition(0)
                                .show();
                        break;
                    default:
                        Toast.makeText(StarActivity.this,"Can not show this type content",Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                //Toast.makeText(mContext,"Click "+mDataset.get(position).mContent,Toast.LENGTH_SHORT).show();
            }
        }));
    }

}
