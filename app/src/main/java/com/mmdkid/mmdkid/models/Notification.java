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

public class Notification extends Model {
    private static final String TAG = "Notification";
    private final static String URI = "v1/notifications";

    // Notification types
    // 关注或订阅的人发表新帖
	public static final int TYPE_NEW_POST = 10;
	public static final int TYPE_NEW_COMMENT = 11;
	public static final int TYPE_NEW_FOLLOWER = 12;
    // 申请为家长
	public static final int TYPE_PARENT_APPLYING =13;
    // 申请加入班级
	public static final int TYPE_CLASS_APPLYING =14;
    // 申请加入朋友圈
	public static final int TYPE_FRIEND_APPLYING =14;

    // Special sender
	public static final int SENDER_SYSTEM = 0;

    // Notification status
	public static final int STATUS_UNREAD = 0;
	public static final int STATUS_READ = 1;
    
    public int mId;
    public int mSender;
    public int mReceiver;
    public int mType;
    public String mTitle;
    public String mModelType;
    public int mModelId;
    public String mReadAt;
    public int mStatus;
    public String mCreatedAt;
    public String mUpdatedAt;
    public Model mModel;

    public static ArrayList<Notification> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the notification model."+response.toString());
        ArrayList<Notification> arrayList = new ArrayList<Notification>();
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
                    Notification notification = populateModel(item);
                    if(notification!=null) arrayList.add(notification);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // return one result.
            Notification notification = populateModel(response);
            if(notification!=null) arrayList.add(notification);
        }
        return arrayList;
    }

    public static Notification populateModel(JSONObject jsonObject){
        JSONObject item = jsonObject;
        try {
            Notification notification = new Notification();
            notification.mId = item.getInt("id");
            notification.mSender = item.getInt("sender");
            notification.mReceiver = item.getInt("receiver");
            notification.mStatus = item.getInt("status");
            notification.mType = item.getInt("type");
            notification.mTitle = item.getString("title");
            notification.mModelId = item.getInt("model_id");
            notification.mModelType = item.getString("model_type");
            notification.mReadAt = item.getString("read_at");
            notification.mCreatedAt = item.getString("created_at");
            notification.mUpdatedAt = item.getString("updated_at");
            if(item.has("model_content")){
                if (notification.mModelType.equals(Content.TYPE_IMAGE)
                        || notification.mModelType.equals(Content.TYPE_POST)
                        || notification.mModelType.equals(Content.TYPE_VIDEO) ){
                    notification.mModel = Content.populateModel(item.getJSONObject("model_content"));
                }
            }
            return notification;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getRequest(Query query)  {
        RESTAPIConnection connection = (RESTAPIConnection) query.getConnection();
        connection.setRequestMethod(Request.Method.GET);
        connection.URL = connection.URL + URI + "?expand=model_content&"
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
        query.mModelClass = Notification.class;
        return query;
    }

}
