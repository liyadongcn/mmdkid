package com.mmdkid.mmdkid.adapters;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;

import java.util.List;

/**
 * Created by LIYADONG on 2017/12/31.
 */

public class CoverImageRecyclerAdapter extends RecyclerView.Adapter {
    private final static String TAG = "CoverRecyclerAdapter";

    private List<Object> mImageList;

    private int mCheckedPosition = -1; // -1表示未选中任何图片

    public CoverImageRecyclerAdapter(List imageList){
        mImageList = imageList;
    }

    public int getCheckedPosition() {
        return mCheckedPosition;
    }

    public void setCheckedPosition(int position) {
        mCheckedPosition = position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cover_image_item, parent, false);
        // 实例化viewholder
        CoverImageViewHolder viewHolder = new CoverImageViewHolder(v);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Object imageObject = mImageList.get(position);

        if (imageObject!=null){
            if (imageObject instanceof Uri){
                ((CoverImageViewHolder) holder).mImageView.setImageURI((Uri)imageObject);
                Log.d(TAG,"Image uri is :" + (Uri) imageObject);
            }
            if (imageObject instanceof Bitmap) {
                ((CoverImageViewHolder) holder).mImageView.setImageBitmap((Bitmap)imageObject);
            }
        }
        if (position != mCheckedPosition){
            ((CoverImageViewHolder) holder).mCheckedBox.setChecked(false);
        }
        final RecyclerView.ViewHolder finalHolder = holder;
        ((CoverImageViewHolder) holder).mCheckedBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CoverImageViewHolder) finalHolder).mCheckedBox.isChecked()){
                    mCheckedPosition = position;
                    notifyDataSetChanged();
                }else {
                    mCheckedPosition = -1;
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    public static  class  CoverImageViewHolder extends RecyclerView.ViewHolder{

        public SimpleDraweeView mImageView;
        public CheckBox mCheckedBox;

        public CoverImageViewHolder(View itemView) {
            super(itemView);
            mImageView = (SimpleDraweeView) itemView.findViewById(R.id.sdvCover);
            mCheckedBox =(CheckBox) itemView.findViewById(R.id.checkBox);

        }
    }
}
