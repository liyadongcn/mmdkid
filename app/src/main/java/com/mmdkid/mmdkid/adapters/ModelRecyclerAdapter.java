package com.mmdkid.mmdkid.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.fragments.viewHolders.DiaryListViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.DiaryViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.GoodsImageOnLeftViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.ImagePostViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.ModelViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.PostMainViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.PostViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.RefreshViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.StudentViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.UserGroupViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.VideoMainViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.VideoViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.VideoYoukuViewHolder;
import com.mmdkid.mmdkid.models.Model;

import java.util.ArrayList;

/**
 * Created by LIYADONG on 2017/7/23.
 */

public class ModelRecyclerAdapter extends RecyclerView.Adapter {
    private static final String LOG_TAG ="ModelRecyclerAdapter";

    private ArrayList<Model> mDataset;
    private Context mContext;
    private LayoutInflater mInflater;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ModelRecyclerAdapter(Context context, ArrayList<Model> myDataset) {
        mDataset =  myDataset;
        mContext = context.getApplicationContext();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            case Model.VIEW_TYPE_DIARY:
                // create a new view
                v =  mInflater
                        .inflate(R.layout.viewholder_today_diary, parent, false);
                //View v = mInflater.inflate(R.layout.content_card_view,parent,false);
                // set the view's size, margins, paddings and layout parameters
                return  new DiaryViewHolder(v);
            case Model.VIEW_TYPE_DIARY_LIST:
                // create a new view
                v =  mInflater
                        .inflate(R.layout.viewholder_diary_list, parent, false);
                //View v = mInflater.inflate(R.layout.content_card_view,parent,false);
                // set the view's size, margins, paddings and layout parameters
                return  new DiaryListViewHolder(v);
            case Model.VIEW_TYPE_USER:
                v =  mInflater
                        .inflate(R.layout.viewholder_today_student, parent, false);
                return  new StudentViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_POST:
                v =  mInflater
                        .inflate(R.layout.viewholder_post_layout, parent, false);
                return  new PostViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_IMAGE_POST:
                v =  mInflater
                        .inflate(R.layout.viewholder_imagepost, parent, false);
                return  new ImagePostViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_VIDEO:
                v =  mInflater
                        .inflate(R.layout.viewholder_video, parent, false);
                return  new VideoViewHolder(v);
            case Model.VIEW_TYPE_USER_GROUP:
                v =  mInflater
                        .inflate(R.layout.viewholder_user_group, parent, false);
                return  new UserGroupViewHolder(v);
            case Model.VIEW_TYPE_REFRESH:
                v =  mInflater
                        .inflate(R.layout.viewholder_refresh, parent, false);
                return  new RefreshViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_POST_MAIN:
                v =  mInflater
                        .inflate(R.layout.content_post_viewholder, parent, false);
                return  new PostMainViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_IMAGE_POST_MAIN:
                v =  mInflater
                        .inflate(R.layout.content_image_viewholder, parent, false);
                return  new ImagePostViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_VIDEO_MAIN:
                v =  mInflater
                        .inflate(R.layout.content_video_viewholder, parent, false);
                return  new VideoMainViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_VIDEO_YOUKU:
                v =  mInflater
                        .inflate(R.layout.content_video_youku_viewholder, parent, false);
                return  new VideoYoukuViewHolder(v);
            case Model.VIEW_TYPE_GOODS_IMAGE_ON_LEFT:
                v =  mInflater
                        .inflate(R.layout.viewholder_goods_image_on_left, parent, false);
                return  new GoodsImageOnLeftViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ModelViewHolder)holder).bindHolder( mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return  mDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        Model model = mDataset.get(position);
        return model.getViewType();
    }
}
