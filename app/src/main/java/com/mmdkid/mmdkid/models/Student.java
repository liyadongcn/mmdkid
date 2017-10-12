package com.mmdkid.mmdkid.models;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by LIYADONG on 2017/8/6.
 */

public class Student extends Model {
    private final static String TAG = "Student";
    private final static String URI = "v1/students";
    private final static String DEFAULT_WEB = "http://www.mmdkid.cn";


    public int mId;
    public String mAvatar;
    public String mNickname;
    public String mRealname;
    public int mRole;
    public int mGender;
    public String mBirthday;
    public String mScenario ="default";
    public int mParentId;
    public String mRelationship;

    @Override
    public void setAttributesNames() {
        this.mFieldNameMap.put("mId","id");
        this.mFieldNameMap.put("mAvatar","avatar");
        this.mFieldNameMap.put("mNickname","nick_name");
        this.mFieldNameMap.put("mRealname","real_name");
        this.mFieldNameMap.put("mRole","role");
        this.mFieldNameMap.put("mGender","gender");
        this.mFieldNameMap.put("mScenario","scenario");
        this.mFieldNameMap.put("mBirthday","birthday");
        this.mFieldNameMap.put("mParentId","parent_id");
        this.mFieldNameMap.put("mRelationship","relationship");
    }

    public static Student populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the student model."+response.toString());
        try {
            Student model = new Student();
            if(response.has("id")) model.mId = response.getInt("id");
            if(response.has("avatar")){
                model.mAvatar = response.getString("avatar");
                Uri uri = Uri.parse( model.mAvatar);
                if(uri.getScheme()==null){
                    model.mAvatar = DEFAULT_WEB + model.mAvatar;
                }
            }
            if(response.has("nick_name")) model.mNickname = response.getString("nick_name");
            if(response.has("real_name")) model.mRealname = response.getString("real_name");
            if(response.has("birthday")) model.mBirthday = response.getString("birthday");
            if(response.has("role")) model.mRole = response.getInt("role");
            if(response.has("gender")) model.mGender = response.getInt("gender");
            if(response.has("parent_id")) model.mParentId = response.getInt("parent_id");
            if(response.has("relationship")) model.mRealname = response.getString("relationship");
            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Student> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the user model."+response.toString());
        ArrayList<Student> arrayList = new ArrayList<Student>();
        if(response.has("items")){
            // response is mutiple results.
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
                for(int i = 0; i < currentPageTotal; i++){
                    Student model = populateModel(items.getJSONObject(i));
                    if(model!=null) arrayList.add(model);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // response is a single result
            Student model = populateModel(response);
            if(model!=null) arrayList.add(model);
        }
        return arrayList;
    }

    @Override
    public JSONObject getJsonRequest(String action,Connection connection) {
        JSONObject request = new JSONObject();
        switch (action) {
            case Model.ACTION_CREATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI;
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"Student json object is :" + request.toString());
                return  request;
            case Model.ACTION_UPDATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.PATCH);
                if(mId!=0){
                    connection.URL = connection.URL + URI + "/"+ mId;
                    Log.d(TAG,"Student update url is " + connection.URL);
                }else {
                    Log.d(TAG,"Student for updating, but there is no student id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"Student json object is :" + request.toString());
                return  request;

        }
        return null;
    }
}
