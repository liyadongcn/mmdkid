package com.mmdkid.mmdkid.helper;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.json.JSONException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ContactUtilTest {
    private static final String TAG = "ContactUtilTest";
    @Test
    public void getContactInfo() {
        // Context of the app under test.
        final Context appContext = InstrumentationRegistry.getTargetContext();
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG,"there is no permission of telephone service.");
            AndPermission.with(appContext)
                    .runtime()
                    .permission( Manifest.permission.READ_CONTACTS)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> permissions) {
                            // 取得授权
                            Log.d(TAG,"Permission is granted by user.");
                            try {
                                ContactUtil.getContactInfo(appContext);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).onDenied(new Action<List<String>>() {
                @Override
                public void onAction(List<String> permissions) {
                    // 用于拒绝授权
                    // 弹出对话框告知用户情况
                    Log.d(TAG,"Permission is denied by user.");
                    AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
                    builder.setTitle("提示")
                            .setMessage("你已拒绝该权限，没有该权限系统将继续运行，但有可能出现系统闪退现象！" )
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
        }else{
            try {
                ContactUtil.getContactInfo(appContext);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}