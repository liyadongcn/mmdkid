package com.mmdkid.mmdkid.helper;

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by LIYADONG on 2017/12/28.
 */
public class HtmlUtilTest {
    private final static String TAG = "HtmlUtilTest";
    private ArrayList<String> mImageList;
    @Test
    public void getImageSrc() throws Exception {
     /*   String s= "<img src=\"file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1514436749659.jpg\" alt=\"dachshund\">\n" +
                "<img src=\"file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1514436745444.jpg\" alt=\"dachshund\">" +
                "<img src=\"file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1514436741212.jpg\" alt=\"dachshund\">" +
                "<img src=\"file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1514436732770.jpg\" alt=\"dachshund\">" +
                "<img src=\"file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1514436728512.jpg\" alt=\"dachshund\">" +
                "<img src=\"file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1514436470451.jpg\" alt=\"dachshund\">" +
                "<img src=\"file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1514436470451.jpg\" alt=\"dachshund\">" +
                "<img src=\"file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1514436466144.jpg\" alt=\"dachshund\"> " +
                "<img src = \"file:///storage/emulated/0/Download/timg (7).jpeg\" alt=\"dachshund\">";*/
       String s="<img src = \"file:///storage/emulated/0/Download/timg (7).jpeg\" alt=\"dachshund\">";
        mImageList = (ArrayList<String>) HtmlUtil.getImageSrc(s);
        for (String image : mImageList){
            Log.d(TAG,"Image is :" + image);
        }
    }

}