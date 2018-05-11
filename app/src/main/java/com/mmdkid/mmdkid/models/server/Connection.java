package com.mmdkid.mmdkid.models.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mmdkid.mmdkid.server.ElasticConnection;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.singleton.InternetSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Connection {
    private final static String TAG = "Connection";
    private String mUrl;
    private Context mContext;
    private OnConnectionListener mListener;

    public void Excute(final Query query){
        JSONObject jsonRequest = query.GetRequest();
        Log.d(TAG,"Elatic Json Request is >>>" + jsonRequest.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, this.mUrl, jsonRequest, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(ContentRecyclerViewActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        Log.d(TAG,"get response." + response.toString());

                        try {
                            JSONArray jsonArray = response.getJSONObject("hits").getJSONArray("hits");
                            int total = response.getJSONObject("hits").getInt("total");
                            query.setTotal(total);
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
                        Log.e(TAG, error.getMessage(), error);
                        if(error.networkResponse!=null){
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e(TAG, new String(htmlBodyBytes), error);
                        }
                        if(mListener!=null) mListener.onErrorRespose(query.mModelClass,error.toString());
                    }
                });
        // Access the RequestQueue through your singleton class.
        InternetSingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

    public interface OnConnectionListener {
        void onErrorRespose(Class c,String error);
        void onResponse(Class c,ArrayList responseDataList);
    }
}
