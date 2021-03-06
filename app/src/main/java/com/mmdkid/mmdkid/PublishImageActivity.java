package com.mmdkid.mmdkid;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.adapters.PublishImageAdapter;
import com.mmdkid.mmdkid.helper.FileUtil;
import com.mmdkid.mmdkid.helper.ImageUtil;
import com.mmdkid.mmdkid.helper.ProgressDialog;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.server.OkHttpManager;
import com.umeng.analytics.MobclickAgent;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;

public class PublishImageActivity extends AppCompatActivity {
    private final static String TAG = "PublishImageActivity";

    private static final int REQUEST_CODE_CHOOSE = 23;

    private ArrayList<Uri> mImageList; // 上传图片的列表，最后一个为增加图片的加号图片
    private ArrayList<File> mCompressedImageFileList;

    private ProgressBar mProgressBar;
    private EditText mTitleView;
    private EditText mDescriptionView;
    private GridView mGridView;
    private PublishImageAdapter mAdapter;

    private LocationManager mLocationManager;
    private TextView mAddressView;
    private Location mCurrentLocation;

    private final static int MESSAGE_UPDATE_ADDRESS = 10;

    private final static int MESSAGE_COMPRESS_IMAGE_FINISH = 11;
    private final static int MESSAGE_COMPRESS_IMAGE_START = 12;
    private final static int MESSAGE_COMPRESS_IMAGE_ERROR = 13;

    private boolean mIsUploading=false; // 是否正在向服务器上载内容
    private boolean mRequestCancel=false; // 是否用户主动放弃上传内容

    private ProgressDialog mProgressDialog;

    private boolean mIsCompressing = false; // 正在压缩图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_publish_image);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_upload_image) ;
        mTitleView = (EditText) findViewById(R.id.evTitle);
        mDescriptionView = (EditText) findViewById(R.id.evDescription);
        mGridView = (GridView) findViewById(R.id.gvImage);

        // 初始化图片列表，加入加号图片
        mImageList = new ArrayList<Uri>() ;
        // 加号图片的uri
        Uri uri =  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + getResources().getResourcePackageName(R.drawable.ic_plus_png) + "/"
                + getResources().getResourceTypeName(R.drawable.ic_plus_png) + "/"
                + getResources().getResourceEntryName(R.drawable.ic_plus_png));
        //Uri uriFresco = new Uri.Builder().scheme("res").path(String.valueOf(R.drawable.ic_plus)).build();
        //Uri uriFresco = Uri.parse("res://com.mmdkid.mmdkid/"+R.drawable.ic_friends);
        mImageList.add(uri);
        //mImageList.add(uriFresco);
        Log.d(TAG,"Image uri is :"+uri);
        //Log.d(TAG,"Fresco Image uri is :"+uriFresco);

        mAdapter = new PublishImageAdapter(this,mImageList);
        mGridView.setAdapter(mAdapter);

        mAddressView = (TextView) findViewById(R.id.tvLocation);
        mAddressView.setText(R.string.location_unkown);
   }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_publish_image, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                if (mIsUploading){
                    // 当前有正在上载的内容 询问用户是否退出
                    AlertDialog.Builder builder = new AlertDialog.Builder(PublishImageActivity.this);
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
                                    OkHttpManager.getInstance(PublishImageActivity.this).cancle();
                                    finish();
                                }
                            })
                            .show();
                }else {
                    // 当前没有上载内容直接退出
                    finish();
                }
                break;
            case R.id.action_publish_image:
                try {
                    publish();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
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

        if(mIsCompressing) {
            // 压缩图片过程中 不能再次发布
            Toast.makeText(this,"正在压缩图片", Toast.LENGTH_LONG).show();
            cancel = true;
        }

        if(mIsUploading){
            // 上载过程中 不能再次发布
            Toast.makeText(this,"正在上载中", Toast.LENGTH_LONG).show();
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
        if (mImageList.size()-1==0){
            // 没有选定图片
            Toast.makeText(this,"图片还没有选定", Toast.LENGTH_LONG).show();
            cancel = true;
        }
        if (cancel){
            if (focusView!=null) focusView.requestFocus();
        }else{
            // 压缩要上传的图片文件
            compressImages();

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void compressImages() throws Exception {
        App app = (App)getApplication();
        if (app.isGuest()){
            // 用户没有登录 直接返回
            finish();
            return ;
        }
        if(mImageList==null || mImageList.isEmpty()) return; // 若没有要上传的图片
        if(mCompressedImageFileList==null){
            mCompressedImageFileList = new ArrayList<File>();
        }else{
            mCompressedImageFileList.clear();
        }
        //使用子线程压缩图片
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 发送开始压缩图片的消息
                Message msg = Message.obtain();
                msg.what = MESSAGE_COMPRESS_IMAGE_START;
                mHandler.sendMessage(msg);
                try {
                    for(int i =0 ; i < mImageList.size()-1; i++){ // imageList中包含最后一个加号图片，所以要去掉
                        //URI jURI = new URI(uri.toString());
                        //File file = new File(jURI);
                        Log.d(TAG,"File uri :" + mImageList.get(i).toString());
                        Log.d(TAG,"File path :" + mImageList.get(i).getPath());
                        Log.d(TAG,"File path :" +Utility.getPath(PublishImageActivity.this,mImageList.get(i)));
                        File file = new File(Utility.getPath(PublishImageActivity.this,mImageList.get(i)));
                        File compressedFile = ImageUtil.compress(PublishImageActivity.this,file);
                        // 原始图片情况
                        int[] demension = ImageUtil.getImageWidthHeight(file.getAbsolutePath());
                        long size= FileUtil.getFileSize(file);
                        Log.d(TAG,"Original Image File path >>>" + file.getAbsolutePath());
                        Log.d(TAG,"Original Image width  height  size >>>" + demension[0] + " "+ demension[1] + " " +size);
                        // 图片压缩后
                        demension = ImageUtil.getImageWidthHeight(compressedFile.getAbsolutePath());
                        size= FileUtil.getFileSize(compressedFile);
                        Log.d(TAG,"Compressed Image File path >>>" +compressedFile.getAbsolutePath());
                        Log.d(TAG,"Compressed Image width  height size >>>" + demension[0] + " "+ demension[1] + " " +size );
                        mCompressedImageFileList.add(compressedFile);
                }
                } catch (Exception e) {
                    e.printStackTrace();
                    msg = Message.obtain();
                    msg.what = MESSAGE_COMPRESS_IMAGE_ERROR;
                    mHandler.sendMessage(msg);
                }
                // 发送压缩结束的消息
                msg = Message.obtain();
                msg.what = MESSAGE_COMPRESS_IMAGE_FINISH;
                mHandler.sendMessage(msg);
            }
        }).start();
        //showProgressDialog("正在压缩...");

        //dismissProgressDialog();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void uploadImages() {
        App app = (App)getApplication();
        if (app.isGuest()){
            // 用户没有登录 直接返回
            finish();
            return ;
        }
        Token token = app.getCurrentToken();
        OkHttpManager manager = OkHttpManager.getInstance(this);
        manager.setAccessToken(token.mAccessToken);
        IdentityHashMap<String, Object> paramsMap = new IdentityHashMap<String, Object>();
        // 图片压缩并上传
        //showProgressDialog("正在压缩...");
        if (mCompressedImageFileList==null || mCompressedImageFileList.isEmpty()) return;
        for(int i =0 ; i < mCompressedImageFileList.size(); i++){
            paramsMap.put(new String("file[]"),mCompressedImageFileList.get(i));
        }
        //dismissProgressDialog();
        paramsMap.put("title",mTitleView.getText().toString());
        paramsMap.put("content",mDescriptionView.getText().toString());
        paramsMap.put("status",10);
        //发布的位置名称
        paramsMap.put("location",mAddressView.getText().toString());
        paramsMap.put("latitude",mCurrentLocation.getLatitude());
        paramsMap.put("longitude",mCurrentLocation.getLongitude());
        mProgressBar.setVisibility(View.VISIBLE);
        mIsUploading =true;
        manager.upLoadFile("image-posts", paramsMap, new OkHttpManager.ReqProgressCallBack<Object>() {

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
                Toast.makeText(PublishImageActivity.this,"发布成功",Toast.LENGTH_LONG).show();
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
                Toast.makeText(PublishImageActivity.this,"发布失败",Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(PublishImageActivity.this);
                builder.setTitle("提示")
                        .setMessage("发布失败:" + errorMsg)
                        .setPositiveButton(getString(R.string.action_retry), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    publish();
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
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

    private boolean isDescriptionValid(String s) {
        return true;
    }

    private boolean isTitleValid(String s) {
        return s.length()<30 && !s.isEmpty();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            // 始终将加号图片放在整个图片列表的最后一个
            mImageList.addAll(mImageList.size()-1, Matisse.obtainResult(data));
            // 去掉重复选择的图片
            Utility.removeDuplicate(mImageList);
            mAdapter.notifyDataSetChanged();
            Log.d("Matisse", "mSelected: " + mImageList);
        }
    }

    // Set up fine and/or coarse location providers depending on whether the fine provider or
    // both providers button is pressed.
    private void setup() {
        Location gpsLocation = null;
        Location networkLocation = null;
        mLocationManager.removeUpdates(listener);
        mAddressView.setText(R.string.location_unkown);
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

    private void updateUILocation(Location location) {
        doReverseGeocoding(location);
    }

    private void doReverseGeocoding(Location location) {
        // Since the geocoding API is synchronous and may take a while.  You don't want to lock
        // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
        (new ReverseGeocodingTask(this)).execute(new Location[] {location});
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

    Handler mHandler = new Handler() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case MESSAGE_UPDATE_ADDRESS:
                    //位置变化 更改位置显示
                    mAddressView.setText((CharSequence) msg.obj);
                    break;
                case MESSAGE_COMPRESS_IMAGE_START:
                    mIsCompressing = true;
                    showProgressDialog("正在压缩图片...");
                    break;
                case MESSAGE_COMPRESS_IMAGE_FINISH:
                    mIsCompressing = false;
                    dismissProgressDialog();
                    // 压缩完成 上传图片
                    uploadImages();
                    break;
                case MESSAGE_COMPRESS_IMAGE_ERROR:
                    mIsCompressing = false;
                    dismissProgressDialog();
                    Toast.makeText(PublishImageActivity.this,"压缩图片错误",Toast.LENGTH_LONG).show();
                    // 清空压缩图片列表
                    mCompressedImageFileList.clear();
                    break;
            }

        }
    };

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
    // Stop receiving location updates whenever the Activity becomes invisible.
    @Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //友盟Session启动、App使用时长等基础数据统计
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //友盟Session启动、App使用时长等基础数据统计
        MobclickAgent.onPause(this);
    }

    private void showProgressDialog(String message){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }
    private void dismissProgressDialog(){
        if (mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }
}
