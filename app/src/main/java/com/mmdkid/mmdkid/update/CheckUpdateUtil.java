package com.mmdkid.mmdkid.update;


import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.BuildConfig;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Version;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 检查更新工具类
 */
public class CheckUpdateUtil {
    private static final String TAG = "CheckUpdateUtil";
    private static NotificationCompat.Builder builder;
    private static NotificationManager manager;
    private static final int UPDATE_ID = 0;
    private static String mCurrentVersionName;
    private static int   mCurrentVersionCode;
    private static Version mNewVersion;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    /**
     * 检查更新
     *
     * @param context
     * @return
     */
    public static void checkUpdate(final Context context,final boolean showMessage) {
        // 获取在线参数（要更新的版本号）
        Version.find(context, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Error:" + error);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if ( c== Version.class && !responseDataList.isEmpty()){
                    // 获取最新版本
                    mNewVersion = (Version) responseDataList.get(0);
                    Log.d(TAG,"Version Code: " + mNewVersion.code);
                    Log.d(TAG,"Version Name: " + mNewVersion.name);
                    Log.d(TAG,"Version Log: " + mNewVersion.log);
                    Log.d(TAG,"Download Locations: " + mNewVersion.locations.toString());
                    // 获取当前版本
                    mCurrentVersionCode = getVersionCode(context);
                    mCurrentVersionName = getVersionName(context);
                    Log.d(TAG,"Current version is : " + mCurrentVersionName);
                    // 比较版本
                    if (mNewVersion.code == mCurrentVersionCode){
                        if (showMessage) Toast.makeText(context,context.getString(R.string.message_latest_version),Toast.LENGTH_LONG).show();
                        return ;//无更新
                    }else if (mNewVersion.code > mCurrentVersionCode){
                        // 版本需要更新
                        Log.d(TAG,"Start updating....");
                        verifyStoragePermissions((Activity) context);
                        showUpdateDialog(false, context);
                        return;
                    }

                }
            }
        }).where("expand","locations").all();

    }

    /**
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String versionName = info.versionName;
            return versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return 当前应用的版本号
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 判断当前网络是否wifi
     */
    public boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }


    /**
     * 显示更新对话框
     *
     * @param isForceUpdate 是否强制更新
     */
    public static void showUpdateDialog(final boolean isForceUpdate, final Context context) {
        // 获取更新日志
        String update_log = mNewVersion.log;
        // 最新版本
        String new_version = mNewVersion.name;
        // 获取下载地址
        final String download_path = mNewVersion.locations.get(0);
        if (TextUtils.isEmpty(update_log) || TextUtils.isEmpty(download_path) || TextUtils.isEmpty(new_version)) {
            return;
        }
        Log.d(TAG, "更新日志--> " + update_log + " 最新版本--> " + new_version + " 下载地址--> " + download_path);
        //弹框提示
        final AlertDialog mAlertDialog = new AlertDialog.Builder(context).create();
        mAlertDialog.show();
        mAlertDialog.setCancelable(false);
        Window window = mAlertDialog.getWindow();
        //window.setGravity(Gravity.BOTTOM);
        //window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        View view = View.inflate(context, R.layout.dialog_update, null);
        window.setContentView(view);

        TextView log_head = (TextView) view.findViewById(R.id.log_head);
        TextView msg_tv = (TextView) view.findViewById(R.id.msg_tv);
        log_head.setText("v" + new_version + "更新日志：");
        msg_tv.setText(update_log);
        Button update = (Button) view.findViewById(R.id.yes_btn);
        Button notNow = (Button) view.findViewById(R.id.no_btn);
        update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 下载更新并安装
                download(context, download_path);
                mAlertDialog.dismiss();
            }
        });
        notNow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        if (isForceUpdate) {//如果是强制更新，则不显示“以后再说”按钮
            notNow.setVisibility(View.GONE);
        }
    }

    /**
     * 下载apk
     */
    private static void download(final Context context, final String download_path) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //step 1: 不变的第一步创建 OkHttpClick
            OkHttpClient okHttpClient = new OkHttpClient();

            //step 2: 创建Requset
            Request request = new Request.Builder()
                    .url(download_path)
                    .build();

            //step 3:建立联系，创建Call
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) {
                    // 安装包的文件名
                    String apkName = download_path.substring(download_path.lastIndexOf("/") + 1);
                    Log.d(TAG,"Apk Name is :" + apkName);
                    // 本地安装路径及文件名
                    String target = Environment.getExternalStorageDirectory() +"/"+ apkName;
                    Log.d(TAG,"Local File Name is :" + target);
                    // 下载输入流
                    InputStream inputStream = response.body().byteStream();
                    // 输出文件流
                    FileOutputStream fileOutputStream = null;
                    File file = new File(target);
                    try {
                        //创建通知栏下载提示
                        builder = new NotificationCompat.Builder(context);
                        builder.setSmallIcon(R.mipmap.ic_launcher)
                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                                .setOngoing(true)
                                .setContentTitle("下载更新");
                        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        // 下载文件
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[2048];
                        long total = response.body().contentLength();
                        Log.d(TAG,"Total is :" + Long.toString(total));
                        long sum = 0;
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                            sum += len;
                            //更新进度
                            int progress = (int) (sum * 1.0f / total * 100);
                            builder.setProgress(100, progress, false)
                                    .setContentText(progress + "%");
                            manager.notify(UPDATE_ID, builder.build());
                        }
                        fileOutputStream.flush();
                        Log.d(TAG, "文件下载成功 Total is " + Long.toString(sum));
                        //取消通知栏下载提示
                        manager.cancel(UPDATE_ID);
                        //下载成功后自动安装apk并打开
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        /*intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        context.startActivity(intent);*/
                        //判断是否是AndroidN以及更高的版本
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
                            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                        } else {
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        }
                        context.startActivity(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if ( e instanceof FileNotFoundException) {
                            // 没有本地存储权限 无法下载安装文件
                            Log.d(TAG,"Error :" + ((FileNotFoundException) e).getMessage());
                            ((Activity)context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context,"No right to access the storage",Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }finally {
                        try {
                            if (inputStream != null)
                                inputStream.close();
                        } catch (IOException e) {
                        }
                        try {
                            if (fileOutputStream != null)
                                fileOutputStream.close();
                        } catch (IOException e) {
                        }
                    }

                }
            });
        } else {
            //SD卡没有插好

        }
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}