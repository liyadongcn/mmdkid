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
 * Created by LIYADONG on 2017/7/27.
 */

public class UserGroupMap extends Model {
    private static final String TAG = "UserGroupMap";
    private final static String URI = "v1/user-group-maps";
    
    public static final int STATUS_ACTIVE =10;
	public static final int STATUS_APPLAYING =11;
	public static final int STATUS_REFUSED =12;

    public int mId;
    public int mUser_id;
    public int mGroup_id;
    public String mCreated_at;
    public String mUpdated_at;
    public int mCreated_by;
    public int mUpdated_by;
    public String mViewed_at;
    public String mMessage;
    public int mStatus;
    public Model mModelGroup;
    public Model mModelUser;

    public static ArrayList<UserGroupMap> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the userGroupMap model."+response.toString());
        ArrayList<UserGroupMap> arrayList = new ArrayList<UserGroupMap>();
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
                    UserGroupMap userGroupMap = populateModel(item);
                    if(userGroupMap!=null) arrayList.add(userGroupMap);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            UserGroupMap userGroupMap = populateModel(response);
            if(userGroupMap!=null ) arrayList.add(userGroupMap);
        }
        return arrayList;
    }

    public static UserGroupMap populateModel(JSONObject jsonObject){
        JSONObject item = jsonObject;
        try {
            UserGroupMap userGroupMap = new UserGroupMap();
            userGroupMap.mId = item.getInt("id");
            userGroupMap.mUser_id = item.getInt("user_id");
            userGroupMap.mGroup_id = item.getInt("group_id");
            userGroupMap.mViewed_at = item.getString("viewed_at");
            userGroupMap.mMessage = item.getString("message");
            userGroupMap.mCreated_by= item.getInt("created_by");
            userGroupMap.mUpdated_by = item.getInt("updated_by");
            userGroupMap.mCreated_at = item.getString("created_at");
            userGroupMap.mUpdated_at = item.getString("updated_at");
            userGroupMap.mStatus= item.getInt("status");
            if (item.has("model_user_group")){
                userGroupMap.mModelGroup = UserGroup.populateModel(item.getJSONObject("model_user_group"));
            }
            if (item.has("model_user")){
                userGroupMap.mModelUser = User.populateModel(item.getJSONObject("model_user"));
            }
            return userGroupMap;
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
        return null;
    }
    public static Query find(Context context, RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = UserGroupMap.class;
        return query;
    }
    
}
