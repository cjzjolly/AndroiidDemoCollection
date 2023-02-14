package com.example.singleChoiceRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoSelectorByRecycler.PicSelectorByRV;
import com.example.piccut.R;

import java.util.ArrayList;
import java.util.List;

/**表格的适配器**/
class RecyclerViewAdapterGrid<T> extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    public List<T> mDataList;
    private List<T> mBeSelectedList = new ArrayList<>();
    private List<MyViewHolder> mViewHolders = new ArrayList<>();

    /**是否多选模式**/
    private boolean mIsMultiChoiceMode = false;

    public RecyclerViewAdapterGrid(Context context, List<T> stringList) {
        this.context = context;
        this.mDataList = stringList;
    }

    /**设置是否启用多选模式
     * @param isMultiChoiceMode 是否多选**/
    public void setIsMultiChoiceMode(boolean isMultiChoiceMode) {
        this.mIsMultiChoiceMode = isMultiChoiceMode;
        if (isMultiChoiceMode == mIsMultiChoiceMode) { //状态一致，不执行清理
            return;
        }
        //状态切换前，进行一次数据清理
        //清理单选状态:
        for (T item : mDataList) {
            ((DataItem) item).mIsChecked = false;
        }
        for (MyViewHolder holder : mViewHolders) {
            holder.cb.setChecked(false);
            holder.multiChoiceIndex.setText("");
        }
        mBeSelectedList.clear(); //清理被多选的数据
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder myViewHolder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item_ver2, parent, false));
        mViewHolders.add(myViewHolder);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //数据数据表的实际情况处理UI，并把UI条目和数据表条目进行一对一绑定
        if (!mIsMultiChoiceMode) { //单选模式的逻辑
            DataItem dataItem = (DataItem) mDataList.get(position);
            holder.tv.setText(dataItem.mName);
            holder.tv.setBackgroundColor(dataItem.mIsChecked ? 0xFF4040FF : 0xFFAAAAAA);
            holder.setDataItem(dataItem);
            holder.cb.setChecked(dataItem.mIsChecked);
            holder.cb.setVisibility(View.VISIBLE);
            holder.multiChoiceIndex.setVisibility(View.GONE);

            /**点击选择时修改对应UI条目的样式，并更改数据表对应条目**/
            holder.cb.setOnClickListener(v -> {
                for (T item : mDataList) { //粗暴：除本条所有数据以外的条目全部设置为未选中状态
                    if (item == holder.mDataItem) {
                        continue;
                    }
                    ((DataItem) item).mIsChecked = false;
                }
                for (MyViewHolder h : mViewHolders) { //粗暴：除本条holder以外，全部设置未非选中的样式。
                    h.cb.setChecked(false);
                    h.tv.setBackgroundColor(0xFFAAAAAA);
                }
                holder.mDataItem.mIsChecked = !holder.mDataItem.mIsChecked;
                holder.cb.setChecked(holder.mDataItem.mIsChecked);
                holder.tv.setBackgroundColor(holder.mDataItem.mIsChecked ? 0xFF4040FF : 0xFFAAAAAA);

            });
        } else { //多选模式的逻辑
            DataItem dataItem = (DataItem) mDataList.get(position);
            holder.tv.setText(dataItem.mName);
            holder.setDataItem(dataItem);
            holder.cb.setVisibility(View.GONE);
            holder.multiChoiceIndex.setVisibility(View.VISIBLE);
            //把它在可选表中的位置提取出来应用:
            int pos = mBeSelectedList.indexOf(dataItem);
            if (pos >= 0) {
                holder.multiChoiceIndex.setText(String.valueOf(pos + 1));
            } else{
                holder.multiChoiceIndex.setText("");
            }
            //点击多选按钮时添加条目到已选表，如果本身就在已选表，则从表中remove自己
            holder.multiChoiceIndex.setOnClickListener(v -> {
                int index = mBeSelectedList.indexOf(holder.mDataItem);
                if (index >= 0) {
                    mBeSelectedList.remove(holder.mDataItem);
                    holder.multiChoiceIndex.setText("");
                } else {
                    mBeSelectedList.add((T) holder.mDataItem);
                    holder.multiChoiceIndex.setText("" + mBeSelectedList.size());
                }
                //全部可见条目刷新一次序号
                for (MyViewHolder h : mViewHolders) { //粗暴：除本条holder以外，全部设置未非选中的样式。
                    int i = mBeSelectedList.indexOf(h.mDataItem);
                    if (i >= 0) {
                        h.multiChoiceIndex.setText(String.valueOf(i + 1));
                    } else{
                        h.multiChoiceIndex.setText("");
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


}
