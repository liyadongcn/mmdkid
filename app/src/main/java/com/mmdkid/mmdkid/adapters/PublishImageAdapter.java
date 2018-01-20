package com.mmdkid.mmdkid.adapters;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.PublishImageActivity;
import com.mmdkid.mmdkid.R;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.util.List;

/**
 * Created by LIYADONG on 2017/12/23.
 */

public class PublishImageAdapter extends BaseAdapter {
    private final static String TAG = "PublishImageAdapter";

    private static final int REQUEST_CODE_CHOOSE = 23;
    private static final int MAX_IMAGE_NUM = 9;

    private List<Uri> mImageList;
    private Context mContext;
    private SimpleDraweeView mImage;
    private ImageView mClose;

    public PublishImageAdapter(Context mContext,List<Uri> imageList) {
        this.mContext = mContext;
        this.mImageList = imageList;
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.publish_image_item,null);
        }
        mImage = (SimpleDraweeView) view.findViewById(R.id.sdvImage);
        mClose = (ImageView) view.findViewById(R.id.ivClose);

        mImage.setImageURI(mImageList.get(i));
        Log.d(TAG,"Image uri is :"+mImageList.get(i));
        if (i==MAX_IMAGE_NUM){
            mImage.setVisibility(View.GONE);
            mClose.setVisibility(View.GONE);
        }else {
            mImage.setVisibility(View.VISIBLE);
            mClose.setVisibility(View.VISIBLE);
        }
        mImage.setOnClickListener(null);
        if (i==mImageList.size()-1){ // 最后一张图片为加号图片
            mClose.setVisibility(View.GONE);
            mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Matisse.from((PublishImageActivity) mContext)
                            .choose(MimeType.ofImage())
                            .countable(true)
                            .maxSelectable(MAX_IMAGE_NUM-mImageList.size()+1)
                            //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                            .theme(R.style.Matisse_Dracula)
                            .gridExpectedSize(mContext.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .imageEngine(new PicassoEngine())
                            .forResult(REQUEST_CODE_CHOOSE);
                }
            });
        }else {// 不是最后一张图片

        }
        final int j = i;
        mClose.setOnClickListener(null);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 删除当前图片
                mImageList.remove(j);
                PublishImageAdapter.this.notifyDataSetChanged();
            }
        });

        return view;
    }
}
