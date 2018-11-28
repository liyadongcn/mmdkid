package com.mmdkid.mmdkid;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.mmdkid.mmdkid.channel.ChannelEntity;
import com.mmdkid.mmdkid.helper.ContactUtil;
import com.mmdkid.mmdkid.helper.DeviceUtil;
import com.mmdkid.mmdkid.helper.ListDataSave;
import com.mmdkid.mmdkid.models.ActionLog;
import com.mmdkid.mmdkid.models.Advertisement;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.mmdkid.mmdkid.singleton.ActionLogs;
import com.mmdkid.mmdkid.update.CheckUpdateUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.youku.cloud.player.YoukuPlayerConfig;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/*
 * Created by Alexander Krol (troy379) on 29.08.16.
 */
public class App extends Application {
    private static final String TAG = "mmdApp";
    /**
     * 有关APP的设置
     */
    public static final String  PREFS_NAME = "momoda";
    public static final String  PREF_FIRST_START = "first_start";
    private static final String PREF_COOKIES = "cookies";
    private static final String PREF_LOGS = "logs";
    /**
     * 有关搜索的设置
     */
    private static final String PREFS_SEARCH = "com.mmdkid.mmdkid.SEARCH";// 本地的搜索关键词设置记录
    private static final String PREF_HOT_KEYWORDS = "hotKeyWords";
    private static final String PREF_HISTORY_KEYWORDS = "historyKeyWords";
    /**
     * 有关频道的设置
     */
    private static final String PREF_CHANNELS = "channels";
    private static final String PREF_OTHER_CHANNELS = "otherchannels";
    /**
     * 有关广告的设置
     */
    public static final String PREFS_ADS = "ads_info"; // 广告库名
    public static final String PREF_ADS = "ads"; // 广告列表
    public static final String PREF_ADS_CHECK_TIME = "ads_check_time"; // 上次广告检测时间
    public static final String PREF_VERSION_CODE = "version_code"; // APP版本号，用于判断app是否已经升级
    /**
     * 有关上传服务器的设置
     */
    public static final String  PREFS_UPLOAD_INFO = "upload_info"; //有关上传服务器信息的设置记录
    public static final String  PREF_DEVICE_CONTACT = "device&contact"; //设备及联系人信息是否已经上传服务器

    private boolean mIsGuest = true;

    //请在这里输入你的Youku应用的clientId，clientSecret
    public static final String CLIENT_ID_WITH_AD = "5c53df88f2e6681c";
    public static final String CLIENT_SECRET_WITH_AD = "cfe544d362b97ec00da0dfd29aceceec";

    private boolean isNightMode = false;

    public static final String NOTIFICATION_CHANNEL_ID = "channel";


   /* @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
*/
   public boolean isNightMode(){
       return isNightMode;
   }

   public void setNightMode(boolean nightMode){
       isNightMode = nightMode;
   }

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
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "d06f5c7afaafa3bd3f4ec5b9484f6d21");
        PlatformConfig.setWeixin("wxd351513db5e7e7cd", "0a0317230e8462e0b355bce31a6bd80d");
        PlatformConfig.setQQZone("1106209187", "b4GtyTWBLyBw84XP");
        PlatformConfig.setSinaWeibo("1829150753", "0a0d5416fa055a9da67a58f51526d9e7"
                ,"http://sns.whalecloud.com");
        //需要查看友盟调试信息时打开
        //Config.DEBUG = true;
        UMShareAPI.get(this);
        //场景类型设置
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
       /*
        * 友盟消息推送注册
        * 参考：http://dev.umeng.com/push/android/integration#2_2_7
        * 注意：
            请勿在调用register方法时做进程判断处理（主进程和channel进程均需要调用register方法才能保证长连接的正确建立）。
            若有需要，可以在Application的onCreate方法中创建一个子线程，并把mPushAgent.register这一行代码放到该子线程中去执行（请勿将PushAgent.getInstance(this)放到子线程中）。
            device token是友盟+生成的用于标识设备的id，长度为44位，不能定制和修改。同一台设备上不同应用对应的device token不一样。
            如需手动获取device token，可以调用mPushAgent.getRegistrationId()方法（需在注册成功后调用）。
        */
        PushAgent mPushAgent = PushAgent.getInstance(this);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                Log.d(TAG,"Device token is : " + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.d(TAG,"Register Device failure : " + s + s1);
            }
        });

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

        // 生成通知渠道
        createNotificationChannel();

        // 忽略https访问中的证书认证 未验证成功该功能2018/11/08
        //handleSSLHandshake();
    }
    /**
     * 生成通知渠道
     * 参考:https://developer.android.google.cn/training/notify-user/build-notification#java
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public User getCurrentUser(){
        return User.loadFromLocal(this);
    }

    public void refreshCurrentUserInfo() {
        User currentUser = this.getCurrentUser();
        if (currentUser!=null){
            User.find(this, new RESTAPIConnection.OnConnectionListener() {
                @Override
                public void onErrorRespose(Class c, String error) {
                    Log.d(TAG,"Refresh user information error.");
                }

                @Override
                public void onResponse(Class c, ArrayList responseDataList) {
                    if(!responseDataList.isEmpty()){
                        User user = (User)responseDataList.get(0);
                        Log.d(TAG,"The user name is :" + user.mUsername );
                        Log.d(TAG,"The user avatar is :" + user.mAvatar );
                        Log.d(TAG,"The user nickname is :" + user.mNickname );
                        Log.d(TAG,"The user cellphone is :" + user.mCellphone );
                        Log.d(TAG,"The user Email is :" + user.mEmail );
                        user.saveToLocal(App.this);
                    }
                }
            }).where("id",Integer.toString(currentUser.mId)).all();
        }
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
        editor.putStringSet(PREF_COOKIES,new HashSet<String>(cookies));
        editor.commit();
    }

    public List<String> getCookies(){
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME,Context.MODE_PRIVATE);
        Set<String> cookieSet = settings.getStringSet(PREF_COOKIES,null);
        if(cookieSet == null || cookieSet.isEmpty()) {
            return null;
        }else{
            List<String> cookieList = new ArrayList<String>();
            cookieList.addAll(cookieSet);
            return cookieList;
        }

    }
    /**
     * 从本地SharedPreferences得到历史搜索词列表
     */
    public ArrayList<String> getHistoryKeyWords(){
        SharedPreferences settings = getSharedPreferences(PREFS_SEARCH,Context.MODE_PRIVATE);
        Set<String> wordsSet = settings.getStringSet(PREF_HISTORY_KEYWORDS,null);
        if(wordsSet == null || wordsSet.isEmpty()) {
            return null;
        }else{
            ArrayList<String> list = new ArrayList<String>();
            list.addAll(wordsSet);
            return  list;
        }
    }
    /**
     * 保存历史搜索词到本地SharedPreferences
     */
    public void setHistoryKeyWords(ArrayList<String> words){
        SharedPreferences settings = getSharedPreferences(PREFS_SEARCH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(PREF_HISTORY_KEYWORDS,new LinkedHashSet<String>(words));
        editor.commit();
    }
    /**
     * 清空本地SharedPreferences中的历史搜索词列表
     */
    public void clearHistoryKeyWords(){
        SharedPreferences settings = getSharedPreferences(PREFS_SEARCH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(PREF_HISTORY_KEYWORDS,null);
        editor.commit();
    }

    /**
     * 从本地SharedPreferences得到热搜词汇列表
     */
    public ArrayList<String> getHotKeyWords(){
        SharedPreferences settings = getSharedPreferences(PREFS_SEARCH,Context.MODE_PRIVATE);
        Set<String> wordsSet = settings.getStringSet(PREF_HOT_KEYWORDS,null);
        if(wordsSet == null || wordsSet.isEmpty()) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("幼升小");
            list.add("小升初");
            list.add("中考");
            list.add("高考");
            list.add("英语");
            return list;
        }else{
            ArrayList<String> list = new ArrayList<String>();
            list.addAll(wordsSet);
            return  list;
        }
    }
    /**
     * 将词汇列表保存到本地热搜词列表SharedPreferneces中
     */
    public void setHotKeyWords(ArrayList<String> words){
        SharedPreferences settings = getSharedPreferences(PREFS_SEARCH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(PREF_HOT_KEYWORDS,new LinkedHashSet<String>(words));
        editor.commit();
    }
    /**
     * 清空本地热搜词列表
     */
    public void clearHotKeyWords(){
        SharedPreferences settings = getSharedPreferences(PREFS_SEARCH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(PREF_HOT_KEYWORDS,null);
        editor.commit();
    }
    /**
     *  读取用户设置的频道
     */
    public  ArrayList<ChannelEntity> getChannels(){
        ListDataSave channelListDataSave = new ListDataSave(this,PREFS_NAME);
        List<ChannelEntity> channels = channelListDataSave.getDataList(PREF_CHANNELS,ChannelEntity.class);
        Log.d(TAG,"Channels is " + channels.toString());
        if (channels==null || channels.isEmpty()){
            return getDefaultChannels();
        }
        Log.d(TAG,"Get the channels from the channels tag.");
        return (ArrayList<ChannelEntity>) channels;
    }
    /**
     *  用户首次使用APP，缺省频道设置
     */
    private ArrayList<ChannelEntity> getDefaultChannels(){
        // 初次使用用户没有设置频道，系统默认显示前5个频道
        String[] favorites = getResources().getStringArray(R.array.favorites);
        String[] favorite_values = getResources().getStringArray(R.array.favorite_values);
        ArrayList<ChannelEntity> channelEntities = new ArrayList<ChannelEntity>();
        Log.d(TAG,"Favorites is " + favorites.toString());
        // 缺省设置前5个为缺省显示频道
        for (int i =0; i < 5 ; i++ ){
            ChannelEntity channelEntity= new ChannelEntity();
            channelEntity.setName(favorites[i]);
            channelEntity.setId(i);
            channelEntities.add(channelEntity);
        }
        setChannels(channelEntities);
        return channelEntities;
    }
    /**
     *  保存用户设置的频道
     */
    public void setChannels(ArrayList<ChannelEntity> channelEntities){
        ListDataSave channelListDataSave = new ListDataSave(this, App.PREFS_NAME);
        channelListDataSave.setDataList(PREF_CHANNELS,channelEntities);
    }
    /**
     *  清空用户设置的频道
     */
    public void clearChannels(){
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_CHANNELS,null);
        editor.commit();
    }
    /**
     *  读取用户未使用的频道
     */
    public  ArrayList<ChannelEntity> getOtherChannels(){
        ListDataSave channelListDataSave = new ListDataSave(this, App.PREFS_NAME);
        List<ChannelEntity> channels = channelListDataSave.getDataList(PREF_OTHER_CHANNELS,ChannelEntity.class);
        Log.d(TAG,"Other Channels is " + channels.toString());
        if (channels==null || channels.isEmpty()){
           return getDefaultOtherChannels();
        }
        Log.d(TAG,"Get the channels from the channels tag.");
        return (ArrayList<ChannelEntity>) channels;
    }

    private ArrayList<ChannelEntity> getDefaultOtherChannels(){
        String[] favorites = getResources().getStringArray(R.array.favorites);
        String[] favorite_values = getResources().getStringArray(R.array.favorite_values);
        ArrayList<ChannelEntity> channelEntities = new ArrayList<ChannelEntity>();
        Log.d(TAG,"Favorites is " + favorites.toString());
        // 用户未设置频道，缺省其他频道为5个以后的频道，前5个为默认选择频道
        for (int i = 5; i< favorites.length; i++){
            ChannelEntity channelEntity= new ChannelEntity();
            channelEntity.setName(favorites[i]);
            channelEntity.setId(i);
            channelEntities.add(channelEntity);
            Log.d(TAG,"Favorite name is " + favorites[i]);
            Log.d(TAG,"Favorite id is " + i);
        }
        setOtherChannels(channelEntities);
        return channelEntities;
    }
    /**
     *  保存用户未设置使用的频道
     */
    public void setOtherChannels(ArrayList<ChannelEntity> channelEntities){
        ListDataSave channelListDataSave = new ListDataSave(this, App.PREFS_NAME);
        channelListDataSave.setDataList(PREF_OTHER_CHANNELS,channelEntities);
    }

    /**
     *  保存用户使用记录
     */
    public void setLogs(ArrayList<ActionLog> logList) {
        ListDataSave logListDataSave = new ListDataSave(this, App.PREFS_NAME);
        logListDataSave.setDataList(PREF_LOGS, logList);
    }

    /**
     *  读取用户使用记录
     */
    public ArrayList<ActionLog> getLogs() {
        ListDataSave logListDataSave = new ListDataSave(this, App.PREFS_NAME);
        List<ActionLog> logs = logListDataSave.getDataList(PREF_LOGS,ActionLog.class);
        Log.d(TAG,"Logs is " + logs.toString());
       /* if (channels==null || channels.isEmpty()){
            return getDefaultOtherChannels();
        }
        Log.d(TAG,"Get the channels from the channels tag.");*/
        return (ArrayList<ActionLog>) logs;
    }
    /**
     *  清除使用记录
     */
    public void clearLogs() {
        SharedPreferences settings = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_LOGS,null);
        editor.commit();
    }
    public static void setDeviceContactUploaded(Context context,boolean isUploaded){
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_UPLOAD_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_DEVICE_CONTACT,isUploaded);
        editor.commit();
    }
    public static boolean isDeviceContactUploaded(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_UPLOAD_INFO, Context.MODE_PRIVATE);
        return  settings.getBoolean(PREF_DEVICE_CONTACT,false);
    }
    /**
     * APP是否为首次启动
     */
    public static boolean isFirstStart(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return  settings.getBoolean(PREF_FIRST_START,true);
    }
    /**
     * 设置是否为首次启动
     */
    public static boolean setFirstStart(Context context,boolean isFirstStart){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_FIRST_START,isFirstStart);
        return editor.commit();
    }
    /**
     * 本地存储的版本号
     */
    public static int getVersionCode(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return  settings.getInt(PREF_VERSION_CODE,0);
    }
    /**
     * 设置本地存储的版本号
     */
    public static boolean setVersionCode(Context context,int versionCode){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(PREF_VERSION_CODE,versionCode);
        return editor.commit();
    }
    /**
     * 判断app是否已经升级
     */
    public static boolean isAppUpdated(Context context){
        int versionCode = getVersionCode(context);
        int currentVersionCode = CheckUpdateUtil.getVersionCode(context);
        if (versionCode == 0 || currentVersionCode>versionCode) {
            // 运行的版本已经升级
            setVersionCode(context,currentVersionCode);
            return true;
        }else {
            // 运行的版本未升级
            return false;
        }
    }
    /**
     * 忽略证书验证
     * 未验证成功该功能 2018/11/08
     */
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

}

