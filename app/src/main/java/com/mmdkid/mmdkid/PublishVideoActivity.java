package com.mmdkid.mmdkid;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.adapters.CoverImageRecyclerAdapter;
import com.mmdkid.mmdkid.helper.MediaDecoder;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.server.OkHttpManager;
import com.umeng.analytics.MobclickAgent;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;

import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;


public class PublishVideoActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "PublishVideoActivity";

    private final int REQUEST_CODE_CHOOSE_VIDEO =10;
    private final int REQUEST_CODE_CHOOSE_COVER =11;
    private final int MAX_FRAME_NUM = 5; // 最大获取的视频的帧数 用于视频封面
    private final long MAX_VIDEO_SIZE = 50*1024*1024;

    private JZVideoPlayerStandard mPlayer;
    private TextView mVideoSelectView;
    private TextView mCoverSelectView;
    private RecyclerView mRecyclerView;
    private CoverImageRecyclerAdapter mAdapter;
    private EditText mTitleView;
    private EditText mDescriptionView;
    private ProgressBar mProgressBar;

    private Uri mSelectedVideo;
    private Uri mSelectedCover;
    private ArrayList<Object> mCoverList;

    private boolean mIsUploading=false; // 是否正在向服务器上载内容
    private boolean mRequestCancel=false; // 是否用户主动放弃上传内容

    // 发布内容的位置信息
    private LocationManager mLocationManager;
    private String mAddressText;
    private Location mCurrentLocation;
    // 位置信息改变
    private final static int MESSAGE_UPDATE_ADDRESS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initView();
        intData();

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_publish_video, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                if (mIsUploading){
                    // 当前有正在上载的内容 询问用户是否退出
                    AlertDialog.Builder builder = new AlertDialog.Builder(PublishVideoActivity.this);
                    builder.setTitle("提示")
                            .setMessage("正在上传，要放弃吗？")
                            .setPositiveButton(getString(R.string.action_continue), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            })
                            .setNegativeButton(getString(R.string.action__cancel_uploading), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mRequestCancel = true;
                                    OkHttpManager.getInstance(PublishVideoActivity.this).cancle();
                                    finish();
                                }
                            })
                            .show();
                }else {
                    // 当前没有上载内容直接退出
                    finish();
                }
                break;
            case R.id.action_publish_video:
                try {
                    publish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void publish() throws Exception {
        boolean cancel = false;
        View focusView = null;

        if(mIsUploading){
            // 上载过程中 不能再次发布
            Toast.makeText(this,"正在上载中", Toast.LENGTH_LONG).show();
            cancel = true;
        }

        if (mSelectedVideo == null){
            // 没有选定视频
            Toast.makeText(this,"还没有选定视频", Toast.LENGTH_LONG).show();
            cancel = true;
        }

        if (Utility.getFileSize(new File(Utility.getPath(this,mSelectedVideo)))>MAX_VIDEO_SIZE){
            // 没有选定视频
            Toast.makeText(this,"视频太大了，要小于50M", Toast.LENGTH_LONG).show();
            cancel = true;
        }

        if (mAdapter.getCheckedPosition()==-1){
            // 没有选定视频封面
            Toast.makeText(this,"还没有选定视频的封面", Toast.LENGTH_LONG).show();
            cancel = true;
        }

        if (!isTitleValid(mTitleView.getText().toString())){
            // 标题不符合要求
            mTitleView.setError("不能为空且不能超过30个字符");
            focusView = mTitleView;
            cancel = true;
        }
        if (!isDescriptionValid(mDescriptionView.getText().toString())){
            // 内容不符合要求
            mDescriptionView.setError("");
            focusView = mDescriptionView;
            cancel = true;
        }
        if (cancel){
            if (focusView!=null) focusView.requestFocus();
        }else{
            // 取的选中封面的Uri
            getSelectedCover();
            Log.d(TAG,"Selected cover is :"+mSelectedCover);
            // 用户访问的token
            App app = (App)getApplication();
            if (app.isGuest()){
                finish();
                return ;
            }
            Token token = app.getCurrentToken();
            // OkHttp上传视频
            OkHttpManager manager = OkHttpManager.getInstance(this);
            manager.setAccessToken(token.mAccessToken);
            IdentityHashMap<String, Object> paramsMap = new IdentityHashMap<String, Object>();
            File video = new File(Utility.getPath(this,mSelectedVideo));
            File cover = new File(Utility.getPath(this,mSelectedCover));
            paramsMap.put(new String("video"),video);
            paramsMap.put(new String("file"),cover);
            paramsMap.put("name",mTitleView.getText().toString());
            paramsMap.put("description",mDescriptionView.getText().toString());
            paramsMap.put("location",mAddressText);
            paramsMap.put("latitude",mCurrentLocation.getLatitude());
            paramsMap.put("longitude",mCurrentLocation.getLongitude());
            mProgressBar.setVisibility(View.VISIBLE);
            mIsUploading = true;
            manager.upLoadFile("media", paramsMap, new OkHttpManager.ReqProgressCallBack<Object>() {

                @Override
                public void onProgress(final long total, final long current) {
                    Log.d(TAG,"Upload total is : "+total +"---------->"+current);
                    mProgressBar.setProgress((int)(current *1.0f/total*100)); // 没有1.0f进度条不更新
                }

                @Override
                public void onReqSuccess(Object result) {
                    Log.d(TAG,"Upload success!");
                    mProgressBar.setVisibility(View.GONE);
                    mIsUploading = false;
                    Toast.makeText(PublishVideoActivity.this,"发布成功",Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onReqFailed(String errorMsg) {
                    Log.d(TAG,"Upload failed. " + errorMsg);
                    if (mRequestCancel) {
                        // 用户主动放弃本次上载
                        return;
                    }
                    mProgressBar.setVisibility(View.GONE);
                    mIsUploading = false;
                    Toast.makeText(PublishVideoActivity.this,"发布失败",Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(PublishVideoActivity.this);
                    builder.setTitle("提示")
                            .setMessage("发布失败:" + errorMsg)
                            .setPositiveButton(getString(R.string.action_retry), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    try {
                                        publish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            })
                            .setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                }
                            })
                            .show();
                }
            });
        }

    }

    private void getSelectedCover(){
        Object selectedCover = mCoverList.get(mAdapter.getCheckedPosition());
        if (selectedCover instanceof Uri){
            mSelectedCover = (Uri) selectedCover;
        }
        if (selectedCover instanceof  Bitmap){
            mSelectedCover =  Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),(Bitmap) mCoverList.get(mAdapter.getCheckedPosition()), null,null));
        }
    }

    private boolean isDescriptionValid(String s) {
        return true;
    }

    private boolean isTitleValid(String s) {
        return s.length()<30 && !s.isEmpty();
    }

    private void initView() {
        mPlayer = (JZVideoPlayerStandard ) findViewById(R.id.videoplayer);
        mVideoSelectView = (TextView) findViewById(R.id.tvVideoSelect);
        mCoverSelectView = (TextView) findViewById(R.id.tvCoverSelect);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mTitleView = (EditText) findViewById(R.id.evTitle);
        mDescriptionView = (EditText) findViewById(R.id.evDescription);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_upload_video) ;

        mVideoSelectView.setOnClickListener(this);
        mCoverSelectView.setOnClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
    }

    private void intData() {
        mCoverList = new ArrayList<>();
        mAdapter = new CoverImageRecyclerAdapter(mCoverList);
        mRecyclerView.setAdapter(mAdapter);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tvVideoSelect:
                Matisse.from(PublishVideoActivity.this)
                        .choose(MimeType.ofVideo())
                        .countable(true)
                        .maxSelectable(1)
                        .showSingleMediaType(true)
                        //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85f)
                        .imageEngine(new PicassoEngine())
                        .forResult(REQUEST_CODE_CHOOSE_VIDEO);
                break;
            case R.id.tvCoverSelect:
                Matisse.from(PublishVideoActivity.this)
                        .choose(MimeType.ofImage())
                        .countable(true)
                        .maxSelectable(1)
                        //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85f)
                        .imageEngine(new PicassoEngine())
                        .forResult(REQUEST_CODE_CHOOSE_COVER);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_VIDEO && resultCode == RESULT_OK) {
            Log.d(TAG, "Video Uris: " + Matisse.obtainResult(data));
            Log.d(TAG, "Video Paths: " + Matisse.obtainPathResult(data));
            //mVideoSelectView.setVisibility(View.GONE);
            mVideoSelectView.setText("更换视频");
            mPlayer.setVisibility(View.VISIBLE);
            // 设置选定的视频
            mSelectedVideo = Matisse.obtainResult(data).get(0);
            mPlayer.setUp(Matisse.obtainPathResult(data).get(0),JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL, "");
            mPlayer.startVideo();
            // 清除封面图片列表
            mCoverList.clear();
            mAdapter.notifyDataSetChanged();
            // 获取视频的关键帧
            getFrameForCover();
        }
        if (requestCode == REQUEST_CODE_CHOOSE_COVER && resultCode == RESULT_OK) {
            Log.d(TAG, "Image Uris: " + (Uri)Matisse.obtainResult(data).get(0));
            Log.d(TAG, "Image Paths: " + Matisse.obtainPathResult(data).get(0));
            mCoverList.addAll(Matisse.obtainResult(data));
            mAdapter.notifyDataSetChanged();
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
    protected void onPause() {
        super.onPause();
        //友盟Session启动、App使用时长等基础数据统计
        MobclickAgent.onPause(this);
        JZVideoPlayer.releaseAllVideos();
    }

    /**
     *  取当前视频的关键帧作为视频封面的候选图片
     */
    public void  getFrameForCover() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 视频的总时长
                MediaDecoder mediaDecoder = new MediaDecoder(PublishVideoActivity.this,mSelectedVideo);
                long length = Long.valueOf(mediaDecoder.getVedioFileLength());
                Log.d(TAG,"Video length is : " +length);
                long interval = length/MAX_FRAME_NUM;
                for (int i = 0; i < MAX_FRAME_NUM; i ++) {
                    Log.d(TAG,"Frame at :" + i*interval);
                    Bitmap frameBitmap = mediaDecoder.decodeFrame(i*interval);
                    if (frameBitmap!=null){
                        mCoverList.add(frameBitmap);
                    }
                }
                mediaDecoder.release();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //友盟Session启动、App使用时长等基础数据统计
        MobclickAgent.onResume(this);
    }
    @Override
    protected void onStart() {
        super.onStart();

        // Check if the GPS setting is currently enabled on the device.
        // This verification should be done during onStart() because the system calls this method
        // when the user returns to the activity, which ensures the desired location provider is
        // enabled each time the activity resumes from the stopped state.
        // 获取位置服务管理器
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //当前gps是否可用
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            // Build an alert dialog here that requests that the user enable
            // the location services, then when the user clicks the "OK" button,
            // call enableLocationSettings()
            // 转到gps设置界面，让用户设置
            //new EnableGpsDialogFragment().show(getSupportFragmentManager(), "enableGpsDialog");
        }

        setup();
    }

    //用户位置改变监听器
    private final LocationListener listener = new LocationListener() {


        @Override
        public void onLocationChanged(Location location) {
            // A new location update is received.  Do something useful with it.  Update the UI with
            // the location update.
            //当位置改变时更新用户位置信息
            updateUILocation(location);
            mCurrentLocation = location;
        }


        @Override
        public void onProviderDisabled(String provider) {
        }


        @Override
        public void onProviderEnabled(String provider) {
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // Set up fine and/or coarse location providers depending on whether the fine provider or
    // both providers button is pressed.
    private void setup() {
        Location gpsLocation = null;
        Location networkLocation = null;
        mLocationManager.removeUpdates(listener);
        mAddressText = getString(R.string.location_unkown);
        // Get coarse and fine location updates.
        // Request updates from both fine (gps) and coarse (network) providers.
        gpsLocation = requestUpdatesFromProvider(
                LocationManager.GPS_PROVIDER, R.string.location_not_support_gps);
        //获取 network location实例
        networkLocation = requestUpdatesFromProvider(
                LocationManager.NETWORK_PROVIDER, R.string.location_not_support_network);

        // If both providers return last known locations, compare the two and use the better
        // one to update the UI.  If only one provider returns a location, use it.
        if (gpsLocation != null && networkLocation != null) {
            updateUILocation(gpsLocation);
        } else if (gpsLocation != null) {
            updateUILocation(gpsLocation);
        } else if (networkLocation != null) {
            updateUILocation(networkLocation);
        }

    }

    /**
     * Method to register location updates with a desired location provider.  If the requested
     * provider is not available on the device, the app displays a Toast with a message referenced
     * by a resource id.
     *
     * @param provider Name of the requested provider.
     * @param errorResId Resource id for the string message to be displayed if the provider does
     *                   not exist on the device.
     * @return A previously returned {@link android.location.Location} from the requested provider,
     *         if exists.
     */
    //注册位置改变监听器
    private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
        Location location = null;
        if (mLocationManager.isProviderEnabled(provider)) {
            //注册监听器
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            mLocationManager.requestLocationUpdates(provider, 10000, 10, listener);
            //最后一次的位置信息会被保存
            location = mLocationManager.getLastKnownLocation(provider);
        } else {
            Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
        }
        return location;
    }

    private void updateUILocation(Location location) {
        doReverseGeocoding(location);
    }

    private void doReverseGeocoding(Location location) {
        // Since the geocoding API is synchronous and may take a while.  You don't want to lock
        // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
        (new PublishVideoActivity.ReverseGeocodingTask(this)).execute(new Location[] {location});
    }

    // AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
    // we do not want to invoke it from the UI thread.
    private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
        Context mContext;


        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }


        @Override
        protected Void doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());


            Location loc = params[0];
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                // Update address field with the exception.
                Message.obtain(mHandler, MESSAGE_UPDATE_ADDRESS, e.toString()).sendToTarget();
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                // Format the first line of address (if available), city, and country name.
                String addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
                // Update address field on UI.
                Message.obtain(mHandler, MESSAGE_UPDATE_ADDRESS, addressText).sendToTarget();
            }
            return null;
        }
    }

    // Stop receiving location updates whenever the Activity becomes invisible.
    @Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(listener);
    }

    Handler mHandler = new Handler() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case MESSAGE_UPDATE_ADDRESS:
                    //位置变化 更改位置显示
                    mAddressText= (String) msg.obj;
                    break;
            }

        }
    };

}
