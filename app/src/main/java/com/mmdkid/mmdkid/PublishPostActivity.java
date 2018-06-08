package com.mmdkid.mmdkid;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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

import com.mmdkid.mmdkid.helper.HtmlUtil;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
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
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void publish() throws URISyntaxException {
        boolean cancel = false;
        View focusView = null;

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
                // 上传所有的本地文件到服务器 并等待结果
                uploadImage();
            }

        }

    }
    /**
     *  上传文章中所有的本地图片到服务器
     */
    private void uploadImage() throws URISyntaxException {
        App app = (App)getApplication();
        if (app.isGuest()){
            Toast.makeText(PublishPostActivity.this,"还没有登录，不能发布!",Toast.LENGTH_LONG).show();
            return ;
        }
        Token token = app.getCurrentToken();
        OkHttpManager manager = OkHttpManager.getInstance(this);
        manager.setAccessToken(token.mAccessToken);
        IdentityHashMap<String, Object> paramsMap = new IdentityHashMap<String, Object>();
        for(int i =0 ; i < mImageList.size(); i++){
            Log.d(TAG,"File path :" +mImageList.get(i));
            //URI uri = new URI(mImageList.get(i));
            File file = new File(HtmlUtil.getString(mImageList.get(i),"file://"));
            paramsMap.put(new String("file[]"),file);
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
}
