package com.mmdkid.mmdkid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.OkHttpManager;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.IdentityHashMap;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ProfileActivity";

    private User mUser;

    private LinearLayout mAvatarllView;
    private LinearLayout mNickNamellView;
    private LinearLayout mSignaturellView;
    private LinearLayout mEmailllView;
    private LinearLayout mCellllView;
    private LinearLayout mPasswordllView;

    private SimpleDraweeView mAvatarView;
    private TextView mNickNameView;
    private TextView mSignatureView;
    private TextView mEmailView;
    private TextView mCellView;

    private ProgressBar mProgressBar;

    private EditText mOriginalPasswordView;
    private EditText mNewPasswordView;
    private EditText mConfirmPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUser = (User)getIntent().getSerializableExtra("user");
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mAvatarllView = (LinearLayout) findViewById(R.id.llAvatar);
        mNickNamellView = (LinearLayout) findViewById(R.id.llNickName);
        mSignaturellView = (LinearLayout) findViewById(R.id.llSignature);
        mEmailllView = (LinearLayout) findViewById(R.id.llEmail);
        mCellllView = (LinearLayout) findViewById(R.id.llCell);
        mPasswordllView = (LinearLayout) findViewById(R.id.llPassword);

        mAvatarllView.setOnClickListener(this);
        mNickNamellView.setOnClickListener(this);
        mSignaturellView.setOnClickListener(this);
        mEmailllView.setOnClickListener(this);
        mCellllView.setOnClickListener(this);
        mPasswordllView.setOnClickListener(this);

        mAvatarView = (SimpleDraweeView) findViewById(R.id.sdvAvatar);
        mNickNameView = (TextView) findViewById(R.id.tvNickName);
        mSignatureView = (TextView) findViewById(R.id.tvSignature);
        mEmailView = (TextView) findViewById(R.id.tvEmail);
        mCellView = (TextView) findViewById(R.id.tvCell);

        mAvatarView.setImageURI(mUser.mAvatar);
        mNickNameView.setText(mUser.mNickname);
        mSignatureView.setText(mUser.mSignature);
        mEmailView.setText(mUser.mEmail);
        mCellView.setText(mUser.mCellphone);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_upload_post) ;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llNickName:
                showEditTextDialog(mNickNameView,"请输入昵称",-1);
                break;
            case R.id.llSignature:
                showEditTextDialog(mSignatureView,"请输入签名",-1);
                break;
            case R.id.llEmail:
                showEditTextDialog(mEmailView,"请输入邮箱",-1);
                break;
            case R.id.llCell:
                showEditTextDialog(mCellView,"请输入手机",InputType.TYPE_CLASS_NUMBER);
                break;
            case R.id.llAvatar:
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setFixAspectRatio(true)
                        .start(this);
                break;
            case R.id.llPassword:
                showChangePasswordDialog();
                break;
        }
    }
    /**
     *  修改用户密码对话框
     */
    private void showChangePasswordDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_password_change, null);
        mOriginalPasswordView = (EditText) view.findViewById(R.id.etOriginalPassword);
        mNewPasswordView = (EditText) view.findViewById(R.id.etNewPassword);
        mConfirmPasswordView = (EditText) view.findViewById(R.id.etConfirmPassword);
        dialogBuilder.setView(view)
                .setTitle("修改密码")
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mOriginalPasswordView.setError("原始密码错误");
                changePassword(dialog);
            }

        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
    /**
     *  修改用户密码
     */
    private void changePassword(final AlertDialog dialog) {
        boolean cancel = false;
        View focusView = null;
        // 清除输入错误
        mOriginalPasswordView.setError(null);
        mNewPasswordView.setError(null);
        mConfirmPasswordView.setError(null);
        // 输入不能为空
        if (!isPasswordValid(mOriginalPasswordView.getText().toString())){
            mOriginalPasswordView.setError("密码错误");
            focusView = mOriginalPasswordView;
            cancel =true;
        }
        if (!isPasswordValid(mNewPasswordView.getText().toString())){
            mNewPasswordView.setError("密码必须为6位及以上");
            focusView = mNewPasswordView;
            cancel =true;
        }
        // 新密码与确认密码必须一致
        if (!mNewPasswordView.getText().toString().equals(mConfirmPasswordView.getText().toString())){
            mConfirmPasswordView.setError("密码输入不一致");
            focusView = mConfirmPasswordView;
            cancel =true;
        }
        if (cancel){
            focusView.requestFocus();
            return;
        }
        // 修改网络密码
        mUser.mPassword = mOriginalPasswordView.getText().toString();
        mUser.mNewPassword = mNewPasswordView.getText().toString();
        mUser.mPasswordRepeat = mConfirmPasswordView.getText().toString();
        mUser.save(User.ACTION_RESET_PASSWORD, this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Reset Password Server Failure." + error);
                try {
                    JSONObject jsonObject = new JSONObject(error);
                    if (jsonObject.has("error")) Toast.makeText(ProfileActivity.this,
                            jsonObject.getString("error"),Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this,
                            error,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c == User.class && !responseDataList.isEmpty()){
                    Toast.makeText(ProfileActivity.this,"密码修改成功",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });

    }


    private void showEditTextDialog(final TextView textView, String dialogTitle,int inputType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edittext, null);
        final EditText editTextView = (EditText) view.findViewById(R.id.etEditText);
        editTextView.setText(textView.getText().toString());
        if (inputType!=-1) editTextView.setInputType(inputType);
        dialogBuilder.setView(view)
                .setTitle(dialogTitle)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (textView.getId()){
                    case R.id.tvEmail:
                        if (!isEmailValid(editTextView.getText().toString())){
                            editTextView.setError("请输入正确的邮件！");
                            return;
                        }
                        break;
                    case R.id.tvCell:
                        if (!isCellValid(editTextView.getText().toString())){
                            editTextView.setError("请输入正确的手机号！");
                            return;
                        }
                        break;
                    case R.id.tvNickName:
                        if (!isNickNameValid(editTextView.getText().toString())){
                            editTextView.setError("请输入正确的昵称！");
                            return;
                        }
                        break;
                    case R.id.tvSignature:
                        if (!isSignatureValid(editTextView.getText().toString())){
                            editTextView.setError("请输入正确签名！");
                            return;
                        }
                        break;
                    default:
                        saveToServer(textView,editTextView.getText().toString(),dialog);
                        return;
                }
                saveToServer(textView,editTextView.getText().toString(),dialog);
            }

        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }


    /**
     *  修改用户信息对话框
     *  例如昵称、邮箱地址、手机信息
     *  每次只修改单个信息
     */
    /*private void showEditViewDialog(final TextView view, String dialogTitle, int inputType) {
        *//* 装入一个EditView
         *//*
        final EditText editText = new EditText(this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(this);
        inputDialog.setTitle(dialogTitle).setView(editText);
        editText.setText(view.getText().toString());
        if (inputType!=-1) editText.setInputType(inputType);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!view.getText().equals(editText.getText().toString())) {
                            // 用户修改了相应的设置
                            // 保存新设置到服务器
                            saveToServer(view,editText.getText().toString(),);
                        }
                    }
                }).setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        *//*Toast.makeText(ProfileActivity.this,
                                "取消修改",
                                Toast.LENGTH_SHORT).show();*//*
                    }
                }).show();

    }*/
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isCellValid(String cell) {
        return cell.length()==11 && TextUtils.isDigitsOnly(cell);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

    private boolean isNickNameValid(String nickName) {
        //TODO: Replace this with your own logic
        return nickName.length() <20;
    }

    private boolean isSignatureValid(String signature) {
        //TODO: Replace this with your own logic
        return signature.length() <30;
    }

    /**
     *  修改昵称，邮箱，及手机信息后，存储到网络服务器
     *  并更新本地存储的用户信息
     */
    private void saveToServer(final TextView view, final String value, final AlertDialog dialog) {
        User user = mUser;
        switch (view.getId()){
            case R.id.tvNickName:
                user.mNickname = value;
                break;
            case R.id.tvSignature:
                user.mSignature = value;
                break;
            case R.id.tvEmail:
                user.mEmail = value;
                break;
            case R.id.tvCell:
                user.mCellphone = value;
                break;
        }
        user.save(Model.ACTION_UPDATE, this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"服务器错误！" + error);
                Toast.makeText(ProfileActivity.this,"服务器错误~："+error,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if (c == User.class && !responseDataList.isEmpty()){
                    mUser = (User)responseDataList.get(0);
                    Log.d(TAG,"保存成功！");
                    Log.d(TAG,"User sianature is :" + mUser.mSignature);
                    Log.d(TAG,"User Nickname is :" + mUser.mNickname);
                    Log.d(TAG,"User email is :" + mUser.mEmail);
                    Log.d(TAG,"User cell is :" + mUser.mCellphone);
                    view.setText(value);
                    ((App)getApplication()).setCurrentUser(mUser);
                    //Toast.makeText(ProfileActivity.this,"修改成功",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    uploadImage(resultUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    Toast.makeText(this,"上传头像失败~",Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    /**
     *  上传头像图片到服务器
     */
    private void uploadImage(Uri resultUri) throws URISyntaxException {
        App app = (App)getApplication();
        if (app.isGuest()){
            Toast.makeText(this,"还没有登录，不能发布!",Toast.LENGTH_LONG).show();
            return ;
        }
        Token token = app.getCurrentToken();
        OkHttpManager manager = OkHttpManager.getInstance(this);
        manager.setAccessToken(token.mAccessToken);
        IdentityHashMap<String, Object> paramsMap = new IdentityHashMap<String, Object>();

        File file = new File(new URI(resultUri.toString()));
        paramsMap.put(new String("file"),file);
        Log.d(TAG,"File path :" + file.getPath());

        mProgressBar.setVisibility(View.VISIBLE);
        manager.upLoadFile("users/" + mUser.mId + "/avatar", paramsMap, new OkHttpManager.ReqProgressCallBack<Object>() {

            @Override
            public void onProgress(final long total, final long current) {
                Log.d(TAG,"Upload total is : "+total +"---------->"+current);
                mProgressBar.setProgress((int)(current *1.0f/total*100)); // 没有1.0f进度条不更新
            }

            @Override
            public void onReqSuccess(Object result) {
                // 上传本地图片到服务器
                Log.d(TAG,"Upload success!");
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this,"头像上传成功",Toast.LENGTH_LONG).show();
                Log.d(TAG,"Success return results :" + (String)result);
                // 根据服务器返回的用户信息更新本地用户信息，这里主要是更新avatar的信息
                refreshUserInfo((String) result);
            }

            @Override
            public void onReqFailed(String errorMsg) {
                Log.d(TAG,"Upload failed. " + errorMsg);
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this,"头像上传失败",Toast.LENGTH_LONG).show();
            }
        });
    }
    /**
     *  上传头像图片后，返回新的头像路径
     *  更新本地存储的用户信息，主要是头像文件网络地址
     *  头像是通过OkHttp完成的，返回Json数据
     *  其他用户信息的修改通过Volley完成
     */
    private void refreshUserInfo(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            User user = User.populateModel(jsonObject);
            if (user!=null){
                mUser = user;
                ((App)getApplication()).setCurrentUser(mUser);
                mAvatarView.setImageURI(mUser.mAvatar);
            }else{
                // 返回的用户信息无法解析
                Toast.makeText(ProfileActivity.this,"更新本地用户信息错误~",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(ProfileActivity.this,"更新本地用户信息错误~",Toast.LENGTH_LONG).show();
        }
    }
}
