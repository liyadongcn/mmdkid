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
 * Created by LIYADONG on 2017/7/20.
 */

public class Diary extends Model {
    private static final String TAG = "Diary";
    private final static String URI = "v1/diaries";
    private final static String URL = "http://www.mmdkid.cn/index.php?r=diary/view&theme=app&id=";

    public int mId;
    public int mUser_id;
    public String mCreated_at;
    public String mUpdated_at;
    public int mCreated_by;
    public int mUpdated_by;
    public String mDate;
    public String mContent;
    public ArrayList<String> mImageList;
    public User mStudent;

    public static ArrayList<Diary> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the diary model."+response.toString());
        ArrayList<Diary> arrayList = new ArrayList<Diary>();
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
                    Diary diary = populateModel(item);
                    if(diary!=null) arrayList.add(diary);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            Diary diary = populateModel(response);
            if(diary!=null) arrayList.add(diary);
        }
        return arrayList;
    }

    public static Diary populateModel(JSONObject jsonObject){
        JSONObject item = jsonObject;
        try {
            Diary diary = new Diary();
            diary.mId = item.getInt("id");
            diary.mUser_id = item.getInt("user_id");
            diary.mCreated_by = item.getInt("created_by");
            diary.mUpdated_by = item.getInt("updated_by");
            diary.mDate = item.getString("date");
            diary.mContent = item.getString("content");
            diary.mCreated_at = item.getString("created_at");
            diary.mUpdated_at = item.getString("updated_at");
            if(item.has("image")){
                diary.mImageList = new ArrayList<String>();
                JSONArray array = item.getJSONArray("image");
                for ( int i =0; i< array.length();i++){
                    diary.mImageList.add(array.getString(i));
                }
            }
            if(item.has("student")){
                diary.mStudent = User.populateModel(item.getJSONObject("student"));
            }
            return diary;
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
        connection.URL = connection.URL + "&page=" + Integer.toString(query.getCurrentPage());
        return null;
    }

    public static Query find(Context context, RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = Diary.class;
        return query;
    }

    @Override
    public String getUrl() {
        return URL+mId;
    }
}
