package com.mmdkid.mmdkid;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Student;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.models.UserRelationship;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class KidFormActivity extends AppCompatActivity {
    private static final String TAG = "KidFormActivity";

    private String[] mRelationship = UserRelationship.getRelationshipNames();

    private EditText mRealNameView;
    private EditText mNickNameView;
    private EditText mBirthdayView;
    private RadioGroup mGenderView;
    private RadioButton mGenderMaleView;
    private RadioButton mGenderFemaleView;
    private TextView mRelationshipView;
    private Button mSaveButton;
    private DatePickerDialog.OnDateSetListener mdateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear+1;
            mDay = dayOfMonth;
            display();
        }
    };

    private int mYear, mMonth, mDay;
    private Date mBirthday;

    private User mCurrentUser;
    private Token mCurrentToken;
    private Student mKid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_kid);

        /**
         * 取得当前用户信息
         */
        App app = (App)getApplicationContext();
        if(app.isGuest()){
            finish();
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        }else {
            mCurrentUser = app.getCurrentUser();
            mCurrentToken = app.getCurrentToken();
        }
        /**
         * 取得传入的Kid信息 若没有则为新建kid
         */
        mKid = (Student) getIntent().getSerializableExtra("model");

        if (mKid == null){
            // 创建新的记录 设置初始值
            mKid = new Student();
            mKid.mGender = User.GENDER_MALE;
            mKid.mRole = User.ROLE_STUDENT;
            mKid.mRelationship = UserRelationship.USER_RELATIONSHIP_MOTHER;
            mBirthday = new Date();
        }else{
            // 修改已有记录
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                mBirthday = sdf.parse(mKid.mBirthday);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mBirthday);
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        mKid.mParentId = mCurrentUser.mId;

        mRealNameView = (EditText) findViewById(R.id.etRealName);
        mRealNameView.setText(mKid.mRealname);
        mNickNameView = (EditText) findViewById(R.id.etNickName);
        mNickNameView.setText(mKid.mNickname);
        mBirthdayView = (EditText) findViewById(R.id.etBirthday);

        mBirthdayView.setText(mKid.mBirthday);
        mBirthdayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(KidFormActivity.this,
                        mdateListener, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        mRelationshipView = (TextView) findViewById(R.id.tvRelationship);
        mRelationshipView.setText(UserRelationship.getRelationshipName(mKid.mRelationship));
        mRelationshipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(KidFormActivity.this);
                dialogBuilder.setTitle("选择关系");
//                dialogBuilder.setMessage("这是一个对话框演示！！！");
                dialogBuilder.setSingleChoiceItems(mRelationship,
                        Arrays.asList(mRelationship).indexOf(UserRelationship.getRelationshipName(mKid.mRelationship)),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mRelationshipView.setText(mRelationship[i]);
                        mKid.mRelationship = UserRelationship.getRelationship(mRelationship[i]);
                        dialogInterface.dismiss();
                    }
                });
                dialogBuilder.setCancelable(true);
                dialogBuilder.setNegativeButton("取消",null);
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });

        mGenderView = (RadioGroup) findViewById(R.id.groupGender);
        mGenderMaleView = (RadioButton) findViewById(R.id.radioMale);
        mGenderFemaleView = (RadioButton) findViewById(R.id.radioFemale);
        if(mKid.mGender == User.GENDER_MALE){
            mGenderMaleView.setChecked(true);
        }else{
            mGenderFemaleView.setChecked(true);
        }
        mGenderView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                RadioButton radioButton = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
                if(radioButton.getText().equals("男")){
                    mKid.mGender = User.GENDER_MALE;
                }else if(radioButton.getText().equals("女")){
                    mKid.mGender = User.GENDER_FEMALE;
                }
            }
        });

        mSaveButton = (Button) findViewById(R.id.buttonSave);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSave();
            }
        });

    }



    private void attemptSave() {
        // Reset errors.
        mRealNameView.setError(null);
        mNickNameView.setError(null);
        mBirthdayView.setError(null);

        // Store values at the time of the login attempt.
        String realName = mRealNameView.getText().toString();
        String nickName = mNickNameView.getText().toString();
        String birthday = mBirthdayView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid real name, if the user entered one.
        if (TextUtils.isEmpty(realName) || !isRealNameValid(realName)) {
            mRealNameView.setError(getString(R.string.error_invalid_realName));
            focusView = mRealNameView;
            cancel = true;
        }

        // Check for a valid nick name, if the user entered one.
        if ( !isNickNameValid(nickName)) {
            mNickNameView.setError(getString(R.string.error_invalid_nickName));
            focusView = mNickNameView;
            cancel = true;
        }

        // Check for a valid birthday, if the user entered one.
        if ( TextUtils.isEmpty(birthday) || !isBirthdayValid(birthday)) {
            mBirthdayView.setError(getString(R.string.error_invalid_birthday));
            focusView = mBirthdayView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mKid.mNickname = nickName;
            mKid.mRealname = realName;
            mKid.mBirthday = birthday;
            JSONObject jsonObject = mKid.toJsonObject();
            if(mKid.mId!=0){
                // 修改已有记录
                Log.d(TAG,"Kid update json object is :" +jsonObject .toString());
                mKid.save(Model.ACTION_UPDATE,this, new RESTAPIConnection.OnConnectionListener() {
                    @Override
                    public void onErrorRespose(Class c, String error) {
                        Log.d(TAG,"Update kid info on server failed.");

                    }

                    @Override
                    public void onResponse(Class c, ArrayList responseDataList) {
                        if (c==Student.class && !responseDataList.isEmpty()){
                            Student kid = (Student) responseDataList.get(0);
                            Log.d(TAG,"The kid real name is " + kid.mRealname);
                            Log.d(TAG,"Update kid success.");
                            Toast.makeText(KidFormActivity.this,"Create a new kid success.",Toast.LENGTH_LONG);
                            kid.mParentId = mCurrentUser.mId;
                            kid.mRelationship = mKid.mRelationship;
                            Intent intent = new Intent();
                            intent.putExtra("model",kid);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                });
            }else{
                // 创建新记录
                jsonObject.remove("id");
                Log.d(TAG,"Kid create json object is :" +jsonObject .toString());
                mKid.save(Model.ACTION_CREATE, this, new RESTAPIConnection.OnConnectionListener() {
                    @Override
                    public void onErrorRespose(Class c, String error) {
                        Log.d(TAG,"Create a new kid on server failed.");
                        Toast.makeText(KidFormActivity.this,"error to create.",Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onResponse(Class c, ArrayList responseDataList) {
                        if (c==Student.class && !responseDataList.isEmpty()){
                            Student kid = (Student) responseDataList.get(0);
                            Log.d(TAG,"New kid real name is " + kid.mRealname);
                            Log.d(TAG,"Create a new kid success.");
                            Toast.makeText(KidFormActivity.this,"Create a new kid success.",Toast.LENGTH_LONG);
                            finish();
                        }
                    }
                });
            }

        }


    }

    private boolean isRealNameValid(String realname) {
        //TODO: Replace this with your own logic
        return realname.length() > 1;
    }

    private boolean isNickNameValid(String nickname) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isBirthdayValid(String birthday) {
        //TODO: Replace this with your own logic
        return true;
    }

    /**
     * 设置日期 利用StringBuffer追加
     */
    public void display() {
        mBirthdayView.setText(new StringBuffer()
                .append(Integer.toString(mYear))
                .append("-")
                .append(String.format("%02d",mMonth))
                .append("-")
                .append(String.format("%02d",mDay)));
    }
}
