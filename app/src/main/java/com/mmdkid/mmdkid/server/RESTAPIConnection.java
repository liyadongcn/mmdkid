package com.mmdkid.mmdkid.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.helper.VolleyErrorHelper;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.singleton.InternetSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by LIYADONG on 2017/6/27.
 */

public class RESTAPIConnection extends Connection {
    private static final String TAG = "RESTAPIConnection";
    public static final String ACCESS_TOKEN_NAME = "accessToken";
    public String SERVER_URL = "http://api.mmdkid.cn/";
    //private static final String SERVER_URL = "http://10.0.3.2/";

    public static final String ACTION_CREATE = "create";
    public static final String ACTION_QUERY = "query";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_VIEW = "view";

    private OnConnectionListener mListener;
    private Context mContext;

    public String ACCESS_TOKEN = "";
    public int REQUEST_METHOD = Request.Method.POST;
    public String REQUEST_URL = "";

    public RESTAPIConnection(Context context){
        this.URL = SERVER_URL;
        this.mContext = context;
        if(context instanceof OnConnectionListener)
            mListener = (OnConnectionListener) context;
        /**
         * 判断当前用户是否已经登录，若已经登录则取得当前用户的访问Token
         *
         */
        App app = (App) context.getApplicationContext();
        if(!app.isGuest()){
            Token token = app.getCurrentToken();
            ACCESS_TOKEN = token.mAccessToken;
        }
        /*if (context instanceof Activity) {
            mListener = (OnConnectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConnectionListener");
        }*/
    }

    public void setRequestMethod(int method){
        REQUEST_METHOD = method;
    }

    public void setListener(OnConnectionListener listener){
        this.mListener = listener;
    }

    public void Query(final Query query){
        JSONObject jsonRequest = query.GetRequest();
        Log.d(TAG,"Request URL is " + this.URL + this.REQUEST_URL);
        if(jsonRequest!=null) Log.d(TAG,"Request data is " + jsonRequest.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (REQUEST_METHOD, this.URL+ this.REQUEST_URL, jsonRequest, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(ContentRecyclerViewActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        Log.d(TAG,"get response......");
                        try {
                            if(response.has("items")){
                                JSONArray items = response.getJSONArray("items");
                                JSONObject meta = response.getJSONObject("_meta");
                                int perPage = meta.getInt("perPage");
                                int currentPage = meta.getInt("currentPage");
                                int totalCount = meta.getInt("totalCount");
                                query.setTotal(totalCount);
                                query.setPageSize(perPage);
                            }
                            Method populateModelsMethod = query.mModelClass.getDeclaredMethod("populateModels",JSONObject.class);
                            if(mListener!=null) mListener.onResponse(query.mModelClass, (ArrayList) populateModelsMethod.invoke(null,response));
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        //Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, error.getMessage(), error);
                        if(error.networkResponse!=null){
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e(TAG, new String(htmlBodyBytes), error);
                        }
                        if(mListener!=null) mListener.onErrorRespose(query.mModelClass,
                                VolleyErrorHelper.getMessage(error,mContext));

                    }
                });

        // Access the RequestQueue through your singleton class.
        InternetSingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);

    }

    public void action(String action,final Model model){
        JSONObject jsonRequest = model.getRequest(action,this);
        Log.d(TAG,"Request URL is " + this.URL);
        if(jsonRequest!=null) Log.d(TAG,"Request data is " + jsonRequest.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (REQUEST_METHOD, this.URL, jsonRequest, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(ContentRecyclerViewActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        Log.d(TAG,"get create response......");
                        try {
                            Method populateModelsMethod = model.getClass().getDeclaredMethod("populateModels",JSONObject.class);
                            if(mListener!=null) mListener.onResponse(model.getClass(), (ArrayList) populateModelsMethod.invoke(null,response));
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        //Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, error.getMessage(), error);
                        if(error.networkResponse!=null){
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e(TAG, new String(htmlBodyBytes), error);
                        }
                        if(mListener!=null) mListener.onErrorRespose(model.getClass(),
                                VolleyErrorHelper.getMessage(error,mContext));

                    }
                });
        // Access the RequestQueue through your singleton class.
        InternetSingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

    public void create(final Model model){
        JSONObject jsonRequest = model.getRequest(ACTION_CREATE,this);
        Log.d(TAG,"Request URL is " + this.URL);
        if(jsonRequest!=null) Log.d(TAG,"Request data is " + jsonRequest.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (REQUEST_METHOD, this.URL, jsonRequest, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(ContentRecyclerViewActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        Log.d(TAG,"get create response......");
                        try {
                            Method populateModelsMethod = model.getClass().getDeclaredMethod("populateModels",JSONObject.class);
                            if(mListener!=null) mListener.onResponse(model.getClass(), (ArrayList) populateModelsMethod.invoke(null,response));
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        //Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, error.getMessage(), error);
                        if(error.networkResponse!=null){
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e(TAG, new String(htmlBodyBytes), error);
                        }
                        if(mListener!=null) mListener.onErrorRespose(model.getClass(),
                                VolleyErrorHelper.getMessage(error,mContext));

                    }
                });
        // Access the RequestQueue through your singleton class.
        InternetSingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

    public void excute(JSONObject jsonRequest, final Class modelClass){
        if(!this.ACCESS_TOKEN.equals("")){
           this.URL = this.URL + "?"
                    + RESTAPIConnection.ACCESS_TOKEN_NAME + "=" + this.ACCESS_TOKEN;
        }
        Log.d(TAG,"Request URL is " + this.URL);
        if(jsonRequest!=null) Log.d(TAG,"Request data is " + jsonRequest.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (REQUEST_METHOD, this.URL, jsonRequest, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(ContentRecyclerViewActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        Log.d(TAG,"get create response......");
                        try {
                            Method populateModelsMethod = modelClass.getDeclaredMethod("populateModels",JSONObject.class);
                            if(mListener!=null) mListener.onResponse(modelClass, (ArrayList) populateModelsMethod.invoke(null,response));
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        //Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, error.getMessage(), error);
                        if(error.networkResponse!=null){
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e(TAG, new String(htmlBodyBytes), error);
                        }
                        if(mListener!=null) mListener.onErrorRespose(modelClass,
                                VolleyErrorHelper.getMessage(error,mContext));

                    }
                });
        // Access the RequestQueue through your singleton class.
        InternetSingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }


    public interface OnConnectionListener {
        void onErrorRespose(Class c, String error);
        void onResponse(Class c, ArrayList responseDataList);
    }
}
