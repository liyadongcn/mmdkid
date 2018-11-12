package com.mmdkid.mmdkid.server;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LIYADONG on 2017/6/9.
 */

public class ElasticConnection extends Connection {
    private static final String TAG = "ElasticConnection";
    private static final String SERVER_URL = "http://211.149.212.21:9200/momoda/_search";
    //private static final String SERVER_URL = "https://192.168.1.140:9200/momoda/_search";
    private Context mContext;
    public int REQUEST_METHOD = Request.Method.POST;

    //private ArrayList<Content> mDataset;
   /* private RecyclerView.Adapter mAdapter=null;
    private SwipeRefreshLayout mRefreshLayout=null;
    private ProgressDialog mProgressDialog=null;*/
    private OnConnectionListener mListener;

    @Override
    public void Query(final Query query) {
        JSONObject jsonRequest = query.GetRequest();
        Log.d(TAG,"Elatic Json Request is >>>" + jsonRequest.toString());
        Log.d(TAG,"Elatic url is:  >>>" + this.URL);
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
                       /* try {
                            JSONArray jsonArray = response.getJSONObject("hits").getJSONArray("hits");
                            int total = response.getJSONObject("hits").getInt("total");
                            Log.i(LOG_TAG, "total hits is:" + total);
                            query.setTotal(total);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Content.PopulateModels(response,mDataset);*/
                       /* if(mProgressDialog!=null) mProgressDialog.dismiss();
                        if(mAdapter!=null) mAdapter.notifyDataSetChanged();
                        if(mRefreshLayout!=null) mRefreshLayout.setRefreshing(false);*/
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(mContext,"服务器访问错误~",Toast.LENGTH_LONG).show();
                        Log.e(TAG, error.getMessage(), error);
                        if(error.networkResponse!=null){
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e(TAG, new String(htmlBodyBytes), error);

                        }
                        if(mListener!=null) mListener.onErrorRespose(query.mModelClass,error.toString());
                    }
                }){
            /*@Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //return super.getHeaders();
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s","admin","admin");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                Log.d(TAG,"Auth is :" + auth);
                params.put("Authorization", auth);
                params.put("Content-Type", "application/json");
                return params;
            }*/
        };
        // Access the RequestQueue through your singleton class.
        InternetSingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);

    }

    /*public ElasticConnection(Context context,ContentRecyclerAdapterDel adapter,SwipeRefreshLayout refreshLayout,ArrayList<Content> dataSet){
        this.URL = SERVER_URL;
        //this.mDataset = dataSet;
        this.mContext = context;
     *//*   this.mAdapter = adapter;
        this.mRefreshLayout = refreshLayout;*//*
    }*/

    public void setRequestMethod(int method){
        REQUEST_METHOD = method;
    }

    public ElasticConnection(Context context){
        this.URL = SERVER_URL;
        this.mContext = context;
        //this.mDataset = dataSet;
        if (context instanceof Activity) {
            mListener = (OnConnectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConnectionListener");
        }
    }

    public ElasticConnection(Fragment fragment){
        this.URL = SERVER_URL;
        mContext = fragment.getContext();
        if (fragment instanceof Fragment) {
            mListener = (OnConnectionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString()
                    + " must implement OnConnectionListener");
        }
    }

    public interface OnConnectionListener {
        void onErrorRespose(Class c,String error);
        void onResponse(Class c,ArrayList responseDataList);
    }

}