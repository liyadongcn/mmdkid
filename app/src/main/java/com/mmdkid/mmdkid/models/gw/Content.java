package com.mmdkid.mmdkid.models.gw;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.ElasticConnection;
import com.mmdkid.mmdkid.server.ElasticQuery;
import com.mmdkid.mmdkid.server.ElasticsearchConnection;
import com.mmdkid.mmdkid.server.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 对应给g2gw的content
 *
 * Created by LIYADONG on 2017/11/02.
 */

public class Content extends Model {
    private static final String TAG = "Content";
    private static final String SERVER_URL = "http://www.g2gw.cn/index.php?r=";
    private final static String SEARCH_URI =  "http://211.149.212.21:9200/g2gw/_search";

    public static final String TYPE_PUSH = "push";
    public static final String TYPE_HOT = "hot";
    public static final String TYPE_POSTS = "posts";
    public static final String TYPE_GOODS = "goods";

    public String mTitle;
    public String mCreatedAt;
    public String mImage;
    public String mContent;
    public String mModelType;
    public int mModelId;
    public String mVideo;
    public String mSource_name;
    public String mSource_url;
    public int mCommentCount;
    public int mViewCount;
    public int mStarCount;
    public int mThumbsup;
    public int mThumbsdown;
    public int mBrand;
    public ArrayList<String> mImageList;
    public ArrayList<String> mLinkList;
    public JSONObject mEditorComment;

    public static Query find(Connection connection)
    {
        Query query = new ElasticQuery(connection);
        query.mModelClass = Content.class;
        Log.d(TAG,connection.URL);
        return query;
    }

    public static Query find(Context context, ElasticsearchConnection.OnConnectionListener listener)
    {
        ElasticsearchConnection connection = new ElasticsearchConnection(context);
        connection.setListener(listener);
        connection.URL = SEARCH_URI;
        Query query = new ElasticQuery(connection);
        query.mModelClass = Content.class;
        Log.d(TAG,connection.URL);
        return query;
    }

    public static Content populateModel(JSONObject jsonObject){
        try {
            Content content = new Content();
            content.mModelType = jsonObject.getString("model_type");
            content.mModelId = jsonObject.getInt("model_id");
            if(jsonObject.has("image")){
                Log.d(TAG,content.mModelType);
                if( content.mModelType.equalsIgnoreCase(Content.TYPE_GOODS) ){
                    JSONArray imageJsonArray = jsonObject.getJSONArray("image");
                    content.mImageList = new ArrayList<String>();
                    Log.d(TAG,imageJsonArray.toString());
                    for (int j=0; j<imageJsonArray.length(); j++){
                        content.mImageList.add(imageJsonArray.getString(j));
                    }
                }else{
                    content.mImage =  jsonObject.getString("image") ;
                    Uri uri = Uri.parse(content.mImage);
                    if(uri.getScheme()==null){
                        content.mImage = "http:"+content.mImage;
                    }
                }
            }
            if (jsonObject.has("link")) {
                JSONArray linkJsonArray = jsonObject.getJSONArray("link");
                content.mLinkList = new ArrayList<String>();
                for (int j=0; j<linkJsonArray.length(); j++){
                    content.mImageList.add(linkJsonArray.getString(j));
                }
            }
            content.mTitle = jsonObject.getString("title");
            content.mCreatedAt = jsonObject.getString("created_at");
            content.mContent = jsonObject.getString("content");
            if (jsonObject.has("source_name")) content.mSource_name = jsonObject.getString("source_name");
            if (jsonObject.has("source_url")) content.mSource_url = jsonObject.getString("source_url");
            content.mVideo = jsonObject.has("video") ? jsonObject.getString("video") : "";
            if (jsonObject.has("thumbsup")) content.mThumbsup = jsonObject.getInt("thumbsup");
            if (jsonObject.has("thumbsdown")) content.mThumbsdown = jsonObject.getInt("thumbsdown");
            if (jsonObject.has("comment_count")) content.mCommentCount = jsonObject.getInt("comment_count");
            if (jsonObject.has("view_count")) content.mViewCount = jsonObject.getInt("view_count");
            if (jsonObject.has("star_count")) content.mStarCount = jsonObject.getInt("star_count");
            //if (jsonObject.has("brand")) content.mBrand = jsonObject.getInt("brand");
            return content;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static  ArrayList<Content> populateModels(JSONObject response) {
        ArrayList<Content> results = new ArrayList<Content>();
        try {
            JSONArray jsonArray = response.getJSONObject("hits").getJSONArray("hits");
            int total = response.getJSONObject("hits").getInt("total");
            Log.i(TAG,"total hits is:"+total);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i).getJSONObject("_source");
                Content content = populateModel(jsonObject);
                if(content!=null) results.add(content);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }
    @Override
    public String getUrl(){
        switch(this.mModelType){
            case TYPE_GOODS:
                return SERVER_URL+"goods/view&id="+mModelId+"&theme=app";
            case TYPE_POSTS:
                return SERVER_URL+"posts/view&id="+mModelId+"&theme=app";
            default:
                return "";
        }
    }
    /*@Override
    public int getViewType(){
        if(mViewType == 0){
            switch(this.mModelType){
                case TYPE_VIDEO:
                    return Model.VIEW_TYPE_CONTENT_VIDEO;
                case TYPE_IMAGE:
                    return Model.VIEW_TYPE_CONTENT_IMAGE_POST;
                case TYPE_POST:
                    return Model.VIEW_TYPE_CONTENT_POST;
            }
        }
        return mViewType;
    }*/

    public static JSONObject getRequest(Query query)  {
        ElasticConnection connection = (ElasticConnection) query.getConnection();
        connection.setRequestMethod(Request.Method.POST);
        connection.URL = connection.URL + SEARCH_URI;
        JSONObject request = new JSONObject();
        try {
            request.put("query",
                    new JSONObject().put("multip_query",
                            new JSONObject()
                                    .put("query","母亲")
                                    .put("fields",new JSONArray()
                                            .put("title")
                                            .put("content"))))
                    .put("from",query.getPageFrom())
                    .put("size",10);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return request;
        /*Map<String,String> parameters = query.getmParameters();
        if (parameters!=null){
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                connection.URL = connection.URL + "&" +  entry.getKey() +"=" + entry.getValue();
            }
        }*/
        //return null;
    }


}