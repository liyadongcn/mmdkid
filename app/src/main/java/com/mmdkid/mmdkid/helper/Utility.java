package com.mmdkid.mmdkid.helper;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.DeviceInfo;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;


import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * Created by LIYADONG on 2017/9/6.
 */

public class Utility {
    private static final String TAG = "Utility";
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

    /**
     * 利用正则表达式判断字符串是否是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

    /**
     * java List转换为字符串并加入分隔符
     */
    public String listToString(List list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static List<Uri> removeDuplicate(List<Uri> list)
    {
        Set set = new LinkedHashSet<String>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context    The context.
     * @param uri      The Uri to query.
     * @param selection   (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String findStringInList(ArrayList<String> list,String targetStr){
        for (String s : list){
            if (s.indexOf(targetStr)!=-1) return s;
        }
        return null;
    }

    /**
     * 获取指定文件大小
     * @param file
     * @return
     * @throws Exception 　　
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }
    /**
     * 获取指定文件夹
     * @param f
     * @return
     * @throws Exception
     *
     */
    public static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     * @param fileS
     * @return
     *
     */
    public static String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    public static void permissonCheck(final Context context, String... permissons) {
            AndPermission.with(context)
                    .runtime()
                    .permission(permissons)
                    .onGranted(new Action<List<String>> () {
                        @Override
                        public void onAction(List<String> permissions) {
                            // 取得授权
                            Log.d(TAG,"Permission is granted by user.");
                        }
                    }).onDenied(new Action<List<String>> () {
                @Override
                public void onAction(List<String> permissions) {
                    // 用于拒绝授权
                    // 弹出对话框告知用户情况
                    List<String> permissionNames = Permission.transformText(context, permissions);
                    String permissionText = TextUtils.join(",", permissionNames);
                    Log.d(TAG,"Permission is denied by user." + permissionText);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("提示")
                            .setMessage("您已拒绝"+permissionText+"权限，没有权限系统将继续运行，" +
                                    "但有可能出现系统闪退现象！您也可以现在退出系统，并在下次启动系统时同意授予权限。" )
                            .setPositiveButton("继续运行", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // 如果用户继续：

                                }
                            })
                            .setNegativeButton("退出应用", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // 如果用户中断：
                                    System.exit(0);
                                }
                            })
                            .show();
                }
            })

                    .start();
    }

    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    public static String longToString(long currentTime, String formatType)
            throws ParseException {
        Date date = longToDate(currentTime, formatType); // long类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }
    // date要转换的date类型的时间
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * 上传设备及联系人信息
     * 用户不授权则不上传
     */
    public static void uploadDeviceContactInfo(final Context context){
        if (App.isDeviceContactUploaded(context)){
            Log.d(TAG,"Device and contact info already saved to server.");
           return;//已经上传服务器 不需要做任何操作
        }
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        Log.d(TAG,"Read phone and contacts Permission is granted by user.");
        DeviceInfo deviceInfo = DeviceUtil.getDevice(context);
        try {
            deviceInfo.mContact = ContactUtil.getContactInfo(context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveToServer(context,deviceInfo);

    }
    /**
     * 将设备信息及联系人信息上传到服务器
     * 没有权限则不会上传
     * 使用okhttp完成网络传输，未使用volley因为volley有超时重传功能，造成重复上传。
     * 联系人信息较大，可能超时
     */
    private static void saveToServer(final Context context, final DeviceInfo deviceInfo) {
        // 使用volley完成
        /*deviceInfo.save(Model.ACTION_CREATE, context, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Save the device and contac info to server failed.");
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c==DeviceInfo.class && !responseDataList.isEmpty()){
                    Log.d(TAG,"Save device and contact info sucess. id is : "
                            + ((DeviceInfo)responseDataList.get(0)).mId);
                    App.setDeviceContactUploaded(context,true);
                }
            }
        });*/

        // 使用okhttp完成传输
        String url = "http://api.mmdkid.cn/v1/device";

        //step 1: 同样的需要创建一个OkHttpClick对象
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .followRedirects(false) // 禁用OkHttp的自动重定向功能 否则cookie信息不完整 没有_identity信息 不能完成自动登录的功能
                .build();

        //step 2: 创建  FormBody.Builder
        FormBody formBody = new FormBody.Builder()
                .add("version",deviceInfo.mVersion==null?"":deviceInfo.mVersion)
                .add("system",deviceInfo.mSystem==null?"":deviceInfo.mSystem)
                .add("sn",deviceInfo.mSN==null?"":deviceInfo.mSN)
                .add("device_id",deviceInfo.mDeviceId==null?"":deviceInfo.mDeviceId)
                .add("mac",deviceInfo.mMac==null?"":deviceInfo.mMac)
                .add("phone",deviceInfo.mPhone==null?"":deviceInfo.mPhone)
                .add("contact",deviceInfo.mContact==null?"":deviceInfo.mContact)
                .build();

        //step 3: 创建请求
        okhttp3.Request request = new okhttp3.Request.Builder().url(url)
                .post(formBody)
                .build();

        //step 4： 建立联系 创建Call对象
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 请求失败
                Log.d(TAG,"Save the device and contacts to server failed.");

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                // 请求成功
                // 获得响应
                Log.d(TAG,response.body().string());
                // 设置上传成功标志
                App.setDeviceContactUploaded(context,true);
            }
        });
    }


}
