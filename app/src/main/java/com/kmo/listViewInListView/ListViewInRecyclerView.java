package com.kmo.listViewInListView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoSelectorByRecycler.PicSelectorByRV;
import com.example.piccut.R;

import java.util.ArrayList;
import java.util.List;

public class ListViewInRecyclerView extends Activity {
    class DataItem {
        String mName = "";
        List<DataItem> mChilds = new ArrayList<>();
    }

    /**UI条目**/
    class MyViewHolder extends RecyclerView.ViewHolder {
        private DataItem mDataItem = null;
        private ListView mLv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mLv = itemView.findViewById(R.id.lv_outline_item);
        }

        public ListView getListView() {
            return mLv;
        }

        public void setDataItem(DataItem dataItem) {
            this.mDataItem = dataItem;
        }

    }

    /**RecyclerView的适配器**/
    class RecyclerViewAdapterGrid<T> extends RecyclerView.Adapter<MyViewHolder> {
        private Context context;
        public List<T> mDataList;

        public RecyclerViewAdapterGrid(Context context, List<T> dataList) {
            this.context = context;
            this.mDataList = dataList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_in_rv_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            //数据数据表的实际情况处理UI，并把UI条目和数据表条目进行一对一绑定
            DataItem dataItem = (DataItem) mDataList.get(position);
            //todo 把对应章节的数据填充到ListView中
            ListView listView = holder.getListView();
            ItemAdapter itemAdapter = new ItemAdapter(ListViewInRecyclerView.this, R.layout.rv_item, mDatas);
            listView.setAdapter(itemAdapter);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }
    }

    /**todo RV内部ListView的适配器**/
    class ItemAdapter extends ArrayAdapter<DataItem> {
        private int mLayoutID;

        public ItemAdapter(@NonNull Context context, int layoutID, @NonNull List<DataItem> objects) {
            super(context, layoutID, objects);
            this.mLayoutID = layoutID;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View rootView = LayoutInflater.from(getContext()).inflate(mLayoutID, parent, false);
            return rootView;
        }
    }

    private List<DataItem> mDatas;
    private RecyclerView mRv;
    private RecyclerViewAdapterGrid ap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatas = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            DataItem dataItem = new DataItem();
            dataItem.mName = i + "";
            for (int j = 0; j < 3; j++) {
                DataItem d = new DataItem();
                d.mName = j + "";
                dataItem.mChilds.add(d);
            }
            mDatas.add(dataItem);
        }
        setContentView(R.layout.recycler_view_grid_demo_2);
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        ap = new RecyclerViewAdapterGrid(this, mDatas);
        mRv.setAdapter(ap);
    }
}
