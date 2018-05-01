package com.mmdkid.mmdkid.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by LIYADONG on 2017/7/18.
 */

public class QueryBuilder {
    private  JSONObject mJsonObject = new JSONObject();

    public static JSONObject matchAllQuery(){
        //String jsonString = "{ \"match_all\": {} }";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("match_all",new JSONObject());
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static JSONObject multiMatchQuery(String text,String[]fields){
       /* String jsonString ="{ \"multi_match\" : { \"query\": \""
                +text
                +"\", \"fields\": [";
        for(int i = 0; i<fields.length; i++){
            jsonString = jsonString +  "\"" + fields[i] + "\",";
        }
        jsonString = jsonString.substring(0,jsonString.length()-1) +  " ]  }";*/
        try {
            JSONArray array = new JSONArray();
            for(int i =0 ; i< fields.length; i++){
                array.put(fields[i]);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("multi_match",new JSONObject()
                    .put("query",text)
                    .put("fields",array));
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public JSONObject commonTermsQuery(){
        return null;
    }
    public QueryBuilder boolQuery(){
        try {
            mJsonObject.put("bool",new JSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public QueryBuilder must(JSONObject jsonObject){
        if(mJsonObject.has("bool")){
            try {
                JSONObject boolJsonObject = mJsonObject.getJSONObject("bool");
                if(boolJsonObject.has("must")){
                    Object mustObject = (Object) boolJsonObject.get("must");
                    if(mustObject instanceof JSONObject){
                        JSONArray array = new JSONArray();
                        array.put(mustObject);
                        array.put(jsonObject);
                        boolJsonObject.put("must",array);
                    }else if (mustObject instanceof JSONArray){
                        ((JSONArray) mustObject).put(jsonObject);
                    }
                }else{
                    boolJsonObject.put("must",jsonObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }
    public JSONObject getJSONQuery(){
        return mJsonObject;
    }

    public static JSONObject termQuery(String field , String text){
        /*String jsonString = "{ \"term\" : { \""
                +field
                + "\":\""
                + text
                +"\" }";*/
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("term",new JSONObject()
                    .put(field,text));
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject moreLikeThisQuery(JSONArray like,String[]fields){
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        for(int i =0 ; i< fields.length; i++){
            array.put(fields[i]);
        }
        try {
            jsonObject.put("more_like_this",
                    new JSONObject()
                    .put("fields",array)
                    .put("like",like));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
