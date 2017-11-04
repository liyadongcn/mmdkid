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

/**
 * 通过RestfulAPI获取的产品信息
 *
 * Created by LIYADONG on 2017/9/29.
 */

public class Goods extends Model {
    private final static String TAG = "Goods";
    private final static String URI = "v1/goods";
    private final static String URL = "http://www.g2gw.cn/index.php?r=goods/view&theme=app&id=";

    public static final int STATUS_DELETED = 0;
    public static final int STATUS_ACTIVE = 1;

    public int id;
    public int brand_id;
    public String code;
    public String description;
    public int thumbsup;
    public int thumbsdown;
    public String url;
    public String title;
    public String created_date;
    public String updated_date;
    public String comment_status;
    public int comment_count;
    public int star_count;
    public int recomended_count;
    public int view_count;
    public String status;
    public int userid;
    public ArrayList<String> imageList;
    public String editorComment;

    public static Goods populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the goods model."+response.toString());
        try {
            Goods model = new Goods();
            if(response.has("id")) model.id = response.getInt("id");
            if(response.has("brand_id")) model.brand_id = response.getInt("brand_id");
            if(response.has("code")) model.code = response.getString("code");
            if(response.has("description")) model.description = response.getString("description");
            if(response.has("thumbsup")) model.thumbsup = response.getInt("thumbsup");
            if(response.has("thumbsdown")) model.thumbsdown = response.getInt("thumbsdown");
            if(response.has("url")) model.url = response.getString("url");
            if(response.has("title")) model.title = response.getString("title");
            if(response.has("status")) model.status = response.getString("status");
            if(response.has("created_date")) model.created_date = response.getString("created_date");
            if(response.has("updated_date")) model.updated_date = response.getString("updated_date");
            if(response.has("comment_status")) model.comment_status = response.getString("comment_status");
            if(response.has("comment_count")) model.comment_count = response.getInt("comment_count");
            if(response.has("status")) model.status = response.getString("status");
            if(response.has("star_count")) model.star_count = response.getInt("star_count");
            if(response.has("recomended_count")) model.recomended_count = response.getInt("recomended_count");
            if(response.has("view_count")) model.view_count = response.getInt("view_count");
            if(response.has("userid")) model.userid = response.getInt("userid");
            if(response.has("image")){
                JSONArray imageJsonArray = response.getJSONArray("image");
                model.imageList = new ArrayList<String>();
                Log.d(TAG,imageJsonArray.toString());
                for (int j=0; j<imageJsonArray.length(); j++){
                    model.imageList.add(imageJsonArray.getString(j));
                }
            }
            if(response.has("editorComment")) model.editorComment = response.getString("editorComment");
            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Goods> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the user model."+response.toString());
        ArrayList<Goods> arrayList = new ArrayList<Goods>();
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
                    Goods model = populateModel(items.getJSONObject(i));
                    if(model!=null) arrayList.add(model);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // response is a single result
            Goods model = populateModel(response);
            if(model!=null) arrayList.add(model);
        }
        return arrayList;
    }

    public static JSONObject getRequest(Query query)  {
        RESTAPIConnection connection = (RESTAPIConnection) query.getConnection();
        connection.setRequestMethod(Request.Method.GET);
        connection.URL = connection.URL + URI + "?"
                + RESTAPIConnection.ACCESS_TOKEN_NAME + "=" + connection.ACCESS_TOKEN;
        /*Map<String,String> parameters = query.getmParameters();
        if (parameters!=null){
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                connection.URL = connection.URL + "&" +  entry.getKey() +"=" + entry.getValue();
            }
        }*/
        connection.URL = connection.URL + query.getCanonicalQueryString();
        return null;
    }
    public static Query find(Context context, RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.URL = "http://api.g2gw.cn/";
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = Goods.class;
        return query;
    }

    @Override
    public String getUrl() {
        return URL + this.id;
    }
}
