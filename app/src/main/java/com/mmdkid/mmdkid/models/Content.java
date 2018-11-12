package com.mmdkid.mmdkid.models;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.ElasticConnection;
import com.mmdkid.mmdkid.server.ElasticQuery;
import com.mmdkid.mmdkid.server.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by LIYADONG on 2016/12/19.
 */

public class Content extends Model {
    private static final String TAG = "Content";
    private static final String SERVER_URL = "http://www.mmdkid.cn/index.php?r=";
    private final static String SEARCH_URI = "content/_search";

    public static final String TYPE_USER = "user";
    public static final String TYPE_VIDEO = "media";
    public static final String TYPE_POST = "post";
    public static final String TYPE_IMAGE = "imagepost";
    public static final String TYPE_HOT = "hot";
    public static final String TYPE_PUSH = "push";
    public static final String TYPE_FOLLOW = "follow";

    public String mId;
    public String mTitle;
    public String mCreatedAt;
    public String mImage;
    public String mContent;
    public String mModelType;
    public int mModelId;
    public String mVideo;
    public String mSource_name;
    public String mSource_url;
    public String mAuthor;
    public int mCommentCount;
    public int mViewCount;
    public int mStarCount;
    public int mThumbsup;
    public int mThumbsdown;
    public int mCreatedBy;
    public ArrayList<String> mImageList;
    public ArrayList<String> mImageDescriptionList;
    public User mUser;

    public static Query find(Connection connection)
    {
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
                /*Log.d(TAG,content.mModelType);
                if( content.mModelType.equalsIgnoreCase(Content.TYPE_IMAGE) ){
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
                }*/
                if (jsonObject.get("image") instanceof String){
                    content.mImage =  jsonObject.getString("image") ;
                    Uri uri = Uri.parse(content.mImage);
                    if(uri.getScheme()==null){
                        content.mImage = "http:"+content.mImage;
                    }
                }else if (jsonObject.get("image") instanceof JSONArray){
                    JSONArray imageJsonArray = jsonObject.getJSONArray("image");
                    content.mImageList = new ArrayList<String>();
                    Log.d(TAG,imageJsonArray.toString());
                    for (int j=0; j<imageJsonArray.length(); j++){
                        content.mImageList.add(imageJsonArray.getString(j));
                    }
                }
            }
            content.mTitle = jsonObject.getString("title");
            content.mCreatedAt = jsonObject.getString("created_at");
            content.mContent = jsonObject.getString("content");
            if (jsonObject.has("id")) content.mId = jsonObject.getString("id");
            if (jsonObject.has("source_name") && !jsonObject.isNull("source_name")) content.mSource_name = jsonObject.getString("source_name");
            if (jsonObject.has("source_url")) content.mSource_url = jsonObject.getString("source_url");
            if (jsonObject.has("author")) {
                content.mAuthor = jsonObject.getString("author");
                if (content.mAuthor=="null") content.mAuthor="";
            }else{
                content.mAuthor = "";
            }
            content.mVideo = jsonObject.has("video") ? jsonObject.getString("video") : "";
            if (jsonObject.has("thumbsup")) content.mThumbsup = jsonObject.getInt("thumbsup");
            if (jsonObject.has("thumbsdown")) content.mThumbsdown = jsonObject.getInt("thumbsdown");
            if (jsonObject.has("comment_count")) content.mCommentCount = jsonObject.getInt("comment_count");
            if (jsonObject.has("view_count")) content.mViewCount = jsonObject.getInt("view_count");
            if (jsonObject.has("star_count")) content.mStarCount = jsonObject.getInt("star_count");
            if (jsonObject.has("created_by")) content.mCreatedBy = jsonObject.getInt("created_by");
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

    public String getContentUrl(){
        switch(this.mModelType){
            case TYPE_VIDEO:
                return SERVER_URL+"media/view&id="+mModelId+"&theme=app";
            case TYPE_IMAGE:
                return SERVER_URL+"imagepost/view&id="+mModelId+"&theme=app";
            case TYPE_POST:
                return SERVER_URL+"post/view&id="+mModelId+"&theme=app";
            default:
                return "";
        }
    }

    public String getContentCommentUrl() {
        return SERVER_URL + this.mModelType + "/comment&id=" + mModelId+"&theme=app";
    }

    @Override
    public int getViewType(){
        // 若没有设置显示式样，则给出缺省的式样
        if(mViewType == 0){
            switch(this.mModelType){
                case TYPE_VIDEO:
                    return Model.VIEW_TYPE_CONTENT_VIDEO;
                case TYPE_IMAGE:
                    return Model.VIEW_TYPE_CONTENT_IMAGE_POST;
                case TYPE_POST:
                    return Model.VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE;
            }
        }
        return mViewType;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Content)) return false;
        return this.mId.equals(((Content)obj).mId);
    }

    public String getThumbsupUrl(){
        switch(this.mModelType){
            case TYPE_VIDEO:
                return SERVER_URL+"media/thumbsup&id="+mModelId+"&theme=app";
            case TYPE_IMAGE:
                return SERVER_URL+"imagepost/thumbsup&id="+mModelId+"&theme=app";
            case TYPE_POST:
                return SERVER_URL+"post/thumbsup&id="+mModelId+"&theme=app";
            default:
                return "";
        }
    }
}