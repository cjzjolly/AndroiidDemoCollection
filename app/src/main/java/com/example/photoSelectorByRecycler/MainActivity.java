package com.example.photoSelectorByRecycler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piccut.R;

import java.util.List;

/**
 * https://developer.android.com/jetpack/androidx/releases/recyclerview?hl=zh-cn
 * https://blog.csdn.net/qq_35605213/article/details/80541461
 * **/
public class MainActivity extends Activity {
    private RecyclerView mRv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_grid_demo);
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new GridLayoutManager(this, 3));

    }

    class RecyclerViewAdapterGrid<T> extends RecyclerView.Adapter<RecyclerViewAdapterGrid.Vh> {

        public RecyclerViewAdapterGrid(Context context, List<T> stringList) {
            this.context = context;
            this.stringList = stringList;
        }

        @NonNull
        @Override
        public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public void onBindViewHolder(@NonNull Vh holder, int position) {
            Log.i("asd", "asd");
        }

        class Vh extends RecyclerView.ViewHolder {

            public Vh(View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tv);
                iv = itemView.findViewById(R.id.iv_delete);
            }
            public TextView tv;
            public ImageView iv;
        }
    }
}
