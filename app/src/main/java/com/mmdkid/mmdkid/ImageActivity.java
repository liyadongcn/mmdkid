package com.mmdkid.mmdkid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.mmdkid.mmdkid.imagepost.ImageOverlayView;
import com.mmdkid.mmdkid.models.Content;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ImageActivity";
    public static final  String IMAGE_LIST = "image_list";
    public static final  String CONTENT = "content";

    private ArrayList<String> mImageList;
    private Content mContent;
    private ImageOverlayView mOverlayView; // 叠加在图片上的视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_image);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        //mImageList = intent.getStringArrayListExtra(IMAGE_LIST);
        mContent = (Content) intent.getSerializableExtra(CONTENT);
        mImageList = mContent.mImageList;

        GenericDraweeHierarchyBuilder draweeHierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(getResources())
                //.setFailureImage(R.drawable.failureDrawable)
                .setProgressBarImage(android.R.drawable.progress_indeterminate_horizontal);
                //.setPlaceholderImage(R.drawable.placeholderDrawable);

        mOverlayView = new ImageOverlayView(this,mContent);
        new ImageViewer.Builder<>(this, mImageList)
                .setStartPosition(0)
                .setImageMargin(ImageActivity.this,R.dimen.image_margin)
                .setOnDismissListener(getDismissListener())
                .setCustomDraweeHierarchyBuilder(draweeHierarchyBuilder)
                .setOverlayView(mOverlayView)
                .show();

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    private ImageViewer.OnDismissListener getDismissListener() {
        return new ImageViewer.OnDismissListener() {
            @Override
            public void onDismiss() {
                ImageActivity.this.finish();
            }
        };
    }

    /*
   *   图片浏览监听，可以设置图片描述，以及分享链接
   * */
    private ImageViewer.OnImageChangeListener getImageChangeListener() {
        return new ImageViewer.OnImageChangeListener() {
            @Override
            public void onImageChange(int position) {
                //CustomImage image = images.get(position);
                mOverlayView.setShareText(mImageList.get(position));
                mOverlayView.setDescription(String.valueOf(position+1)+"/"+ Integer.toString(mImageList.size()));
            }
        };
    }

}
