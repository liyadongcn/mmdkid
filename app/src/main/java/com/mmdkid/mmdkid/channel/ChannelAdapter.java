package com.mmdkid.mmdkid.channel;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.ActionLog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;



/**
 * 拖拽排序 + 增删
 * Created by YoKeyword on 15/12/28.
 */
public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {
    private static final String TAG = "ChannelAdapter";

    // 我的频道 标题部分
    public static final int TYPE_MY_CHANNEL_HEADER = 0;
    // 我的频道
    public static final int TYPE_MY = 1;
    // 其他频道 标题部分
    public static final int TYPE_OTHER_CHANNEL_HEADER = 2;
    // 其他频道
    public static final int TYPE_OTHER = 3;

    // 我的频道之前的header数量  该demo中 即标题部分 为 1
    private static final int COUNT_PRE_MY_HEADER = 1;
    // 其他频道之前的header数量  该demo中 即标题部分 为 COUNT_PRE_MY_HEADER + 1
    private static final int COUNT_PRE_OTHER_HEADER = COUNT_PRE_MY_HEADER + 1;

    private static final long ANIM_TIME = 360L;

    // touch 点击开始时间
    private long startTime;
    // touch 间隔时间  用于分辨是否是 "点击"
    private static final long SPACE_TIME = 100;

    private LayoutInflater mInflater;
    private ItemTouchHelper mItemTouchHelper;

    // 是否为 编辑 模式
    private boolean isEditMode;

    private List<ChannelEntity> mMyChannelItems, mOtherChannelItems;

    // 我的频道点击事件
    private OnMyChannelItemClickListener mChannelItemClickListener;

    private Context mContext;

    // 增加频道
    private static final int ACTION_NEW = 1;
    // 编辑频道
    private static final int ACTION_EDIT = 2;

    public ChannelAdapter(Context context, ItemTouchHelper helper, List<ChannelEntity> mMyChannelItems, List<ChannelEntity> mOtherChannelItems) {
        this.mInflater = LayoutInflater.from(context);
        this.mItemTouchHelper = helper;
        this.mMyChannelItems = mMyChannelItems;
        this.mOtherChannelItems = mOtherChannelItems;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {    // 我的频道 标题部分
            return TYPE_MY_CHANNEL_HEADER;
        } else if (position == mMyChannelItems.size() + 1) {    // 其他频道 标题部分
            return TYPE_OTHER_CHANNEL_HEADER;
        } else if (position > 0 && position < mMyChannelItems.size() + 1) {
            return TYPE_MY;
        } else {
            return TYPE_OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
            case TYPE_MY_CHANNEL_HEADER:
                view = mInflater.inflate(R.layout.item_my_channel_header, parent, false);
                final MyChannelHeaderViewHolder holder = new MyChannelHeaderViewHolder(view);
                holder.tvBtnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isEditMode) {
                            startEditMode((RecyclerView) parent);
                            holder.tvBtnEdit.setText(R.string.finish);
                        } else {
                            cancelEditMode((RecyclerView) parent);
                            holder.tvBtnEdit.setText(R.string.edit);
                        }
                    }
                });
                return holder;

            case TYPE_MY:
                view = mInflater.inflate(R.layout.item_my, parent, false);
                final MyViewHolder myHolder = new MyViewHolder(view);

                myHolder.textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        int position = myHolder.getAdapterPosition();
                        if (isEditMode) {
                            RecyclerView recyclerView = ((RecyclerView) parent);
                            View targetView = recyclerView.getLayoutManager().findViewByPosition(mMyChannelItems.size() + COUNT_PRE_OTHER_HEADER);
                            View currentView = recyclerView.getLayoutManager().findViewByPosition(position);
                            // 如果targetView不在屏幕内,则indexOfChild为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                            // 如果在屏幕内,则添加一个位移动画
                            if (recyclerView.indexOfChild(targetView) >= 0) {
                                int targetX, targetY;

                                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                                int spanCount = ((GridLayoutManager) manager).getSpanCount();

                                // 移动后 高度将变化 (我的频道Grid 最后一个item在新的一行第一个)
                                if ((mMyChannelItems.size() - COUNT_PRE_MY_HEADER) % spanCount == 0) {
                                    View preTargetView = recyclerView.getLayoutManager().findViewByPosition(mMyChannelItems.size() + COUNT_PRE_OTHER_HEADER - 1);
                                    targetX = preTargetView.getLeft();
                                    targetY = preTargetView.getTop();
                                } else {
                                    targetX = targetView.getLeft();
                                    targetY = targetView.getTop();
                                }

                                moveMyToOther(myHolder);
                                startAnimation(recyclerView, currentView, targetX, targetY);

                            } else {
                                moveMyToOther(myHolder);
                            }
                        } else {
                            mChannelItemClickListener.onItemClick(v, position - COUNT_PRE_MY_HEADER);
                        }
                    }
                });

                myHolder.textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        if (!isEditMode) {
                            RecyclerView recyclerView = ((RecyclerView) parent);
                            startEditMode(recyclerView);

                            // header 按钮文字 改成 "完成"
                            View view = recyclerView.getChildAt(0);
                            if (view == recyclerView.getLayoutManager().findViewByPosition(0)) {
                                TextView tvBtnEdit = (TextView) view.findViewById(R.id.tv_btn_edit);
                                tvBtnEdit.setText(R.string.finish);
                            }
                        }

                        mItemTouchHelper.startDrag(myHolder);
                        return true;
                    }
                });

                myHolder.textView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (isEditMode) {
                            switch (MotionEventCompat.getActionMasked(event)) {
                                case MotionEvent.ACTION_DOWN:
                                    startTime = System.currentTimeMillis();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                        mItemTouchHelper.startDrag(myHolder);
                                    }
                                    break;
                                case MotionEvent.ACTION_CANCEL:
                                case MotionEvent.ACTION_UP:
                                    startTime = 0;
                                    break;
                            }

                        }
                        return false;
                    }
                });
                return myHolder;

            case TYPE_OTHER_CHANNEL_HEADER:
                view = mInflater.inflate(R.layout.item_other_channel_header, parent, false);
                final OtherChannelHeaderViewHolder otherChannelHeaderholder = new OtherChannelHeaderViewHolder(view);
                return otherChannelHeaderholder;

            case TYPE_OTHER:
                view = mInflater.inflate(R.layout.item_other, parent, false);
                final OtherViewHolder otherHolder = new OtherViewHolder(view);
                otherHolder.textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        // 其他频道 长按删除或编辑
                        RecyclerView recyclerView = ((RecyclerView) parent);
                        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                        int currentPiosition = otherHolder.getAdapterPosition();
                        Log.d(TAG,"Long clicked position is :" + currentPiosition);
                        Log.d(TAG,"Long clicked channel is :" + getChannelEntity(currentPiosition).getName());
                        showChannelOptionDialog(getPositionInOtherChannel(currentPiosition));
                        return true; // true 不再处理消息 false 消息继续传递
                    }
                });
                otherHolder.textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecyclerView recyclerView = ((RecyclerView) parent);
                        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                        int currentPiosition = otherHolder.getAdapterPosition();
                        // 如果RecyclerView滑动到底部,移动的目标位置的y轴 - height
                        View currentView = manager.findViewByPosition(currentPiosition);
                        // 目标位置的前一个item  即当前MyChannel的最后一个
                        View preTargetView = manager.findViewByPosition(mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);

                        // 如果targetView不在屏幕内,则为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                        // 如果在屏幕内,则添加一个位移动画
                        if (recyclerView.indexOfChild(preTargetView) >= 0) {
                            int targetX = preTargetView.getLeft();
                            int targetY = preTargetView.getTop();

                            int targetPosition = mMyChannelItems.size() - 1 + COUNT_PRE_OTHER_HEADER;

                            GridLayoutManager gridLayoutManager = ((GridLayoutManager) manager);
                            int spanCount = gridLayoutManager.getSpanCount();
                            // target 在最后一行第一个
                            if ((targetPosition - COUNT_PRE_MY_HEADER) % spanCount == 0) {
                                View targetView = manager.findViewByPosition(targetPosition);
                                targetX = targetView.getLeft();
                                targetY = targetView.getTop();
                            } else {
                                targetX += preTargetView.getWidth();

                                // 最后一个item可见
                                if (gridLayoutManager.findLastVisibleItemPosition() == getItemCount() - 1) {
                                    // 最后的item在最后一行第一个位置
                                    if ((getItemCount() - 1 - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER) % spanCount == 0) {
                                        // RecyclerView实际高度 > 屏幕高度 && RecyclerView实际高度 < 屏幕高度 + item.height
                                        int firstVisiblePostion = gridLayoutManager.findFirstVisibleItemPosition();
                                        if (firstVisiblePostion == 0) {
                                            // FirstCompletelyVisibleItemPosition == 0 即 内容不满一屏幕 , targetY值不需要变化
                                            // // FirstCompletelyVisibleItemPosition != 0 即 内容满一屏幕 并且 可滑动 , targetY值 + firstItem.getTop
                                            if (gridLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                                                int offset = (-recyclerView.getChildAt(0).getTop()) - recyclerView.getPaddingTop();
                                                targetY += offset;
                                            }
                                        } else { // 在这种情况下 并且 RecyclerView高度变化时(即可见第一个item的 position != 0),
                                            // 移动后, targetY值  + 一个item的高度
                                            targetY += preTargetView.getHeight();
                                        }
                                    }
                                } else {
                                    System.out.println("current--No");
                                }
                            }

                            // 如果当前位置是otherChannel可见的最后一个
                            // 并且 当前位置不在grid的第一个位置
                            // 并且 目标位置不在grid的第一个位置

                            // 则 需要延迟250秒 notifyItemMove , 这是因为这种情况 , 并不触发ItemAnimator , 会直接刷新界面
                            // 导致我们的位移动画刚开始,就已经notify完毕,引起不同步问题
                            if (currentPiosition == gridLayoutManager.findLastVisibleItemPosition()
                                    && (currentPiosition - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER) % spanCount != 0
                                    && (targetPosition - COUNT_PRE_MY_HEADER) % spanCount != 0) {
                                moveOtherToMyWithDelay(otherHolder);
                            } else {
                                moveOtherToMy(otherHolder);
                            }
                            startAnimation(recyclerView, currentView, targetX, targetY);

                        } else {
                            moveOtherToMy(otherHolder);
                        }
                    }
                });
                return otherHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

            MyViewHolder myHolder = (MyViewHolder) holder;
            myHolder.textView.setText(mMyChannelItems.get(position - COUNT_PRE_MY_HEADER).getName());
            if (isEditMode) {
                myHolder.imgEdit.setVisibility(View.VISIBLE);
            } else {
                myHolder.imgEdit.setVisibility(View.INVISIBLE);
            }

        } else if (holder instanceof OtherViewHolder) {

            ((OtherViewHolder) holder).textView.setText(mOtherChannelItems.get(position - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER).getName());

        } else if (holder instanceof MyChannelHeaderViewHolder) {

            MyChannelHeaderViewHolder headerHolder = (MyChannelHeaderViewHolder) holder;
            if (isEditMode) {
                headerHolder.tvBtnEdit.setText(R.string.finish);
            } else {
                headerHolder.tvBtnEdit.setText(R.string.edit);
            }
        }
    }

    @Override
    public int getItemCount() {
        // 我的频道  标题 + 我的频道.size + 其他频道 标题 + 其他频道.size
        return mMyChannelItems.size() + mOtherChannelItems.size() + COUNT_PRE_OTHER_HEADER;
    }

    /**
     * 开始增删动画
     */
    private void startAnimation(RecyclerView recyclerView, final View currentView, float targetX, float targetY) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);

        Animation animation = getTranslateAnimator(
                targetX - currentView.getLeft(), targetY - currentView.getTop());
        currentView.setVisibility(View.INVISIBLE);
        mirrorView.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewGroup.removeView(mirrorView);
                if (currentView.getVisibility() == View.INVISIBLE) {
                    currentView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 我的频道 移动到 其他频道
     *
     * @param myHolder
     */
    private void moveMyToOther(MyViewHolder myHolder) {
        int position = myHolder.getAdapterPosition();

        int startPosition = position - COUNT_PRE_MY_HEADER;
        if (startPosition > mMyChannelItems.size() - 1) {
            return;
        }
        ChannelEntity item = mMyChannelItems.get(startPosition);
        mMyChannelItems.remove(startPosition);
        mOtherChannelItems.add(0, item);

        notifyItemMoved(position, mMyChannelItems.size() + COUNT_PRE_OTHER_HEADER);
    }

    /**
     * 其他频道 移动到 我的频道
     *
     * @param otherHolder
     */
    private void moveOtherToMy(OtherViewHolder otherHolder) {
        int position = processItemRemoveAdd(otherHolder);
        if (position == -1) {
            return;
        }
        notifyItemMoved(position, mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
    }

    /**
     * 其他频道 移动到 我的频道 伴随延迟
     *
     * @param otherHolder
     */
    private void moveOtherToMyWithDelay(OtherViewHolder otherHolder) {
        final int position = processItemRemoveAdd(otherHolder);
        if (position == -1) {
            return;
        }
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyItemMoved(position, mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
            }
        }, ANIM_TIME);
    }

    private Handler delayHandler = new Handler();

    private int processItemRemoveAdd(OtherViewHolder otherHolder) {
        int position = otherHolder.getAdapterPosition();

        int startPosition = position - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER;
        if (startPosition > mOtherChannelItems.size() - 1) {
            return -1;
        }
        ChannelEntity item = mOtherChannelItems.get(startPosition);
        mOtherChannelItems.remove(startPosition);
        mMyChannelItems.add(item);
        return position;
    }


    /**
     * 添加需要移动的 镜像View
     */
    private ImageView addMirrorView(ViewGroup parent, RecyclerView recyclerView, View view) {
        /**
         * 我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，然后再调用getDrawingCache方法就可以获得view的cache图片了。
         buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
         若想更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
         当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
         */
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        final ImageView mirrorView = new ImageView(recyclerView.getContext());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        recyclerView.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0);
        parent.addView(mirrorView, params);

        return mirrorView;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        ChannelEntity item = mMyChannelItems.get(fromPosition - COUNT_PRE_MY_HEADER);
        mMyChannelItems.remove(fromPosition - COUNT_PRE_MY_HEADER);
        mMyChannelItems.add(toPosition - COUNT_PRE_MY_HEADER, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * 开启编辑模式
     *
     * @param parent
     */
    private void startEditMode(RecyclerView parent) {
        isEditMode = true;

        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = parent.getChildAt(i);
            ImageView imgEdit = (ImageView) view.findViewById(R.id.img_edit);
            if (imgEdit != null) {
                imgEdit.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 完成编辑模式
     *
     * @param parent
     */
    private void cancelEditMode(RecyclerView parent) {
        isEditMode = false;

        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = parent.getChildAt(i);
            ImageView imgEdit = (ImageView) view.findViewById(R.id.img_edit);
            if (imgEdit != null) {
                imgEdit.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 获取位移动画
     */
    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY);
        // RecyclerView默认移动动画250ms 这里设置360ms 是为了防止在位移动画结束后 remove(view)过早 导致闪烁
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    interface OnMyChannelItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnMyChannelItemClickListener(OnMyChannelItemClickListener listener) {
        this.mChannelItemClickListener = listener;
    }

    /**
     * 我的频道
     */
    class MyViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        private TextView textView;
        private ImageView imgEdit;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
            imgEdit = (ImageView) itemView.findViewById(R.id.img_edit);
        }

        /**
         * item 被选中时
         */
        @Override
        public void onItemSelected() {
            textView.setBackgroundResource(R.drawable.bg_channel_p);
        }

        /**
         * item 取消选中时
         */
        @Override
        public void onItemFinish() {
            textView.setBackgroundResource(R.drawable.bg_channel);
        }
    }

    /**
     * 其他频道
     */
    class OtherViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public OtherViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
        }
    }

    /**
     * 我的频道  标题部分
     */
    class MyChannelHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBtnEdit;

        public MyChannelHeaderViewHolder(View itemView) {
            super(itemView);
            tvBtnEdit = (TextView) itemView.findViewById(R.id.tv_btn_edit);
        }
    }

    /**
     * 其他频道  标题部分
     */
    class OtherChannelHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBtnNew;

        public OtherChannelHeaderViewHolder(View itemView) {
            super(itemView);
            tvBtnNew = (TextView) itemView.findViewById(R.id.tv_btn_new_channel);
            tvBtnNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(get,"增加自定义频道",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"增加自定义频道");
                    showChannelEditDialog(ACTION_NEW,-1);
                }
            });
        }
    }
    /**
     * 其他频道  创建或者编辑一个已有的频道
     */
    private void showChannelEditDialog(final int action, final int position){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(mContext);;
        View view = inflater.inflate(R.layout.dialog_channel_edit, null);
        final EditText channelNameView = (EditText) view.findViewById(R.id.etChannelName);
        final EditText channelKeyWordsView = (EditText) view.findViewById(R.id.etChannelKeyWords);
        String title;
        if (action==ACTION_EDIT){
            title = "编辑";
            channelNameView.setText(mOtherChannelItems.get(position).getName());
            channelKeyWordsView.setText(mOtherChannelItems.get(position).getKeyWords());
        }else if (action==ACTION_NEW){
            title = "新建";
        }else {
            title = "unkown";
        }
        dialogBuilder.setView(view)
                .setTitle(title)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isChannelNameValid(channelNameView.getText().toString())){
                    channelNameView.setError("频道名称必须填写！");
                    return;
                }
                if (!isChannelKeyWordsValid(channelKeyWordsView.getText().toString())){
                    channelKeyWordsView.setError("请输入正确的频道关键词！");
                    return;
                }
                if (action==ACTION_NEW){
                    // 新增加一个频道
                    if (hasChannel(channelNameView.getText().toString())){
                        channelNameView.setError("已有该频道名称,请换一个！");
                        return;
                    }
                    ChannelEntity channelEntity = new ChannelEntity();
                    channelEntity.setName(channelNameView.getText().toString());
                    channelEntity.setId(getUniqueChannelId());
                    channelEntity.setKeyWords(channelKeyWordsView.getText().toString());
                    Log.d(TAG,"Channel id is :" + channelEntity.getId());
                    // 加入到otherchannel中
                    mOtherChannelItems.add(0,channelEntity);
                }
                if (action==ACTION_EDIT){
                    ChannelEntity channelEntity = mOtherChannelItems.get(position);
                    channelEntity.setName(channelNameView.getText().toString());
                    channelEntity.setKeyWords(channelKeyWordsView.getText().toString());
                }
                notifyDataSetChanged();
                dialog.dismiss();
            }

        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
    /**
     * 其他频道  频道关键词是否合格
     */
    private boolean isChannelKeyWordsValid(String s) {
        return !s.isEmpty();
    }
    /**
     * 其他频道  频道名称是否合格
     */
    private boolean isChannelNameValid(String s) {
        return !s.isEmpty();
    }
    /**
     * 其他频道  给新创建频道一个唯一的编号
     * 把id值最大的频道的id + 1 给新频道
     */
    private long getUniqueChannelId(){
        ChannelEntity otherMax = Collections.max(mOtherChannelItems,comparator);
        for(ChannelEntity entity : mOtherChannelItems){
            Log.d(TAG,"Channel id>>>"+ entity.getId() + ">>>Channel Name>>>" + entity.getName());
        }
        Log.d(TAG,"Other channel max id is: " +otherMax.getId()+">>>"+otherMax.getName());
        ChannelEntity myMax = Collections.max(mMyChannelItems,comparator);
        for(ChannelEntity entity : mMyChannelItems){
            Log.d(TAG,"Channel id>>>"+ entity.getId() + ">>>Channel Name>>>" + entity.getName());
        }
        Log.d(TAG,"My channel max id is: " + myMax.getId()+">>>"+ myMax.getName());
        if (otherMax.getId() > myMax.getId()) {
            return otherMax.getId()+1;
        }else {
            return myMax.getId() +1;
        }
    }
    /**
     * 频道集合比较函数 用于频道列表编号最大值
     */
    public static Comparator<ChannelEntity> comparator = new Comparator<ChannelEntity>() {

        @Override
        public int compare(ChannelEntity a1, ChannelEntity a2) {

            return a1.getId()> a2.getId()? 1 : -1;
        }


    };
    /**
     * 根据当前recyclerview的位置信息计算得到频道对象
     * 减去一个我的频道头，减去我的频道数量，减去其他频道的频道头得到当前点击的频道对象
     */
    private ChannelEntity getChannelEntity(int position){
        return mOtherChannelItems.get(position-mMyChannelItems.size()-1-1);
    }
    /**
     * 根据当前recyclerview的位置信息计算得到频道对象在其他频道中的位置
     * position表示在recyclerview中的位置
     */
    private int getPositionInOtherChannel(int position){
        return position-mMyChannelItems.size()-1-1;
    }
    /**
     * position表示在otherChannel中的位置
     */
    private void showChannelOptionDialog(final int position){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(mContext);;
        View view = inflater.inflate(R.layout.dialog_channel_option, null);
        final TextView editOptionView = (TextView) view.findViewById(R.id.tvChannelEdit);
        final TextView deleteOptionView = (TextView) view.findViewById(R.id.tvChannelDelete);
        dialogBuilder.setView(view);
        final AlertDialog dialog = dialogBuilder.create();
        editOptionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 编辑当前频道
                showChannelEditDialog(ACTION_EDIT,position);
                dialog.dismiss();
            }
        });
        deleteOptionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 删除当前频道
                deleteChannel(position);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void deleteChannel(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder
                .setTitle("删除频道")
                .setMessage("确认删除当前频道？")
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOtherChannelItems.remove(position);
                notifyDataSetChanged();
                dialog.dismiss();
            }

        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
    /**
     * 判断频道名称是否已经存在
     */
    private boolean hasChannel(String name){
        for (ChannelEntity channelEntity : mMyChannelItems){
            if (channelEntity.getName().equals(name)) return true;
        }
        for (ChannelEntity channelEntity : mOtherChannelItems){
            if (channelEntity.getName().equals(name)) return true;
        }
        return false;
    }
}
