package com.example.sidingMenuRecyclerView;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.piccut.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SlideRecyclerView测试
 * <p>
 * Created by DavidChen on 2018/6/1.
 */

public class SlideRecyclerViewActivity extends AppCompatActivity {

    private SlideRecyclerView recycler_view_list;
    private List<Inventory> mInventories;
    private InventoryAdapter mInventoryAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);
        recycler_view_list = (SlideRecyclerView) findViewById(R.id.recycler_view_list);
        recycler_view_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_inset));
        recycler_view_list.addItemDecoration(itemDecoration);
        mInventories = new ArrayList<>();
        Inventory inventory;
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            inventory = new Inventory();
            inventory.setItemDesc("测试数据" + i);
            inventory.setQuantity(random.nextInt(100000));
            inventory.setItemCode("0120816");
            inventory.setDate("20180219");
            inventory.setVolume(random.nextFloat());
            mInventories.add(inventory);
        }
        mInventoryAdapter = new InventoryAdapter(this, mInventories);
        recycler_view_list.setAdapter(mInventoryAdapter);
        mInventoryAdapter.setOnDeleteClickListener(new InventoryAdapter.OnDeleteClickLister() {
            @Override
            public void onDeleteClick(View view, int position) {
                mInventories.remove(position);
                mInventoryAdapter.notifyDataSetChanged();
                recycler_view_list.closeMenu();
            }
        });
    }
}