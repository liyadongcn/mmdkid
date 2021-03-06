package com.mmdkid.mmdkid;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.fragments.DiscoveryFragment;
import com.mmdkid.mmdkid.fragments.FollowFragment;
import com.mmdkid.mmdkid.fragments.HomeFragment;
import com.mmdkid.mmdkid.fragments.MeFragment;
import com.mmdkid.mmdkid.fragments.VideoFragment;
import com.mmdkid.mmdkid.fragments.gw.ContentFragment;
import com.mmdkid.mmdkid.helper.AdUtil;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.models.login.Login;
import com.mmdkid.mmdkid.singleton.ActionLogs;
import com.mmdkid.mmdkid.update.CheckUpdateUtil;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import java.util.List;

import cn.jzvd.JZVideoPlayer;


public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener
        ,VideoFragment.OnFragmentInteractionListener
        ,DiscoveryFragment.OnFragmentInteractionListener
        ,MeFragment.OnFragmentInteractionListener
        ,ContentFragment.OnFragmentInteractionListener
        ,FollowFragment.OnFragmentInteractionListener
        {

    private static final String TAG = "MainActivity";

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

    private BottomBar mBottomBar;
    /**
     * 发布弹出窗口
     */
    private PublishPopupWindow mPopwindow;

    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

            @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查应用是否有存储权限 检查并获取粗略位置信息权限
        Utility.permissonCheck(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS);
        // 检查应用是否有精确位置权限 并申请
        // Utility.permissonCheck(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // 检查并获取粗略位置信息权限
        //Utility.permissonCheck(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        // 初始化界面
        initView();
    }

    private void initView(){
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
 /*
        * 参考：http://dev.umeng.com/push/android/integration#2_2_7
        * 注意：
            此方法与统计分析sdk中统计日活的方法无关！请务必调用此方法！
            如果不调用此方法，不仅会导致按照"几天不活跃"条件来推送失效，
            还将导致广播发送不成功以及设备描述红色等问题发生。可以只在
            应用的主Activity中调用此方法，但是由于SDK的日志发送策略，
            有可能由于主activity的日志没有发送成功，而导致未统计到日活数据。
         */
        PushAgent.getInstance(this).onAppStart();

        CheckUpdateUtil.checkUpdate(this,false);//检查更新

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId ) {
                    // The tab with id R.id.tab_favorites was selected,
                    // change your content accordingly.
                    case R.id.tab_home:
                        toolbar.setVisibility(View.VISIBLE);
                        mViewPager.setCurrentItem(0);
                        break;
                  /*  case R.id.tab_today:
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.tab_favorites:
                        mViewPager.setCurrentItem(2);
                        break;*/
                    case R.id.tab_publish:
                        App app = (App) getApplicationContext();
                        if (app.isGuest()){
                            // 未登录，弹出登录界面
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            return;
                        }else {
                            // 已登录，可以发布内容
                            mPopwindow = new PublishPopupWindow(MainActivity.this, itemsOnClick);
                            mPopwindow.showAtLocation(mBottomBar,
                                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                        }
                        break;
                    case R.id.tab_video:
                        mViewPager.setCurrentItem(1);
                        toolbar.setVisibility(View.GONE);
                        break;
                    case R.id.tab_discovery:
                        mViewPager.setCurrentItem(2);
                        toolbar.setVisibility(View.VISIBLE);
                        break;
                    case R.id.tab_me:
                        mViewPager.setCurrentItem(3);
                        toolbar.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId ) {
                    // The tab with id R.id.tab_favorites was reselected,
                    // change your content accordingly.
                    case R.id.tab_home:
                        mViewPager.setCurrentItem(0);
                        break;
                   /* case R.id.tab_today:
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.tab_favorites:
                        mViewPager.setCurrentItem(2);
                        break;*/
                    case R.id.tab_publish:
                        App app = (App) getApplicationContext();
                        if (app.isGuest()){
                            // 未登录，弹出登录界面
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            return;
                        }else {
                            // 已登录，可以发布内容
                            mPopwindow = new PublishPopupWindow(MainActivity.this, itemsOnClick);
                            mPopwindow.showAtLocation(mBottomBar,
                                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                        }
                        break;
                    case R.id.tab_video:
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.tab_discovery:
                        mViewPager.setCurrentItem(2);
                        break;
                    case R.id.tab_me:
                        mViewPager.setCurrentItem(3);
                        break;
                }
            }
        });



      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                *//*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*//*
                Intent intent = new Intent(MainActivity.this, ChannelActivity.class);
                startActivity(intent);
            }
        });*/

        autoLogin();
    }

    // 发布弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            mPopwindow.dismiss();
            mPopwindow.backgroundAlpha(MainActivity.this, 1f);
            Intent intent;
            switch (v.getId()) {
                case R.id.article:
                    Toast.makeText(MainActivity.this, "文章", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MainActivity.this,PublishPostActivity.class);
                    startActivity(intent);
                    break;
                case R.id.image:
                    Toast.makeText(MainActivity.this, "图片", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MainActivity.this, PublishImageActivity.class);
                    startActivity(intent);
                    break;
                case R.id.video:
                    Toast.makeText(MainActivity.this, "视频", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MainActivity.this, PublishVideoActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }

    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
       /* SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));*/
        //searchView.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        Intent intent;
        switch (id){
            case  R.id.action_settings:
                intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
                return true;
         /*   case R.id.action_add_kid:
                intent = new Intent(MainActivity.this,KidFormActivity.class);
                startActivity(intent);
                return true;*/
            case R.id.search:
                intent = new Intent(MainActivity.this,SearchResultsActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY,"");
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }


    /*@Override
    public void onErrorRespose(String error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setMessage(error);
        dialogBuilder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }*/

   /* @Override
    public void onErrorRespose(Class c, String error) {

        if(c == User.class){
            App app = (App)getApplicationContext();
            app.setIsGuest(true);
            app.setCurrentUser(null);
            mBottomBar.getTabWithId(R.id.tab_me).setTitle("未登录");
        }
    }*/

   /* @Override
    public void onResponse(Class c, ArrayList responseDataList) {
        App app = (App)getApplicationContext();
        if(c == User.class){
            if(!responseDataList.isEmpty()){
                // get user info from the server
                User user = (User) responseDataList.get(0);
                app.setCurrentUser(user);
                app.setIsGuest(false);
                Log.d(TAG,"Get the user info from the server.");
                Log.d(TAG,"The username is " + user.mUsername );
                Log.d(TAG,"The cellphone is " + user.mCellphone );
                Log.d(TAG,"The email is " + user.mEmail );
                mBottomBar.getTabWithId(R.id.tab_me).setTitle("已登录");
            }else {
                // there is no user info
                app.setIsGuest(true);
                app.setCurrentUser(null);
            }
        }
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
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
                    return HomeFragment.newInstance("","");
               /* case 1:
                    return TodayFragment.newInstance("","");
                case 2:
                    return FavoriteFragment.newInstance("","");*/
                case 1:
                    return VideoFragment.newInstance("","");
                case 2:
                    return DiscoveryFragment.newInstance("","");
                case 3:
                    return MeFragment.newInstance("","");
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }

        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //友盟Session启动、App使用时长等基础数据统计
        MobclickAgent.onResume(this);
        Log.d(TAG,"MainActivity is onResume.");
        App app = (App) getApplicationContext();
        if(!app.isGuest()){
            mBottomBar.getTabWithId(R.id.tab_me).setTitle("已登录");
        }else{
            mBottomBar.getTabWithId(R.id.tab_me).setTitle("未登录");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //友盟Session启动、App使用时长等基础数据统计
        MobclickAgent.onPause(this);
        JZVideoPlayer.releaseAllVideos();
    }

    private void autoLogin(){
        App app = (App)getApplicationContext();
        User currentUser = app.getCurrentUser();
        Token token = app.getCurrentToken();
        //Log.d(TAG,"Token is " + token.toString());
        if(currentUser== null){
            app.setIsGuest(true);
            Log.d(TAG,"There is no user in the local.");
            mBottomBar.getTabWithId(R.id.tab_me).setTitle("未登录");
        }else{
            if(app.isUserAccessTokenValid()&& currentUser.getIdentity()!=null){
                // user accesstoken is valid
                Log.d(TAG,"Login identity is :" + currentUser.getIdentity());
                //attemptToGetUserInfo(currentUser.getIdentity(),token.mAccessToken);
                Login login = new Login(this, mLoginListener);
                // 自动登录时 不自动登录web
                login.setLoginWeb(false);
                login.startWithToken(currentUser.getIdentity(),token);
                app.setIsGuest(false);
            }else{
                // user accesstoken is not valid
                app.setIsGuest(true);
                // remove cached user info , token and cookies
                app.setCurrentUser(null);
                Log.d(TAG,"User token is not valid.");
                Log.d(TAG,"User identity is " + currentUser.getIdentity());
                mBottomBar.getTabWithId(R.id.tab_me).setTitle("未登录");
            }
        }
        // 读取用户上次的浏览记录
        ActionLogs.getInstance(this).setLogList(app.getLogs());
        // 上传设备及联系人信息
        Utility.uploadDeviceContactInfo(this);
        // 从服务器下载缓存广告信息
        AdUtil.getAdvertisementsFromServer(this,AdUtil.DEFAULT_PERIOD_TIME);
    }

   /* private void attemptToGetUserInfo(String identity,String accessToken) {
        RESTAPIConnection connection = new RESTAPIConnection(this);
        connection.ACCESS_TOKEN = accessToken;
        User.find(connection).where("user_name",identity).all();
        Log.d(TAG,"Try to get the user info...");
    }*/

    private Login.LoginListener mLoginListener = new Login.LoginListener() {
        @Override
        public void onError(Class c, String error) {
            //showProgress(false);
            if (c==Token.class)  Log.d(TAG,"Token Error:"+error);
            if (c==User.class)  Log.d(TAG,"User Error:"+error);
            if (c==null)  Log.d(TAG,"Web Error:"+error);
            //mIsLogging = false;
            Toast.makeText(MainActivity.this,error,Toast.LENGTH_LONG).show();
            mBottomBar.getTabWithId(R.id.tab_me).setTitle("未登录");
        }

        @Override
        public void onSuccess(Object user, Object token, Object cookies) {
            Log.d(TAG,"User cellphone is :"+((User)user).mCellphone);
            Log.d(TAG,"User email is :"+((User)user).mEmail);
            Log.d(TAG,"User name is :"+((User)user).mUsername);
            Log.d(TAG,"Token is:" + ((Token)token).mAccessToken);
            if (cookies!=null) Log.d(TAG,"Cookies is :" + ((List<String>)cookies));
            App app = (App) getApplication();
            ((Token)token).saveToLocal(MainActivity.this);
            ((User)user).saveToLocal(MainActivity.this);
            if (cookies!=null) app.setCookies(((List<String>)cookies));
            app.setIsGuest(false);
            // 所有登录过程全部成功
            mBottomBar.getTabWithId(R.id.tab_me).setTitle("已登录");
        }
    };
}
