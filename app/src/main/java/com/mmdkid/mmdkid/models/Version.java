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
import java.util.Iterator;

/**
 * Created by LIYADONG on 2017/10/8.
 */

public class Version extends Model {
    private static final String TAG = "Version";
    private final static String URI = "v1/version";

    public int id;
    public int code;
    public String name;
    public String platform;
    public String log;
    public String created_at;
//    public String updated_at;
//    public int created_by;
//    public int updated_by;
    public int status;
    public ArrayList<String> locations;

    public static Version populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the version model."+response.toString());
        try {
            Version model = new Version();
            if(response.has("id")) model.id = response.getInt("id");
            if(response.has("code")) model.code = response.getInt("code");
            if(response.has("name")) model.name = response.getString("name");
            if(response.has("platform")) model.platform = response.getString("platform");
            if(response.has("log")) model.log = response.getString("log");
            if(response.has("created_at")) model.created_at = response.getString("created_at");
            if(response.has("status")) model.status = response.getInt("status");
            if(response.has("locations")){
                if (response.get("locations") instanceof JSONArray){
                    JSONArray jsonArray = response.getJSONArray("locations");
                    model.locations = new ArrayList<String>();
                    Log.d(TAG,jsonArray.toString());
                    for (int j=0; j<jsonArray.length(); j++){
                        model.locations.add(jsonArray.getString(j));
                    }
                } else if (response.get("locations") instanceof JSONObject){
                    JSONObject jsonObject = (JSONObject) response.get("locations");
                    Iterator iterator = jsonObject.keys();
                    model.locations = new ArrayList<String>();
                    String key,value;
                    while(iterator.hasNext()){
                        key = (String) iterator.next();
                        value = jsonObject.getString(key);
                        model.locations.add(value);
                    }
                }

            }
            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Version> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the version model."+response.toString());
        ArrayList<Version> arrayList = new ArrayList<Version>();
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
                    Version model = populateModel(items.getJSONObject(i));
                    if(model!=null) arrayList.add(model);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // response is a single result
            Version model = populateModel(response);
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
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = Version.class;
        return query;
    }
}
