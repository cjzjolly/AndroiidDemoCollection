package com.example.singleChoiceRecyclerView;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.photoSelectorByRecycler.PicSelectorByRV;
import com.example.piccut.R;

/**UI条目**/
class MyViewHolder extends RecyclerView.ViewHolder {
    public DataItem mDataItem = null;
    public TextView tv;
    public TextView multiChoiceIndex;
    public ImageView iv;
    public CheckBox cb;

    public MyViewHolder(View itemView) {
        super(itemView);
        tv = itemView.findViewById(R.id.tv_selector_pic_index);
        multiChoiceIndex = itemView.findViewById(R.id.tv_selector_multi_choice);
        iv = itemView.findViewById(R.id.iv_selector_pic_content);
        cb = itemView.findViewById(R.id.cb_set_selected);
    }


    public void setDataItem(DataItem dataItem) {
        this.mDataItem = dataItem;
    }
}
