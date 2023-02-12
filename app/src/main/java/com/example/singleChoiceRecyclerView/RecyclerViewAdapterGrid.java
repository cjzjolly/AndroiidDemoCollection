package com.example.singleChoiceRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoSelectorByRecycler.PicSelectorByRV;
import com.example.piccut.R;

import java.util.List;

/**表格的适配器**/
class RecyclerViewAdapterGrid<T> extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    public List<T> mDataList;

    public RecyclerViewAdapterGrid(Context context, List<T> stringList) {
        this.context = context;
        this.mDataList = stringList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder myViewHolder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item, parent, false));
        myViewHolder.cb.setOnClickListener(view -> {

        });
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //数据数据表的实际情况处理UI，并把UI条目和数据表条目进行一对一绑定
        DataItem dataItem = (DataItem) mDataList.get(position);
        holder.tv.setText(dataItem.mName);
        holder.tv.setBackgroundColor(dataItem.mIsChecked ? 0xFF4040FF : 0xFFAAAAAA);
        holder.cb.setSelected(dataItem.mIsChecked);
        holder.setDataItem(dataItem);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
