package com.mmdkid.mmdkid.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.fragments.contentViewHolders.ContentViewHolder;
import com.mmdkid.mmdkid.fragments.contentViewHolders.ImageViewHolder;
import com.mmdkid.mmdkid.fragments.contentViewHolders.PostViewHolder;
import com.mmdkid.mmdkid.fragments.contentViewHolders.VideoViewHolder;
import com.mmdkid.mmdkid.models.Model;

import java.util.ArrayList;

public class ContentRecyclerAdapterDel extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG ="ContentRecyclerAdapterDel";

    private ArrayList<Content> mDataset;
    private Context mContext;
    private LayoutInflater mInflater;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ContentRecyclerAdapterDel(Context context, ArrayList<Content> myDataset) {
        mDataset =  myDataset;
        mContext = context.getApplicationContext();
        mInflater = LayoutInflater.from(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View v;
        switch (viewType){
            case Model.VIEW_TYPE_CONTENT_IMAGE_POST:
                // create a new view
                v = mInflater
                        .inflate(R.layout.content_image_viewholder, parent, false);
                //View v = mInflater.inflate(R.layout.content_card_view,parent,false);
                // set the view's size, margins, paddings and layout parameters
                return  new ImageViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_POST_IMAGE_MIDDLE:
                // create a new view
                v =  mInflater
                        .inflate(R.layout.content_post_viewholder, parent, false);
                //View v = mInflater.inflate(R.layout.content_card_view,parent,false);
                // set the view's size, margins, paddings and layout parameters
                return  new PostViewHolder(v);
            case Model.VIEW_TYPE_CONTENT_VIDEO:
                // create a new view
                v =  mInflater
                        .inflate(R.layout.content_video_viewholder, parent, false);
                //View v = mInflater.inflate(R.layout.content_card_view,parent,false);
                // set the view's size, margins, paddings and layout parameters
                return  new VideoViewHolder(v);
        }

        return null;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ContentViewHolder)holder).bindHolder(mDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getViewType();
    }
}





