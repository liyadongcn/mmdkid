package com.mmdkid.mmdkid.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    private ArrayList<String> mImageList;

    public ImageAdapter(Context c, ArrayList<String> imageList) {
        mContext = c;
        mImageList = imageList;
    }

    public int getCount() {
        return mImageList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleDraweeView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new SimpleDraweeView(mContext);
           /* ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.height = 100;
            imageView.setLayoutParams(params);*/
            imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT
                    ,400));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (SimpleDraweeView) convertView;
        }
        imageView.setImageURI(mImageList.get(position));
        return imageView;
    }


}