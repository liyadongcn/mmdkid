package com.mmdkid.mmdkid.singleton;

import android.content.Context;
import android.util.Log;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.models.ActionLog;
import com.mmdkid.mmdkid.models.Behavior;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
/**
 * 单例模式
 * 定时上传浏览记录并定时保存浏览记录到本地
 * 不能上传的浏览记录将被删除
 * 上传图片及视频的观看记录，图片及视频的观看为本地直接观看服务器端没有记录
 */

public class ActionLogs {
    private static final String TAG = "ActionLogs";
    private static final long TIMER_PERIOD = 1000*30; // 每30秒执行一次定时任务
    private static final int LOCAL_MAX_LOGS = 10;     // 本地保存的最大记录数
    private static ActionLogs mInstance;
    private ArrayList<ActionLog> mLogList;
    private static Context mContext;

    private long mStartTime;
    private long mStopTime;
    private String mAction;
    private Object mData;
    private static boolean mIsLogging=false;

    private ActionLogs(Context context) {
        mLogList = new ArrayList<ActionLog>();
        mContext = context;
        // 启动一个定时任务
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG,"Action logs time up.");
                // 将还没有保存到服务器的记录 保存到服务器
                try {
                    // 保存用户的图片浏览记录
                    saveToServer(ActionLog.ACTION_VIEW,Content.TYPE_IMAGE);
                    // 保存用户的video播放记录
                    saveToServer(ActionLog.ACTION_VIEW,Content.TYPE_VIDEO);
                    // 保存最近的浏览记录到本地
                    saveToLocal(LOCAL_MAX_LOGS);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        },1000,TIMER_PERIOD);
    }
    /**
     *  定时的将用户浏览记录保存到本地
     *  用户下一次启动APP时 作为推荐文档的依据
     */
    private void saveToLocal(int maxNum) {
        ArrayList<ActionLog> logList;
        // 取得最近的浏览记录
        if (mLogList.isEmpty()) return;
        if(mLogList.size()> maxNum){
            logList = new ArrayList<ActionLog>( mLogList.subList(mLogList.size()-maxNum,mLogList.size()));
        }else{
            logList = new ArrayList<ActionLog>( mLogList.subList(0,mLogList.size()));
        }
        // 保存到本地
        App app = (App) mContext.getApplicationContext();
        app.setLogs(logList);
    }

    private void saveToServer(String action,String type) throws ParseException {
        ArrayList<ActionLog> logList = getLogs(type);
        if (logList.isEmpty()) return;
        for(final ActionLog log : logList){
            if (log.mAction.equals(action) && !log.mIsSaved && log.mUserID!=0){
                // 保存log到服务器 匿名用户的log不上传
                // 新建一个Behavior
                Behavior behavior = new Behavior();
                behavior.mUserId = log.mUserID;
                behavior.mName = Behavior.BEHAVIOR_VIEW;
                behavior.mModelType = log.mModelType;
                behavior.mModelId = log.mModelID;
                behavior.mCreatedAt = Long.toString(log.mStartTimestamp/1000);
                // 将该Behavior保存得到服务器
                behavior.save(Model.ACTION_CREATE, mContext, new RESTAPIConnection.OnConnectionListener() {
                    @Override
                    public void onErrorRespose(Class c, String error) {
                        Log.d(TAG,"Save a action log failed>>>" + log.mModelType);
                        // 上传出错 删除
                        mLogList.remove(log);
                    }

                    @Override
                    public void onResponse(Class c, ArrayList responseDataList) {
                        // 保存成功 将当前的action log 置为已经保存
                        Log.d(TAG,"Save a action log success>>>" + log.mModelType);
                        log.mIsSaved = true;
                    }
                });

            }
        }
    }

    public static synchronized ActionLogs getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ActionLogs(context);
        }
        return mInstance;
    }
    /**
     *  直接增加一个action类型的log日志     *
     */
    public void add(String action,Object data) {
        ActionLog log = new ActionLog();
        // 检查当前用户
        User user = ((App)mContext.getApplicationContext()).getCurrentUser();
        if (user!=null) log.mUserID = user.mId;
        // 用户行为
        log.mAction = action;
        // 检查数据类型
        if (data instanceof Content){
            log.mElasticDocID = ((Content) data).mId;
            log.mModelID = ((Content) data).mModelId;
            log.mModelType = ((Content) data).mModelType;
            switch (log.mModelType){
                case Content.TYPE_POST:
                    log.mContentLength = ((Content) data).mContent.length();
                    break;
                case Content.TYPE_IMAGE:
                    log.mContentLength = ((Content) data).mImageList.size();
                    break;
                case Content.TYPE_VIDEO:
                    break;
            }

        }
        log.mStartTimestamp = System.currentTimeMillis();
        // 增加一个记录
        add(log);
    }
    /**
     *  直接增加一个log日志     *
     */
    public  void add(ActionLog log){
        // 没有时间戳 自动增加一个时间
        if(log.mStartTimestamp==0) log.mStartTimestamp = System.currentTimeMillis();
        //计算阅读速度 越慢越感兴趣
        if(log.mContentLength!=0 && log.mDuration!=0) log.mReadingSpeed = log.mContentLength/log.mDuration/1000;
        mLogList.add(log);
        Log.d(TAG,"Toal logs>>>"+mLogList.size()
                + ">>>Add new log : "
                + log.mUserID
                + ">>>" + log.mAction
                + ">>>" + log.mElasticDocID
                + ">>>" + log.mModelType
                + ">>>" + log.mContentLength
                + ">>>" + log.mStartTimestamp/1000
                + ">>>" + log.mStopTimestamp/1000
                + ">>>" + log.mDuration/1000
                + ">>>" + Double.toString(log.mReadingSpeed));
    }
    /**
     *  开始记录一个action日志 data只支持数据类型为Content的数据
     */
    public void start(String action,Object data){
        if (data!=null && data instanceof Content) {
            // 只记录数据为content类型的
            mStartTime = System.currentTimeMillis();
            mAction = action;
            mData = data;
            mIsLogging = true;
        }
    }
    /**
     *  停止记录一个action日志 data只支持数据类型为Content的数据
     */
    public void stop(){
        // 只记录数据类型Content
        if (!mIsLogging || mData==null || !(mData instanceof Content)) return;
        mStopTime = System.currentTimeMillis();
        ActionLog log = new ActionLog();
        // 检查当前用户
        User user = ((App)mContext.getApplicationContext()).getCurrentUser();
        if (user!=null) log.mUserID = user.mId;
        // 用户行为
        log.mAction = mAction;
        // 检查数据类型
        if (mData instanceof Content){
            log.mElasticDocID = ((Content) mData).mId;
            log.mModelID = ((Content) mData).mModelId;
            log.mModelType = ((Content) mData).mModelType;
            switch (log.mModelType){
                case Content.TYPE_POST:
                    log.mContentLength = ((Content) mData).mContent.length();
                    break;
                case Content.TYPE_IMAGE:
                    log.mContentLength = ((Content) mData).mImageList.size();
                    break;
                case Content.TYPE_VIDEO:
                    break;
            }

        }
        log.mStartTimestamp = mStartTime;
        log.mStopTimestamp = mStopTime;
        log.mDuration = mStopTime - mStartTime;
        // 增加一个记录
        add(log);
        // 结束本次记录
        mIsLogging =false;
//        getMostInterested(Content.TYPE_POST);
//        getMostInterested(Content.TYPE_IMAGE);
    }

  /*  public ArrayList<String> getMostInterested(String type){
        ArrayList<ActionLog> logList = getLogs(type);
        if (logList.isEmpty()) return null;
        //用户记录排序 按照阅读速度
        Collections.sort(logList,comparator);
        //打印排序结果
        for(ActionLog sortedLog:logList){
            Log.d(TAG,"Type " + type
                    + ">>>" + sortedLog.mUserID
                    + ">>>" + sortedLog.mAction
                    + ">>>" + sortedLog.mElasticDocID
                    + ">>>" + sortedLog.mModelType
                    + ">>>" + sortedLog.mContentLength
                    + ">>>" + sortedLog.mStartTimestamp/1000
                    + ">>>" + sortedLog.mStopTimestamp/1000
                    + ">>>" + sortedLog.mDuration/1000
                    + ">>>" + Double.toString(sortedLog.mReadingSpeed));
        }
        //计算阅读最慢的记录
        ActionLog log = Collections.min(logList,comparator);
        Log.d(TAG,"Total Log >>>"+mLogList.size());
        Log.d(TAG,"Min log : "
                + type
                + ">>>" + log.mUserID
                + ">>>" + log.mAction
                + ">>>" + log.mElasticDocID
                + ">>>" + log.mModelType
                + ">>>" + log.mContentLength
                + ">>>" + log.mStartTimestamp/1000
                + ">>>" + log.mStopTimestamp/1000
                + ">>>" + log.mDuration/1000
                + ">>>" + Double.toString(log.mReadingSpeed));
        return null;
    }*/

    public static Comparator<ActionLog> comparator = new Comparator<ActionLog>() {

        @Override
        public int compare(ActionLog a1, ActionLog a2) {
            return a1.mReadingSpeed > a2.mReadingSpeed ? 1 : -1;
        }
    };
    /**
     *  根据数据类型type找出所有的相关日志
     */
    public ArrayList<ActionLog> getLogs(String type){
        ArrayList<ActionLog> logList = new ArrayList<ActionLog>();
        for (ActionLog log : mLogList){
            if (log.mModelType!=null && log.mModelType.equals(type)){
                logList.add(log);
            }
        }
        return logList;
    }

    public void setLogList(ArrayList<ActionLog> logList){
        mLogList = logList;
    }

    public ArrayList<ActionLog> getLogList(){
        return mLogList ;
    }
}
