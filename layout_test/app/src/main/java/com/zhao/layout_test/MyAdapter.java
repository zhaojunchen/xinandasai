package com.zhao.layout_test;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<TableInfo> mylist;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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
    public MyAdapter(List<TableInfo> mylist) {
        mylist = mylist;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_table, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TableInfo tableInfo = mylist.get(position);
        holder.row_id.setText(tableInfo.getId());
        holder.row_note.setText(tableInfo.getNote());
        holder.row_wxid.setText(tableInfo.getWxid());
        holder.row_key.setText(tableInfo.getWxid());
        holder.row_isDec.setChecked(tableInfo.isDec());
        holder.row_isEnc.setChecked(tableInfo.isEnc());
    }

    @Override
    public int getItemCount() {
        if (mylist == null) {
            Log.e("fuck", "fuck");
            return -1;
        }
        return mylist.size();
    }
}