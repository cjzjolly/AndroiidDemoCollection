package com.example.singleChoiceRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
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
    private List<MyViewHolder> mViewHolders = new ArrayList<>();

    public RecyclerViewAdapterGrid(Context context, List<T> stringList) {
        this.context = context;
        this.mDataList = stringList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder myViewHolder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item, parent, false));
        mViewHolders.add(myViewHolder);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //数据数据表的实际情况处理UI，并把UI条目和数据表条目进行一对一绑定
        DataItem dataItem = (DataItem) mDataList.get(position);
        holder.tv.setText(dataItem.mName);
        holder.tv.setBackgroundColor(dataItem.mIsChecked ? 0xFF4040FF : 0xFFAAAAAA);
        holder.setDataItem(dataItem);
        holder.cb.setChecked(dataItem.mIsChecked);

        /**点击选择时修改对应UI条目的样式，并更改数据表对应条目**/
        holder.cb.setOnClickListener(v -> {
            for (T item : mDataList) { //粗暴：除本条所有数据以外的条目全部设置为未选中状态
                if (item == holder) {
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
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


}
