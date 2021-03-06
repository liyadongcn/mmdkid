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
    public static final int VIEW_TYPE_CONTENT_IMAGE_ONE= 21;
    public static final int VIEW_TYPE_CONTENT_IMAGE_THREE= 22;
    public static final int VIEW_TYPE_CONTENT_IMAGE_FOUR= 23;
    public static final int VIEW_TYPE_CONTENT_IMAGE_SIX= 24;
    public static final int VIEW_TYPE_CONTENT_IMAGE_NINE= 26;
    public static final int VIEW_TYPE_CONTENT_IMAGE_LEFT= 27;

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

    // 用户发布内容管理的显示式样
    public static final int VIEW_TYPE_PUBLISH_MANAGE_POST= 100;
    public static final int VIEW_TYPE_PUBLISH_MANAGE_IMAGE= 101;
    public static final int VIEW_TYPE_PUBLISH_MANAGE_VIDEO= 102;

    // 关注用户内容的显示式样
    public static final int VIEW_TYPE_FOLLOW_POST= 200;
    public static final int VIEW_TYPE_FOLLOW_IMAGE= 201;
    public static final int VIEW_TYPE_FOLLOW_VIDEO= 202;
    // 需要登录显示样式
    public static final int VIEW_TYPE_NEED_LOGIN = 61;

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
        connection.excute(this.getJsonRequest(ACTION_DELETE,connection),this.getClass());
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
//        Field[] fields = this.getClass().getDeclaredFields();
        Field[] fields = this.getClass().getFields();
        for(Field field : fields){
            try {
                if(getAttributeName(field.getName())!=null)
                    jsonObject.put(getAttributeName(field.getName()),field.get(this));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }
        return jsonObject;
    }
}
