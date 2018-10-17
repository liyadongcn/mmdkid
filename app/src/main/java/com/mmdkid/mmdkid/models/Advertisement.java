package com.mmdkid.mmdkid.models;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.helper.Utility;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Advertisement extends Model {
    private static final String TAG = "Advertisement";
    private static final String URI = "v1/advertisement";

    private static final String ACTION_GET_ADS_FROM_SERVER = "get_advertisements_from_server";
    /**
     * 有效
     */
    public static final int STATUS_VALID =10;
    /**
     * 无效
     */
    public static final int STATUS_INVAlID =11;
    /**
     * 唯一编号
     */
    public int mId;
    /**
     * 广告名称
     */
    public String mName;
    /**
     * 广告图片网络地址
     */
    public String mImgUrl;
    /**
     * 广告网络地址
     */
    public String mUrl;
    /**
     * 广告图片本地缓存路径
     */
    public String mLocalPath;
    /**
     * 广告开始投放时间
     */
    public String mStartAt;
    /**
     * 广告结束投放时间
     */
    public String mEndAt;
    /**
     * 状态
     */
    public int mStatus;
    /**
     * 创建时间
     */
    public String mCreatedAt;
    /**
     * 修改时间
     */
    public String mUpdatedAt;
    /**
     * 创建人
     */
    public int mCreatedBy;
    /**
     * 修改人
     */
    public int mUpdatedBy;
    /**
     * 广告检测时间，改时间以后的广告有效，才能从服务器返回
     */
    public String mCheckTime;
    /**
     * 广告是否有效
     * 缓存的图片是否在
     * 广告时间是否过期
     */
    public boolean isCacheValid(){
        if (mLocalPath == null || mLocalPath.isEmpty()) return false;
        File file = new File(mLocalPath);
        if(!file.exists()) return false;
        try {
            if (Utility.compare(mEndAt,Utility.dateToString(new Date(),"yyyy-MM-dd hh:mm:ss"))) return false;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static ArrayList<Advertisement> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the advertisement model."+response.toString());
        ArrayList<Advertisement> arrayList = new ArrayList<Advertisement>();
        if(response.has("items")){
            // return multiple results.
            try {
                JSONArray items = response.getJSONArray("items");
                JSONObject meta = response.getJSONObject("_meta");
                int perPage = meta.getInt("perPage");
                int currentPage = meta.getInt("currentPage");
                int totalCount = meta.getInt("totalCount");
                int currentPageTotal;
                if(perPage*currentPage < totalCount){
                    currentPageTotal = perPage;
                }else{
                    currentPageTotal = perPage - (perPage*currentPage - totalCount);
                }
                JSONObject item;
                for(int i = 0; i < currentPageTotal; i++){
                    item = items.getJSONObject(i);
                    Advertisement advertisement = populateModel(item);
                    //if(advertisement!=null && advertisement.mModel!=null) arrayList.add(advertisement);
                    if(advertisement!=null) arrayList.add(advertisement);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            Advertisement advertisement = populateModel(response);
            if(advertisement!=null) arrayList.add(advertisement);
        }
        return arrayList;
    }

    public static Advertisement populateModel(JSONObject response){
        Log.d(TAG,"Single advertisement object.");
        try {
            Advertisement advertisement = new Advertisement();
            if(response.has("id")) advertisement.mId = response.getInt("id");
            if(response.has("name")) advertisement.mName = response.getString("name");
            if(response.has("url")) advertisement.mUrl = response.getString("url");
            if(response.has("img_url")) advertisement.mImgUrl = response.getString("img_url");
            if(response.has("start_at")) advertisement.mStartAt = response.getString("start_at");
            if(response.has("end_at")) advertisement.mEndAt = response.getString("end_at");
            if(response.has("status")) advertisement.mStatus = response.getInt("status");
            return advertisement;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JSONObject getJsonRequest(String action, Connection connection) {
        JSONObject request = new JSONObject();
        switch (action) {
            case ACTION_GET_ADS_FROM_SERVER:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.GET);
                connection.URL = connection.URL + URI + "?check_time=" +this.mCheckTime;
                Log.d(TAG,"Advertisement request url :" + connection.URL);
                return  request;
        }
        return null;
    }

    public static void getAdvertisements(Context context, String checkTime,RESTAPIConnection.OnConnectionListener listener){
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        Advertisement advertisement = new Advertisement();
        advertisement.mCheckTime = checkTime;
        connection.excute(advertisement.getJsonRequest(ACTION_GET_ADS_FROM_SERVER,connection),Advertisement.class);
    }
}
