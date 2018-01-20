package com.mmdkid.mmdkid.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
  
public class MediaDecoder {  
    private static final String TAG = "MediaDecoder";  
    private MediaMetadataRetriever retriever = null;  
    private String fileLength;  
  
    /*public MediaDecoder(String file) {
        if(CommonUtils.checkFile(file)){
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file);
            fileLength = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            Log.i(TAG, "fileLength : "+fileLength);
        }
    }  */
    public MediaDecoder(Context context,Uri uri) {
        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context,uri);
        fileLength = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.i(TAG, "fileLength : "+fileLength);

    }
    /**  
     * 获取视频某一帧  
     * @param timeMs 毫秒  
     *
     */  
    public Bitmap decodeFrame(long timeMs){
        if(retriever == null) return null;
        return retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);

    }  
    /**  
     * 取得视频文件播放长度  
     * @return  
     */  
    public String getVedioFileLength(){  
        return fileLength;  
    }  

    public void release(){
        retriever.release();
    }
} 