package com.mmdkid.mmdkid.update;


import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.BuildConfig;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.helper.NetWorkUtil;
import com.mmdkid.mmdkid.models.Version;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//import android.support.v4.app.NotificationCompat;

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
                        showUpdateDialog(false, context);// 强制更新
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
       /* View view = View.inflate(context, R.layout.dialog_update, null);
        window.setContentView(view);

        TextView log_head = (TextView) view.findViewById(R.id.log_head);
        TextView msg_tv = (TextView) view.findViewById(R.id.msg_tv);
        log_head.setText("V" + new_version + "更新日志：");
        msg_tv.setText(update_log);
        Button update = (Button) view.findViewById(R.id.yes_btn);
        Button notNow = (Button) view.findViewById(R.id.no_btn);*/
        // 升级对话框布局
        View view = View.inflate(context, R.layout.dialog_update_cool, null);
        window.setContentView(view);
        // 设置升级对话框背景透明
        window.setBackgroundDrawableResource(android.R.color.transparent);
        // 设置升级对话框的宽度为屏幕的0.8
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay(); //获取屏幕宽高
        Point point = new Point();
        display.getSize(point);
        WindowManager.LayoutParams layoutParams = window.getAttributes(); //获取当前对话框的参数值
        layoutParams.width = (int) (point.x * 0.8); //宽度设置为屏幕宽度的0.8
//        layoutParams.height = (int) (point.y * 0.5); //高度设置为屏幕高度的0.5
        window.setAttributes(layoutParams);


        TextView title = (TextView) view.findViewById(R.id.tv_title);
        TextView description = (TextView) view.findViewById(R.id.tv_description);
        title.setText("发现新版本V" + new_version + "可以下载啦！");
        description.setText(update_log);
        Button update = (Button) view.findViewById(R.id.btn_update);
        ImageButton notNow = (ImageButton) view.findViewById(R.id.ib_close);
        update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 下载更新并安装
                mAlertDialog.dismiss();
                if (NetWorkUtil.isWifiAvailable(context)){ //使用wifi直接下载
                    download(context, download_path);
                }else {// 用户决定是否使用移动数据下载
                    showNetworkDialog(context);
                }


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

    private static void showNetworkDialog(final Context context){
        // 获取下载地址
        final String download_path = mNewVersion.locations.get(0);
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage("您正在使用移动网络，继续下载将消耗流量")
                .setNegativeButton("停止下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("继续下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        download(context, download_path);
                    }
                })
                .create().show();
    }


    /**
     * 下载apk并进行安装
     */
    private static void download(final Context context, final String download_path) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 安装包的文件名
            String apkName = download_path.substring(download_path.lastIndexOf("/") + 1);
            Log.d(TAG,"Apk Name is :" + apkName);
            // 安装路径及文件名
            // String target = Environment.getExternalStorageDirectory() +"/"+ apkName;
            String target = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) +"/"+ apkName;
            Log.d(TAG,"Local File Name is :" + target);

            final File file = new File(target);

            if(file.exists()){
                // 安装包文件已经存在
                Log.d(TAG,"Install package exists.");
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                dialogBuilder.setTitle("提示")
                        .setMessage("安装包已经存在！")
                        .setNegativeButton("直接安装", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                installAPK(context,file);
                            }
                        })
                        .setPositiveButton("重新下载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadInstall(context,download_path,file);
                            }
                        })
                        .create();
                Log.d(TAG,"Dialog created.");
                dialogBuilder.show();
            }else{
                downloadInstall(context,download_path,file);
            }
        } else {
            //SD卡没有插好

        }
    }
    /**
     * Install the apk package.
     * @param file 安装包文件
     *
     */
    private static void installAPK(Context context,File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
       /* if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Toast.makeText(context,"需要安装权限",Toast.LENGTH_LONG).show();
        }*/
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// addFlags 必须写在 setFlags后面 否则不生效
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    /**
     * 使用OkHttp下载安装包文件并自动进行安装
     *
     */
    private static void downloadInstall(final Context context, String download_path, final File file) {
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
            public void onResponse(Call call, final Response response) {

                // 下载输入流
                final InputStream inputStream = response.body().byteStream();

                // 输出文件流
                FileOutputStream fileOutputStream = null;
                try {
                    //创建通知栏下载提示
                    builder = new NotificationCompat.Builder(context,App.NOTIFICATION_CHANNEL_ID);
                    builder.setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                            .setOngoing(true)
                            .setContentTitle("下载更新");
                    manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    // 下载安装包文件
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
                    //下载成功后自动安装apk
                    installAPK(context,file);
                } catch (final IOException e) {
                    e.printStackTrace();
                    // 下载安装有错误
                    Log.d(TAG,"Error :" + e.getMessage());
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
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