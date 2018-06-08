package com.mmdkid.mmdkid.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.channel.ChannelActivity;
import com.mmdkid.mmdkid.channel.ChannelEntity;
import com.mmdkid.mmdkid.models.Content;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ViewPager mViewPager;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private OnFragmentInteractionListener mListener;

    private ArrayList<PageTitle> mPageTitleList;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        // get the favorite channels
        getChannels();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) fragmentView.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mSectionsPagerAdapter.notifyDataSetChanged();

        TabLayout tabLayout = (TabLayout) fragmentView.findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) fragmentView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(getActivity(), ChannelActivity.class);
                startActivity(intent);
            }
        });

        return fragmentView;
    }
    /*
    * 读取频道设置,初次设置读取favorites，以后读取channels
    * */
    private void getChannels() {
        mPageTitleList = new ArrayList<PageTitle>();
        App app = (App)getContext().getApplicationContext();
        //app.clearChannels();
        ArrayList<ChannelEntity> channels = app.getChannels();
        PageTitle pageTitle;
        if (channels==null || channels.isEmpty()){
            // 通过读取favorites设置channels
            Log.d(TAG,"Channels is null or empty.");
        }else {
            Log.d(TAG,"Get the channels data." + channels.toString());
           /* for(int i =0; i<channels.size();i++){
                Log.d(TAG,"Channel is : " + channels.get(i).getName());
            }*/
            for(ChannelEntity channel: channels){
                Log.d(TAG,"Channel is : " + channel.getName());
                pageTitle = new PageTitle();
                pageTitle.name = channel.getName();
                pageTitle.id = Long.toString(channel.getId());
                pageTitle.keyWords = channel.getKeyWords();
                mPageTitleList.add(pageTitle);
            }
        }
    }

    private void resetChannels(ArrayList<ChannelEntity> channelEntities){
        if (mPageTitleList!=null){
            mPageTitleList.clear();

        }else{
            mPageTitleList = new ArrayList<PageTitle>();
        }
        PageTitle pageTitle;
        for(ChannelEntity channel: channelEntities){
            Log.d(TAG,"Reset Channel is : " + channel.getName());
            pageTitle = new PageTitle();
            pageTitle.name = channel.getName();
            pageTitle.id = Long.toString(channel.getId());
            pageTitle.keyWords = channel.getKeyWords();
            mPageTitleList.add(pageTitle);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ChannelActivity.CHANNEL_SETTING_REQUEST){
            if (resultCode == ChannelActivity.CHANNEL_SETTING_RESULT_OK){
                ArrayList<ChannelEntity> channelEntities = (ArrayList<ChannelEntity>) data.getSerializableExtra("channels");
                resetChannels(channelEntities);
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        Log.d(TAG,"Homefragment onStart");
        // get the favorite channels 新的频道设置
        getChannels();
        mSectionsPagerAdapter.notifyDataSetChanged();
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     * 需要理解instantiateItem源码
     * 参考：
     * https://blog.csdn.net/b805887485/article/details/76039443
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof Fragment){
                String fragmentName = ((Fragment) object).getTag();
                Log.d(TAG,"Fragment Name >>>" + fragmentName);
                // 遍历名称
                int i =0;
                for(PageTitle title : mPageTitleList){
                    Log.d(TAG,"ViewPager channel >>>" + createFragmentName(mViewPager.getId(),Long.valueOf(title.id)));
                    if (createFragmentName(mViewPager.getId(),Long.valueOf(title.id)).equals(fragmentName)){
                        Log.d(TAG,"Find the fragment with same name>>>" + createFragmentName(mViewPager.getId(),Long.valueOf(title.id)));
                        return i+4;
                    }
                    i++;
                }
            }
            return POSITION_NONE;
        }

        private  String createFragmentName(int viewId, long id) {
            return "android:switcher:" + viewId + ":" + id +500; // +500 避免页面多时冲突 title的id和viewpager的id冲突
        }
        /**
         *  前4个频道为固定频道
         *  动态变动的频道使用频道名称作为fragment的标识
         */
        @Override
        public long getItemId(int position) {
            if (position<4){
                return position;
            }else{
                return Long.valueOf(mPageTitleList.get(position-4).id)+500; // +500 避免页面多时冲突 title的id和viewpager的id冲突
            }

        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return ContentFragment.newInstance(Content.TYPE_PUSH,"");
                case 1:
                    return ContentFragment.newInstance(Content.TYPE_HOT,"");
                case 2:
                    return ContentFragment.newInstance(Content.TYPE_VIDEO,"");
                case 3:
                    return ContentFragment.newInstance(Content.TYPE_IMAGE,"");
              /*  case 4:
                    return ContentFragment.newInstance(Content.TYPE_POST,"");*/
                default:
                    //return PlaceholderFragment.newInstance(position);
                    // 使用标题头作为搜索依据 过滤内容
                    if (mPageTitleList.get(position-4).keyWords!=null){
                        if (! mPageTitleList.get(position-4).keyWords.isEmpty()){
                            return ContentFragment.newInstance(Content.TYPE_PUSH,mPageTitleList.get(position-4).keyWords);
                        }
                    }
                    return ContentFragment.newInstance(Content.TYPE_PUSH,mPageTitleList.get(position-4).name);

            }

        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 4+mPageTitleList.size();
        }

/*        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            if (manager != null) {
                FragmentTransaction trans = manager.beginTransaction();
                trans.remove((Fragment) object);
                trans.commit();
            }
        }*/

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "推荐";
                case 1:
                    return "热点";
                case 2:
                    return "视频";
                case 3:
                    return "图片";
               /* case 4:
                    return "文章";*/
                default:
                    return mPageTitleList.get(position-4).name;
            }
            //return null;
        }
    }

    private class PageTitle{
        String name;
        String id;
        String keyWords;
    }
}
