package com.mmdkid.mmdkid.server;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by LIYADONG on 2017/12/25.
 */

public class OkHttpManager extends Object {
    private static final String TAG = OkHttpManager.class.getSimpleName();

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_TYPE_MULTIPART_FROM_DATA = MediaType.parse("multipart/form-data; charset=utf-8");
    private static final MediaType MEDIA_OBJECT_STREAM = MediaType.parse("application/octet-stream");
    private static final MediaType MEDIA_TYPE_FILE = MediaType.parse("file/*");


    private static final String BASE_URL = "http://api.mmdkid.cn/v1/";//请求接口根地址
    private static volatile OkHttpManager mInstance;//单利引用
    public static final int TYPE_GET = 0;//get请求
    public static final int TYPE_POST_JSON = 1;//post请求参数为json
    public static final int TYPE_POST_FORM = 2;//post请求参数为表单
    private OkHttpClient mOkHttpClient;//okHttpClient 实例
    private Handler okHttpHandler;//全局处理子线程和M主线程通信

    private static final String TOKEN_NAME = "accessToken";//访问token的名称
    private String mAccessToken; // 访问token

    private static final int WRITE_TIME_OUT = 120; // 写入超时120秒

    /**
     * 初始化OkHttpManager
     */
    public OkHttpManager(Context context) {
        //初始化OkHttpClient
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(200, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        //初始化Handler
        okHttpHandler = new Handler(context.getMainLooper());
    }
    /**
     * 设置访问token
     */
    public void setAccessToken(String token) {
        mAccessToken = token;
    }

    /**
     * 获取单例引用
     *
     * @return
     */
    public static OkHttpManager getInstance(Context context) {
        OkHttpManager inst = mInstance;
        if (inst == null) {
            synchronized (OkHttpManager.class) {
                inst = mInstance;
                if (inst == null) {
                    inst = new OkHttpManager(context.getApplicationContext());
                    mInstance = inst;
                }
            }
        }
        return inst;
    }

    public interface ReqCallBack<T> {
        /**
         * 响应成功
         */
        void onReqSuccess(T result);

        /**
         * 响应失败
         */
        void onReqFailed(String errorMsg);
    }

    /**
     * 统一处理成功信息
     * @param result
     * @param callBack
     * @param <T>
     */
    private <T> void successCallBack(final T result, final ReqCallBack<T> callBack) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onReqSuccess(result);
                }
            }
        });
    }

    /**
     * 统一处理失败信息
     * @param errorMsg
     * @param callBack
     * @param <T>
     */
    private <T> void failedCallBack(final String errorMsg, final ReqCallBack<T> callBack) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onReqFailed(errorMsg);
                }
            }
        });
    }

    /**
     * 不带参数上传文件
     * @param actionUrl 接口地址
     * @param filePath  本地文件地址
     */
    public <T> void upLoadFile(String actionUrl, String filePath, final ReqCallBack<T> callBack) {
        //补全请求地址
        String requestUrl = String.format("%s%s", BASE_URL, actionUrl);
        //创建File
        File file = new File(filePath);
        //创建RequestBody
        RequestBody body = RequestBody.create(MEDIA_OBJECT_STREAM, file);
        //创建Request
        final Request request = new Request.Builder().url(requestUrl).post(body).build();
        //设定访问超时为120秒
        final Call call = mOkHttpClient.newBuilder().writeTimeout(120, TimeUnit.SECONDS).build().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, e.toString());
                failedCallBack("上传失败", callBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String string = response.body().string();
                    Log.d(TAG, "response ----->" + string);
                    successCallBack((T) string, callBack);
                } else {
                    failedCallBack("上传失败", callBack);
                }
            }
        });
    }

    /**
     * 带参数上传文件
     * @param actionUrl 接口地址
     * @param paramsMap 参数
     * @param callBack 回调
     * @param <T>
     */
    public <T>void upLoadFile(String actionUrl, HashMap<String, Object> paramsMap, final ReqCallBack<T> callBack) {
        try {
            //补全请求地址
            String requestUrl = String.format("%s%s", BASE_URL, actionUrl);
            MultipartBody.Builder builder = new MultipartBody.Builder();
            //设置类型
            builder.setType(MultipartBody.FORM);
            //追加参数
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    builder.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();
            //创建Request
            final Request request = new Request.Builder().url(requestUrl).post(body).build();
            //单独设置参数 比如读取超时时间
            final Call call = mOkHttpClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.toString());
                    failedCallBack("上传失败", callBack);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        Log.d(TAG, "response ----->" + string);
                        successCallBack((T) string, callBack);
                    } else {
                        failedCallBack("上传失败", callBack);
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    /**
     * 带参数带进度上传文件,支持多文件上传
     * @param actionUrl 接口地址
     * @param paramsMap 参数
     * @param callBack 回调
     * @param <T>
     */
    public <T> void upLoadFile(String actionUrl, IdentityHashMap<String, Object> paramsMap, final ReqProgressCallBack<T> callBack) {
        try {
            //补全请求地址
            String requestUrl = String.format("%s%s", BASE_URL, actionUrl);
            if (!mAccessToken.isEmpty()){
                requestUrl = requestUrl + "?" + TOKEN_NAME + "=" + mAccessToken;
            }
            Log.d(TAG,"Request URL : " +requestUrl);
            MultipartBody.Builder builder = new MultipartBody.Builder();
            //设置类型
            builder.setType(MultipartBody.FORM);
            //追加参数
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    Log.d(TAG,"File Name :" + file.getName());
                    Log.d(TAG,"File Path :" + file.getPath());
                    Log.d(TAG,"File exit :" + file.exists());
                    Log.d(TAG,"File length :" + file.length());
                    RequestBody fileBody = RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file);
                    builder.addFormDataPart(key, file.getName(), fileBody);
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();
            //创建Request
            final Request request = new Request.Builder().url(requestUrl).post(progressRequestBody(body,callBack)).build();
            final Call call = mOkHttpClient.newBuilder().writeTimeout(0, TimeUnit.SECONDS).build().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.toString());
                    failedCallBack("上传失败"+e.toString(), callBack);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        Log.d(TAG, "response ----->" + string);
                        successCallBack((T) string, callBack);
                    } else {
                        failedCallBack("上传失败:"+ response.body().string(), callBack);
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }
    /**
     * 根据文件名称或文件url判断文件的minme类型
     */
    private String guessMimeType(String path){
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(path);
        if(type == null){
            type = "application/octet-stream";
        }
        return type;
    }

    /**
     * 创建带进度的RequestBody，长度为所有上传文件的总长度
     * @param requestBody RequestBody类
     * @param callBack 回调
     * @param <T>
     * @return
     */

    public <T> RequestBody progressRequestBody(final RequestBody requestBody, final ReqProgressCallBack<T> callBack){
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return requestBody.contentType();
            }
            @Override
            public long contentLength() throws IOException {
                return requestBody.contentLength();
            }
            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink bufferedSink=null;
                if(bufferedSink == null){
                    //开始包装
                    bufferedSink = Okio.buffer(sink(sink));
                }
                //写入
                requestBody.writeTo(bufferedSink);
                bufferedSink.flush();
            }
            private Sink sink(Sink sink) throws IOException {
                return new ForwardingSink(sink) {
                    //当前写入字节数
                    long transfered = 0L;
                    //总得字节数
                    long total = contentLength();
                    @Override
                    public void write(Buffer source, long byteCount) throws IOException {
                        super.write(source, byteCount);
                        transfered += byteCount;
                        progressCallBack(total, transfered, callBack);
                    }
                };
            }
        };
    }

    /**
     * 创建带进度的RequestBody，只适合上传单个文件
     * @param contentType MediaType
     * @param file  准备上传的文件
     * @param callBack 回调
     * @param <T>
     * @return
     */
    public <T> RequestBody createProgressRequestBody(final MediaType contentType, final File file, final ReqProgressCallBack<T> callBack) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source;
                try {
                    Log.d(TAG,"Oki source start :" + file.getName());
                   /* source = Okio.source(file);
                    Buffer buf = new Buffer();
                    long remaining = contentLength();
                    long current = 0;
                    for (long readCount; (readCount = source.read(buf, 2048)) != -1; ) {
                        sink.write(buf, readCount);
                        current += readCount;
                        Log.d(TAG, "current------>" + current);
                        progressCallBack(remaining, current, callBack);
                    }*/
                    source = Okio.source(file);
                    long total = file.length();
                    long read;
                    long transfered=0;
                    while ((read = source.read(sink.buffer(), 2048)) != -1) {
                        transfered += read;
                        sink.flush();
                        progressCallBack(total, transfered, callBack);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public interface ReqProgressCallBack<T>  extends ReqCallBack<T>{
        /**
         * 响应进度更新
         */
        void onProgress(long total, long current);
    }

    /**
     * 统一处理进度信息
     * @param total    总计大小
     * @param current  当前进度
     * @param callBack
     * @param <T>
     */
    private <T> void progressCallBack(final long total, final long current, final ReqProgressCallBack<T> callBack) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onProgress(total, current);
                }
            }
        });
    }
    /**
     * 取消当前所有的OkHttpClient的请求
     *
     */
    public void cancle(){
        if (mOkHttpClient!=null){
            mOkHttpClient.dispatcher().cancelAll();
            Log.d(TAG,"Cancle all the okhttp calls.");
        }
    }
}
