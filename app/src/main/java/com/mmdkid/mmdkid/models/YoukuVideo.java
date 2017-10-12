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

    public String getVid(){
        Matcher m= Pattern.compile("/sid/(.*?)/").matcher(this.mUrl);
        if (m.find()){
            return m.group(1);
        } else {
            return null;
        }
    }
}
