package com.mmdkid.mmdkid.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.fragments.viewHolders.DiaryListViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.DiaryViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.GWGoodsImageOnLeftViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.GWPostImageOnMiddleViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.GoodsImageOnLeftViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.ImagePostViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.ModelViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.PostImageMiddleViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.PostImageLeftViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.PostImageRightViewHolder;
import com.mmdkid.mmdkid.fragments.viewHolders.PostImageThreeViewHolder;
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
            case Model.VIEW_TYPE_CONTENT_POST_IMAGE_LEFT:
                v =  mInflater
                        .inflate(R.layout.viewholder_post_image_left, parent, false);
                return  new PostImageLeftViewHolder(v);
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
            case Model.VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE:
                v =  mInflater
                        .inflate(R.layout.viewholder_post_image_middle, parent, false);
                return  new PostImageMiddleViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_POST_IMAGE_RIGHT:
                v =  mInflater
                        .inflate(R.layout.viewholder_post_image_right, parent, false);
                return  new PostImageRightViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_POST_IMAGE_THREE:
                v =  mInflater
                        .inflate(R.layout.viewholder_post_image_three, parent, false);
                return  new PostImageThreeViewHolder(v);
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
            case Model.VIEW_TYPE_GWCONTENT_GOODS_IMAGE_ON_LEFT:
                v =  mInflater
                        .inflate(R.layout.viewholder_gwcontent_goods_image_on_left, parent, false);
                return  new GWGoodsImageOnLeftViewHolder(v);
            case Model.VIEW_TYPE_GWCONTENT_POST_IMAGE_ON_MIDDLE:
                v =  mInflater
                        .inflate(R.layout.viewholder_gwcontent_post_image_on_middle, parent, false);
                return  new GWPostImageOnMiddleViewHolder(v);
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
