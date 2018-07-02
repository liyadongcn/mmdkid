package com.mmdkid.mmdkid.helper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;

public class FileUtilTest {
    private final static String TAG = "FileUtilTest";
    @Test
    public void getCacheDir() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        String path = FileUtil.getCacheDir(appContext);
        Log.d(TAG,"Cache File dir :" + path);
    }

    @Test
    public void getFilesDir() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        String path = FileUtil.getFilesDir(appContext);
        Log.d(TAG,"Files dir :" + path);
    }

    @Test
    public void from() {
    }
}