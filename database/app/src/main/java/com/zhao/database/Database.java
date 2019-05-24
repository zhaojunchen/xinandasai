package com.zhao.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;

public class Database {
    private static final String TAG = "Xposed";
    public static void show(SQLiteDatabase db) {

        Log.i(TAG, "打印数据库的所有表");

        Cursor cursor = db.rawQuery("select * from HOSTS;", null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();

            do {
                XposedBridge.log("");
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String WXID = cursor.getString(cursor.getColumnIndex("WXID"));
                String private_key = cursor.getString(cursor.getColumnIndex("private_key"));
                String public_key = cursor.getString(cursor.getColumnIndex("public_key"));
                XposedBridge.log("ID:" + String.valueOf(id) + "  微信ID" + WXID + "  私钥" + private_key + "  公钥" + public_key);
            } while (cursor.moveToNext());

        } else {
            XposedBridge.log("\n--------------\n数据库出现致命错误\n-------------\n");

        }

        /** 输出*/
        cursor = db.rawQuery("select * from FriendTable;", null);
        if (cursor.getCount() != 0) {

            cursor.moveToFirst();
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String wxid = cursor.getString(cursor.getColumnIndex("wxid"));
                String note = cursor.getString(cursor.getColumnIndex("note"));
                String public_key = cursor.getString(cursor.getColumnIndex("public_key"));
                int isEnc = cursor.getInt(cursor.getColumnIndex("isEnc"));
                int isDec = cursor.getInt(cursor.getColumnIndex("isDec"));
                int BELONG = cursor.getInt(cursor.getColumnIndex("BELONG"));
                XposedBridge.log("ID:" + String.valueOf(id) + "  微信ID" + wxid + "对称公钥" + public_key + "加密：" + isEnc + " " + isDec + " " + BELONG);

            } while (cursor.moveToNext());
        } else {
            XposedBridge.log("\n--------------\n数据库出现致命错误\n-------------\n");

        }


    }
}
