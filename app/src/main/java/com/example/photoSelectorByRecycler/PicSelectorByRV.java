package com.example.photoSelectorByRecycler;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piccut.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * https://developer.android.com/jetpack/androidx/releases/recyclerview?hl=zh-cn
 * https://blog.csdn.net/qq_35605213/article/details/80541461
 **/
public class PicSelectorByRV extends Activity {
    private RecyclerView mRv;
    private List<DataItem> datas;
    private RecyclerViewAdapterGrid ap;

    class DataItem {
        String mName = "";
        String mPicUri = ""; //todo 还没用
        boolean mIsChecked = false;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datas = new ArrayList<>();
//        直接用d操作集合会崩溃，Arrays.asList集合不可增删改；详细可以看我的博客
        for (int i = 0; i < 12; i++) {
            DataItem dataItem = new DataItem();
            dataItem.mName = i + "";
            datas.add(dataItem);
        }
        setContentView(R.layout.recycler_view_grid_demo);
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new GridLayoutManager(this, 3));
        ap = new RecyclerViewAdapterGrid(this, datas);
        mRv.setAdapter(ap);
        helper.attachToRecyclerView(mRv);
    }

    /**拖动器**/
    private ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFrlg = 0;
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                dragFrlg = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                dragFrlg = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            return makeMovementFlags(dragFrlg, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            //得到当拖拽的viewHolder的Position
            int fromPosition = viewHolder.getAdapterPosition();
            //拿到当前拖拽到的item的viewHolder
            int toPosition = target.getAdapterPosition();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(datas, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(datas, i, i - 1);
                }
            }
            ap.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        /**
         * 长按选中Item的时候开始调用
         * 长按高亮
         * @param viewHolder
         * @param actionState
         */
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                selectedStyle(viewHolder);
                //获取系统震动服务//震动70毫秒
                Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                vib.vibrate(70);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        /**
         * 手指松开的时候还原高亮
         * @param recyclerView
         * @param viewHolder
         */
        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            unSelectedStyle(viewHolder);
            ap.notifyDataSetChanged();  //完成拖动后刷新适配器，这样拖动后删除就不会错乱
        }

        /**拖动时的样式**/
        private void selectedStyle(RecyclerView.ViewHolder viewHolder) {
            viewHolder.itemView.setScaleX(1.2f);
            viewHolder.itemView.setScaleY(1.2f);
            viewHolder.itemView.setAlpha(0.8f);
        }

        /**拖动完后恢复原来的样式**/
        private void unSelectedStyle(RecyclerView.ViewHolder viewHolder) {
            viewHolder.itemView.setScaleX(1f);
            viewHolder.itemView.setScaleY(1f);
            viewHolder.itemView.setAlpha(1f);
        }
    });


    class MyViewHolder extends RecyclerView.ViewHolder {
        private DataItem mDataItem = null;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_selector_pic_index);
            iv = itemView.findViewById(R.id.iv_selector_pic_content);
            cb = itemView.findViewById(R.id.cb_set_selected);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mDataItem.mIsChecked = isChecked;
                    tv.setBackgroundColor(mDataItem.mIsChecked ? 0xFF4040FF : 0xFFAAAAAA);
                }
            });
        }

        public TextView tv;
        public ImageView iv;
        public CheckBox cb;

        public void setDataItem(DataItem dataItem) {
            this.mDataItem = dataItem;
        }
    }

    class RecyclerViewAdapterGrid<T> extends RecyclerView.Adapter<MyViewHolder> {
        private Context context;
        public List<T> stringList;

        public RecyclerViewAdapterGrid(Context context, List<T> stringList) {
            this.context = context;
            this.stringList = stringList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            DataItem dataItem = (DataItem) stringList.get(position);
            holder.tv.setText(dataItem.mName);
            holder.tv.setBackgroundColor(dataItem.mIsChecked ? 0xFF4040FF : 0xFFAAAAAA);
            holder.cb.setSelected(dataItem.mIsChecked);
            holder.setDataItem(dataItem);
        }

        @Override
        public int getItemCount() {
            return stringList.size();
        }
    }
}
