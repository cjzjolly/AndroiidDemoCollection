package com.example.singleChoiceRecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.piccut.R;

import java.util.ArrayList;
import java.util.List;

public class SingleOrMultiChoiceRecyclerActivity extends Activity {
    private RecyclerView mRv;
    private CheckBox mCBModeSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_single_or_multi_select_demo);
        List<DataItem> labelTest = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DataItem dataItem = new DataItem();
            dataItem.mName = "" + i;
            labelTest.add(dataItem);
        }
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new GridLayoutManager(this, 2));
        RecyclerViewAdapterGrid adapterGrid = new RecyclerViewAdapterGrid(this, labelTest);
        mRv.setAdapter(adapterGrid);
        mCBModeSwitch = findViewById(R.id.cb_multi_choice_mode);
        mCBModeSwitch.setOnCheckedChangeListener((but, isChecked) -> {
            adapterGrid.setIsMultiChoiceMode(isChecked);
        });
    }
}
