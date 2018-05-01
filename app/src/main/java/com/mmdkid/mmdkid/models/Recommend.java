package com.mmdkid.mmdkid.models;

import android.content.Context;
import android.util.Log;

import com.mmdkid.mmdkid.singleton.ActionLogs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *  根据浏览记录产生相应的推荐
 *  推荐基于内容的相似性
 *  使用elasticsearch的more_like_this
 */
public class Recommend {
    private static final String TAG = "Recommend";
    private static final int MAX_LOGS = 10;      // 最多分析用户浏览记录数
    private static final int MAX_RECOMMEND = 3; // 最多建议相似文章数量
    private Context mContext;
    private ArrayList<String> mDocList;           // 近似文档列表 elasticsearch doc id
    private String mType;                         // 推荐文档类型
    private boolean mIsChanged=false;           // 推荐文档列表是否发生变化

    public Recommend(Context context,String type){
        mContext = context;
        mType = type;
        mDocList = new ArrayList<String>();
        refresh();
    }
    /**
     *  根据用户浏览记录 刷新近似文档列表
     */
    public void refresh() {
        // 按照类型取得用户的浏览记录
        ArrayList<ActionLog> logList = ActionLogs.getInstance(mContext).getLogs(mType);
        if (logList.isEmpty()) {
            Log.d(TAG,"No action logs for type>>>" + mType);
            return;
        }
        // 取得需要分析的浏览记录
        if(logList.size()> MAX_LOGS){
            logList = new ArrayList<ActionLog>(logList.subList(logList.size()-MAX_LOGS,logList.size()));
        }
        //用户记录排序 按照阅读速度及记忆曲线决定
        Collections.sort(logList,comparator);
        //打印排序结果
        for(ActionLog sortedLog:logList){
            Log.d(TAG,"All Type Sorted" + mType
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
        // 按照最大推荐数量取出记录
        if(logList.size()> MAX_RECOMMEND){
            logList = new ArrayList<ActionLog>(logList.subList(0,MAX_RECOMMEND-1));
        }
        // 遍历浏览记录 取出文档编号
        ArrayList<String> docList = new ArrayList<String>();
        for(ActionLog log : logList){
            docList.add(log.mElasticDocID);
        }
        if (mDocList.equals(docList)){
            // 推荐文档未发生变化
            mIsChanged =false;
        }else{
            mDocList.clear();
            mDocList.addAll(docList);
            mIsChanged = true;
        }
    }
    public boolean isChanged(){
        return mIsChanged;
    }
    /**
     *  根据近似文档列表 生成elasticsearch的json请求
     */
    public JSONArray getLikeJsonArray() {
        refresh();
        if (mDocList.isEmpty()) return null;
        JSONArray jsonArray = new JSONArray();
        try {
            for(String docId : mDocList){
                JSONObject jsonLike = new JSONObject();
                jsonLike.put("_index","momoda");
                jsonLike.put("_type","content");
                jsonLike.put("_id",docId);
                jsonArray.put(jsonLike);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonArray;
    }

    public static Comparator<ActionLog> comparator = new Comparator<ActionLog>() {

        @Override
        public int compare(ActionLog a1, ActionLog a2) {

            return a1.mReadingSpeed * forgettingRate(a1) > a2.mReadingSpeed * forgettingRate(a2)? 1 : -1;
        }
        // 艾宾浩斯遗忘曲线
        // 设初次记忆后经过了x小时，那么记忆率y近似地满足y=1-0.56x^0.06
        private double forgettingRate(ActionLog log){
            // 当前时间
            long now = System.currentTimeMillis();
            return 0.56*Math.pow(now-log.mStopTimestamp,0.06);
        }

    };
}
