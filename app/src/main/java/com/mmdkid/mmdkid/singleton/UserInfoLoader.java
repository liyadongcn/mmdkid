package com.mmdkid.mmdkid.singleton;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.LruCache;
import android.util.Size;
import android.widget.Toast;

import com.mmdkid.mmdkid.PublishPostActivity;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class UserInfoLoader {
    private final static String TAG = "UserInfoLoader";

    private static UserInfoLoader mLoader;
    private LruCache<Integer,User> mLrucache;
    private UserInfoListener mListener;
    private Context mContext;

    private Set<Integer> mUserSet = new HashSet<Integer>();// 用户id列表 不能重复

    private final static int MESSAGE_USERINFO_READY = 11; // 压缩文章中图片结束

    Handler mHandler = new Handler() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case MESSAGE_USERINFO_READY:
                    Log.d(TAG,"Get user info ready message.");
                    if (mListener!=null) mListener.OnSuccess((User)msg.obj);
                    break;

            }

        }
    };

    public static UserInfoLoader getInstance(Context context){
        if(mLoader ==  null){
            synchronized (UserInfoLoader.class){
                if(mLoader == null){
                    mLoader = new UserInfoLoader(context);
                }
            }
        }

        return mLoader;
    }
    //用来初始化缓存对象
    private UserInfoLoader(Context context){
        //获取到最大可用的内存空间
        int maxSize = (int)Runtime.getRuntime().maxMemory()/8;//一般用除以八来表示，具体视APP大小而定
        mLrucache = new LruCache<Integer,User>(maxSize){
            @Override
            protected int sizeOf(Integer key, User value) {
                return 100;
            }
        };
        mContext = context;
    }
    //通过网络加载用户信息
    public void getUserInfo(final int id, UserInfoListener listener){
        //首先判断内存缓存中是否有这张图片
        User user = getUserInfoFromCache(id);
        if(user != null && listener!=null){
            Log.d(TAG,"Get the user info from cache directly." +id);
            listener.OnSuccess(user);
        }else{
            mListener = listener;
            //getUserInfoFromNet(id);
            if(mUserSet.add(id)){
                getUserInfoFromNet(id); // 一个用户只下载一次信息，集合中没有才下载，避免重复下载
            }else{
                // 正在下载用户信息
                // Log.d(TAG,"Fetching the user info through the net ." + id);
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        //要执行的操作
                        Log.d(TAG,"Waiting for the result from the net.");
                        User user;
                        do {
                            // 等待100ms再去获取
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }while((user = getUserInfoFromCache(id))==null);
                        Log.d(TAG,"Send the message of get the user info");
                        Message msg = Message.obtain();
                        msg.what = MESSAGE_USERINFO_READY;
                        msg.obj = user;
                        mHandler.sendMessage(msg);
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 0);//500毫秒后执行TimeTask的run方法
            }
        }
    }

    //从缓存中读取用户信息
    private User getUserInfoFromCache(int id){
        return mLrucache.get(id);
    }

    //将下载下来的用户信息保存到缓存中
    private void putUserInfoToCache(User user,int id){
        if(user !=null){
            mLrucache.put(id,user);
        }
    }
    private void getUserInfoFromNet(int id){
        //从网络下载用户信息，下载成功后并保存在缓存中
        User.getUserInfo(id,mContext, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                // 查找用户信息出错
                Log.d(TAG,"Get the user info failed. " );
                if (mListener!=null) mListener.OnFailure(error);
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                // 查找用户信息成功
                if (c == User.class && !responseDataList.isEmpty() ){
                    User user  = (User) responseDataList.get(0); // 起到缓存的作用
                    putUserInfoToCache(user,user.mId);
                    Log.d(TAG,"Get the user info from internet : " + user.mId);
                    if (mListener!=null) mListener.OnSuccess(user);
                }else {
                    if (mListener!=null) mListener.OnSuccess(null);
                }
            }
        });
    }

    public interface UserInfoListener{
        void OnSuccess(User user);
        void OnFailure(String error);
    }
}
