package com.mmdkid.mmdkid.models;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by LIYADONG on 2017/7/27.
 */

public class PostPublishMap extends Model {
    private static final String TAG = "PostPublishMap";
    private final static String URI = "v1/post-publish-maps";
    
    public static final int STATUS_READ =1;
    public static final int STATUS_UNREAD =2;
    
    public int mId;
    public int mPost_id;
    public String mModel_type;
    public int mModel_id;
    public String mCreated_at;
    public String mUpdated_at;
    public int mCreated_by;
    public int mUpdated_by;
    public int mStatus;
    public Model mModelContent;

    public static ArrayList<PostPublishMap> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the postPublishMap model."+response.toString());
        ArrayList<PostPublishMap> arrayList = new ArrayList<PostPublishMap>();
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
                    PostPublishMap postPublishMap = populateModel(item);
                    if(postPublishMap!=null) arrayList.add(postPublishMap);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            PostPublishMap postPublishMap = populateModel(response);
            if(postPublishMap!=null ) arrayList.add(postPublishMap);
        }
        return arrayList;
    }

    public static PostPublishMap populateModel(JSONObject jsonObject){
        JSONObject item = jsonObject;
        try {
            PostPublishMap postPublishMap = new PostPublishMap();
            postPublishMap.mId = item.getInt("id");
            postPublishMap.mPost_id = item.getInt("post_id");
            postPublishMap.mModel_id = item.getInt("model_id");
            postPublishMap.mModel_type = item.getString("model_type");
            postPublishMap.mCreated_by= item.getInt("created_by");
            postPublishMap.mUpdated_by = item.getInt("updated_by");
            postPublishMap.mCreated_at = item.getString("created_at");
            postPublishMap.mUpdated_at = item.getString("updated_at");
            if(!item.isNull("status")) postPublishMap.mStatus= item.getInt("status");
            if(item.has("model_content")){
                postPublishMap.mModelContent = Content.populateModel(item.getJSONObject("model_content"));
            }
            return postPublishMap;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getRequest(Query query)  {
        RESTAPIConnection connection = (RESTAPIConnection) query.getConnection();
        connection.setRequestMethod(Request.Method.GET);
        connection.URL = connection.URL + URI + "?"
                + RESTAPIConnection.ACCESS_TOKEN_NAME + "=" + connection.ACCESS_TOKEN;
        Map<String,String> parameters = query.getmParameters();
        if (parameters!=null){
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                connection.URL = connection.URL + "&" +  entry.getKey() +"=" + entry.getValue();
            }
        }
        return null;
    }
    public static Query find(Context context, RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = PostPublishMap.class;
        return query;
    }
}
