package com.mmdkid.mmdkid.helper;

//import android.app.ProgressDialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.mmdkid.mmdkid.R;
/**
 * Dialog只有在调用show的时候才创建显示对象
 * 参考：https://blog.csdn.net/qasimcyrus/article/details/76408232
 */

public class ProgressDialog extends android.app.ProgressDialog {

    private TextView mMessageView;
    private String mMessageString;

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
      /*  LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.progress_dialog, null);*/
        setContentView(R.layout.progress_dialog);
        //setContentView(view);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        mMessageView =(TextView) findViewById(R.id.tvMessage);
        mMessageView.setText(mMessageString);

    }

    @Override
    public void setMessage(CharSequence message) {
        super.setMessage(message);
        mMessageString = message.toString();
    }
}
