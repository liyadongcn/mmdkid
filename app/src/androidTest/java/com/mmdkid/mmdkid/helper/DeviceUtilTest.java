package com.mmdkid.mmdkid.helper;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.mmdkid.mmdkid.models.DeviceInfo;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class DeviceUtilTest {
    private final static String TAG = "DeviceUtilTest";

    @Test
    public void getMac() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.d(TAG,"Device mac address is :" + DeviceUtil.getMac(appContext));
    }

    @Test
    public void getDevice() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.d(TAG,"Device info object is : " + DeviceUtil.getDevice(appContext).toString());
    }
    @Test
    public void getDeviceContactInfo(){
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        final DeviceInfo deviceInfo = DeviceUtil.getDevice(appContext);
        try {
            deviceInfo.mContact = ContactUtil.getContactInfo(appContext);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"Device info object is : " + deviceInfo.toString());
        final CountDownLatch latch = new CountDownLatch(1); //创建CountDownLatch 用于异步测试
        deviceInfo.save(Model.ACTION_CREATE, appContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Device info save to server error.");
                latch.countDown();  //这里countDown，外面的await()才能结束
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c==DeviceInfo.class && !responseDataList.isEmpty()){
                    Log.d(TAG,"Save the device info to server success. record id : " +
                            ((DeviceInfo)responseDataList.get(0)).mId);
                }
                latch.countDown();  //这里countDown，外面的await()才能结束
            }
        });
    }
}