package com.mmdkid.mmdkid.fragments.viewHolders;


import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.gw.Content;
import com.mmdkid.mmdkid.models.Goods;
import com.mmdkid.mmdkid.models.Model;

/**
 * RESTFULAPI获取的goods信息显示view
 * Created by LIYADONG on 2017/9/29.
 */

public class GWGoodsImageOnLeftViewHolder extends ModelViewHolder {

    public CardView mCardView;
    public TextView mTitleView;
    public SimpleDraweeView mImageView;
    public TextView mCommentView;

    public GWGoodsImageOnLeftViewHolder(View itemView) {
        super(itemView);
        mCardView = (CardView) itemView.findViewById(R.id.cvContent);
        mCardView.setElevation(10);
        mTitleView = (TextView)itemView.findViewById(R.id.cvContentTitle);
        mImageView =(SimpleDraweeView)itemView.findViewById(R.id.cvContentImage);
        mCommentView = (TextView)itemView.findViewById(R.id.tvComment);
    }

    @Override
    public void bindHolder(Model model) {
        if (model instanceof Content && ((Content) model).mModelType.equals(Content.TYPE_GOODS)){
            Content content = (Content) model;
            mTitleView.setText(content.mTitle);
            if (content.mImageList!= null && !content.mImageList.isEmpty() )mImageView.setImageURI(content.mImageList.get(0));
            /*if (goods.editorComment!=null && !goods.editorComment.equals("null")){
                mCommentView.setVisibility(View.VISIBLE);
                mCommentView.setText(goods.editorComment);
            }else {
                mCommentView.setVisibility(View.GONE);
            }*/
            mCommentView.setVisibility(View.GONE);
        }
    }
}
