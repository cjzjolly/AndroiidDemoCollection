package com.example.photoSelectorByRecycler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piccut.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://developer.android.com/jetpack/androidx/releases/recyclerview?hl=zh-cn
 * https://blog.csdn.net/qq_35605213/article/details/80541461
 * **/
public class MainActivity extends Activity {
    private RecyclerView mRv;
    private List<String> d = Arrays.asList(
            "A","B","C","D","E","F","G"
            ,"H","I","J","K","L","M","N"
            ,"O","P","Q","R","S","T"
            ,"U","V","W","X","Y","Z");


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String>  datas = new ArrayList<>();
//        直接用d操作集合会崩溃，Arrays.asList集合不可增删改；详细可以看我的博客
        for (int i = 0; i < d.size(); i++) {
            datas.add(d.get(i));
        }
        setContentView(R.layout.recycler_view_grid_demo);
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new GridLayoutManager(this, 3));
        mRv.setAdapter(new RecyclerViewAdapterGrid(this, datas));
        helper.attachToRecyclerView(mRv);
    }

    private ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return 0;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    });


    class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
                tv = itemView.findViewById(R.id.tv);
                iv = itemView.findViewById(R.id.iv_delete);
        }

        public TextView tv;
        public ImageView iv;
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
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item, null));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.tv.setText(stringList.get(position).toString());
        }

        @Override
        public int getItemCount() {
            return stringList.size();
        }
    }
}
