package com.mmdkid.mmdkid.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LIYADONG on 2017/6/9.
 */

public class Sort extends Object {
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";
    private Map<String,String> mSortMap;

    public Sort() {
        mSortMap = new HashMap<String, String>();
    }

    public void add(String name, String sort){
        mSortMap.put(name,sort);
    }

    public Map<String,String> getSortMap(){
        return mSortMap;
    }
}
