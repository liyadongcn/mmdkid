package com.mmdkid.mmdkid;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.mmdkid.mmdkid.adapters.TextAdapter;
import com.mmdkid.mmdkid.fragments.ContentFragment;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.providers.HistorySuggestionsProvider;

import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "SearchResultsActivity";

    private ArrayList<String> mHistoryList;
    private ArrayList<String> mHotList;
    private String mKeyword="";

    private GridView mHistoryView;
    private TextAdapter mHistoryAdapter;
    private GridView mHotView;
    private TextAdapter mHotAdapter;
    private TextView mHistoryText;
    private TextView mHotText;
    private SearchView  mSearchView;
    private TextView mHistoryDelView;

    private App mApp;

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

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        //mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mHistoryDelView = (TextView) findViewById(R.id.tvHistoryDel);
        mHistoryDelView.setOnClickListener(this);

        //showResultView(View.GONE);

        mApp = (App)getApplication();
        mHistoryList = mApp.getHistoryKeyWords();
        if(mHistoryList==null){
            mHistoryList = new ArrayList<String>();
        }
        /*mHistoryList.add("母亲");
        mHistoryList.add("父亲");*/


        mHotList = new ArrayList<String>();
        mHotList.add("母亲hot");
        mHotList.add("父亲hot");

        mHistoryText = (TextView) findViewById(R.id.tvHistoryCaption);
        mHistoryView = (GridView) findViewById(R.id.gridviewHistory);
        mHistoryAdapter = new TextAdapter(this,mHistoryList);
        mHistoryView.setAdapter(mHistoryAdapter);
        mHistoryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
//                Toast.makeText(SearchResultsActivity.this, "" + position,
//                        Toast.LENGTH_SHORT).show();
                mKeyword = mHistoryList.get(position);
                Log.d(TAG,"History Keyword Seletction is :" + mKeyword);
                search();
            }
        });

        mHotText = (TextView) findViewById(R.id.tvHotCaption);
        mHotView = (GridView) findViewById(R.id.gridviewHot);
        mHotAdapter = new TextAdapter(this,mHotList);
        mHotView.setAdapter(mHotAdapter);
        mHotView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
//                Toast.makeText(SearchResultsActivity.this, "" + position,
//                        Toast.LENGTH_SHORT).show();
                mKeyword = mHotList.get(position);
                search();
            }
        });

        //handleIntent(getIntent());

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_search_results, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.setQuery(query,false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showSuggestedView(View.VISIBLE);
                showResultView(View.GONE);
                return false;
            }

        });

        return true;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG,"start prepare options menu.");
        mSearchView.setQuery(getIntent().getStringExtra(SearchManager.QUERY),true);
        CursorAdapter cursorAdapter = mSearchView.getSuggestionsAdapter();
        for (int i=0; i<cursorAdapter.getCount(); i++){
            Log.d(TAG,cursorAdapter.getItem(i).toString());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG,intent.getAction());
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mKeyword = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            Log.d(TAG,"Get the key word from the intent : "+ mKeyword);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    HistorySuggestionsProvider.AUTHORITY, HistorySuggestionsProvider.MODE);
            suggestions.saveRecentQuery(mKeyword, null);
            //Log.d(TAG,suggestions.);
            search();
        }
    }

    private void search(){

        if(mKeyword.isEmpty()){
            showSuggestedView(View.VISIBLE);
            showResultView(View.GONE);

        }else{
            if(mSearchView!=null) {
                mSearchView.setQuery(mKeyword,false);
               /*CursorAdapter cursorAdapter = mSearchView.getSuggestionsAdapter();
                Cursor cursor;
                int indexColumnSuggestion;
                for (int i=0; i<cursorAdapter.getCount(); i++){
                    cursor = (Cursor) cursorAdapter.getItem(i);
                    cursor.getString(0);
                    Log.d(TAG,cursor.getString(1));
                }*/
               if(!mHistoryList.contains(mKeyword)) {
                   // 历史记录中增加新的记录
                   mHistoryList.add(0,mKeyword);
                   mHistoryAdapter.notifyDataSetChanged();
                   Log.d(TAG,"Get the key word from the search function : "+ mKeyword);
               }
            }

            // 显示切换 显示搜索结果View 隐藏搜索词View
            showSuggestedView(View.GONE);
            showResultView(View.VISIBLE);

        }
        if(mViewPager.getAdapter()==null){
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SearchResultsActivity.SectionsPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mSectionsPagerAdapter);

        }else{
            mSectionsPagerAdapter.notifyDataSetChanged();
        }

    }

    private void showSuggestedView(int visibility){
        mHistoryView.setVisibility(visibility);
        mHotView.setVisibility(visibility);
        mHistoryText.setVisibility(visibility);
        mHotText.setVisibility(visibility);
        mHistoryDelView.setVisibility(visibility);
    }

    private void showResultView(int visibility){
        mViewPager.setVisibility(visibility);
        mTabLayout.setVisibility(visibility);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tvHistoryDel:
                // 删除所有历史搜索词
                mHistoryList.clear();
                mHistoryAdapter.notifyDataSetChanged();
                mApp.clearHistoryKeyWords();
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        mApp.setHistoryKeyWords(mHistoryList);
        super.onDestroy();
    }

    /* @Override
    public void onErrorRespose(String error) {
        // Elasticsearch network access has problem.
    }*/

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> mFragmentList;

        private FragmentManager mFragmentManager;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentList = new ArrayList<Fragment>();
            mFragmentManager = fm;
            createFragments();
        }

       /* @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }*/

        private void createFragments(){
            Fragment fragment=null;
            for (int position=0; position<getCount() ; position ++){
                switch (position) {
                    case 0:
                        fragment = ContentFragment.newInstance(Content.TYPE_PUSH,mKeyword);
                        break;
                    case 1:
                        fragment = ContentFragment.newInstance(Content.TYPE_VIDEO,mKeyword);
                        break;
                    case 2:
                        fragment = ContentFragment.newInstance(Content.TYPE_IMAGE,mKeyword);
                        break;
                    case 3:
                        fragment = ContentFragment.newInstance(Content.TYPE_POST,mKeyword);
                        break;
                    /*default:
                        fragment = HomeActivity.PlaceholderFragment.newInstance(position + 1);*/
                }
                mFragmentList.add(fragment);
            }
        }

        //@Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            Log.d(TAG, String.valueOf(mFragmentList.size()));
            ContentFragment fragment;
            if(mFragmentList != null && mFragmentList.size() >0){
                for (int i = 0; i < mFragmentList.size(); i++) {
                    fragment =(ContentFragment)mFragmentList.get(i);
                    if (fragment.isCreated()){
                        Log.d(TAG,"正在刷新Frament " + fragment.getArguments().getString("param1"));
                        fragment.update(mKeyword);
                    }else {
                        Bundle args = fragment.getArguments();
                        args.remove("param2");
                        args.putString("param2",mKeyword);
                        fragment.setArguments(args);
                    }
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            /*if(mFragmentList.size()>0 && mFragmentList.get(position)!=null){
                return mFragmentList.get(position);
            }else{*/
            /*    Fragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = ContentFragment.newInstance(Content.TYPE_PUSH,mKeyword);
                        break;
                    case 1:
                        fragment = ContentFragment.newInstance(Content.TYPE_VIDEO,mKeyword);
                        break;
                    case 2:
                        fragment = ContentFragment.newInstance(Content.TYPE_IMAGE,mKeyword);
                        break;
                    case 3:
                        fragment = ContentFragment.newInstance(Content.TYPE_POST,mKeyword);
                        break;
                   *//* default:
                        fragment = HomeActivity.PlaceholderFragment.newInstance(position + 1);*//*
                }
                mFragmentList.add(fragment);
                return  fragment;*/
            //}
            return mFragmentList.get(position);

        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "综合";
                case 1:
                    return "视频";
                case 2:
                    return "图片";
                case 3:
                    return "文章";

            }
            return null;
        }
    }



}
