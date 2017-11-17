package com.mmdkid.mmdkid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.mmdkid.mmdkid.fragments.register.RegisterEmailFragment;
import com.mmdkid.mmdkid.fragments.register.RegisterPhoneFragment;

public class RegisterActivity extends AppCompatActivity implements
        RegisterEmailFragment.OnFragmentInteractionListener,
        RegisterPhoneFragment.OnFragmentInteractionListener,
        View.OnClickListener{

    private RegisterEmailFragment mEmailFragment;
    private RegisterPhoneFragment mPhoneFragment;
    private TextView mSwitchTextView;
    private TextView mLoginTextView;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

       mSwitchTextView = (TextView) findViewById(R.id.tvSwitch);
       mSwitchTextView.setOnClickListener(this);
       // 缺省显示切换至邮件注册
       mSwitchTextView.setText(getResources().getString(R.string.register_email));
       mLoginTextView = (TextView) findViewById(R.id.tvLogin);
       mLoginTextView.setOnClickListener(this);

        //首先需要先实例好三个全局Fragment
        mEmailFragment = RegisterEmailFragment.newInstance("","");
        mPhoneFragment = RegisterPhoneFragment.newInstance("","");

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.fragmentContainer, mPhoneFragment);
        mFragmentTransaction.add(R.id.fragmentContainer, mEmailFragment);
        mFragmentTransaction.hide(mEmailFragment);
        mFragmentTransaction.commit();


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tvSwitch:
                // 切换注册方式
                mFragmentTransaction = mFragmentManager.beginTransaction();
                if (mPhoneFragment.isVisible()){
                    // 邮件注册
                    mFragmentTransaction.hide(mPhoneFragment);
                    mFragmentTransaction.show(mEmailFragment);
                    mSwitchTextView.setText(getResources().getString(R.string.register_phone));
                }else{
                    // 手机注册
                    mFragmentTransaction.show(mPhoneFragment);
                    mFragmentTransaction.hide(mEmailFragment);
                    mSwitchTextView.setText(getResources().getString(R.string.register_email));
                }
                mFragmentTransaction.commit();

                break;
            case R.id.tvLogin:
                // 已有账号，直接去登录
                Intent intent = new Intent(this,LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
