package com.mmdkid.mmdkid.imagepost;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.helper.HtmlUtil;
import com.mmdkid.mmdkid.models.Content;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.net.URI;
import java.net.URISyntaxException;


/*
 * Created by Alexander Krol (troy379) on 29.08.16.
 */
public class ImageOverlayView extends RelativeLayout {

    private TextView tvDescription;

    private String sharingText;

    private Content content;

    public ImageOverlayView(Context context) {
        super(context);
        init();
    }

    public ImageOverlayView(Context context,Content content) {
        super(context);
        this.content = content;
        init();
    }

    public ImageOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setDescription(String description) {
        tvDescription.setText(description);
    }

    public void setShareText(String text) {
        this.sharingText = text;
    }

    private void sendShareIntent() {
        /*Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        sendIntent.setType("text/plain");
        getContext().startActivity(sendIntent);*/
        if (this.content!=null) {
           /* Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("image/jpg");
            intent.setType("text*//*");
            intent.putExtra(Intent.EXTRA_STREAM, this.content.getContentUrl());
            intent.putExtra(Intent.EXTRA_SUBJECT, "测试标题");
            intent.putExtra(Intent.EXTRA_TEXT, "测试内容");
            getContext().startActivity(Intent.createChooser(intent, "来自xxx"));*/
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.tools.ShareImgUI");
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("image/jpg");
            try {
                intent.putExtra(Intent.EXTRA_STREAM, new URI(sharingText));//uri为你要分享的图片的uri
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            getContext().startActivity(intent);
        }

    }

    private void sendShareContent(){
        if (this.content!=null){
            UMImage image = new UMImage(getContext(), content.mImageList.get(0));//取第一个图片为分享显示图片
            image.compressStyle = UMImage.CompressStyle.SCALE;//大小压缩，默认为大小压缩，适合普通很大的图
            String url = content.getContentUrl();
            UMWeb web = new UMWeb(url);
            web.setTitle(content.mTitle);//标题
            web.setThumb(image);  //缩略图
            String text = HtmlUtil.getTextFromHtml(content.mContent,20);
            if (text!=null && !text.equals("null")) {
                web.setDescription(text);//取最多20字作为描述
            }else{
                web.setDescription((String) getContext().getResources().getText(R.string.share_description_empty));
            }
            new ShareAction((Activity) getContext())
                    //.withText("hello")
                    .withMedia(web)
                    .setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.SINA)
                    .setCallback(mShareListener)
                    .open();
        }

    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_image_overlay, this);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        view.findViewById(R.id.btnShare).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendShareIntent();
                actionKey(KeyEvent.KEYCODE_BACK);
                sendShareContent();
            }
        });
        view.findViewById(R.id.imgClose).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actionKey(KeyEvent.KEYCODE_BACK);
            }
        });
    }

    /**
     * 模拟键盘事件方法
     * @param keyCode
     */
    public void actionKey(final int keyCode) {
        new Thread () {
            public void run () {
                try {
                    Instrumentation inst=new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch(Exception e) {
                    e.printStackTrace();                    }
            }
        }.start();
    }

    private UMShareListener mShareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            Toast.makeText(getContext(),"成功了",Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            Toast.makeText(getContext(),"失败"+t.getMessage(),Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            Toast.makeText(getContext(),"取消了",Toast.LENGTH_LONG).show();

        }
    };
}
