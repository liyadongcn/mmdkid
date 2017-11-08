package com.mmdkid.mmdkid;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private final static String TAG = "ExampleInstrumentedTest";
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.mmdkid.mmdkid", appContext.getPackageName());
    }
    @Test
    public void check_json() throws Exception {
        String jsonString = "{\n" +
                "    \"id\": \"596a35495c3c7\",\n" +
                "    \"model_id\": 92,\n" +
                "    \"model_type\": \"post\",\n" +
                "    \"title\": \"深度：欧洲一小国有意采购歼10 或被土耳其出手搅黄\",\n" +
                "    \"content\": \"<p>　</p>\",\n" +
                "    \"image\": [\n" +
                "        \"//5b0988e595225.cdn.sohucs.com/images/20171106/11c596b2ebc94211a9278589090e1446.jpeg\",\n" +
                "        \"//5b0988e595225.cdn.sohucs.com/images/20171106/ffe63b616f284d279bd7ac4430dd947c.jpeg\",\n" +
                "        \"//5b0988e595225.cdn.sohucs.com/images/20171106/f72ff8a299f048f0baa5d16b27cf2a1b.jpeg\"\n" +
                "    ],\n" +
                "    \"tag\": [],\n" +
                "    \"category\": null,\n" +
                "    \"status\": 10,\n" +
                "    \"source_url\": \"http://mil.news.sina.com.cn/jssd/2016-07-01/doc-ifxtsatm1152289.shtml\",\n" +
                "    \"source_name\": \"\",\n" +
                "    \"author\": \"\",\n" +
                "    \"created_at\": \"2016-07-01 18:36:37\",\n" +
                "    \"updated_at\": \"2016-07-01 18:36:37\",\n" +
                "    \"created_by\": 2,\n" +
                "    \"updated_by\": 2,\n" +
                "    \"thumbsup\": 0,\n" +
                "    \"thumbsdown\": 0,\n" +
                "    \"comment_count\": 0,\n" +
                "    \"view_count\": 2,\n" +
                "    \"star_count\": 0\n" +
                "}";
        JSONObject jsonObject = new JSONObject(jsonString);
        if (jsonObject.has("image") ){
            if (jsonObject.get("image") instanceof String ){
                Log.d(TAG, (String) jsonObject.get("image"));
                //System.out.println(jsonObject.get("image"));
            }else if (jsonObject.get("image") instanceof JSONArray){
                Log.d(TAG, "it is a json array.");

            }
        }
    }
}
