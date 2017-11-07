package com.mmdkid.mmdkid.models;

import android.content.Context;

import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LIYADONG on 2016/12/19.
 */

public class Model extends Object implements Serializable{
    public static final int VIEW_TYPE_DIARY = 1;
    public static final int VIEW_TYPE_DIARY_LIST = 2;

    public static final int VIEW_TYPE_USER = 10;

    public static final int VIEW_TYPE_CONTENT = 20;
    public static final int VIEW_TYPE_NOTIFICATION = 50;

    //public static final int VIEW_TYPE_CONTENT_POST= 33;
    public static final int VIEW_TYPE_CONTENT_IMAGE_POST= 34;
    public static final int VIEW_TYPE_CONTENT_VIDEO= 35;

    //public static final int VIEW_TYPE_CONTENT_POST_MAIN= 30;
    public static final int VIEW_TYPE_CONTENT_IMAGE_POST_MAIN= 31;
    public static final int VIEW_TYPE_CONTENT_VIDEO_MAIN= 32;
    public static final int VIEW_TYPE_CONTENT_VIDEO_YOUKU= 36;

    public static final int VIEW_TYPE_CONTENT_POST_IMAGE_LEFT= 137;
    public static final int VIEW_TYPE_CONTENT_POST_IMAGE_RIGHT= 138;
    public static final int VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE= 139;
    public static final int VIEW_TYPE_CONTENT_POST_IMAGE_THREE= 140;

    public static final int VIEW_TYPE_USER_GROUP= 40;

    public static final int VIEW_TYPE_REFRESH = 60;

    public static final int VIEW_TYPE_GOODS_IMAGE_ON_LEFT= 70;

    public static final int VIEW_TYPE_GWCONTENT_GOODS_IMAGE_ON_LEFT= 90;
    public static final int VIEW_TYPE_GWCONTENT_POST_IMAGE_ON_MIDDLE= 91;

    protected int mViewType=0;

    public static final String ACTION_CREATE = "create";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_UPDATE = "update";

    protected Map<String,String> mFieldNameMap = new HashMap<String,String>();

    public JSONObject getRequest(String action, Connection connection){
        return null;
    }
    public int getViewType(){
        return mViewType;
    }
    public void setViewType(int type){
        mViewType = type;
    }
    public String getUrl(){
        return null;
    }

    public void save(String action ,Context context, RESTAPIConnection.OnConnectionListener listener){
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        connection.excute(this.getJsonRequest(action,connection),this.getClass());
    }
    public void delete(Context context,RESTAPIConnection.OnConnectionListener listener){
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
    }

    public JSONObject getJsonRequest(String action,Connection connection){
        return null;
    }

    public void setAttributesNames(){

    }

    public String getAttributeName(String fieldName){
        return mFieldNameMap.get(fieldName)==null ? fieldName: mFieldNameMap.get(fieldName);
    }

    public JSONObject toJsonObject(){
        JSONObject jsonObject = new JSONObject();
        setAttributesNames();
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields){
            try {
                if(getAttributeName(field.getName())!=null)
                    jsonObject.put(getAttributeName(field.getName()),field.get(this));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}
