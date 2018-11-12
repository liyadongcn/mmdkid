package com.mmdkid.mmdkid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
/**
 * 引导页 系统首次启动或者升级后首次启动显示
 * 图片大小应小于60k，否则不显示
 */
public class GuideActivity extends AppCompatActivity  implements ViewPager.OnPageChangeListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    /**
     * 引导页图片资源
     * 图片大小应小于60k，否则不显示
     */
    private ArrayList<Integer> mImageResourceList = new ArrayList<Integer>(){{add(R.drawable.guide_image1);
    add(R.drawable.guide_image2);
    ; add(R.drawable.guide_image3);}};
    /**
     * 引导页图片上的小圆点
     */
    private ImageView[] mDotViews;
    /**
     * 最后一个引导页上的浮动按钮 点击进入APP
     */
    private FloatingActionButton mFAB;
    /**
     * 进入主程序按钮
     */
    private TextView mLaunchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        //隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐藏导航栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(mImageResourceList,getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        //初始化引导页上的小圆点
        initDots();

        mFAB = (FloatingActionButton) findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击进入APP
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //隐藏浮动按钮 最后一页才显示
        mFAB.hide();

        mLaunchView = (TextView) findViewById(R.id.tvLaunch);
        mLaunchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击进入APP
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mLaunchView.setVisibility(View.GONE);
    }
    private void initDots(){
        LinearLayout layout = (LinearLayout)findViewById(R.id.dot_Layout);
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(20, 20);
        mParams.setMargins(10, 0, 10,0);//设置小圆点左右之间的间隔
        mDotViews = new ImageView[mImageResourceList.size()];
        //判断小圆点的数量，从0开始，0表示第一个
        for(int i = 0; i < mImageResourceList.size(); i++)
        {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(mParams);
            imageView.setImageResource(R.drawable.dotselector);
            if(i== 0)
            {
                imageView.setSelected(true);//默认启动时，选中第一个小圆点
            }
            else {
                imageView.setSelected(false);
            }
            mDotViews[i] = imageView;//得到每个小圆点的引用，用于滑动页面时，（onPageSelected方法中）更改它们的状态。
            layout.addView(imageView);//添加到布局里面显示
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_guide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int i = 0; i < mDotViews.length; i++)
        {
            if(position == i)
            {
                mDotViews[i].setSelected(true);
            }
            else {
                mDotViews[i].setSelected(false);
            }
        }
        if (position == mDotViews.length-1){
            //mFAB.show();
            mLaunchView.setVisibility(View.VISIBLE);
        }else{
            //mFAB.hide();
            mLaunchView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_RESOURCE_ID = "resource_id";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int resourceId) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_RESOURCE_ID, resourceId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_guide, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//           textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            ImageView imageView = (ImageView) rootView.findViewById(R.id.section_image);
            imageView.setImageResource(getArguments().getInt(ARG_RESOURCE_ID));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),getArguments().getInt(ARG_RESOURCE_ID));
//            imageView.setImageBitmap(bitmap);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Integer> mResourceIdList;

        public SectionsPagerAdapter(ArrayList<Integer>resourceList,FragmentManager fm) {
            super(fm);
            mResourceIdList = resourceList;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(mResourceIdList.get(position));
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return mResourceIdList.size();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 如果切换到后台，就设置下次不进入功能引导页
        App.setFirstStart(this,false);
    }
}
