package com.mmdkid.mmdkid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.models.Student;
import com.mmdkid.mmdkid.models.User;

public class KidDetailActivity extends AppCompatActivity {
    private static final String TAG = "KidDetailActivity";

    private SimpleDraweeView mAvatar;
    private TextView mRealName;
    private TextView mBirthday;
    private TextView mGender;
    private TextView mNickName;

    private Student mKid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_detail);

        mAvatar = (SimpleDraweeView) findViewById(R.id.sdvAvatar);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KidDetailActivity.this,KidFormActivity.class);
                intent.putExtra("model",mKid);
                startActivityForResult(intent,0);
            }
        });
        mRealName = (TextView) findViewById(R.id.tvRealName);
        mNickName = (TextView) findViewById(R.id.tvNickName);
        mBirthday = (TextView) findViewById(R.id.tvBirthday);
        mGender = (TextView) findViewById(R.id.tvGender);

        mKid = (Student) getIntent().getSerializableExtra("model");

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAvatar.setImageURI(mKid.mAvatar);
        mNickName.setText(mKid.mNickname);
        mRealName.setText(mKid.mRealname);
        mBirthday.setText(mKid.mBirthday);
        mGender.setText(User.getGenderName(mKid.mGender));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            if (resultCode == RESULT_OK){
                mKid = (Student) data.getSerializableExtra("model");
                Log.d(TAG,"Get Updated Student info. ");

            }
        }
    }

    /**
     * 刷新
     */
    private void refresh() {
        onCreate(null);
    }
}
