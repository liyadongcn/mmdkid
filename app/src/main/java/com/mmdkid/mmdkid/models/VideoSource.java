package com.mmdkid.mmdkid.models;

import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by LIYADONG on 2017/9/26.
 */

public class VideoSource {
    private static final String TAG = "VideoSource";
    public static final String VIDEO_SOURCE_YOUKU = "youku";
    public static final String HOST_YOUKU = "player.youku.com";
    public String mUrl;

    public VideoSource(String url){
        this.mUrl = url;
    }

    // 通过视频链接url判断视频的来源
    public static  String getSourceName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        Log.d(TAG,"Url is : " + url);
        Log.d(TAG,"Url host  is : " +uri.getHost());
        if(uri.getHost().equals(HOST_YOUKU)){
            return VIDEO_SOURCE_YOUKU;
        }
        return null;
    }

}
