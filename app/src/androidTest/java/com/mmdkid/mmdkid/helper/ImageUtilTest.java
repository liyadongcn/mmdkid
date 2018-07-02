package com.mmdkid.mmdkid.helper;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.util.Log;


import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ImageUtilTest {
    private static final String TAG = "ImageUtilTest";

    @Test
    public void compress() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        File imageFile = new File(Environment.getExternalStorageDirectory().getPath(),"img.jpg");
        try {
            File compressedImage = ImageUtil.compress(appContext,imageFile);
            Log.d(TAG,"Original Image File path:" + imageFile.getAbsolutePath());
            int[] demension = ImageUtil.getImageWidthHeight(imageFile.getAbsolutePath());
            Log.d(TAG,"Original Image width and height :" + demension[0] + " "+ demension[1] );
            Log.d(TAG,"Compressed Image File path:" +compressedImage.getAbsolutePath());
            demension = ImageUtil.getImageWidthHeight(compressedImage.getAbsolutePath());
            Log.d(TAG,"Compressed Image width and height :" + demension[0] + " "+ demension[1] );
            FileUtil.copyFile(compressedImage.getAbsolutePath(),Environment.getExternalStorageDirectory().getPath()+"/compressedImg.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}