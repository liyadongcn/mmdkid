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
import java.util.Map;

/**
 * Created by LIYADONG on 2018/1/13.
 */

public class ImagePost extends Model {
    private final static String TAG = "ImagePost";
    private final static String URI = "v1/image-posts";
    private final static String DEFAULT_WEB = "http://www.mmdkid.cn";

    public int id;
    public int model_id ;
    public String model_type;
    public int type ;
    public String title;
    public String content;
    public int status;
    public String source_url;
    public String source_name;
    public String author;
    public String created_at;
    public String updated_at;
    public int created_by;
    public int updated_by;
    public String effective_at;
    public String expired_at;
    public int thumbsup;
    public int thumbsdown;
    public int comment_count;
    public int view_count;
    public int star_count;
    public ArrayList<String> imageList;
    public ArrayList<String> thumbnailList;

    @Override
    public void setAttributesNames() {
        this.mFieldNameMap.put("id","id");
        this.mFieldNameMap.put("model_id","model_id");
        this.mFieldNameMap.put("model_type","model_type");
        this.mFieldNameMap.put("type","type");
        this.mFieldNameMap.put("title","title");
        this.mFieldNameMap.put("content","content");
        this.mFieldNameMap.put("status","status");
        this.mFieldNameMap.put("source_url","source_url");
        this.mFieldNameMap.put("source_name","source_name");
        this.mFieldNameMap.put("author","author");
        this.mFieldNameMap.put("created_at","created_at");
        this.mFieldNameMap.put("updated_at","updated_at");
        this.mFieldNameMap.put("created_by","created_by");
        this.mFieldNameMap.put("updated_by","updated_by");
        this.mFieldNameMap.put("effective_at","effective_at");
        this.mFieldNameMap.put("expired_at","expired_at");
        this.mFieldNameMap.put("thumbsup","thumbsup");
        this.mFieldNameMap.put("thumbsdown","thumbsdown");
        this.mFieldNameMap.put("comment_count","comment_count");
        this.mFieldNameMap.put("view_count","view_count");
        this.mFieldNameMap.put("star_count","star_count");
    }

    public static ImagePost populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the post model."+response.toString());
        try {
            ImagePost model = new ImagePost();
            if(response.has("id")) model.id = response.getInt("id");
            if(response.has("model_id")) model.model_id = response.getInt("model_id");
            if(response.has("model_type")) model.model_type = response.getString("model_type");
            if(response.has("type")) model.type = response.getInt("type");
            if(response.has("title")) model.title = response.getString("title");
            if(response.has("content")) model.content = response.getString("content");
            if(response.has("status")) model.status = response.getInt("status");
            if(response.has("source_url")) model.source_url = response.getString("source_url");
            if(response.has("source_name")) model.source_name = response.getString("source_name");
            if(response.has("author")) model.author = response.getString("author");
            if(response.has("created_at")) model.created_at = response.getString("created_at");
            if(response.has("updated_at")) model.updated_at = response.getString("updated_at");
            if(response.has("created_by")) model.created_by = response.getInt("created_by");
            if(response.has("updated_by")) model.updated_by = response.getInt("updated_by");
            if(response.has("effective_at")) model.effective_at = response.getString("effective_at");
            if(response.has("expired_at")) model.expired_at = response.getString("expired_at");
            if(response.has("thumbsup")) model.thumbsup = response.getInt("thumbsup");
            if(response.has("thumbsdown")) model.thumbsdown = response.getInt("thumbsdown");
            if(response.has("comment_count")) model.comment_count = response.getInt("comment_count");
            if(response.has("view_count")) model.view_count = response.getInt("view_count");
            if(response.has("star_count")) model.star_count = response.getInt("star_count");
            if(response.has("images") && !response.isNull("images")){
                JSONArray imageJsonArray = response.getJSONArray("images");
                model.imageList = new ArrayList<String>();
                Log.d(TAG,imageJsonArray.toString());
                for (int j=0; j<imageJsonArray.length(); j++){
                    model.imageList.add(imageJsonArray.getString(j));
                }
            }
            if(response.has("thumbnails") && !response.isNull("thumbnails")){
                JSONArray imageJsonArray = response.getJSONArray("thumbnails");
                model.thumbnailList = new ArrayList<String>();
                Log.d(TAG,imageJsonArray.toString());
                for (int j=0; j<imageJsonArray.length(); j++){
                    model.thumbnailList.add(imageJsonArray.getString(j));
                }
            }
            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<ImagePost> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the user model."+response.toString());
        ArrayList<ImagePost> arrayList = new ArrayList<ImagePost>();
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
                    ImagePost model = populateModel(items.getJSONObject(i));
                    if(model!=null) arrayList.add(model);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // response is a single result
            ImagePost model = populateModel(response);
            if(model!=null) arrayList.add(model);
        }
        return arrayList;
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
                Log.d(TAG,"ImagePost json object is :" + request.toString());
                return  request;
            case Model.ACTION_UPDATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.PATCH);
                if(id!=0){
                    connection.URL = connection.URL + URI + "/"+ id;
                    Log.d(TAG,"ImagePost update url is " + connection.URL);
                }else {
                    Log.d(TAG,"ImagePost for updating, but there is no post id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"ImagePost json object is :" + request.toString());
                return  request;
            case Model.ACTION_DELETE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.DELETE);
                if(id!=0){
                    connection.URL = connection.URL + URI + "/"+ id;
                    Log.d(TAG,"ImagePost delete url is " + connection.URL);
                }else {
                    Log.d(TAG,"ImagePost for delete, but there is no ImagePost id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"ImagePost delete json object is :" + request.toString());
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
        query.mModelClass = ImagePost.class;
        return query;
    }

    @Override
    public String getUrl() {
        return DEFAULT_WEB+"/index.php?r=imagepost/view&id="+ id +"&theme=app";
    }
}
