package com.zhao.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;

public class Database {
    private static final String TAG = "Xposed";

    public static void show(SQLiteDatabase db) {

        Log.i(TAG, "\n打印数据库的所有表\n");
        Log.i(TAG, "\n打印用户表\n");


        Cursor cursor = db.rawQuery("select * from HOSTS;", null);
        if (cursor.moveToNext()) {

            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String WXID = cursor.getString(cursor.getColumnIndex("WXID"));
                String private_key = cursor.getString(cursor.getColumnIndex("private_key"));
                String public_key = cursor.getString(cursor.getColumnIndex("public_key"));
                XposedBridge.log("ID:" + String.valueOf(id) + "\n微信ID" + WXID + "\n私钥" + private_key + "\n公钥" + public_key);
            } while (cursor.moveToNext());

        } else {
            XposedBridge.log("\n用户表为空\n");
        }


        /** 输出*/
        Log.i(TAG, "\n打印好友列表\n");
        cursor = db.rawQuery("select * from FriendTable;", null);
        if (cursor.moveToNext()) {

            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String wxid = cursor.getString(cursor.getColumnIndex("wxid"));
                String note = cursor.getString(cursor.getColumnIndex("note"));
                String public_key = cursor.getString(cursor.getColumnIndex("public_key"));
                int isEnc = cursor.getInt(cursor.getColumnIndex("isEnc"));
                int isDec = cursor.getInt(cursor.getColumnIndex("isDec"));
                int BELONG = cursor.getInt(cursor.getColumnIndex("BELONG"));
                XposedBridge.log("ID:" + String.valueOf(id) + "\n微信ID" + wxid + "\n对称公钥" + public_key + "\n加密：" + String.valueOf(isEnc) + "\n解密" + String.valueOf(isDec) + "\n属组" + String.valueOf(BELONG));
            } while (cursor.moveToNext());
        } else {
            XposedBridge.log("\n好友表为空\n");

        }


        Log.i(TAG, "打印公钥表");
        cursor = db.rawQuery("select * from linktable;", null);
        if (cursor.moveToNext()) {
            do {
                XposedBridge.log("wxid" + cursor.getString(0));
                XposedBridge.log("public_key" + cursor.getString(1));
                XposedBridge.log("属组" + cursor.getInt(2));

            } while (cursor.moveToNext());
        } else {
            XposedBridge.log("\n公钥表为空\n");
        }

        if (cursor != null) {
            cursor.close();
        }
        
    }
}
