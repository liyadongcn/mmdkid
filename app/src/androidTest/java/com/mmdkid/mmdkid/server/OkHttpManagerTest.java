package com.mmdkid.mmdkid.server;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Test;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by LIYADONG on 2017/12/25.
 */
public class OkHttpManagerTest {
    private final static String TAG = "OkHttpManagerTest";

    @Test
    public void getInstance() throws Exception {
    }

    @Test
    public void upLoadFile() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        OkHttpManager manager = OkHttpManager.getInstance(appContext);
        manager.setAccessToken("920fb189da47f1c6418a11b5b449fdf17db9250b");
        IdentityHashMap<String, Object> paramsMap = new IdentityHashMap<String, Object>();
        paramsMap.put("title","测试图片上传功能");
        Log.d(TAG,Environment.getExternalStorageDirectory().getPath()+"faceImage.jpg");
        File uploadFile1 = new File(Environment.getExternalStorageDirectory().getPath(),"faceImage.jpg");
        File uploadFile2 = new File(Environment.getExternalStorageDirectory().getPath(),"111.jpg");
        paramsMap.put(new String("file[]"),uploadFile1);
        paramsMap.put(new String("file[]"),uploadFile2);
        paramsMap.put("status",10);
        for (String key : paramsMap.keySet()) {
            Object object = paramsMap.get(key);
            if (!(object instanceof File)) {

            } else {
                File file = (File) object;
                Log.d(TAG,"Map key:"+key);
                Log.d(TAG,"File Name :" + file.getName());
                Log.d(TAG,"File Path :" + file.getPath());
                Log.d(TAG,"File exit :" + file.exists());
                Log.d(TAG,"File length :" + file.length());

            }
        }
        final CountDownLatch latch = new CountDownLatch(1); //创建CountDownLatch 用于异步测试
        manager.upLoadFile("image-posts", paramsMap, new OkHttpManager.ReqProgressCallBack<Object>() {

            @Override
            public void onProgress(long total, long current) {
                Log.d(TAG,"Upload total is : "+total +"---------->"+current);
            }

            @Override
            public void onReqSuccess(Object result) {
                Log.d(TAG,"Upload success!");
                latch.countDown();  //这里countDown，外面的await()才能结束
            }

            @Override
            public void onReqFailed(String errorMsg) {
                Log.d(TAG,"Upload failed. " + errorMsg);
                latch.countDown();  //这里countDown，外面的await()才能结束
            }
        });
        latch.await();
    }

    @Test
    public void upLoadFile1() throws Exception {
    }

    @Test
    public void upLoadFile2() throws Exception {
    }

    @Test
    public void createProgressRequestBody() throws Exception {
    }

}