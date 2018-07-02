package com.mmdkid.mmdkid.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;
/*
    * 上传图片的压缩策略
    *
    * Ø  图片宽高均≤1080px时，图片尺寸保持不变，（这里增加一个判断，如果图片大小＞500k）图片压缩quality=70% ;

    Ø  宽或高均＞1080px :

    ——图片宽高比≤2，则将图片宽或者高取大的等比压缩至1080px，（这里增加一个判断，如果图片大小＞500k）图片压缩quality=70% ;

    ——但是图片宽高比＞2时，则宽或者高取小的等比压缩至1080px，（这里增加一个判断，如果图片大小＞500k）图片压缩quality=70% ;

    Ø  宽高一个＞1080px，另一个＜1080px，但是图片宽高比＞2时，则宽高尺寸不变，（这里增加一个判断，如果图片大小＞500k）图片压缩quality=70% ;
    */
public class ImageUtil {
    private static long SIZE_LIMIT = 500*1024;
    private static int DEMENTION_LIMIT = 1080;

    public static int[] getImageWidthHeight(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth,options.outHeight};
    }

    public static BitmapFactory.Options getImageOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        return options;
    }

    public static File compress(Context context,File imageFile) throws Exception {
        //int [] demention = getImageWidthHeight(imageFile.getAbsolutePath());
        BitmapFactory.Options options = getImageOptions(imageFile.getAbsolutePath());
        int width = options.outWidth;
        int height = options.outHeight;
        long size = FileUtil.getFileSize(imageFile);
        Bitmap.CompressFormat format;
        switch (options.outMimeType){
            case "image/jpeg":
                format = Bitmap.CompressFormat.JPEG;
                break;
            case "image/png":
                format = Bitmap.CompressFormat.PNG;
                break;
            case "image/webp":
                format = Bitmap.CompressFormat.WEBP;
                break;
                default:
                    throw new Exception("Unkown image type.");
        }
        if (width<=DEMENTION_LIMIT && height <= DEMENTION_LIMIT){
            // 图片的宽高均小于1080
            if (size>SIZE_LIMIT){
                return new Compressor(context)
                        .setMaxWidth(width)
                        .setMaxHeight(height)
                        .setCompressFormat(format)
                        .setQuality(70)
                        .compressToFile(imageFile);
            }else {
                return imageFile;
            }
        }else if (width > DEMENTION_LIMIT && height > DEMENTION_LIMIT){
            // 图片的宽高均大于1080
            if (width/height <= 2 || height/width <=2){
                // 宽高比小于2
                if (width >= height){
                    // 图片宽度大于高度
                    if (size>SIZE_LIMIT){
                        // 图片大于500k
                        return new Compressor(context)
                                .setMaxWidth(DEMENTION_LIMIT)
                                .setCompressFormat(format)
                                .setQuality(70)
                                .compressToFile(imageFile);
                    }else {
                        // 图片小于500k
                        return new Compressor(context)
                                .setMaxWidth(DEMENTION_LIMIT)
                                .setCompressFormat(format)
                                .compressToFile(imageFile);
                    }
                }else {
                   // 图片高度大于宽度
                    if (size>SIZE_LIMIT){
                        // 图片大于500k
                        return new Compressor(context)
                                .setMaxHeight(DEMENTION_LIMIT)
                                .setCompressFormat(format)
                                .setQuality(70)
                                .compressToFile(imageFile);
                    }else {
                        // 图片小于500k
                        return new Compressor(context)
                                .setMaxHeight(DEMENTION_LIMIT)
                                .setCompressFormat(format)
                                .compressToFile(imageFile);
                    }
                }
            }else{
                // 宽高比大于2
                if (width >= height){
                    // 图片宽度大于高度
                    if (size>SIZE_LIMIT){
                        // 图片大于500k
                        return new Compressor(context)
                                .setMaxHeight(DEMENTION_LIMIT)
                                .setCompressFormat(format)
                                .setQuality(70)
                                .compressToFile(imageFile);
                    }else {
                        // 图片小于500k
                        return new Compressor(context)
                                .setMaxHeight(DEMENTION_LIMIT)
                                .setCompressFormat(format)
                                .compressToFile(imageFile);
                    }
                }else {
                    // 图片高度大于宽度
                    if (size>SIZE_LIMIT){
                        // 图片大于500k
                        return new Compressor(context)
                                .setMaxWidth(DEMENTION_LIMIT)
                                .setCompressFormat(format)
                                .setQuality(70)
                                .compressToFile(imageFile);
                    }else {
                        // 图片小于500k
                        return new Compressor(context)
                                .setMaxWidth(DEMENTION_LIMIT)
                                .setCompressFormat(format)
                                .compressToFile(imageFile);
                    }
                }
            }
        }else {
            // 图片宽度大于1080 或 高度大于1080
            if (size>SIZE_LIMIT){
                // 图片大于500k
                return new Compressor(context)
                        .setCompressFormat(format)
                        .setQuality(70)
                        .compressToFile(imageFile);
            }else {
                return imageFile;
            }
        }
       // return new Compressor(context).setMaxWidth(1080).setCompressFormat(Bitmap.CompressFormat.WEBP).compressToFile(imageFile);
    }
}
