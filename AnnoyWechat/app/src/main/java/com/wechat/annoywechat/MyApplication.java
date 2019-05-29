package com.wechat.annoywechat;

import android.app.Application;
import android.content.Context;


public class MyApplication extends Application {
    private static Context context;
    /**
     * 当前用户的wxid身份
     */
    private static String CURRENT_USER = null;
    /**
     * 当前用户的ID号
     */

    private static String CURRENT_NICK_NAME;
    private static int CURRENT_USER_ID = -1;
    private static int dbversion = 15;

    public static int getDbversion() {
        return dbversion;

    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        /** 解决Aplication冲突的问题*/


    }

    public static String getCurrentNickName() {
        return CURRENT_NICK_NAME;
    }

    public static void setCurrentNickName(String currentNickName) {
        CURRENT_NICK_NAME = currentNickName;
    }

    public static String getCurrentUser() {
        return CURRENT_USER;
    }

    public static void setCurrentUser(String currentUser) {
        CURRENT_USER = currentUser;
    }


    public static int getCurrentUserId() {
        return CURRENT_USER_ID;
    }

    public static void setCurrentUserId(int currentUserId) {
        CURRENT_USER_ID = currentUserId;
    }

    public static Context getContext() {
        return context;
    }
}
