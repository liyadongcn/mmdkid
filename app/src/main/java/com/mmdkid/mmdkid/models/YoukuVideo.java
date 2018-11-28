package com.mmdkid.mmdkid.models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LIYADONG on 2017/9/26.
 */

public class YoukuVideo extends VideoSource {

    public YoukuVideo(String url) {
        super(url);
    }

    public static String getVid(String url){
        Matcher m= Pattern.compile("/sid/(.*?)/").matcher(url);
        if (m.find()){
            return m.group(1);
        } else {
            return null;
        }
    }

    public static String getVid(com.mmdkid.mmdkid.models.Content content){
        if (content.mSource_name!=null && content.mSource_name.equals(VideoSource.VIDEO_SOURCE_YOUKU)){
            return content.mVideo;
        }else {
            return null;
        }

    }

    public static String getVid(com.mmdkid.mmdkid.models.v2.Content content){
        if (content.mSource_name!=null && content.mSource_name.equals(VideoSource.VIDEO_SOURCE_YOUKU)){
            return content.mVideo;
        }else {
            return null;
        }

    }

}
