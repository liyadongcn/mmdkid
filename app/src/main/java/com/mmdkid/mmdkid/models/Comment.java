package com.mmdkid.mmdkid.models;

import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by LIYADONG on 2017/8/12.
 */

public class Comment extends Model {
    private final static String TAG = "Comment";
    private final static String URI = "v1/comments";

    public static final int STATUS_ACTIVE = 10;

    public int mId;
    public int mParent_id;
    public String mModel_type;
    public int mModel_id;
    public int mThumbsup;
    public int mThumbsdown;
    public String mAuthor;
    public String mAuthor_ip;
    public String mContent;
    public String mCreated_at;
    public String mUpdated_at;
    public int mCreated_by;
    public int mUpdated_by;
    public int mStatus=STATUS_ACTIVE;

    @Override
    public void setAttributesNames() {
        this.mFieldNameMap.put("mId","id");
        this.mFieldNameMap.put("mParent_id","parent_id");
        this.mFieldNameMap.put("mModel_type","model_type");
        this.mFieldNameMap.put("mModel_id","model_id");
        this.mFieldNameMap.put("mThumbsup","thumbsup");
        this.mFieldNameMap.put("mThumbsdown","thumbsdown");
        this.mFieldNameMap.put("mAuthor","author");
        this.mFieldNameMap.put("mAuthor_ip","author_id");
        this.mFieldNameMap.put("mContent","content");
        this.mFieldNameMap.put("mCreated_at","created_at");
        this.mFieldNameMap.put("mUpdated_at","updated_at");
        this.mFieldNameMap.put("mCreated_by","created_by");
        this.mFieldNameMap.put("mUpdated_by","updated_by");
        this.mFieldNameMap.put("mStatus","status");
    }

    public static Comment populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the comment model."+response.toString());
        try {
            Comment model = new Comment();
            if(response.has("id")) model.mId = response.getInt("id");
            if(response.has("parent_id")) model.mParent_id = response.getInt("parent_id");
            if(response.has("model_type")) model.mModel_type = response.getString("model_type");
            if(response.has("model_id")) model.mModel_id = response.getInt("model_id");
            if(response.has("thumbsup")) model.mThumbsup = response.getInt("thumbsup");
            if(response.has("thumbsdown")) model.mThumbsdown = response.getInt("thumbsdown");
            if(response.has("author")) model.mAuthor = response.getString("author");
            if(response.has("author_ip")) model.mAuthor_ip = response.getString("author_ip");
            if(response.has("content")) model.mContent = response.getString("content");
            if(response.has("created_at")) model.mCreated_at = response.getString("created_at");
            if(response.has("updated_at")) model.mUpdated_at = response.getString("updated_at");
            if(response.has("created_by")) model.mCreated_by = response.getInt("created_by");
            if(response.has("updated_by")) model.mUpdated_by = response.getInt("updated_by");
            if(response.has("status")) model.mStatus = response.getInt("status");
            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Comment> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the user model."+response.toString());
        ArrayList<Comment> arrayList = new ArrayList<Comment>();
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
                    Comment model = populateModel(items.getJSONObject(i));
                    if(model!=null) arrayList.add(model);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // response is a single result
            Comment model = populateModel(response);
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
                Log.d(TAG,"Comment json object is :" + request.toString());
                return  request;
        }
        return null;
    }
    
}
