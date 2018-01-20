package com.mmdkid.mmdkid.helper;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LIYADONG on 2017/9/13.
 */

public class HtmlUtil {
    private final static String TAG = "HtmlUtil";

    private static final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
    private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
    private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
    private static final String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符

    /**
     * @param htmlStr
     * @return
     *  删除Html标签
     */
    public static String delHTMLTag(String htmlStr) {
        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签

        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(htmlStr);
        htmlStr = m_space.replaceAll(""); // 过滤空格回车标签
        return htmlStr.trim(); // 返回文本字符串
    }

    public static String getTextFromHtml(String htmlStr,int length){
        htmlStr = delHTMLTag(htmlStr);
        htmlStr = htmlStr.replaceAll(" ", "");
        htmlStr = htmlStr.substring(0, htmlStr.length()>length ? length-1: htmlStr.length());
        return htmlStr;
    }

    public static String getUrl(String urlString){
        Uri uri = Uri.parse(urlString);
        if(uri.getScheme()==null){
            uri = Uri.parse("http:"+urlString);
            Log.d(TAG,"Correct String is :" + uri.toString());
            return uri.toString();
        }
        Log.d(TAG,"Orginal String is :"+urlString);
       return urlString;
    }

    /**
     * 从HTML源码中提取图片路径，最后以一个 String 类型的 List 返回，如果不包含任何图片，则返回一个 size=0　的List
     * 重复的将去除
     * @param htmlStr HTML源码
     * @return <img>标签 src 属性指向的图片地址的List集合
     * @author Carl He
     */
    public static List<String> getImageSrc(String htmlStr) {
        Set<String> pics = new HashSet<>();
        String img = "";
        Pattern p_image;
        Matcher m_image;
        //     String regEx_img = "<img.*src=(.*?)[^>]*?>"; //图片链接地址
        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile
                (regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            // 得到<img />数据
            img = m_image.group();
            // 匹配<img>中的src数据
            // src\s*=\s*"?(.*?)("|>|\s+) 原配是这个 但是不能匹配文件名含空格的
            // 例如： <img src = "file:///storage/emulated/0/Download/timg (7).jpeg" alt="dachshund">
            Matcher m = Pattern.compile("src\\s*=\\s*\\\"([^\\\"]*?)\\\"").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return new ArrayList<>(pics);
    }
    //s是需要删除某个子串的字符串s1是需要删除的子串
    public static  String getString(String s, String s1)
    {
        int postion = s.indexOf(s1);
        int length = s1.length();
        int Length = s.length();
        String newString = s.substring(0,postion) + s.substring(postion + length, Length);
        return newString;//返回已经删除好的字符串
    }
}
