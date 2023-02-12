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
    private DataItem mDataItem = null;
    public TextView tv;
    public ImageView iv;
    public CheckBox cb;

    public MyViewHolder(View itemView) {
        super(itemView);
        tv = itemView.findViewById(R.id.tv_selector_pic_index);
        iv = itemView.findViewById(R.id.iv_selector_pic_content);
        cb = itemView.findViewById(R.id.cb_set_selected);
        /**点击选择时修改对应UI条目的样式，并更改数据表对应条目**/
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDataItem.mIsChecked = isChecked;
                tv.setBackgroundColor(mDataItem.mIsChecked ? 0xFF4040FF : 0xFFAAAAAA);
            }
        });
    }


    public void setDataItem(DataItem dataItem) {
        this.mDataItem = dataItem;
    }
}
