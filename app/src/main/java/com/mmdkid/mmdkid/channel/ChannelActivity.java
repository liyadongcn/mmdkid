package com.mmdkid.mmdkid.channel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.R;

import java.util.ArrayList;
import java.util.List;



/**
 * 频道 增删改查 排序
 * Created by YoKeyword on 15/12/29.
 */
public class ChannelActivity extends AppCompatActivity {
    private final static String TAG = "ChannelActivity";

    public static int CHANNEL_SETTING_REQUEST =10;
    public static int CHANNEL_SETTING_RESULT_OK = 12;

    private RecyclerView mRecy;

    private App mApp;
    private List<ChannelEntity> mItems = null;
    private List<ChannelEntity> mOtherItems = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecy = (RecyclerView) findViewById(R.id.recy);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                Log.d(TAG,"Channel Activity home pressed.");
                saveChannlesSetting();
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChannlesSetting(){
        mApp.setChannels((ArrayList<ChannelEntity>) mItems);
        mApp.setOtherChannels((ArrayList<ChannelEntity>) mOtherItems);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"Channel Activity Back pressed.");
        saveChannlesSetting();
        super.onBackPressed();
    }

    private void init() {
       /* App app = (App)getApplication();
        final List<ChannelEntity> items = app.getChannels();
        final List<ChannelEntity> otherItems =app.getOtherChannels();*/
       mApp = (App) getApplication();
       mItems = mApp.getChannels();
       mOtherItems = mApp.getOtherChannels();
        /*final List<ChannelEntity> items = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            ChannelEntity entity = new ChannelEntity();
            entity.setName("频道" + i);
            items.add(entity);
        }
        final List<ChannelEntity> otherItems = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ChannelEntity entity = new ChannelEntity();
            entity.setName("其他" + i);
            otherItems.add(entity);
        }*/

        GridLayoutManager manager = new GridLayoutManager(this, 4);
        mRecy.setLayoutManager(manager);

        ItemDragHelperCallback callback = new ItemDragHelperCallback();
        final ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecy);

        final ChannelAdapter adapter = new ChannelAdapter(this, helper, mItems, mOtherItems);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = adapter.getItemViewType(position);
                return viewType == ChannelAdapter.TYPE_MY || viewType == ChannelAdapter.TYPE_OTHER ? 1 : 4;
            }
        });
        mRecy.setAdapter(adapter);

        adapter.setOnMyChannelItemClickListener(new ChannelAdapter.OnMyChannelItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(ChannelActivity.this, mItems.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {


        super.onDestroy();
    }
}
