package com.mmdkid.mmdkid.models;

/**
 * Created by LIYADONG on 2017/9/26.
 */

public class VideoSource {
    private static final String TAG = "VideoSource";
    public static final String VIDEO_SOURCE_YOUKU = "优酷";
    public static final String HOST_YOUKU = "player.youku.com";
    public String mUrl;

    public VideoSource(String url){
        this.mUrl = url;
    }

    // 通过视频链接url判断视频的来源 没有使用
    /*public static  String getSourceNameFromUrl(String url) throws URISyntaxException {
        Log.d(TAG,"Url is : " + url);
        Uri uri = Uri.parse(url);
        Log.d(TAG,"Url is : " + url);
        Log.d(TAG,"Url host  is : " +uri.getHost());
        String host = uri.getHost();
        if(host!=null && host.equals(HOST_YOUKU)){
            return VIDEO_SOURCE_YOUKU;
        }
        return null;
    }*/

}
