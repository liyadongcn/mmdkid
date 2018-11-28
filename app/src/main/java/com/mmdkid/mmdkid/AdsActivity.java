package com.mmdkid.mmdkid;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.helper.AdUtil;
import com.mmdkid.mmdkid.models.Advertisement;

import java.io.File;
import java.util.ArrayList;

public class AdsActivity extends AppCompatActivity {
    private final static String TAG = "AdsActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ArrayList<Advertisement> mAdvertisementList;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);

        initData();

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),mAdvertisementList);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                finish();
            }
        });

    }

    private void initData() {
        mAdvertisementList = AdUtil.getAdvertisements(this);
        if (mAdvertisementList.isEmpty()){
            // 没有广告
            Toast.makeText(this,"没有广告~",Toast.LENGTH_LONG).show();
            finish();
        }
        return;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ads, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_ADVERTISEMENT = "advertisement";

        private Advertisement mAdvertisement;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(Advertisement advertisement) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_ADVERTISEMENT,advertisement);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mAdvertisement = (Advertisement) getArguments().getSerializable(ARG_ADVERTISEMENT);
            View rootView = inflater.inflate(R.layout.fragment_ads, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(mAdvertisement.mName);
            ImageView adView = (ImageView) rootView.findViewById(R.id.ivAdvertisement);
            File file = new File( mAdvertisement.mLocalPath);
            if (file.exists()){
                // 广告缓存的图片文件存在 显示该图片
                adView.setImageURI(Uri.fromFile(file));
                if ( mAdvertisement.mUrl!=null && ! mAdvertisement.mUrl.isEmpty()){
                    // 广告有链接 则设置点击跳转
                    adView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //跳转到广告的链接
                            Intent intent = new Intent(getContext(),WebViewActivity.class);
                            intent.putExtra("url", mAdvertisement.mUrl);
                            intent.putExtra("showType",WebViewActivity.SHOW_TYPE_ADVERTISEMENT);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
                }
            }else{
                // 广告图片不存在 直接进入主程序
                Log.d(TAG,"No valid advertisment image. " + mAdvertisement.mName
                        + "Location: " +mAdvertisement.mLocalPath);

            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Advertisement> mAdsList;

        public SectionsPagerAdapter(FragmentManager fm,ArrayList<Advertisement> adsList) {
            super(fm);
            mAdsList = adsList;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(mAdsList.get(position));
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return mAdsList.size();
        }
    }
}
