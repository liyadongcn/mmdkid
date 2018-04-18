package com.mmdkid.mmdkid.helper;

//import android.app.ProgressDialog;

import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.mmdkid.mmdkid.R;

public class ProgressDialog extends android.app.ProgressDialog {

    public ProgressDialog(Context context) {
        super(context, R.style.ProgressDialog);
    }

    public ProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        init(getContext());
    }

    private void init(Context context)
    {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
       /* setCancelable(false);
        setCanceledOnTouchOutside(false);*/

        setContentView(R.layout.progress_dialog);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

    }
}
