package com.mmdkid.mmdkid;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mmdkid.mmdkid.adapters.PublishImageAdapter;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.server.OkHttpManager;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.IdentityHashMap;

public class PublishImageActivity extends AppCompatActivity {
    private final static String TAG = "PublishImageActivity";

    private static final int REQUEST_CODE_CHOOSE = 23;

    private ArrayList<Uri> mImageList; // 上传图片的列表，最后一个为增加图片的加号图片

    private ProgressBar mProgressBar;
    private EditText mTitleView;
    private EditText mDescriptionView;
    private GridView mGridView;
    private PublishImageAdapter mAdapter;

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
                finish();
            case R.id.action_publish_image:
                try {
                    publish();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void publish() throws URISyntaxException {

        boolean cancel = false;
        View focusView = null;

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
            App app = (App)getApplication();
            if (app.isGuest()){
                finish();
                return ;
            }
            Token token = app.getCurrentToken();
            OkHttpManager manager = OkHttpManager.getInstance(this);
            manager.setAccessToken(token.mAccessToken);
            IdentityHashMap<String, Object> paramsMap = new IdentityHashMap<String, Object>();
            for(int i =0 ; i < mImageList.size()-1; i++){
                //URI jURI = new URI(uri.toString());
                //File file = new File(jURI);
                Log.d(TAG,"File uri :" + mImageList.get(i).toString());
                Log.d(TAG,"File path :" + mImageList.get(i).getPath());
                Log.d(TAG,"File path :" +Utility.getPath(this,mImageList.get(i)));
                File file = new File(Utility.getPath(this,mImageList.get(i)));
                paramsMap.put(new String("file[]"),file);
            }
            paramsMap.put("title",mTitleView.getText().toString());
            paramsMap.put("content",mDescriptionView.getText().toString());
            paramsMap.put("status",10);
            mProgressBar.setVisibility(View.VISIBLE);
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
                    Toast.makeText(PublishImageActivity.this,"发布成功",Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onReqFailed(String errorMsg) {
                    Log.d(TAG,"Upload failed. " + errorMsg);
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(PublishImageActivity.this,"发布失败",Toast.LENGTH_LONG).show();

                }
            });
        }
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
}
