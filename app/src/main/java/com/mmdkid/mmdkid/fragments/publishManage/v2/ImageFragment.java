package com.mmdkid.mmdkid.fragments.publishManage.v2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.PublishManagePopup;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.adapters.ModelRecyclerAdapter;
import com.mmdkid.mmdkid.fragments.RecyclerViewClickListener;
import com.mmdkid.mmdkid.helper.ProgressDialog;
import com.mmdkid.mmdkid.imagepost.ImageOverlayView;
import com.mmdkid.mmdkid.models.Content;
import com.mmdkid.mmdkid.models.ImagePost;
import com.mmdkid.mmdkid.models.Model;
import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.ElasticConnection;
import com.mmdkid.mmdkid.server.ElasticQuery;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.QueryBuilder;
import com.mmdkid.mmdkid.server.RESTAPIConnection;
import com.mmdkid.mmdkid.server.Sort;
import com.stfalcon.frescoimageviewer.ImageViewer;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends Fragment implements ElasticConnection.OnConnectionListener{
    private static final String TAG = "ImageFragment";

    private Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout mRefreshLayout;
    private ProgressDialog mProgressDialog;

    private ArrayList<Model> mDataset;
    private ElasticQuery mQuery;
    private ElasticConnection mConnection;

    private User mCurrentUser;
    private Token mCurrentToken;
    private boolean mIsFetching = false;
    private int mCurrentPosition = -1;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageFragment.OnFragmentInteractionListener mListener;

    /**
     *  删除操作弹出窗口
     */
    private PublishManagePopup mPopwindow;

    private List<String> mImagePostList; // 当前显示的图片列表
    private String mImageDescription;   // 当前图片的描述
    private ImageOverlayView mOverlayView; // 叠加在图片上的视图


    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(String param1, String param2) {
        ImageFragment fragment = new ImageFragment();
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
        mContext = getActivity();
        // init the data set
        mDataset = new ArrayList<Model>();
        // show the progress dialog
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.show();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_publish_manage_image, container, false);
        // use the recycler view
        mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.rvContent);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(fragmentView.getContext());
        //mLayoutManager = new GridLayoutManager(this,2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ModelRecyclerAdapter(fragmentView.getContext(), mDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Swipe refresh listener
        mRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.layout_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                // 加载更多数据
                if (mQuery.hasMore()) {
                    //mConnection.Query(mQuery);
                    mQuery.all();
                } else {
                    // 提示没有更多数可以加载
                    Toast.makeText(mContext, getString(R.string.no_more_data), Toast.LENGTH_LONG).show();
                    mRefreshLayout.setRefreshing(false);
                }

            }
        });

        // RecyclerView item 点击监听
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(mContext, mRecyclerView, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 判断点击的是否为删除按钮
                if (view.getId() == R.id.imageDelete) {
                    showNormalDialog((Content) mDataset.get(position));
                } else {
                    /*Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra("url", mDataset.get(position).getUrl());
                    Log.d(TAG, mDataset.get(position).getUrl());
                    startActivity(intent);*/
                    showImagePost(mDataset.get(position));
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //Toast.makeText(mContext,"Click "+mDataset.get(position).mContent,Toast.LENGTH_SHORT).show();
                mPopwindow = new PublishManagePopup(getActivity(), mPopItemClick);
                mPopwindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                mCurrentPosition = position;
            }
        }));

        //添加分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        // 滚动到底部自动加载
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = recyclerView.getAdapter().getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                int visibleItemCount = recyclerView.getChildCount();

                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItemPosition == totalItemCount - 1
                        && visibleItemCount > 0) {
                    //加载更多
                    if (mQuery.hasMore()) {
                        //mConnection.Query(mQuery);
                        mRefreshLayout.setRefreshing(true);
                        mQuery.all();
                    } else {
                        // 提示没有更多数可以加载
                        Toast.makeText(mContext, getString(R.string.no_more_data), Toast.LENGTH_LONG).show();
                        mRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
        return fragmentView;
    }

    private void showImagePost(Model model) {
        if (!(model instanceof Content)) return ;
        Content content = (Content)model;
        AnimationDrawable animationDrawable = new AnimationDrawable();
        Drawable drawable = getResources().getDrawable(R.drawable.loading);
        if(drawable != null){
            animationDrawable.addFrame(drawable,100);
            animationDrawable.setOneShot(false);
        }
        GenericDraweeHierarchyBuilder draweeHierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(getResources())
                //.setFailureImage(R.drawable.failureDrawable)
                // .setProgressBarImage(R.drawable.spinner_gif);
                .setProgressBarImage(animationDrawable);
        //.setProgressBarImage(new ProgressBarDrawable());

        //.setPlaceholderImage(R.drawable.placeholderDrawable);
        mOverlayView = new ImageOverlayView(mContext,content);
        mImagePostList = content.mImageList;
        mImageDescription = content.mContent;
        new ImageViewer.Builder<>(mContext, content.mImageList)
                .setStartPosition(0)
                .setImageMargin(getContext(),R.dimen.image_margin)
                .setImageChangeListener(getImageChangeListener())
                .setCustomDraweeHierarchyBuilder(draweeHierarchyBuilder)
                .setOverlayView(mOverlayView)
                .show();
    }

    /*
     *   图片浏览监听，可以设置图片描述，以及分享链接
     * */
    private ImageViewer.OnImageChangeListener getImageChangeListener() {
        return new ImageViewer.OnImageChangeListener() {
            @Override
            public void onImageChange(int position) {
                //CustomImage image = images.get(position);
                mOverlayView.setShareText(mImagePostList.get(position));
                mOverlayView.setDescription(String.valueOf(position+1)+"/"+ Integer.toString(mImagePostList.size())
                        + " " + mImageDescription);
            }

        };
    }

    private View.OnClickListener mPopItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mPopwindow.dismiss();
            mPopwindow.backgroundAlpha(getActivity(), 1f);
            Intent intent;
            switch (view.getId()) {
                case R.id.delete:
                    if (mCurrentPosition != -1) {
                        showNormalDialog((Content) mDataset.get(mCurrentPosition));
                    }
                    break;
            }
        }
    };

    private void showNormalDialog(final Content content) {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        //normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle("删除");
        normalDialog.setMessage("您确认要删除吗?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        ImagePost imagePost = new ImagePost();
                        imagePost.id = content.mModelId;
                        imagePost.delete(mContext, new RESTAPIConnection.OnConnectionListener() {
                            @Override
                            public void onErrorRespose(Class c, String error) {
                                Toast.makeText(mContext, "删除出错！", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onResponse(Class c, ArrayList responseDataList) {
                                Toast.makeText(mContext, "删除成功！", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                mDataset.remove(mCurrentPosition);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        // 显示
        normalDialog.show();
    }


    private void initData() {
        mDataset.clear();
        App app = (App) mContext.getApplicationContext();
        if (!app.isGuest()) {
            mCurrentUser = app.getCurrentUser();
            mCurrentToken = app.getCurrentToken();
            // 设置查询
            mConnection = new ElasticConnection(this);
            mQuery = (ElasticQuery) Content.find(mConnection);
            // 创建查询条件
            JSONObject request =   new QueryBuilder().boolQuery()
                    .must(QueryBuilder.termQuery("model_type", Content.TYPE_IMAGE))
                    .must(QueryBuilder.termQuery("created_by", Integer.toString(mCurrentUser.mId)))
                    .getJSONQuery();
            Log.d(TAG,"Image boolQuery is :" + request.toString());
            mQuery.setJsonRequest(request);
            // 按照时间排序
            Sort sort = new Sort();
            sort.add("created_at",Sort.SORT_DESC);
            mQuery.setSort(sort);
            mQuery.all();
            mIsFetching = true;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume.....");

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ImageFragment.OnFragmentInteractionListener) {
            mListener = (ImageFragment.OnFragmentInteractionListener) context;
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

    @Override
    public void onErrorRespose(Class c, String error) {
        Log.d(TAG, "Get the error response from the server");
        mIsFetching = false;
        mProgressDialog.dismiss();
    }

    @Override
    public void onResponse(Class c, ArrayList responseDataList) {
        Log.d(TAG, "Get correct response from the server.");
        mIsFetching = false;
        if (c == Content.class && !responseDataList.isEmpty()) {
            Log.d(TAG, "Get the content response from the server.");
            for (Object obj : responseDataList) {
                Content content = (Content) obj;
                Log.d(TAG, "Model id: " + content.mModelId);
                Log.d(TAG, "Model type: " + content.mModelType);
                Log.d(TAG, "Content images: " + content.mImageList);
                mDataset.add(content);
                content.setViewType(Model.VIEW_TYPE_PUBLISH_MANAGE_IMAGE);
            }
            mAdapter.notifyDataSetChanged();
            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(mDataset.size() - responseDataList.size(), 0);
        }
        mProgressDialog.dismiss();
        mRefreshLayout.setRefreshing(false);
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


}
