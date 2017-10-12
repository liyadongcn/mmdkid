package com.mmdkid.mmdkid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.youku.cloud.player.YoukuPlayerConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*
 * Created by Alexander Krol (troy379) on 29.08.16.
 */
public class App extends Application {
    private static final String TAG = "App";

    public static final String PREFS_NAME = "momoda";

    private boolean mIsGuest = true;

    //请在这里输入你的Youku应用的clientId，clientSecret
    public static final String CLIENT_ID_WITH_AD = "5c53df88f2e6681c";
    public static final String CLIENT_SECRET_WITH_AD = "cfe544d362b97ec00da0dfd29aceceec";


   /* @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
*/

    @Override
    public void onCreate() {
        super.onCreate();
        // 优酷播放器设置
        /**设置client_id和client_secret*/
        YoukuPlayerConfig.setClientIdAndSecret(CLIENT_ID_WITH_AD,CLIENT_SECRET_WITH_AD);
        /**sdk初始化*/
        YoukuPlayerConfig.onInitial(this); // 开启后出错
        YoukuPlayerConfig.setLog(false);

        // 友盟设置
        PlatformConfig.setWeixin("wxd351513db5e7e7cd", "0a0317230e8462e0b355bce31a6bd80d");
        PlatformConfig.setQQZone("1106209187", "b4GtyTWBLyBw84XP");
        PlatformConfig.setSinaWeibo("1829150753", "0a0d5416fa055a9da67a58f51526d9e7","http://sns.whalecloud.com/sina2/callback");
        //需要查看友盟调试信息时打开
        Config.DEBUG = true;
        UMShareAPI.get(this);

        /**
         * IMPORTANT! Enable the configuration below, if you expect to open really large images.
         * Also you can add the {@code android:largeHeap="true"} to Manifest file to avoid an OOM error.*/
        /*Set<RequestListener> requestListeners = new HashSet<>();
        requestListeners.add(new RequestLoggingListener());*/
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                //.setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                //.setRequestListeners(requestListeners)
                .build();
        Fresco.initialize(this, config);
        //FLog.setMinimumLoggingLevel(FLog.VERBOSE);
  /*      ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setBitmapMemoryCacheParamsSupplier(bitmapCacheParamsSupplier)
                .setCacheKeyFactory(cacheKeyFactory)
                .setDownsampleEnabled(true)
                .setWebpSupportEnabled(true)
                .setEncodedMemoryCacheParamsSupplier(encodedCacheParamsSupplier)
                .setExecutorSupplier(executorSupplier)
                .setImageCacheStatsTracker(imageCacheStatsTracker)
                .setMainDiskCacheConfig(mainDiskCacheConfig)
                .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
                .setNetworkFetchProducer(networkFetchProducer)
                .setPoolFactory(poolFactory)
                .setProgressiveJpegConfig(progressiveJpegConfig)
                .setRequestListeners(requestListeners)
                .setSmallImageDiskCacheConfig(smallImageDiskCacheConfig)
                .build();
        Fresco.initialize(context, config);*/
    }

    public User getCurrentUser(){
        return User.loadFromLocal(this);
    }

    public Token getCurrentToken(){
        return Token.loadFromLocal(this);
    }


    public void setCurrentUser(User user){
        if(user!=null){
            user.saveToLocal(this);
        }else{
            clearLocalPreferences();
        }
    }

    private void clearLocalPreferences(){
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    public boolean isGuest(){
        return mIsGuest;
    }

    public void setIsGuest(boolean isGuest){
        mIsGuest = isGuest;
    }

    public void logout(){
        clearLocalPreferences();
        mIsGuest = true;
    }

    public boolean isUserAccessTokenValid(){
        Token token = Token.loadFromLocal(this);
        if (token!=null){
            return token.isValid();
        }else{
            return false;
        }
    }

    public void setCookies(List<String> cookies){
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("cookies",new HashSet<String>(cookies));
        editor.commit();
    }

    public List<String> getCookies(){
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME,Context.MODE_PRIVATE);
        Set<String> cookieSet = settings.getStringSet("cookies",null);
        if(cookieSet == null || cookieSet.isEmpty()) {
            return null;
        }else{
            List<String> cookieList = new ArrayList<String>();
            cookieList.addAll(cookieSet);
            return cookieList;
        }

    }

    public ArrayList<String> getHistoryKeyWords(){
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME,Context.MODE_PRIVATE);
        Set<String> wordsSet = settings.getStringSet("historyKeyWords",null);
        if(wordsSet == null || wordsSet.isEmpty()) {
            return null;
        }else{
            ArrayList<String> list = new ArrayList<String>();
            list.addAll(wordsSet);
            return  list;
        }
    }

    public void setHistoryKeyWords(ArrayList<String> words){
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("historyKeyWords",new LinkedHashSet<String>(words));
        editor.commit();
    }

    public void clearHistoryKeyWords(){
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("historyKeyWords",null);
        editor.commit();
    }

}
