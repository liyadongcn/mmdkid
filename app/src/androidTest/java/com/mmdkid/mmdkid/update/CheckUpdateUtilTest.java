package com.mmdkid.mmdkid.update;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Test;

import java.io.File;

/**
 * Created by LIYADONG on 2018/1/15.
 */
public class CheckUpdateUtilTest {
    private final String TAG = "CheckUpdateUtilTest";
    @Test
    public void installApk() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        String target = Environment.getExternalStorageDirectory() +"/"+ "mmdkid_v0.8.26_20180114_release.apk";
        Log.d(TAG,"Local File Name is :" + target);
        File file = new File(target);
        String[] command = {"chmod", "777", file.getPath() };
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        CheckUpdateUtil.installApk(appContext,file);
    }

}