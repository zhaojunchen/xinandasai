package com.zhao.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {
    private static final String TAG = "SecondActivity";
    private List<User> datalist = new ArrayList<>();
    MyDatabaseHelp dbhelper = new MyDatabaseHelp(this, "MyDb", null, MyApplication.getDbversion());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initdata();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        MyAdapter adapter = new MyAdapter(datalist);
        recyclerView.setAdapter(adapter);

    }

    private void initdata() {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String CURRENT_USER_ID = String.valueOf(MyApplication.getCurrentUserId());
        db.beginTransaction();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select id,wxid,note,isEnc,isDec,BELONG from FriendTable where BELONG = ? order by note;", new String[]{CURRENT_USER_ID});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "initdata: 查询失败");
            e.printStackTrace();
            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
            return;
        }finally {
            db.endTransaction();
        }

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                User user = new User();
                int id = cursor.getInt(cursor.getColumnIndex("id" ));
                String wxid = cursor.getString(cursor.getColumnIndex("wxid" ));
                String note = cursor.getString(cursor.getColumnIndex("note" ));
                boolean isEnc = (cursor.getInt(cursor.getColumnIndex("isEnc" ))) == 0 ? false : true;
                boolean isDec = (cursor.getInt(cursor.getColumnIndex("isDec" ))) == 0 ? false : true;
                int BELONG = cursor.getInt(cursor.getColumnIndex("BELONG" ));
                user.setId(id);
                user.setId(id);
                user.setWxid(wxid);
                user.setNote(note);
                user.setEnc(isEnc);
                user.setDec(isDec);
                user.setBELONG(BELONG);
                datalist.add(user);
            } while (cursor.moveToNext());
        }
        /** 关闭游标*/
        cursor.close();
    }

}
