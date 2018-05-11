package com.mmdkid.mmdkid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.fragments.homePage.ContentFragment;
import com.mmdkid.mmdkid.fragments.publishManage.PostFragment;
import com.mmdkid.mmdkid.fragments.publishManage.VideoFragment;
import com.mmdkid.mmdkid.fragments.publishManage.v2.ImageFragment;
import com.mmdkid.mmdkid.models.Behavior;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.util.ArrayList;

/**
 *
 */
public class HomePageActivity extends AppCompatActivity {
    private static final String TAG = "HomePageActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    private User mUser; // 主页显示用户
    private SimpleDraweeView mAvatar;
    private TextView mDisplayName;
    private TextView mSignature;
    private TextView mFollowing;
    private TextView mFollower;

    private TextView mFollowActionView;
    private User mCurrentUser; // 当前用户
    private App mApp;

    private Behavior mBehavior; // 当前关注记录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(mViewPager);

        mUser = (User) getIntent().getSerializableExtra("model");

        mApp = (App)getApplication();
        if (!mApp.isGuest()){
            // 已登录取得当前登录用户
            mCurrentUser = mApp.getCurrentUser();
        }

        mAvatar = (SimpleDraweeView) findViewById(R.id.sdvAvatar);
        mAvatar.setImageURI(mUser.mAvatar);

        mDisplayName = (TextView) findViewById(R.id.tvDisplayName);
        mDisplayName.setText(mUser.getDisplayName());

        mSignature = (TextView) findViewById(R.id.tvSignature);
        if(mUser.mSignature==null || mUser.mSignature.isEmpty()){
            mSignature.setText(R.string.homepage_no_signature);
        }else{
            mSignature.setText(mUser.mSignature);
        }

        mFollowing = (TextView) findViewById(R.id.tvFollowing);
        mFollowing.setText(Integer.toString(mUser.mFollowing));

        mFollower = (TextView) findViewById(R.id.tvFollower);
        mFollower.setText(Integer.toString(mUser.mFollower));

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.d(TAG,"Vertical Offset>>>" + verticalOffset + ">>>Toolbar Height>>>" + toolbar.getHeight());
                if (-verticalOffset < toolbar.getHeight()) {
                    mCollapsingToolbarLayout.setTitle("");
                    //使用下面两个CollapsingToolbarLayout的方法设置展开透明->折叠时你想要的颜色
                   /* collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
                    collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.colorAccent));*/
                } else {
                    mCollapsingToolbarLayout.setTitle(mDisplayName.getText());
                }
            }
        });

        mFollowActionView = (TextView) findViewById(R.id.tvFollowAction);
        initFollowActionView();
       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    private void initFollowActionView() {
        // 用户是否登录
        if (mApp.isGuest()){
            // 用户未登录直接显示关注按钮
            mFollowActionView.setText(R.string.homepage_follow);
            return;
        }
        // 是否为自己当前用户
        if (mCurrentUser!=null && mCurrentUser.mId == mUser.mId){
            mFollowActionView.setVisibility(View.GONE);
            return;
        }
        // 当前用户是否已经关注
        Behavior.find(this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 当前用户未关注
                Log.d(TAG,"Get the error response from server>>>" + error);
                mFollowActionView.setText(R.string.homepage_follow);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (responseDataList.size()>=1){
                    // 当前用户已关注
                    mBehavior = (Behavior) responseDataList.get(0);
                    mFollowActionView.setText(R.string.homepage_followed);
                }else{
                    // 未找到关注记录
                    mFollowActionView.setText(R.string.homepage_follow);
                }

            }
        })
        .where("user_id", String.valueOf(mCurrentUser.mId))
        .where("name",Behavior.BEHAVIOR_FOLLOW)
        .where("model_type","user")
        .where("model_id", String.valueOf(mUser.mId))
        .all();

        // 设置监听
        mFollowActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentUser!=null && mUser!=null){
                    if (mFollowActionView.getText().toString().equals(getString(R.string.homepage_follow))){
                        // 关注用户
                        follow(mCurrentUser.mId,mUser.mId);
                    }else{
                        // 取消关注
                        unFollow(mCurrentUser.mId,mUser.mId);
                    }
                }

            }
        });
    }

    private void follow(int currentUserId,int userId) {
        // 用户未登录
        if(mApp.isGuest()){
            // 显示登录界面
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        if (currentUserId==0 || userId==0) return;
        Behavior behavior = new Behavior();
        behavior.mUserId = currentUserId;
        behavior.mName = Behavior.BEHAVIOR_FOLLOW;
        behavior.mModelType = "user";
        behavior.mModelId = userId;
        behavior.save(Model.ACTION_CREATE, this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 创建关注信息出错
                Log.d(TAG,"Create a new follow behavior failed.");
            }
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c==Behavior.class && responseDataList.size()!=0){
                    // 创建关注信息成功
                    Log.d(TAG,"Create a new follow behavior success.");
                    mBehavior = (Behavior) responseDataList.get(0);
                    mFollowActionView.setText(R.string.homepage_followed);
                }
            }
        });
    }

    private void unFollow(int currentUserId,int userId){
        // 用户未登录
        if(mApp.isGuest()){
            // 用户必须登录才能取消关注
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        if (mBehavior==null) return;
        mBehavior.delete(this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 取消关注网络操作失败
                Log.d(TAG,"Delete the follow behavior failed.");
            }
            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c==Behavior.class && !responseDataList.isEmpty()){
                    // 删除关注成功
                    Log.d(TAG,"Delete the follow behavior success.");
                    mBehavior = null;
                    mFollowActionView.setText(R.string.homepage_follow);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    return ContentFragment.newInstance(Content.TYPE_PUSH,Integer.toString(mUser.mId));
                case 1:
                    return ContentFragment.newInstance(Content.TYPE_POST,Integer.toString(mUser.mId));
                case 2:
                    return ContentFragment.newInstance(Content.TYPE_IMAGE,Integer.toString(mUser.mId));
                case 3:
                    return ContentFragment.newInstance(Content.TYPE_VIDEO,Integer.toString(mUser.mId));
            }
            return PublishManageActivity.PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "综合";
                case 1:
                    return "文章";
                case 2:
                    return "图片";
                case 3:
                    return "视频";
            }
            return super.getPageTitle(position);
        }
    }
}
