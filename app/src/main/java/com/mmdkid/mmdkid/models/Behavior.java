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
 * Created by LIYADONG on 2017/7/19.
 */

public class Behavior extends Model {
    private static final String TAG = "Behavior";
    private final static String URI = "v1/behaviors";

    public static final String BEHAVIOR_FOLLOW="follow";
	public static final String BEHAVIOR_FOLLOWER="follower";
	public static final String BEHAVIOR_STAR="star";
	public static final String BEHAVIOR_LIKE="like";
	public static final String BEHAVIOR_LASTVIEW="last_view";
	public static final String BEHAVIOR_VIEW="view";

    public int mId;
    public int mUserId;
    public String mName;
    public String mParams;
    public String mModelType;
    public int mModelId;
    public String mCreatedAt;
    public String mUpdatedAt;
    public Model mModel;

    public static ArrayList<Behavior> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the behavior model."+response.toString());
        ArrayList<Behavior> arrayList = new ArrayList<Behavior>();
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
                    Behavior behavior = populateModel(item);
                    if(behavior!=null && behavior.mModel!=null) arrayList.add(behavior);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            Behavior behavior = populateModel(response);
            if(behavior!=null  && behavior.mModel!=null) arrayList.add(behavior);
        }
        return arrayList;
    }

    public static Behavior populateModel(JSONObject jsonObject){
        JSONObject item = jsonObject;
        try {
            Behavior behavior = new Behavior();
            behavior.mId = item.getInt("id");
            behavior.mUserId = item.getInt("user_id");
            behavior.mName = item.getString("name");
            behavior.mParams = item.getString("params");
            behavior.mModelId = item.getInt("model_id");
            behavior.mModelType = item.getString("model_type");
            behavior.mCreatedAt = item.getString("created_at");
            behavior.mUpdatedAt = item.getString("updated_at");
            if(item.has("model_content")){
                if (behavior.mModelType.equals(Content.TYPE_IMAGE)
                        || behavior.mModelType.equals(Content.TYPE_POST)
                        || behavior.mModelType.equals(Content.TYPE_VIDEO) ){
                    behavior.mModel = Content.populateModel(item.getJSONObject("model_content"));
                }
            }
            if(item.has("model") && behavior.mModelType.equals(Content.TYPE_USER)){
                behavior.mModel = User.populateModel(item.getJSONObject("model"));
            }
            if(item.has("model_user") && behavior.mModelType.equals(Content.TYPE_USER)){
                behavior.mModel = User.populateModel(item.getJSONObject("model_user"));
            }
            return behavior;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

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
        query.mModelClass = Behavior.class;
        return query;
    }
}
