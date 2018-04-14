package com.mmdkid.mmdkid.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by LIYADONG on 2017/6/27.
 *
 */

public class Token extends Model {

    private final static String TAG = "Token";

    public String mAccessToken;
    public int   mExpiresIn;
    public String mTokenType;
    public String mScope;
    public String mRefreshToken;
    public Date   mCreatedAt;

    public static Query find(Connection connection)
    {
        Query query = new Query(connection);
        query.mModelClass = Token.class;
        connection.URL = connection.URL + "oauth2/token";
        Log.d(TAG,connection.URL);
        return query;
    }

    public static Query find(Context context, RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.URL = connection.URL + "oauth2/token";
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = Token.class;
        return query;
    }

    public static JSONObject getRequest(Query query)  {
        JSONObject jsonObject = new JSONObject();
        RESTAPIConnection connection = (RESTAPIConnection) query.getConnection();
        try {
            jsonObject.put("grant_type","password");
            jsonObject.put("client_id","testclient");
            jsonObject.put("client_secret","testpass");
            Map<String,String> parameters = query.getmParameters();
            if (parameters!=null){
                for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                    jsonObject.put(parameter.getKey(),parameter.getValue());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    public static ArrayList<Token> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the token model."+response.toString());
        ArrayList<Token> arrayList = new ArrayList<Token>();
        Token token = new Token();
        try {
            token.mAccessToken = response.getString("access_token");
            token.mExpiresIn = response.getInt("expires_in");
            token.mTokenType = response.getString("token_type");
            token.mScope = response.getString("scope");
            token.mRefreshToken = response.getString("refresh_token");
            token.mCreatedAt =  new Date(System.currentTimeMillis());
            arrayList.add(token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void saveToLocal(Context context){
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("access_token",this.mAccessToken);
        editor.putString("token_type",this.mTokenType);
        editor.putString("scope",this.mScope);
        editor.putString("refresh_token",this.mRefreshToken);
        editor.putInt("expires_in",this.mExpiresIn);
        editor.putLong("created_at",this.mCreatedAt.getTime());
        editor.commit();
    }

    public static Token loadFromLocal(Context context){
        Token token = new Token();
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME,Context.MODE_PRIVATE);
        if(TextUtils.isEmpty(token.mAccessToken=settings.getString("access_token",""))) return null;
        token.mExpiresIn=settings.getInt("expires_in",0);
        token.mTokenType=settings.getString("token_type","");
        token.mScope=settings.getString("scope","");
        token.mRefreshToken=settings.getString("refresh_token","");
        token.mCreatedAt = new Date(settings.getLong("created_at",0));
        Log.d(TAG,"Token from local is " + token.toString());
        return token;
    }

    public boolean isValid(){
        Log.d(TAG,"Token expire in " + mExpiresIn);
        Log.d(TAG,"Token created at " + mCreatedAt.getTime());
        Log.d(TAG,"Now is " +  System.currentTimeMillis());
        if((mExpiresIn + mCreatedAt.getTime()/1000) > (System.currentTimeMillis()/1000) ){
            return true;
        }else {
            return false;
        }
    }
}
