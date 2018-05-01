package com.mmdkid.mmdkid.models;

import android.location.Location;

public class ActionLog extends Object {
    public static final String ACTION_VIEW = "view";
    public static final String ACTION_STAR = "star";
    public static final String ACTION_COMMENT = "comment";
    public static final String ACTION_TUMBSUP = "thumbsup";
    public static final String ACTION_TUMBSDOWN = "thumbsdown";

    //用户ID
    public int mUserID;
    //内容在elastic上的唯一ID
    public String mElasticDocID;
    //用户动作
    public String mAction;
    //开始时间戳
    public long mStartTimestamp;
    //结束时间戳
    public long mStopTimestamp;
    //内容在数据库中类型
    public String mModelType;
    //内容长度
    public int mContentLength;
    //阅读速度 越慢越感兴趣
    public double mReadingSpeed;
    //内容在数据库中唯一ID
    public int mModelID;
    //用户当前所处位置
    public Location mLocation;
    //持续时间长度
    public long mDuration;
    //是否已经保存到服务器
    public boolean mIsSaved = false;
}
