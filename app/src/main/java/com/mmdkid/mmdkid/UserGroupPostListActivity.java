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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.adapters.ModelRecyclerAdapter;
import com.mmdkid.mmdkid.fragments.RecyclerViewClickListener;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.PostPublishMap;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.models.UserGroup;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.util.ArrayList;

public class UserGroupPostListActivity extends AppCompatActivity {

    private static final String TAG ="GroupPostListActivity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout mRefreshLayout;

    private ProgressDialog mProgressDialog;

    private SimpleDraweeView mAvatar;
    private TextView mName;

    private ArrayList<Model> mDataset;
    private Query mQuery;
    private RESTAPIConnection mConnection;

    private User mCurrentUser;
    private Token mCurrentToken;
    private UserGroup mCurrentGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_post_list);
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

        mCurrentGroup = (UserGroup) getIntent().getSerializableExtra("model");

        App app = (App)getApplication();
        if (app.isGuest()) finish();
        mCurrentUser = app.getCurrentUser();
        mCurrentToken = app.getCurrentToken();

        mDataset = new ArrayList<Model>();
        mQuery = PostPublishMap.find(this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Get the error response from the server");
                mProgressDialog.dismiss();
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                Log.d(TAG,"Get correct response from the server.");

                if(c == PostPublishMap.class && !responseDataList.isEmpty()){
                    Log.d(TAG,"Get the content response from the server.");
                    Content content;
                    for(Object obj : responseDataList){
                        PostPublishMap postPublishMap = (PostPublishMap) obj;
                        content = (Content) postPublishMap.mModelContent;
                        if(content!=null){
                            switch (content.mModelType){
                                case Content.TYPE_IMAGE:
                                    content.setViewType(Model.VIEW_TYPE_CONTENT_IMAGE_POST);
                                    break;
                                case Content.TYPE_POST:
                                    content.setViewType(Model.VIEW_TYPE_CONTENT_POST_IMAGE_LEFT);
                                    break;
                                case Content.TYPE_VIDEO:
                                    content.setViewType(Model.VIEW_TYPE_CONTENT_VIDEO);
                                    break;
                            }
                            mDataset.add(content);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
                mProgressDialog.dismiss();
                mRefreshLayout.setRefreshing(false);
            }
        }).where("model_id",Integer.toString(mCurrentGroup.mId))
                .where("model_type","usergroup")
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

        mAvatar = (SimpleDraweeView) findViewById(R.id.sdvAvatar) ;
        mAvatar.setImageURI(mCurrentGroup.mAvatar);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserGroupPostListActivity.this,UserGroupActivity.class);
                intent.putExtra("model",mCurrentGroup);
                startActivity(intent);
            }
        });
        mName = (TextView) findViewById(R.id.tvName);
        mName.setText(mCurrentGroup.mName);

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
                    Toast.makeText(UserGroupPostListActivity.this, "no more data.", Toast.LENGTH_LONG).show();
                    mRefreshLayout.setRefreshing(false);
                }

            }
        });

        // RecyclerView item 点击监听
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this, mRecyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Content content = (Content) mDataset.get(position);
                Intent intent = new Intent(UserGroupPostListActivity.this,WebViewActivity.class);
                intent.putExtra("url",content.getContentUrl());
                Log.d(TAG,content.getContentUrl());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //Toast.makeText(mContext,"Click "+mDataset.get(position).mContent,Toast.LENGTH_SHORT).show();
            }
        }));
    }

}
