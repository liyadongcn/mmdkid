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

public class UserGroup extends Model {
    private static final String TAG = "UserGroup";
    private final static String URI = "v1/user-groups";
    
    public static final int GROUP_STATUS_AVAILABLE = 1;
	public static final int GROUP_STATUS_DISABLE  = 2;

	public static final int GROUP_TYPE_CLASS  = 1;
	public static final int GROUP_TYPE_FRIEND  = 2;
    
    public int mId;
    public String mName;
    public String mDescription;
    public int mMaster_id;
    public String mCreated_at;
    public String mUpdated_at;
    public int mCreated_by;
    public int mUpdated_by;
    public int mCount;
    public int mStatus;
    public int mType;
    public String mAvatar;

    public static ArrayList<UserGroup> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the userGroup model."+response.toString());
        ArrayList<UserGroup> arrayList = new ArrayList<UserGroup>();
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
                    UserGroup userGroup = populateModel(item);
                    if(userGroup!=null) arrayList.add(userGroup);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            UserGroup userGroup = populateModel(response);
            if(userGroup!=null ) arrayList.add(userGroup);
        }
        return arrayList;
    }

    public static UserGroup populateModel(JSONObject jsonObject){
        JSONObject item = jsonObject;
        try {
            UserGroup userGroup = new UserGroup();
            userGroup.mId = item.getInt("id");
            userGroup.mMaster_id = item.getInt("master_id");
            userGroup.mCount = item.getInt("count");
            userGroup.mName = item.getString("name");
            userGroup.mDescription = item.getString("description");
            userGroup.mCreated_by= item.getInt("created_by");
            userGroup.mUpdated_by = item.getInt("updated_by");
            userGroup.mCreated_at = item.getString("created_at");
            userGroup.mUpdated_at = item.getString("updated_at");
            userGroup.mStatus= item.getInt("status");
            userGroup.mType= item.getInt("type");
            userGroup.mAvatar = item.getString("avatar");
            return userGroup;
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
        query.mModelClass = UserGroup.class;
        return query;
    }
}
