package com.mmdkid.mmdkid.server;

import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LIYADONG on 2017/6/9.
 */

public class Query extends Object {
    private static final String TAG= "Query";
    private Connection mConnection;
    private Sort mSort;
    private int  mPageFrom=0;
    private int  mPageSize=10;
    private int  mTotal=0;
    private int  mCurrentPage = 1;

    private Map<String,String> mParameters = new HashMap<String,String>();

    public Class mModelClass;


    public Query(Connection conn){
        mConnection = conn;

    }

    public void setParameter(String name,String value){
        mParameters.put(name,value);
    }

    public String getParameter(String name){
        return (String)mParameters.get(name);
    }
    public Query where(String name,String value){
        setParameter(name,value);
        return this;
    }

    public Map<String,String> getmParameters(){
        return mParameters;
    }

    public int getPageFrom() {
        return mPageFrom;
    }

    public void setPageFrom(int mPageFrom) {
        this.mPageFrom = mPageFrom;
    }

    public int getPageSize() {
        return mPageSize;
    }

    public void setPageSize(int mPageSize) {
        this.mPageSize = mPageSize;
    }

    public void setSort(Sort mSort) {
        this.mSort = mSort;
    }

    public Sort getSort() {
        return mSort;
    }

   /* public ArrayList<Model> GetAll(){
        return  response = mConnection.Query(this);
    }*/

    public Connection getConnection(){
        return mConnection;
    }

    public JSONObject GetRequest(){
        JSONObject jsonObject=null;
        try {
            Method getRequestMethod = mModelClass.getDeclaredMethod("getRequest",Query.class);
            jsonObject = (JSONObject) getRequestMethod.invoke(null,this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public int getTotal(){
        return mTotal;
    }

    public void setTotal(int total){
        mTotal = total;
    }

    public boolean hasMore(){
        mPageFrom = mPageFrom + mPageSize;
        if(mPageFrom < mTotal){
            mCurrentPage= mCurrentPage+1;
            return true;
        }else {
            return false;
        }
    }

    public int getCurrentPage(){
        return mCurrentPage;
    }

    public void all(){
        Log.d(TAG,"Total : " + mTotal);
        Log.d(TAG,"PageSize : " + mPageSize);
        Log.d(TAG,"CurrentPage : " + mCurrentPage);
        Log.d(TAG,"CurrentFrom : " + mPageFrom);
        mConnection.Query(this);
    }

    // 将所有的查询参数转换为字符串 只用于GET方法
    public String getCanonicalQueryString() {
        String queryString = null;
        if (mParameters!=null){
            for (Map.Entry<String, String> entry : mParameters.entrySet()) {
                queryString = queryString + "&" +  entry.getKey() +"=" + entry.getValue();
            }
        }
        if (mTotal != 0){
            queryString = queryString + "&page=" + mCurrentPage + "&per-page=" + mPageSize;
        }
        if (!queryString.isEmpty()) queryString.substring(1,queryString.length()-1);
        return queryString;
    }
}
