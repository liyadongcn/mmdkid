package com.mmdkid.mmdkid.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/* 存储对象列表到sharedpreference中
*  目前用于频道设置的存储
*  */
public class ListDataSave {
    private final static String TAG ="ListDataSave";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;    
    
    public ListDataSave(Context mContext, String preferenceName) {
        preferences = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);    
        editor = preferences.edit();    
    }    
    
    /**  
     * 保存List  
     * @param tag  
     * @param datalist  
     */    
    public <T> void setDataList(String tag, List<T> datalist) {
        if (null == datalist || datalist.size() <= 0)    
            return;    
    
        Gson gson = new Gson();
        //转换成json数据，再保存    
        String strJson = gson.toJson(datalist);    
        //editor.clear();
        editor.putString(tag, strJson);    
        editor.commit();    
    
    }    
    
    /**  
     * 获取List  
     * @param tag  
     * @return  
     */    
    public <T> List<T> getDataList(String tag,Class<T> clazz) {
        List<T> datalist=new ArrayList<T>();
        String strJson = preferences.getString(tag, null);
        Log.d(TAG,"Channels json string is :" + strJson);
        if (null == strJson) {    
            return datalist;    
        }    
       /*  Gson gson = new Gson();
       datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());*/
        try {
            Gson gson = new Gson();
            JsonArray arry = new JsonParser().parse(strJson).getAsJsonArray();
            for (JsonElement jsonElement : arry) {
                datalist.add(gson.fromJson(jsonElement, clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datalist;    
    
    }    
}    