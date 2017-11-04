package com.mmdkid.mmdkid.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mmdkid.mmdkid.singleton.InternetSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by LIYADONG on 2017/11/3.
 */

public class ElasticsearchConnection extends Connection {
    private final static String TAG = "ElasticsearchConnection";
    private Context mContext;
    public int REQUEST_METHOD = Request.Method.POST;
    private ElasticsearchConnection.OnConnectionListener mListener;

    public ElasticsearchConnection(Context context){
        this.mContext = context;
        if(context instanceof OnConnectionListener)
            mListener = (OnConnectionListener) context;
    }

    @Override
    public void Query(final Query query) {
        JSONObject jsonRequest = query.GetRequest();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, this.URL, jsonRequest, new Response.Listener<JSONObject>() {

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
                        // TODO Auto-generated method stub

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

    public void setListener(OnConnectionListener listener){
        this.mListener = listener;
    }

    public interface OnConnectionListener {
        void onErrorRespose(Class c,String error);
        void onResponse(Class c,ArrayList responseDataList);
    }
}
