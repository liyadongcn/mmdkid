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

public class UserRelationship extends Model {
    private static final String TAG = "UserRelationship";
    private final static String URI = "v1/user-relationships";
    
    public static final String USER_RELATIONSHIP_MOTHER = "1";
	public static final String USER_RELATIONSHIP_FATHER = "2";
	public static final String USER_RELATIONSHIP_BROTHER = "3";
	public static final String USER_RELATIONSHIP_SISTER = "4";
	public static final String USER_RELATIONSHIP_GRANDFATHER = "5";
	public static final String USER_RELATIONSHIP_GRANDMOTHER = "6";
	public static final String USER_RELATIONSHIP_GRANDPA = "7";
	public static final String USER_RELATIONSHIP_GRANDMA = "8";
	public static final String USER_RELATIONSHIP_OTHER = "9";
	
	public static final int STATUS_ACTIVE =10;
	public static final int STATUS_APPLAYING =11;
	public static final int STATUS_REFUSED =12;
    
     public int mId;
     public int mUser1_id;
     public int mUser2_id;
     public String mRelationship;
     public String mCreated_at;
     public String mUpdated_at;
     public int mCreated_by;
     public int mUpdated_by;
     public int mStatus ;
     public String mMessage;
    public Model mChild;
    public Model mParent;

    public static ArrayList<UserRelationship> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the user-relationship model."+response.toString());
        ArrayList<UserRelationship> arrayList = new ArrayList<UserRelationship>();
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
                    UserRelationship userRelationship = populateModel(item);
                    if(userRelationship!=null) arrayList.add(userRelationship);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            UserRelationship userRelationship = populateModel(response);
            if(userRelationship!=null) arrayList.add(userRelationship);
        }
        return arrayList;
    }

    public static UserRelationship populateModel(JSONObject jsonObject){
        JSONObject item = jsonObject;
        try {
            UserRelationship userRelationship = new UserRelationship();
            userRelationship.mId = item.getInt("id");
            userRelationship.mUser1_id = item.getInt("user1_id");
            userRelationship.mUser2_id = item.getInt("user2_id");
            userRelationship.mStatus = item.getInt("status");
            userRelationship.mCreated_by = item.getInt("created_by");
            userRelationship.mRelationship = item.getString("relationship");
            userRelationship.mMessage = item.getString("message");
            userRelationship.mUpdated_by = item.getInt("updated_by");
            userRelationship.mCreated_at = item.getString("created_at");
            userRelationship.mUpdated_at = item.getString("updated_at");
            userRelationship.mCreated_at = item.getString("created_at");
            userRelationship.mUpdated_at = item.getString("updated_at");
            if(item.has("child")){
                userRelationship.mChild = Student.populateModel(item.getJSONObject("child"));
            }
            if(item.has("parent")){
                userRelationship.mParent = User.populateModel(item.getJSONObject("parent"));
            }
            return userRelationship;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getRequest(Query query)  {
        RESTAPIConnection connection = (RESTAPIConnection) query.getConnection();
        connection.setRequestMethod(Request.Method.GET);
        connection.URL = connection.URL + URI + "?expand=child&"
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
        query.mModelClass = UserRelationship.class;
        return query;
    }

    public static String getRelationship(String name){
        switch (name){
            case "妈妈":
                return USER_RELATIONSHIP_MOTHER;
            case "爸爸":
                return USER_RELATIONSHIP_FATHER;
            case "奶奶":
                return USER_RELATIONSHIP_GRANDMOTHER;
            case "爷爷":
                return USER_RELATIONSHIP_GRANDFATHER;
            case "姥姥":
                return USER_RELATIONSHIP_GRANDMA;
            case "姥爷":
                return USER_RELATIONSHIP_GRANDPA;
            case "哥哥":
                return USER_RELATIONSHIP_BROTHER;
            case "姐姐":
                return USER_RELATIONSHIP_SISTER;

        }
        return USER_RELATIONSHIP_OTHER;
    }

    public static String getRelationshipName(String relationship){
        switch (relationship){
            case USER_RELATIONSHIP_MOTHER:
                return "妈妈";
            case USER_RELATIONSHIP_FATHER:
                return "爸爸";
            case USER_RELATIONSHIP_GRANDMOTHER:
                return "奶奶";
            case USER_RELATIONSHIP_GRANDFATHER:
                return "爷爷";
            case USER_RELATIONSHIP_GRANDMA:
                return "姥姥";
            case USER_RELATIONSHIP_GRANDPA:
                return "姥爷";
            case USER_RELATIONSHIP_BROTHER:
                return "哥哥";
            case USER_RELATIONSHIP_SISTER:
                return "姐姐";

        }
        return null;
    }

    public static String[] getRelationshipNames(){
        return new String[]{"妈妈","爸爸","奶奶","爷爷","姥姥","姥爷","其他"};
    }

}
