package com.wechat.annoywechat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * link:https://github.com/amitshekhariitbhu/Android-Debug-Database
 * adb forward tcp:8080 tcp:8080
 * localhost:8080
 */

public class MyDatabaseHelp extends SQLiteOpenHelper {
    private static final String TAG = "MyDatabaseHelp";
    /**
     * 注意这个数据库的名字  一个微用户使用一个信息表
     */
    private static String KEY_TABLE = "CREATE TABLE IF NOT EXISTS" +
            " HOSTS(id INTEGER PRIMARY KEY AUTOINCREMENT,WXID TEXT,NICKNAME TEXT,private_key " +
            "TEXT,public_key TEXT);";

    private static String FRIEND_TABEL = "CREATE TABLE IF NOT EXISTS" +
            " FriendTable(wxid TEXT,id INTEGER PRIMARY KEY" +
            " , note TEXT, public_key TEXT, isEnc INTEGER, " +
            "isDec INTEGER, BELONG INTEGER);";

    private static String INDEX = "CREATE INDEX index_name on FriendTable (wxid);";

    private static String link_table = "CREATE TABLE IF NOT EXISTS linktable(" +
            "wxid TEXT ,public_key TEXT,belong INTEGER,primary key(wxid,belong))";


    private Context mcontext;

    public MyDatabaseHelp(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context, name, cursorFactory, version);
        mcontext = context;
    }

    /**
     * 初始化创建数据表
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            db.execSQL(KEY_TABLE);
            db.execSQL(FRIEND_TABEL);
            db.execSQL(INDEX);
            db.execSQL(link_table);

            db.setTransactionSuccessful();
            /** 调试*/
            Log.d(TAG, "onCreate: 数据库版本号" + String.valueOf(MyApplication.getDbversion()));
            Toast.makeText(mcontext, "数据库创建成功", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onCreate:初始化建立数据库出错");
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /**
         * 更新数据库->输入新的版本号
         /** drop table if exists BOOk;*/
        /** 删除这个表*/
        db.beginTransaction();
        try {
            db.execSQL("drop table HOSTS;");
            db.execSQL("drop table FriendTable;");
            db.execSQL("drop table linktable");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        onCreate(db);
    }
}
