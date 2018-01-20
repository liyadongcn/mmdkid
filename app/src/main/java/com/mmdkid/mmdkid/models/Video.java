package com.mmdkid.mmdkid.models;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by LIYADONG on 2017/12/29.
 */

public class Video extends Model {
    protected final static String TAG = "Media";
    protected final static String URI = "v1/media";
    protected final static String DEFAULT_WEB = "http://www.mmdkid.cn";

    public int id;
    public int parent_id;
    public String name;
    public String description;
    public int order;
    public String created_at;
    public String updated_at;
    public int created_by;
    public int updated_by;
    public int status;
    public int stars;
    public int thumbsup;
    public int thumbsdown;
    public int view_count;
    public int star_count;
    public int comment_count;
    public ArrayList<String> videoList;
    public ArrayList<String> imageList;

    @Override
    public void setAttributesNames() {
        this.mFieldNameMap.put("id","id");
        this.mFieldNameMap.put("parent_id","parent_id");
        this.mFieldNameMap.put("name","name");
        this.mFieldNameMap.put("description","description");
        this.mFieldNameMap.put("order","order");
        this.mFieldNameMap.put("status","status");
        this.mFieldNameMap.put("created_at","created_at");
        this.mFieldNameMap.put("updated_at","updated_at");
        this.mFieldNameMap.put("created_by","created_by");
        this.mFieldNameMap.put("updated_by","updated_by");
        this.mFieldNameMap.put("stars","stars");
        this.mFieldNameMap.put("thumbsup","thumbsup");
        this.mFieldNameMap.put("thumbsdown","thumbsdown");
        this.mFieldNameMap.put("comment_count","comment_count");
        this.mFieldNameMap.put("view_count","view_count");
        this.mFieldNameMap.put("star_count","star_count");
    }
    public static Video populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the media model."+response.toString());
        try {
            Video model = new Video();
            if(response.has("id")) model.id = response.getInt("id");
            if(response.has("parent_id")) model.parent_id = response.getInt("parent_id");
            if(response.has("name")) model.name = response.getString("name");
            if(response.has("description")) model.description = response.getString("description");
            if(response.has("order")) model.order = response.getInt("order");
            if(response.has("status")) model.status = response.getInt("status");
            if(response.has("created_at")) model.created_at = response.getString("created_at");
            if(response.has("updated_at")) model.updated_at = response.getString("updated_at");
            if(response.has("created_by")) model.created_by = response.getInt("created_by");
            if(response.has("updated_by")) model.updated_by = response.getInt("updated_by");
            if(response.has("stars")) model.stars = response.getInt("stars");
            if(response.has("thumbsup")) model.thumbsup = response.getInt("thumbsup");
            if(response.has("thumbsdown")) model.thumbsdown = response.getInt("thumbsdown");
            if(response.has("comment_count")) model.comment_count = response.getInt("comment_count");
            if(response.has("view_count")) model.view_count = response.getInt("view_count");
            if(response.has("star_count")) model.star_count = response.getInt("star_count");
            if(response.has("images") && !response.isNull("images")){ // 有可能返回时null值 所以要判断
                JSONArray imageJsonArray = response.getJSONArray("images");
                model.imageList = new ArrayList<String>();
                Log.d(TAG,imageJsonArray.toString());
                for (int j=0; j<imageJsonArray.length(); j++){
                    model.imageList.add(imageJsonArray.getString(j));
                }
            }
            if(response.has("video") && !response.isNull("video")){ // 有可能返回时null值 所以要判断
                JSONObject videoJson = response.getJSONObject("video");
                model.videoList = new ArrayList<String>();
                Log.d(TAG,videoJson.toString());
                Iterator<?> iterator = videoJson.keys();// 应用迭代器Iterator 获取所有的key值
                while (iterator.hasNext()) { // 遍历每个key 这里key值是视频的url
                    String key = (String) iterator.next();
                    model.videoList.add(key);
                }
            }
            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Video> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the user model."+response.toString());
        ArrayList<Video> arrayList = new ArrayList<Video>();
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
                    Video model = populateModel(items.getJSONObject(i));
                    if(model!=null) arrayList.add(model);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // response is a single result
            Video model = populateModel(response);
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
                Log.d(TAG,"Media json object is :" + request.toString());
                return  request;
            case Model.ACTION_UPDATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.PATCH);
                if(id!=0){
                    connection.URL = connection.URL + URI + "/"+ id;
                    Log.d(TAG,"Media update url is " + connection.URL);
                }else {
                    Log.d(TAG,"Media for updating, but there is no media id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"Media json object is :" + request.toString());
                return  request;
            case Model.ACTION_DELETE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.DELETE);
                if(id!=0){
                    connection.URL = connection.URL + URI + "/"+ id;
                    Log.d(TAG,"Media delete url is " + connection.URL);
                }else {
                    Log.d(TAG,"Media for delete, but there is no media id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"Media delete json object is :" + request.toString());
                return  request;
        }
        return null;
    }
    /**
     *  用于查询返回的json
     */
    public static JSONObject getRequest(Query query)  {
        RESTAPIConnection connection = (RESTAPIConnection) query.getConnection();
        connection.setRequestMethod(Request.Method.GET);
        connection.REQUEST_URL = "";
        connection.REQUEST_URL = connection.REQUEST_URL + URI + "?"
                + RESTAPIConnection.ACCESS_TOKEN_NAME + "=" + connection.ACCESS_TOKEN;
        Map<String,String> parameters = query.getmParameters();
        if (parameters!=null){
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                connection.REQUEST_URL = connection.REQUEST_URL + "&" +  entry.getKey() +"=" + entry.getValue();
            }
        }
        connection.REQUEST_URL = connection.REQUEST_URL + "&page=" + Integer.toString(query.getCurrentPage());
        return null;
    }

    public static Query find(Context context, RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = Video.class;
        return query;
    }

    @Override
    public String getUrl() {
        return DEFAULT_WEB+"/index.php?r=media/view&id="+ id +"&theme=app";
    }
}
