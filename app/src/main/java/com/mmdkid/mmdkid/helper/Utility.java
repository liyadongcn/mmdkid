package com.mmdkid.mmdkid.helper;

/**
 * Created by LIYADONG on 2017/9/6.
 */

public class Utility {
    //获取指定位数的随机字符串(包含小写字母、大写字母、数字,0<length)
    public static String getRandomString(int length ) {
        //随机字符串的随机字符库
        String KeyString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuffer sb = new StringBuffer();
        int len = KeyString.length();
        for (int i = 0; i < length; i++) {
            sb.append(KeyString.charAt((int) Math.round(Math.random() * (len - 1))));
        }
        return sb.toString();
    }
    /**
     * 小于100000的不转换，大于或等于100000的转换为10万，以此类推，110000转为11万，112000为11.2万
     * @author liyadong
     */
    public static String getNumberString(long num){
        if(num<100000){
            return Long.toString(num);
        }else {
            int n = (int) num / 10000;
            return Integer.toString(n) + "万";
        }
    }
}
