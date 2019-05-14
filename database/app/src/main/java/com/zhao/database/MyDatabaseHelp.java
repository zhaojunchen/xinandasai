package com.zhao.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelp extends SQLiteOpenHelper {
    /**
     * 注意这个数据库的名字  一个微用户使用一个信息表
     */
    public static String mname;
    public static String CREATE_DATABASE = "create table " + mname + "(" +
            "id INTEGER primary key autoincrement," +
            "note TEXT," +
            "wxid TEXT," +
            "Mkey  TEXT," +
            "publicKey TEXT," +
            "isEnc INTEGER," +
            "isDec INTEGER);";

    private Context mcontext;

    public MyDatabaseHelp(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context, name, cursorFactory, version);
        mcontext = context;
        mname = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DATABASE);
        Toast.makeText(mcontext, "数据库创建成功", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /**
         * 更新数据库->输入新的版本号
         * 执行程序dbHelper.getWruteableDatabase()*/
        db.execSQL("drop table if exists " + mname + ";");
        onCreate(db);


    }
}
