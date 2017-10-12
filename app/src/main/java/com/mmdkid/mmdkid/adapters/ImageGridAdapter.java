package com.mmdkid.mmdkid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.User;

import java.util.ArrayList;

/**
 * Created by LIYADONG on 2017/7/27.
 */

public class ImageGridAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Model> mDataset;

    private SimpleDraweeView mAvatar;
    private TextView mName;

    public ImageGridAdapter(Context mContext,ArrayList<Model> dataSet) {
        this.mContext = mContext;
        this.mDataset = dataSet;
    }

    @Override
    public int getCount() {
        return mDataset.size();
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
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_image_text,null);
        }
        mAvatar = (SimpleDraweeView) view.findViewById(R.id.sdvAvatar);
        mName = (TextView) view.findViewById(R.id.tvName);

        Model model = mDataset.get(i);
        if (model instanceof User){
            User user = (User)model;
            mAvatar.setImageURI(user.mAvatar);
            if(user.mRole == User.ROLE_STUDENT){
                mName.setText(user.mRealname);
            }else {
                mName.setText(user.getDisplayName());
            }

        }
        return view;
    }
}
