package com.mmdkid.mmdkid;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
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
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;

import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;


public class PublishVideoActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "PublishVideoActivity";

    private final int REQUEST_CODE_CHOOSE_VIDEO =10;
    private final int REQUEST_CODE_CHOOSE_COVER =11;
    private final int MAX_FRAME_NUM = 5; // 最大获取的视频的帧数 用于视频封面
    private final long MAX_VIDEO_SIZE = 50*10024;

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
                finish();
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

        if (mSelectedVideo == null){
            // 没有选定视频
            Toast.makeText(this,"还没有选定视频", Toast.LENGTH_LONG).show();
            cancel = true;
        }

        if (Utility.getFileSize(new File(Utility.getPath(this,mSelectedVideo)))/1024>MAX_VIDEO_SIZE){
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
            mProgressBar.setVisibility(View.VISIBLE);
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
                    Toast.makeText(PublishVideoActivity.this,"发布成功",Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onReqFailed(String errorMsg) {
                    Log.d(TAG,"Upload failed. " + errorMsg);
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(PublishVideoActivity.this,"发布失败",Toast.LENGTH_LONG).show();

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
}
