package com.mmdkid.mmdkid.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.models.Advertisement;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 广告工具类
 */
public class AdUtil {
    private static final String TAG = "AdUtil";

    /**
     * 从服务器取得的广告数目计数器
     * 用于广告图片下载计数
     * 没下载完成一个计数器减一
     */
    private static int mAdsCount;
    /**
     * 缺省的从服务器获取广告的时间间隔
     * 单位毫秒
     */
    public static final long DEFAULT_PERIOD_TIME = 10*60*1000;
    /**
     * 获取本地缓存的广告
     */
    public static ArrayList<Advertisement> getAdvertisements(Context context){
        ListDataSave adListDataSave = new ListDataSave(context, App.PREFS_ADS);
        List<Advertisement> ads = adListDataSave.getDataList(App.PREF_ADS,Advertisement.class);
        Log.d(TAG,"Logs is " + ads.toString());
        return (ArrayList<Advertisement>) ads;
    }
    /**
     * 保存广告到本地
     */
    private static void setAdvertisements(Context context, ArrayList<Advertisement> adList){
        if (adList==null || adList.isEmpty()){
            // 删除广告列表
            SharedPreferences settings = context.getSharedPreferences(App.PREFS_ADS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(App.PREF_ADS);
            editor.commit();
        }else{
            // 保存广告列表
            ListDataSave adListDataSave = new ListDataSave(context, App.PREFS_ADS);
            adListDataSave.setDataList(App.PREF_ADS, adList);
        }
    }
    /**
     * 获取本地保存的上次广告检测时间
     */
    private static String getAdCheckTime(Context context){
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_ADS, Context.MODE_PRIVATE);
        String checkTime = settings.getString(App.PREF_ADS_CHECK_TIME,"");
        if (checkTime.isEmpty()){
            Log.d(TAG,"Check time is empty.");
            // 缺省返回当前时间前两天的广告信息
           checkTime = Utility.getTimeByHour(-48);
           setAdCheckTime(context,checkTime);
        }
        Log.d(TAG,"Check time :"+checkTime);
        return  checkTime;
    }
    /**
     * 保存上次广告检测时间
     */
    private static boolean setAdCheckTime(Context context,String checkTime){
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_ADS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(App.PREF_ADS_CHECK_TIME,checkTime);
        return editor.commit();
    }
    /**
     * 增加一个有效缓存的广告到本地
     */
    private static boolean addAdvertisement(Context context, Advertisement advertisement){
        ArrayList<Advertisement> adList = getAdvertisements(context);
        if (!advertisement.isCacheValid()) return false;
        if (adList==null){
            adList = new ArrayList<Advertisement>();
        }
        // 去掉重复广告信息
        for (int i = 0; i < adList.size(); i++){
            if (advertisement.mId == adList.get(i).mId){
                adList.remove(i);
                i--;
            }
        }
        adList.add(advertisement);
        setAdvertisements(context,adList);
        return true;
    }
    /**
     * 从服务器端获取广告 定时任务
     */
    public static void getAdvertisementsFromServer(final Context context, long period){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG,"Start to get the advertisements from the server.");
                Advertisement.getAdvertisements(context, getAdCheckTime(context), new RESTAPIConnection.OnConnectionListener() {
                    @Override
                    public void onErrorRespose(Class c, String error) {
                        Log.d(TAG,"Get the advertisements from the server failed." + error);
                    }

                    @Override
                    public void onResponse(Class c, ArrayList responseDataList) {
                        Log.d(TAG,"Get the advertisements from the server success.Records number: " + responseDataList.size() );
                        // 记录广告检测时间
                        setAdCheckTime(context,Utility.dateToString(new Date(),"yyyy-MM-dd HH:mm:ss"));
                        if(c==Advertisement.class && !responseDataList.isEmpty()){
                            // 获得新广告
                            Advertisement advertisement;
                            String fileName;
                            // 本地缓存目录
                            String cachePath = getCachePath(context);
                            if (!cachePath.isEmpty()){
                                for (int i = 0; i<responseDataList.size();i++){
                                    advertisement = (Advertisement)responseDataList.get(i);
                                    // 广告图片的名字
                                    fileName = Utility.getFileNameFromUrl(advertisement.mImgUrl);
                                    if (!fileName.isEmpty()){
                                        Log.d(TAG,"Ad image url image file name: "+ fileName);
                                        // 下载广告图片到本地缓存目录
                                        saveImageFromDataSource(advertisement,cachePath+"/"+fileName,context);
                                    }
                                }
                            }else {
                                Log.d(TAG,"Can not get the cache path.");
                            }

                        }
                    }
                });
            }
        },1000,period);
    }
    /**
     * 获取广告图片缓存目录
     * 若不存在就创建
     */
    private static String getCachePath(Context context){
        String cachePath = context.getExternalCacheDir()+"/ad-images";
        File file = new File(cachePath);
        if (file.exists() && file.isDirectory()){
            // 缓存目录存在
            Log.d(TAG,"缓存目录存在");
            return cachePath;
        }else {
            // 缓存目录不存在则创建该目录
            Log.d(TAG,"缓存目录不存在则创建该目录");
            if (file.mkdir()){
                return cachePath;
            }else{
                return "";
            }
        }
    }
    /**
     * 获取当前可播放广告
     * 并删除无效广告
     */
    public static Advertisement getAdvertisement(Context context){
//        Advertisement advertisement = new Advertisement();
//        advertisement.mUrl = "http://www.baidu.com";
//        advertisement.mLocalPath = context.getExternalCacheDir().getPath()+"/ad-images/111.jpg";
//        Log.d(TAG,"Local Path:" + advertisement.mLocalPath);
        // 读取本地存储的所有缓存广告
        ArrayList<Advertisement> adList = getAdvertisements(context);
        // 广告是否有效 时间有效 缓存的图片有效
        if (adList.isEmpty()) return null;
        for (int i =0 ; i<adList.size(); i++){
            if (!adList.get(i).isCacheValid()) {
                // 删除无效广告
                Log.d(TAG,"Delete one invalid advertisment");
                adList.remove(i);
                i--;
            }
        }
        // 存储有效广告到本地
        Log.d(TAG,"Save the valid advertsements. total:" +adList.size() );
        setAdvertisements(context,adList);
        if (adList.isEmpty()){
            return null;
        }else {
            // 随机选取一个广告播放
            return adList.get((int)(Math.random()*adList.size()));
        }
    }
    /**
     * 获取广告网络图片并保存到本地
     * 使用fresco下载网络图片
     */
    private static void saveImageFromDataSource(final Advertisement advertisement, final String localSavePath, final Context context){
        String url = advertisement.mImgUrl;
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true).build();

        DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline()
                .fetchDecodedImage(imageRequest, context);

        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> source) {
            }

            @Override
            protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> source) {
                CloseableReference<CloseableImage> reference = source.getResult();
                CloseableImage image = reference.get();

                if(image instanceof CloseableBitmap){
                    CloseableBitmap bitmapimage = (CloseableBitmap) image;//图片转为bitmap
                    Bitmap picbitmap = bitmapimage.getUnderlyingBitmap();
                    if (saveBitmap(picbitmap, localSavePath)){
                        advertisement.mLocalPath = localSavePath;
                        // 广告图片成功缓存到本地 增加一个本地广告存储
                        addAdvertisement(context,advertisement);
                    }
                }
            }
        }, CallerThreadExecutor.getInstance());
    }

    private static Boolean saveBitmap(Bitmap bitmap, String localSavePath) {

        if (TextUtils.isEmpty(localSavePath)) {
            throw new NullPointerException("保存的路径不能为空");
        }

        File f = new File(localSavePath);
        if (f.exists()) {// 如果本来存在的话，删除
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;

        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }

        return true;

    }
}
