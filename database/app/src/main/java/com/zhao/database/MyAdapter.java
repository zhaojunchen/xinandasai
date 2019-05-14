package com.zhao.database;

import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.widget.Toast.makeText;
import static com.zhao.database.MyApplication.getContext;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<DataSource> datalist;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView row_id;
        public TextView row_note;
        public TextView row_wxid;
        public TextView row_key;
        public CheckBox row_isEnc;
        public CheckBox row_isDec;

        public MyViewHolder(View view) {
            super(view);
            row_id = (TextView) view.findViewById(R.id.row_id);
            row_wxid = (TextView) view.findViewById(R.id.row_wxid);
            row_note = (TextView) view.findViewById(R.id.row_note);
            row_key = (TextView) view.findViewById(R.id.row_key);
            row_isEnc = (CheckBox) view.findViewById(R.id.row_isEnc);
            row_isDec = (CheckBox) view.findViewById(R.id.row_isDec);

        }
    }

    public MyAdapter(List<DataSource> mylist) {
        this.datalist = mylist;
    }

    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        final MyViewHolder holder = new MyViewHolder(view);


        holder.row_isDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**设置点击时间  点击后这个这个状态就会变换
                 * 状态先变换 然后执行事件*/
                if (holder.row_isDec.isChecked() == true) {
                    /** 获取全局context*/
                    makeText(getContext(), "取消勾选:" + String.valueOf(holder.row_isDec.isChecked()), Toast.LENGTH_LONG).show();

                } else {
                    makeText(getContext(), "此时的ID是" + String.valueOf(holder.row_id), Toast.LENGTH_LONG).show();
                    makeText(getContext(), "勾选:" + String.valueOf(holder.row_isDec.isChecked() + "此时的ID是" + String.valueOf(holder.row_id.getText())), Toast.LENGTH_LONG).show();

                }

            }
        });


        holder.row_isEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**设置点击时间  点击后这个这个状态就会变换
                 * 状态先变换 然后执行事件*/
                if (holder.row_isEnc.isChecked() == true) {
                    /** 获取全局context*/
                    makeText(getContext(), "取消勾选:" + String.valueOf(holder.row_isDec.isChecked()), Toast.LENGTH_LONG).show();
                    /**数据库修改*/

                } else {
                    makeText(getContext(), "此时的ID是" + String.valueOf(holder.row_id), Toast.LENGTH_LONG).show();
                    makeText(getContext(), "勾选:" + String.valueOf(holder.row_isDec.isChecked() + "此时的ID是" + String.valueOf(holder.row_id.getText())), Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.row_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** 获取list的对象位置*/
                int position = holder.getAdapterPosition();
                DataSource dataSource = datalist.get(position);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataSource tableInfo = datalist.get(position);

        /**千万注意类型转化的不一致导致的错误
         * id本身为int类型直接转化为setText()
         * 马上就会导致崩溃*/
        holder.row_id.setText(String.valueOf(tableInfo.getId()));
        holder.row_note.setText(tableInfo.getNote());
        holder.row_wxid.setText(tableInfo.getWxid());
        holder.row_key.setText(tableInfo.getWxid());
        holder.row_isDec.setChecked(tableInfo.isDec());
        holder.row_isEnc.setChecked(tableInfo.isEnc());
    }


    @Override
    public int getItemCount() {
        if (datalist == null) {
            Log.e("fuck", "fuck");
            return -1;
        }
        return datalist.size();
    }
}



