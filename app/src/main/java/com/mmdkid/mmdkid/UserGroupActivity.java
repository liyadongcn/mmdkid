package com.mmdkid.mmdkid;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mmdkid.mmdkid.adapters.ImageGridAdapter;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.models.UserGroup;
import com.mmdkid.mmdkid.models.UserGroupMap;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.util.ArrayList;

public class UserGroupActivity extends AppCompatActivity {
    private static final String TAG ="UserGroupActivity";

    private SimpleDraweeView mAvatar;
    private TextView mName;
    private TextView mDescription;
    private TextView mCreated_at;
    private TextView mUpdated_at;

    private GridView mImageGridView;
    private ImageGridAdapter mAdapter;

    private ProgressDialog mProgressDialog;

    private ArrayList<Model> mDataset;
    private Query mQuery;
    private RESTAPIConnection mConnection;

    private User mCurrentUser;
    private Token mCurrentToken;
    private UserGroup mCurrentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initData();

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

    private void initData() {
        mCurrentGroup = (UserGroup) getIntent().getSerializableExtra("model");

        App app = (App)getApplication();
        if (app.isGuest()) finish();
        mCurrentUser = app.getCurrentUser();
        mCurrentToken = app.getCurrentToken();

        mDataset = new ArrayList<Model>();

        // 获取当前用户组内所有用户
        UserGroupMap.find(this, new RESTAPIConnection.OnConnectionListener() {
            @Override
            public void onErrorRespose(Class c, String error) {
                Log.d(TAG,"Get the error response for the user groups from the server");
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Class c, ArrayList responseDataList) {
                if(c == UserGroupMap.class && !responseDataList.isEmpty()){
                    Log.d(TAG,"Get the user groups response from the server.");
                    for(Object obj : responseDataList){
                        UserGroupMap userGroupMap = (UserGroupMap) obj;
                        User user;
                        if(userGroupMap.mModelUser!=null) {
                            user = (User) userGroupMap.mModelUser;
                            Log.d(TAG,"Get one user named " + user.mNickname );
                            if(mCurrentGroup.mType== UserGroup.GROUP_TYPE_CLASS && user.mRole == User.ROLE_STUDENT){
                                mDataset.add(user);
                            }else if(mCurrentGroup.mType== UserGroup.GROUP_TYPE_FRIEND){
                                mDataset.add(user);
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
                mProgressDialog.dismiss();
            }
        }).where("expand","model_user")
                .where("group_id",Integer.toString(mCurrentGroup.mId))
                .where("status",Integer.toString(UserGroupMap.STATUS_ACTIVE))
                .all();
        
    }

    private void initView() {

        // show the progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        mAvatar = (SimpleDraweeView) findViewById(R.id.sdvAvatar);
        mName = (TextView) findViewById(R.id.tvName);
        mDescription = (TextView) findViewById(R.id.tvDescription);
        mCreated_at = (TextView) findViewById(R.id.tvCreated_at);
        mUpdated_at = (TextView) findViewById(R.id.tvUpdated_at);

        mCurrentGroup = (UserGroup) getIntent().getSerializableExtra("model");

        mAvatar.setImageURI(mCurrentGroup.mAvatar);
        mName.setText(mCurrentGroup.mName);
        mDescription.setText(mCurrentGroup.mDescription);
        mCreated_at.setText(mCurrentGroup.mCreated_at);
        mUpdated_at.setText(mCurrentGroup.mUpdated_at);

        mImageGridView = (GridView) findViewById(R.id.gridview);
        mAdapter = new ImageGridAdapter(this,mDataset);
        mImageGridView.setAdapter(mAdapter);


    }
}
