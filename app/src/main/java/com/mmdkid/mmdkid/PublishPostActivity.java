package com.mmdkid.mmdkid;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.helper.FileUtil;
import com.mmdkid.mmdkid.helper.HtmlUtil;
import com.mmdkid.mmdkid.helper.ImageUtil;
import com.mmdkid.mmdkid.helper.ProgressDialog;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Post;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.server.OkHttpManager;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.umeng.analytics.MobclickAgent;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.richeditor.RichEditor;

public class PublishPostActivity extends AppCompatActivity {
    private final static String TAG = "PublishPostActivity";
    private final static String HTML_TAG_START = "<p>";
    private final static String HTML_TAG_END = "</p>";

    private RichEditor mEditor;
    private TextView mPreview;
    private EditText mTitleView;
    private ProgressBar mProgressBar;

    private static final int REQUEST_CODE_CHOOSE = 23; // 图片选择返回的代码值
    private static final int MAX_IMAGE_NUM = 9;// 最大一次插入的图片数

    private ArrayList<String> mImageList; // 上传图片的列表，最后一个为增加图片的加号图片
    private Map<String,String> mImageMap = new HashMap<String, String>(); // 本地图片与上传后服务器图片url对应

    private String mPostHtml;

    private boolean mIsUploading=false; // 是否正在向服务器上载内容
    private boolean mRequestCancel=false; // 是否用户主动放弃上传内容

    private final static int MESSAGE_COMPRESS_IMAGE_FINISH = 11; // 压缩文章中图片结束
    private final static int MESSAGE_COMPRESS_IMAGE_START = 12;  // 压缩文章中图片开始
    private final static int MESSAGE_COMPRESS_IMAGE_ERROR = 13;  // 压缩文章中图片有错误
    private final static int MESSAGE_UPDATE_ADDRESS = 10;         // 位置信息改变

    private ProgressDialog mProgressDialog;

    private boolean mIsCompressing = false; // 正在压缩图片
    private ArrayList<File> mCompressedImageFileList; // 压缩后的图片列表

    // 发布文章的位置信息
    private LocationManager mLocationManager;
    private String mAddressText;
    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

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
        getMenuInflater().inflate(R.menu.menu_publish_post, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                if (mIsUploading){
                    // 当前有正在上载的内容 询问用户是否退出
                    AlertDialog.Builder builder = new AlertDialog.Builder(PublishPostActivity.this);
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
                                    OkHttpManager.getInstance(PublishPostActivity.this).cancle();
                                    finish();
                                }
                            })
                            .show();
                }else {
                    // 当前没有上载内容直接退出
                    finish();
                }
                break;
            case R.id.action_publish_post:
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
            mTitleView.setError("不能为空且不能超过50个字符");
            focusView = mTitleView;
            cancel = true;
        }
        if (!isPostValid(mEditor.getHtml())){
            // 内容不符合要求
            Toast.makeText(PublishPostActivity.this,"文章内容还没写！",Toast.LENGTH_LONG).show();
            focusView = mEditor;
            cancel = true;
        }
        if (cancel){
            if (focusView!=null) focusView.requestFocus();
        }else{
            // 文章可以发布
            // 检测文章中所有的本地图片并返回图片路径列表
            Log.d(TAG,"Html is : "+ mEditor.getHtml());
            mImageList = (ArrayList<String>) HtmlUtil.getImageSrc(mEditor.getHtml());
            if (mImageList.isEmpty()){
                // 文章中没有图片，直接发布
                mPostHtml = mEditor.getHtml();
                pulishPost();
            }else{
                // 文章中有图片，先上传文中图片，再发布文章
                for (String image : mImageList){
                    Log.d(TAG,"Image is :" + image);
                }
                // 压缩文章中的图片 根据压缩返回的消息 上传文章
                compressImages();
            }

        }

    }
    /**
     *  上传文章中所有的本地图片到服务器
     *  文章中的图片已经经过压缩
     */
    private void upload() throws URISyntaxException {
        App app = (App)getApplication();
        if (app.isGuest()){
            Toast.makeText(PublishPostActivity.this,"还没有登录，不能发布!",Toast.LENGTH_LONG).show();
            return ;
        }
        Token token = app.getCurrentToken();
        OkHttpManager manager = OkHttpManager.getInstance(this);
        manager.setAccessToken(token.mAccessToken);
        IdentityHashMap<String, Object> paramsMap = new IdentityHashMap<String, Object>();
        if (mCompressedImageFileList!=null && !mCompressedImageFileList.isEmpty()) {
            for(int i =0 ; i < mCompressedImageFileList.size(); i++){
                Log.d(TAG,"Compressed Image path in the post:" +mCompressedImageFileList.get(i).getAbsolutePath());
                paramsMap.put(new String("file[]"),mCompressedImageFileList.get(i));
            }
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mIsUploading = true;
        manager.upLoadFile("posts/upload", paramsMap, new OkHttpManager.ReqProgressCallBack<Object>() {

            @Override
            public void onProgress(final long total, final long current) {
                Log.d(TAG,"Upload total is : "+total +"---------->"+current);
                mProgressBar.setProgress((int)(current *1.0f/total*100)); // 没有1.0f进度条不更新
            }

            @Override
            public void onReqSuccess(Object result) {
                // 上传本地图片到服务器
                Log.d(TAG,"Upload success!");
                mProgressBar.setVisibility(View.GONE);
                mIsUploading =false;
                Toast.makeText(PublishPostActivity.this,"文中图片上传成功",Toast.LENGTH_LONG).show();
                Log.d(TAG,"Success return results :" + (String)result);
                /*返回本地文件与服务器文件url的对应,只有文件名没有文件的本地路径
                {
                    "2.jpg": "http://api.mmdkid.cn/uploads/image/20171229/df983b7450e256520814659b7b8976cd.jpg",
                    "3.jpg": "http://api.mmdkid.cn/uploads/image/20171229/3111a7e89da98ba87e80e34349162474.jpg",
                    "3bf33a87e950352ab1faf8fa5343fbf2b3118bad.jpg": "http://api.mmdkid.cn/uploads/image/20171229/182e5e2d183420cf274183dc3ae9af63.jpg"
                }*/
                // 用服务器上的图片地址替代本地地址
                getPostHtml((String)result);
                // 发布post到服务器
                pulishPost();
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
                Toast.makeText(PublishPostActivity.this,"上传文中图片失败",Toast.LENGTH_LONG).show();
            }
        });
    }
    /**
     * 返回本地文件与服务器文件url的对应,只有文件名没有文件的本地路径
        {
            "2.jpg": "http://api.mmdkid.cn/uploads/image/20171229/df983b7450e256520814659b7b8976cd.jpg",
            "3.jpg": "http://api.mmdkid.cn/uploads/image/20171229/3111a7e89da98ba87e80e34349162474.jpg",
            "3bf33a87e950352ab1faf8fa5343fbf2b3118bad.jpg": "http://api.mmdkid.cn/uploads/image/20171229/182e5e2d183420cf274183dc3ae9af63.jpg"
        }
     */
    private void getPostHtml(String result) {
        try {
            String key;   // 对应本地文件文件名称
            String value; // 对应服务器文件的url
            String image; // 对应本地文件路径及文件名称
            mPostHtml = HTML_TAG_START + mEditor.getHtml() + HTML_TAG_END; // 调整发布文章的显示 增加p标签
            JSONObject jsonObject = new JSONObject((String)result);
            Iterator iterator = jsonObject.keys();
            while(iterator.hasNext()){
                key = (String) iterator.next();
                value = (String)jsonObject.getString(key);
                if ((image=Utility.findStringInList(mImageList,key))!=null){
                    Log.d(TAG,"Find local file:"+image);
                    Log.d(TAG,"Server url: " +value);
                    mPostHtml=mPostHtml.replace(image,value);
                }
            }
            Log.d(TAG,"Final Html is :"+mPostHtml);
            return;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     *  发布一篇文章到服务器
     */
    private void pulishPost(){
        Post post = new Post();
        post.title = mTitleView.getText().toString();
        post.content = mPostHtml;
        post.type = Post.TYPE_PUBLIC;
        post.status = Post.STATUS_ACTIVE;
        post.location = mAddressText;
        post.longitude = mCurrentLocation.getLongitude();
        post.latitude = mCurrentLocation.getLatitude();
        mIsUploading = true;
        post.save(Model.ACTION_CREATE,this,restapiListener);
    }
    /**
     *  文章发布监听
     */
    private RESTAPIConnection.OnConnectionListener restapiListener = new RESTAPIConnection.OnConnectionListener() {
        @Override
        public void onErrorRespose(Class c, String error) {
            Toast.makeText(PublishPostActivity.this,"发布文章失败~"+error,Toast.LENGTH_LONG).show();
            mIsUploading =false;
        }

        @Override
        public void onResponse(Class c, ArrayList responseDataList) {
            mIsUploading =false;
            if(!responseDataList.isEmpty()){
                Log.d(TAG,"Get the post from the server ");
                Post post = (Post) responseDataList.get(0);
                Log.d(TAG,"Post id is " + post.id);
                Log.d(TAG,"Post title is  " + post.title);
                Log.d(TAG,"Post content is  " + post.content);
                Log.d(TAG,"Post status is " + post.status);
                Log.d(TAG,"Post type is " + post.type);
                Toast.makeText(PublishPostActivity.this,"发布文章成功~",Toast.LENGTH_LONG).show();
                finish();
            }else {
                Toast.makeText(PublishPostActivity.this,"发布文章失败~",Toast.LENGTH_LONG).show();
            }
        }
    };
    /**
     *  检验文章内容
     */
    private boolean isPostValid(String s) {
        return !s.isEmpty();
    }
    /**
     *  检验文章标题
     */
    private boolean isTitleValid(String s) {
        return s.length()<50 && !s.isEmpty();
    }

    /**
     *  初始化布局
     */
    private void initView() {
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_upload_post) ;
        mTitleView = (EditText) findViewById(R.id.evTitle);
        mEditor = (RichEditor) findViewById(R.id.editor);
        mEditor.loadCSS("style.css");
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(18);
        mEditor.setEditorFontColor(Color.BLACK);
        //mEditor.setEditorBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundResource(R.drawable.bg);
        mEditor.setPadding(10, 10, 10, 10);
        //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        mEditor.setPlaceholder("正文");
        //mEditor.setInputEnabled(false);

        mPreview = (TextView) findViewById(R.id.preview);
        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override public void onTextChange(String text) {
                mPreview.setText(text);
            }
        });

        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.redo();
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setItalic();
            }
        });

       /* findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });*/

        /*findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setStrikeThrough();
            }
        });*/

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

      /*  findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override public void onClick(View v) {
                mEditor.setTextColor(isChanged ? Color.BLACK : Color.RED);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override public void onClick(View v) {
                mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setNumbers();
            }
        });*/

        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                /*mEditor.insertImage("http://www.1honeywan.com/dachshund/image/7.21/7.21_3_thumb.JPG",
                        "dachshund");*/
                // 弹出图片选择器 可以多选
                Matisse.from(PublishPostActivity.this)
                        .choose(MimeType.ofImage())
                        .countable(true)
                        .maxSelectable(MAX_IMAGE_NUM) // 单次选择允许的最大图片数
                        //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .theme(R.style.Matisse_Dracula)
                        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85f)
                        .imageEngine(new PicassoEngine())
                        .forResult(REQUEST_CODE_CHOOSE);
                // 上传所选图片到服务器
                // 成功 返回图片的超链 插入当前文档
                // 失败 报错
            }
        });

        /*findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.insertLink("https://github.com/wasabeef", "wasabeef");
            }
        });*/
        /*findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.insertTodo();
            }
        });*/
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            // 加入所有选择的所有图片
            for(Uri uri :  Matisse.obtainResult(data)) {
                mEditor.insertImage("file://" + Utility.getPath(this,uri),"dachshund");
                //mEditor.insertImage(uri.getPath(),"ll");
                //Log.d(TAG,"Insert image uri.getPath: "+uri.getPath());
                Log.d(TAG,"Insert image url is :" +"file://" + Utility.getPath(this,uri) );
            }
        }
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

    Handler mHandler = new Handler() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case MESSAGE_COMPRESS_IMAGE_START:
                    mIsCompressing = true;
                    showProgressDialog("正在压缩图片...");
                    break;
                case MESSAGE_COMPRESS_IMAGE_FINISH:
                    mIsCompressing = false;
                    dismissProgressDialog();
                    // 压缩图片完成 上传文章和图片
                    try {
                        upload();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        Toast.makeText(PublishPostActivity.this,"发布文章错误",Toast.LENGTH_LONG).show();
                    }
                    break;
                case MESSAGE_COMPRESS_IMAGE_ERROR:
                    mIsCompressing = false;
                    dismissProgressDialog();
                    Toast.makeText(PublishPostActivity.this,"压缩文章中图片错误",Toast.LENGTH_LONG).show();
                    // 清空压缩图片列表
                    mCompressedImageFileList.clear();
                    break;
                case MESSAGE_UPDATE_ADDRESS:
                    //位置变化 更改位置显示
                    mAddressText= (String) msg.obj;
                    break;
            }

        }
    };

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
                    for(int i =0 ; i < mImageList.size(); i++){
                        //URI jURI = new URI(uri.toString());
                        //File file = new File(jURI);
                        Log.d(TAG,"File uri :" + mImageList.get(i).toString());
                        Log.d(TAG,"File path :" +HtmlUtil.getString(mImageList.get(i),"file://"));
                        File file = new File(HtmlUtil.getString(mImageList.get(i),"file://"));
                        File compressedFile = ImageUtil.compress(PublishPostActivity.this,file);
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
                    // 发送压缩图片错误消息
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
        (new PublishPostActivity.ReverseGeocodingTask(this)).execute(new Location[] {location});
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
}
