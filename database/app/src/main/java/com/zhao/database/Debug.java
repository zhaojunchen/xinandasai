package com.zhao.database;


import de.robv.android.xposed.XposedBridge;


public class Debug {
    public static String TAG = "db";

    public static void e() {
        XposedBridge.log("break");
    }

    public static void e(String info) {
        XposedBridge.log(info);
    }

}
