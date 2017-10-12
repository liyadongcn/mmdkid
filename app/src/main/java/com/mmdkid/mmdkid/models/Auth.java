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
 * Created by LIYADONG on 2017/7/10.
 */

public class Auth extends Model {
    private static final String TAG = "Auth";
    private final static String URI = "v1/auths";
    private final static String SEARCH_URI = "v1/auths/search";


    public int    mId;
    public int    mUserId;
    public String mSourceId;
    public String mSource;
    public String mUsername;
    public String mSecret;
    public String mCreated_at;
    public String mUpdated_at;
    public int mStatus;

    @Override
    public void setAttributesNames() {
        this.mFieldNameMap.put("mId","id");
        this.mFieldNameMap.put("mUserId","user_id");
        this.mFieldNameMap.put("mSourceId","source_id");
        this.mFieldNameMap.put("mSource","source");
        this.mFieldNameMap.put("mSecret","secret");
        this.mFieldNameMap.put("mCreated_at","created_at");
        this.mFieldNameMap.put("mUpdated_at","updated_at");
        this.mFieldNameMap.put("mStatus","status");
    }

    @Override
    public JSONObject getRequest(String action, Connection connection) {
        switch (action){
            case RESTAPIConnection.ACTION_CREATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI  + "?expand=user_name";
                JSONObject request = new JSONObject();
                try {
                    request.put("user_id",mUserId);
                    request.put("source",mSource);
                    request.put("source_id",mSourceId);
                 } catch (JSONException e) {
                    e.printStackTrace();
                }
                return request;
        }
        return null;
    }

    public static ArrayList<Auth> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the auth model."+response.toString());
        ArrayList<Auth> arrayList = new ArrayList<Auth>();
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
                for(int i = 0; i < currentPageTotal; i++){
                    Auth model = populateModel(items.getJSONObject(i));
                    if(model!=null) arrayList.add(model);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            // response is a single result
            Auth model = populateModel(response);
            if(model!=null) arrayList.add(model);
        }
        return arrayList;
    }

    public static Auth populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the auth model."+response.toString());
        try {
            Auth model = new Auth();
            if(response.has("id")) model.mId = response.getInt("id");
            if(response.has("user_id")) model.mUserId = response.getInt("user_id");
            //if(response.has("source_id")) model.mSourceId = response.getString("source_id");
            //if(response.has("source")) model.mSource = response.getString("source");
            model.mSourceId = response.getString("source_id");
            model.mSource = response.getString("source");
            if(response.has("user_name")) model.mUsername = response.getString("user_name");
            if(response.has("secret")) model.mSecret = response.getString("secret");
            if(response.has("created_at")) model.mCreated_at = response.getString("created_at");
            if(response.has("updated_at")) model.mUpdated_at = response.getString("updated_at");
            if(response.has("status")) model.mStatus = response.getInt("status");
            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getRequest(Query query)  {
        RESTAPIConnection connection = (RESTAPIConnection) query.getConnection();
        connection.setRequestMethod(Request.Method.GET);
        connection.URL = connection.URL + SEARCH_URI + "?expand=user_name";
        Map<String,String> parameters = query.getmParameters();
        if (parameters!=null){
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                connection.URL = connection.URL + "&" +  entry.getKey() +"=" + entry.getValue();
            }
        }
        return null;
    }

   /* public static void create(Auth auth,Context context,RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        connection.create(auth);
    }*/

    public static Query find(Context context,RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = Auth.class;
        return query;
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
                Log.d(TAG,"Auth json object is :" + request.toString());
                return  request;
            case Model.ACTION_UPDATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.PATCH);
                if(mId!=0){
                    connection.URL = connection.URL + URI + "/"+ mId;
                    Log.d(TAG,"Auth update url is " + connection.URL);
                }else {
                    Log.d(TAG,"Auth for updating, but there is no student id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"Auth json object is :" + request.toString());
                return  request;

        }
        return null;
    }
}
