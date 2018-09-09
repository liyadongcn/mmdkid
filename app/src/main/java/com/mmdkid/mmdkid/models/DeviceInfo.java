package com.mmdkid.mmdkid.models;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DeviceInfo  extends Model {
    protected static final String TAG = "DeviceInfo";
    protected static final String URI = "v1/device";

    public static final String SYS_ANDROID = "android";
    public static final String SYS_IOS = "ios";

    // 设备信息在服务器上的唯一编号
    public int mId;
    // 设备使用的操作系统版本
    public String mVersion;
    // 设备使用的操作系统名称
    public String mSystem;
    // 设备的MAC地址
    public String mMac;
    // 设备的SIM卡序列号
    public String mSN;
    // 设备的ID GSM手机的 IMEI 和 CDMA手机的 MEID
    public String mDeviceId;
    // 手机号码
    public String mPhone;
    // 设备中所有的联系人信息
    public String mContact;

    @Override
    public void setAttributesNames() {
        this.mFieldNameMap.put("mId","id");
        this.mFieldNameMap.put("mVersion","version");
        this.mFieldNameMap.put("mSystem","system");
        this.mFieldNameMap.put("mMac","mac");
        this.mFieldNameMap.put("mSN","sn");
        this.mFieldNameMap.put("mDeviceId","device_id");
        this.mFieldNameMap.put("mContact","contact");
        this.mFieldNameMap.put("mPhone","phone");
    }

    public static ArrayList<DeviceInfo> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the deviceInfo model."+response.toString());
        ArrayList<DeviceInfo> arrayList = new ArrayList<DeviceInfo>();
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
                    DeviceInfo deviceInfo = populateModel(item);
                    //if(deviceInfo!=null && deviceInfo.mModel!=null) arrayList.add(deviceInfo);
                    if(deviceInfo!=null) arrayList.add(deviceInfo);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            DeviceInfo deviceInfo = populateModel(response);
            if(deviceInfo!=null) arrayList.add(deviceInfo);
        }
        return arrayList;
    }

    public static DeviceInfo populateModel(JSONObject response){
        Log.d(TAG,"Single deviceInfo object.");
        try {
            DeviceInfo deviceInfo = new DeviceInfo();
            if(response.has("id")) deviceInfo.mId = response.getInt("id");
            if(response.has("system")) deviceInfo.mSystem = response.getString("system");
            if(response.has("version")) deviceInfo.mVersion = response.getString("version");
            if(response.has("mac")) deviceInfo.mMac = response.getString("mac");
            if(response.has("sn")) deviceInfo.mSN = response.getString("sn");
            if(response.has("device_id")) deviceInfo.mDeviceId = response.getString("device_id");
            if(response.has("contact")) deviceInfo.mContact = response.getString("contact");
            if(response.has("phone")) deviceInfo.mPhone = response.getString("phone");
            return deviceInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JSONObject getJsonRequest(String action, Connection connection) {
        JSONObject request = new JSONObject();
        switch (action) {
            case Model.ACTION_CREATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI;
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"DeviceInfo json object is :" + request.toString());
                return  request;
            case Model.ACTION_UPDATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.PATCH);
                if(mId!=0){
                    connection.URL = connection.URL + URI + "/"+ mId;
                    Log.d(TAG,"DeviceInfo update url is " + connection.URL);
                }else {
                    Log.d(TAG,"DeviceInfo for updating, but there is no post id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"DeviceInfo json object is :" + request.toString());
                return  request;
            case Model.ACTION_DELETE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.DELETE);
                if(mId!=0){
                    connection.URL = connection.URL + URI + "/"+ mId;
                    Log.d(TAG,"DeviceInfo delete url is " + connection.URL);
                }else {
                    Log.d(TAG,"DeviceInfo for delete, but there is no DeviceInfo id.");
                    return null;
                }
               /* request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"DeviceInfo delete json object is :" + request.toString());*/
                return  request;
        }
        return null;
    }


    public boolean isAvailable(){
        if (TextUtils.isEmpty(mMac) && TextUtils.isEmpty(mSN) && TextUtils.isEmpty(mDeviceId)){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "mId=" + mId +
                ", mVersion='" + mVersion + '\'' +
                ", mSystem='" + mSystem + '\'' +
                ", mMac='" + mMac + '\'' +
                ", mSN='" + mSN + '\'' +
                ", mDeviceId='" + mDeviceId + '\'' +
                ", mPhone='" + mPhone + '\'' +
                ", mContact='" + mContact + '\'' +
                '}';
    }
}
