package com.mmdkid.mmdkid.imagepost;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mmdkid.mmdkid.R;


/*
 * Created by Alexander Krol (troy379) on 29.08.16.
 */
public class ImageOverlayView extends RelativeLayout {

    private TextView tvDescription;

    private String sharingText;

    public ImageOverlayView(Context context) {
        super(context);
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
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        sendIntent.setType("text/plain");
        getContext().startActivity(sendIntent);
    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_image_overlay, this);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        view.findViewById(R.id.btnShare).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendShareIntent();
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
}
