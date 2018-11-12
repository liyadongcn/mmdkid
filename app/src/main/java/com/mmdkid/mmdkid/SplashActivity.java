package com.mmdkid.mmdkid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mmdkid.mmdkid.helper.AdUtil;
import com.mmdkid.mmdkid.models.Advertisement;

import java.io.File;

/**
 * APP首次启动显示引导页面
 * 非首次启动，若有广告则显示广告页面，用户可以点击跳过提前终止广告显示
 * 广告显示时间到自动进入APP主页面
 */
public class SplashActivity extends AppCompatActivity {

    private final static String TAG = "SplashActivity";
    /**
     * APP是否为首次启动
     */
    private boolean isFirstStart = true;
    /**
     * 显示广告
     */
    private boolean showAd = true;
    /**
     * 用户跳过广告
     */
    private boolean isSkipAd = false;
    /**
     * 广告内容视图
     */
    private ImageView mAdvertisementView;
    /**
     * 跳过广告视图
     */
    private TextView mSkipView;
    /**
     * 广告持续显示时间长度，单位：秒
     */
    private int mAdTime = 5;
    /**
     * 广告显示计时器
     */
    private Handler mAdHandler;
    /**
     * 防止用户即点击了广告又点击了跳过广告
     */
    private boolean isClicked =false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        isFirstStart = App.isFirstStart(this);
        if (isFirstStart || App.isAppUpdated(this)) {
            // 首次启动 或者app升级 显示引导页
            Intent intent = new Intent(this, GuideActivity.class);
            startActivity(intent);
            finish();
        }else if (showAd){
            // 需要播放广告
            initView();
            Log.d(TAG,"Finish initing the view.");
            mAdHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    mAdTime = mAdTime-1;
                    Log.d(TAG,"Ad Time left:" + mAdTime);
                    if (mAdTime > 0 && !isSkipAd) {
                        // 广告播放未超时 未跳过 继续计时
                        mAdHandler.sendEmptyMessageDelayed(0, 1000);
                    }else{
                        if (!isSkipAd){
                            // 广告播放超时 用户没有跳过 自动进入主程序
                            toMainActivity();
                        }
                    }
                }
            };
            // 发送广告计时开始消息
            mAdHandler.sendEmptyMessageDelayed(0, 1000);
        }
        else{
            // 非首次启动 延迟2S跳转
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toMainActivity();
                }
            }, 2000);
        }

    }
    /**
     * 只有需要播放广告的时候初始化页面
     */
    public void initView(){
        //隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐藏导航栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //广告显示处理
        mAdvertisementView = (ImageView) findViewById(R.id.iv_advertising);
        final  Advertisement  Advertisement = AdUtil.getAdvertisement(this);
        if ( Advertisement!=null){
            // 存在需要播放的广告
            File file = new File( Advertisement.mLocalPath);
            if (file.exists()){
                // 广告缓存的图片文件存在 显示该图片
                mAdvertisementView.setImageURI(Uri.fromFile(file));
                if ( Advertisement.mUrl!=null && ! Advertisement.mUrl.isEmpty()){
                    // 广告有链接 则设置点击跳转
                    mAdvertisementView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isClicked) return;
                            isClicked = true;
                            //跳转到广告的链接
                            isSkipAd =true;
                            Intent intent = new Intent(SplashActivity.this,WebViewActivity.class);
                            intent.putExtra("url", Advertisement.mUrl);
                            intent.putExtra("showType",WebViewActivity.SHOW_TYPE_ADVERTISEMENT);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }else{
                // 广告图片不存在 直接进入主程序
                toMainActivity();
            }
        }else{
            // 不存在需要播放的广告
            toMainActivity();
        }

        mSkipView = (TextView) findViewById(R.id.tvSkip);
        mSkipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked) return;
                isClicked = true;
                // 用户跳过广告
                toMainActivity();
            }
        });
    }
    private void toMainActivity(){
        // 停止消息发送
        isSkipAd = true;
        // 启动主程序
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
