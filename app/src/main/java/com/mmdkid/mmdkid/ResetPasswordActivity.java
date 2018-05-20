package com.mmdkid.mmdkid;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.fragments.register.RegisterEmailFragment;
import com.mmdkid.mmdkid.fragments.register.RegisterPhoneFragment;
import com.mmdkid.mmdkid.fragments.resetPassword.PhoneNumberFragment;
import com.mmdkid.mmdkid.fragments.resetPassword.ResetPasswordFragment;
import com.mmdkid.mmdkid.fragments.resetPassword.VerifyPhoneFragment;

public class ResetPasswordActivity extends AppCompatActivity implements
        ResetPasswordFragment.OnFragmentInteractionListener,
        PhoneNumberFragment.OnFragmentInteractionListener,
        VerifyPhoneFragment.OnFragmentInteractionListener
{
    private final static String TAG = "ResetPasswordActivity";
    private static final String FORGOT_PASSWORD_URL = "http://www.mmdkid.cn/index.php?r=site/request-password-reset&theme=app";

    private ResetPasswordFragment mResetPasswordFragment;
    private PhoneNumberFragment mPhoneNumberFragment;
    private VerifyPhoneFragment mVerifyPhoneFragment;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //首先Fragment
        mResetPasswordFragment = mResetPasswordFragment.newInstance("","");

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.fragmentContainer, mResetPasswordFragment);
        mFragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                mFragmentTransaction = mFragmentManager.beginTransaction();
                if (mPhoneNumberFragment!=null && mPhoneNumberFragment.isVisible()){
                    mFragmentTransaction.hide(mPhoneNumberFragment);
                    if(mResetPasswordFragment!=null) mFragmentTransaction.show(mResetPasswordFragment);
                }else if (mVerifyPhoneFragment!=null && mVerifyPhoneFragment.isVisible()){
                    mFragmentTransaction.hide(mVerifyPhoneFragment);
                    if(mPhoneNumberFragment!=null) mFragmentTransaction.show(mPhoneNumberFragment);
                }else if (mResetPasswordFragment.isVisible()){
                    finish();
                }
                mFragmentTransaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onResetPasswordSuccess() {
        // 通过手机号码修改密码成功
        Toast.makeText(this,"密码修改成功！请使用新密码登录",Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onPhoneVerified(String phone) {
        // 手机号码存在可以发送验证码并修改密码
        Log.d(TAG,"Phone is viliabale .");
        mVerifyPhoneFragment = VerifyPhoneFragment.newInstance(phone,"");
        mFragmentTransaction = mFragmentManager.beginTransaction();
        if(mPhoneNumberFragment!=null) mFragmentTransaction.hide(mPhoneNumberFragment);
        mFragmentTransaction.add(R.id.fragmentContainer, mVerifyPhoneFragment);
        mFragmentTransaction.commit();
    }

    @Override
    public void onResetPasswordByPhoneClicked() {
        // 使用手机号找回密码
        Log.d(TAG,"Reset by the phone clicked");
        mPhoneNumberFragment = PhoneNumberFragment.newInstance("","");
        mFragmentTransaction = mFragmentManager.beginTransaction();
        if(mResetPasswordFragment!=null) mFragmentTransaction.hide(mResetPasswordFragment);
        mFragmentTransaction.add(R.id.fragmentContainer, mPhoneNumberFragment);
        mFragmentTransaction.commit();
    }

    @Override
    public void onResetPasswordByEmailClicked() {
        // 通过邮箱找回密码
        Log.d(TAG,"Reset by the email clicked");
        Intent intent = new Intent(this,WebViewActivity.class);
        intent.putExtra("url",FORGOT_PASSWORD_URL);
        startActivity(intent);
    }


}
