package com.mmdkid.mmdkid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;

import java.util.ArrayList;

/**
 * 图片显示的GridView Adapter 可以是九宫格 可以是4方格 或者是两行6个图片
 * Created by LIYADONG on 2018/1/6.
 */

public class ImageGridViewAdapter extends BaseAdapter {
    private ArrayList<String> mImageList;
    private SimpleDraweeView mImage;
    private Context mContext;


    public ImageGridViewAdapter(Context mContext,ArrayList<String> imageList) {
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
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_image_item,null);
        }
        mImage = (SimpleDraweeView) view.findViewById(R.id.sdvImage);
        mImage.setImageURI(mImageList.get(i));
        return view;
    }
}
