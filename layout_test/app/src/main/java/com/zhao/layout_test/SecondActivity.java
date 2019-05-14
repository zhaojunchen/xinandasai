package com.zhao.layout_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {
    /**
     * 资源
     */
    private static final String TAG = "SecondActivity";
    private List<TableInfo> mylist = new ArrayList<>();
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_item_table_info);
        /** 数据库资源初始化*/
        for (int i = 0; i < 100; i++) {
            Log.e(TAG, "wuhan");
        }
        inittableinfo();
//        recyclerView = (RecyclerView) findViewById(R.id.recycleview);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(linearLayoutManager);
//
//        myAdapter = new MyAdapter(mylist);
//        recyclerView.setAdapter(myAdapter);

    }

    private void inittableinfo() {
        int id = 1;
        String wxid = "wxid";
        String note = "备注";
        String key = "key";
        boolean ischeck = true;
        for (int i = 0; i < 100; i++) {
            Log.e(TAG, "$i");
            TableInfo tableInfo = new TableInfo(i, wxid, note, key, ischeck, ischeck);
            this.mylist.add(tableInfo);
        }
    }
}
