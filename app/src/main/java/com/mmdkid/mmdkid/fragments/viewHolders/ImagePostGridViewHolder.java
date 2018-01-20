package com.mmdkid.mmdkid.fragments.viewHolders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.adapters.ImageGridViewAdapter;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.views.AutoGridView;

import java.util.ArrayList;

/**
 * Created by LIYADONG on 2018/1/6.
 */

public class ImagePostGridViewHolder extends ModelViewHolder {

    private Context mContext;
    private AutoGridView mGridView;
    private ImageGridViewAdapter mAdapter;
    private int mImageNum;
    public TextView mTextViewTitle;
    public TextView mTextViewDate;

    public ImagePostGridViewHolder(Context context, View itemView, int imageNum) {
        super(itemView);
        this.mImageNum = imageNum;
        mContext = context;
        mTextViewTitle = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mTextViewDate = (TextView)itemView.findViewById(R.id.cvContentDate);
        mGridView = (AutoGridView) itemView.findViewById(R.id.gridview);
        // 根据要显示的照片数量设定gridview的属性
        if (imageNum == 9) {
            mGridView.setNumColumns(3);
        }else if (imageNum == 4){
            mGridView.setNumColumns(2);
        }else if (imageNum == 6) {
            mGridView.setNumColumns(3);
        }
    }

    @Override
    public void bindHolder(Model model) {
        if(model instanceof Content) {
            Content content = (Content) model;
            mTextViewTitle.setText(content.mTitle);
            mTextViewDate.setText(content.mCreatedAt);
            if(content.mImageList.size()>=mImageNum){
                mAdapter = new ImageGridViewAdapter(mContext, new ArrayList<String>( content.mImageList.subList(0,mImageNum)));
                mGridView.setAdapter(mAdapter);
            }
        }
    }
    /**
     *  不使用自定义的AutoGridView时 可以通过该函数动态计算GridView的高度
     *  setGridViewHeight(mGridView);
     *  mAdapter.notifyDataSetChanged();
     */
    public void setGridViewHeight(GridView gridview) {
        // 获取gridview的adapter
        ListAdapter listAdapter = gridview.getAdapter();
        if (listAdapter == null) {
            return;
        }
        // 固定列宽，有多少列
        //获取测量mode
        int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        //measure一次才能获取到属性值
        gridview.measure(width,height);
        int numColumns= gridview.getNumColumns(); //没有测量gridview总是返回-1
        int totalHeight = 0;
        // 计算每一列的高度之和
        for (int i = 0; i < listAdapter.getCount(); i += numColumns) {
            // 获取gridview的每一个item
            View listItem = listAdapter.getView(i, null, gridview);
            listItem.measure(0, 0);
            // 获取item的高度和
            totalHeight += listItem.getMeasuredHeight();
        }
        // 获取gridview的布局参数
        ViewGroup.LayoutParams params = gridview.getLayoutParams();
        params.height = totalHeight;
        gridview.setLayoutParams(params);
    }

}
