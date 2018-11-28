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
 * Created by LIYADONG on 2017/7/19.
 */

public class Behavior extends Model {
    protected static final String TAG = "Behavior";
    protected static final String URI = "v1/behaviors";

    public static final String BEHAVIOR_FOLLOW="follow";
	public static final String BEHAVIOR_FOLLOWER="follower";
	public static final String BEHAVIOR_STAR="star";
	public static final String BEHAVIOR_LIKE="like";
	public static final String BEHAVIOR_LASTVIEW="last_view";
	public static final String BEHAVIOR_VIEW="view";
    public static final String BEHAVIOR_THUMBSUP="thumbsup";

    public int mId;
    public int mUserId;
    public String mName;
    public String mParams;
    public String mModelType;
    public int mModelId;
    public String mCreatedAt;
    public String mUpdatedAt;
    public Model mModel;

    @Override
    public void setAttributesNames() {
        this.mFieldNameMap.put("mId","id");
        this.mFieldNameMap.put("mUserId","user_id");
        this.mFieldNameMap.put("mName","name");
        this.mFieldNameMap.put("mParams","params");
        this.mFieldNameMap.put("mModelType","model_type");
        this.mFieldNameMap.put("mModelId","model_id");
        this.mFieldNameMap.put("mCreated_at","created_at");
        this.mFieldNameMap.put("mUpdated_at","updated_at");
    }

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
                    //if(behavior!=null && behavior.mModel!=null) arrayList.add(behavior);
                    if(behavior!=null) arrayList.add(behavior);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            Behavior behavior = populateModel(response);
            if(behavior!=null) arrayList.add(behavior);
        }
        return arrayList;
    }

    public static Behavior populateModel(JSONObject response){
        Log.d(TAG,"Single behavior object.");
        try {
            Behavior behavior = new Behavior();
            if(response.has("id")) behavior.mId = response.getInt("id");
            if(response.has("user_id")) behavior.mUserId = response.getInt("user_id");
            if(response.has("name")) behavior.mName = response.getString("name");
            if(response.has("params")) behavior.mParams = response.getString("params");
            if(response.has("model_id")) behavior.mModelId = response.getInt("model_id");
            if(response.has("model_type")) behavior.mModelType = response.getString("model_type");
            if(response.has("created_at")) behavior.mCreatedAt = response.getString("created_at");
            if(response.has("updated_at")) behavior.mUpdatedAt = response.getString("updated_at");
            if(response.has("model_content")){
                if (behavior.mModelType.equals(Content.TYPE_IMAGE)
                        || behavior.mModelType.equals(Content.TYPE_POST)
                        || behavior.mModelType.equals(Content.TYPE_VIDEO) ){
                    behavior.mModel = Content.populateModel(response.getJSONObject("model_content"));
                }
            }
            if(response.has("model") && behavior.mModelType.equals(Content.TYPE_USER)){
                behavior.mModel = User.populateModel(response.getJSONObject("model"));
            }
            if(response.has("model_user") && behavior.mModelType.equals(Content.TYPE_USER)){
                behavior.mModel = User.populateModel(response.getJSONObject("model_user"));
            }
            return behavior;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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
                Log.d(TAG,"Behavior json object is :" + request.toString());
                return  request;
            case Model.ACTION_UPDATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.PATCH);
                if(mId!=0){
                    connection.URL = connection.URL + URI + "/"+ mId;
                    Log.d(TAG,"Behavior update url is " + connection.URL);
                }else {
                    Log.d(TAG,"Behavior for updating, but there is no post id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"Behavior json object is :" + request.toString());
                return  request;
            case Model.ACTION_DELETE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.DELETE);
                if(mId!=0){
                    connection.URL = connection.URL + URI + "/"+ mId;
                    Log.d(TAG,"Behavior delete url is " + connection.URL);
                }else {
                    Log.d(TAG,"Behavior for delete, but there is no Behavior id.");
                    return null;
                }
               /* request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"Behavior delete json object is :" + request.toString());*/
                return  request;
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
