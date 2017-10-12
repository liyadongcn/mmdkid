package com.mmdkid.mmdkid.server;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by LIYADONG on 2017/6/9.
 */

public class ElasticQuery extends Query {
    private static final String LOG_TAG = "ElasticQuery";
    private QueryBuilder mQueryBuider;
    private JSONObject mJsonRequest;

    public ElasticQuery(Connection conn) {
        super(conn);
    }

    public void SetQueryBuilder(QueryBuilder queryBuilder){
        mQueryBuider = queryBuilder;
    }

    public void setJsonRequest(JSONObject jsonObject){
        mJsonRequest = jsonObject;
    }

    @Override
    public JSONObject GetRequest() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("query",mJsonRequest);
            jsonObject.put("from",getPageFrom());
            jsonObject.put("size",getPageSize());
            Sort sort = getSort();
            if(sort!=null){
                JSONArray array = new JSONArray();
                Map<String,String> sortMap = sort.getSortMap();
                for (Map.Entry<String, String> entry : sortMap.entrySet()) {
                    array.put(new JSONObject().put(entry.getKey(),entry.getValue()));
                   //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                }
                jsonObject.put("sort",array);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

 /*    @Override
    public JSONObject GetRequest() {
        // QueryBuilder qb = matchAllQuery();
        //QueryBuilder qb = commonTermsQuery("model_type", "media");
        //QueryBuilder qb = simpleQueryStringQuery("母情节");
        Log.d(LOG_TAG,mQueryBuider.toString());
        try{
            XContentBuilder builder = jsonBuilder();
            builder .startObject()
                    .field("query", mQueryBuider)
                    .field("from",getPageFrom())
                    .field("size", getPageSize());
            if(this.getSort()!=null){
                builder.field("sort")
                        .startArray();
                Sort sort = getSort();
                Map<String,String> sortMap = sort.getSortMap();
                for (Map.Entry<String, String> entry : sortMap.entrySet()) {
                     builder.startObject()
                            .field(entry.getKey())
                            .startObject()
                            .field("order",entry.getValue())
                            .endObject()
                            .endObject();
                    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                }
                builder .endArray();
            }
            builder .endObject();

            Log.d(LOG_TAG,builder.string());
            JSONObject jsonRequest = null;
            jsonRequest = new JSONObject(builder.string());
            return jsonRequest;
        }catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }*/
}
