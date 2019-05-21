package com.zhao.a_test_for_spellbook

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences


class Hooker : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("加载包" + "${lpparam.packageName}")

        XposedBridge.log("加载到UI界面")

        /** 当登录时回去这个*/
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.tencent.mm.ui.LauncherUI", lpparam.classLoader),
            "onCreate", Bundle::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val sharedPreferences = (param?.thisObject as Activity).getSharedPreferences(
                        "com.tencent.mm_preferences",
                        Context.MODE_PRIVATE)
                    /** 微信ID:wxid_np7ngzlhk55322*/
                    /** wxid_0ol8nckpqrnr22陈渊*/
                    XposedBridge.log("WXID:" + sharedPreferences.getString("login_weixin_username", null))
                    /** 网名 赵君臣*/
                    XposedBridge.log("用户名" + sharedPreferences.getString("last_login_nick_name", null))
                }
            })



        XposedHelpers.findAndHookMethod(
            "com.tencent.wcdb.database.SQLiteDatabase", lpparam.classLoader,
            "insertWithOnConflict", String::
            class.java, String::
            class.java,
            ContentValues::
            class.java, Int::
            class.java,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val thisObject = param.thisObject
                    val table = param.args[0] as String
                    val nullColumnHack = param.args[1] as String?
                    val initialValues = param.args[2] as ContentValues?
                    val conflictAlgorithm = param.args[3] as Int
                    XposedBridge.log("---------\nhook database-----\n")
                    /** 当前对象数据库对象*/
                    XposedBridge.log("thisObject$thisObject")
                    /** table表名--->message文字信息表*/
                    XposedBridge.log("table:$table")
                    /** nullColumHack列名*/
                    XposedBridge.log("nullColumnHack:$nullColumnHack")
                    XposedBridge.log("initialValues:${initialValues.toString()}")
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    val thisObject = param.thisObject
                    val table = param.args[0] as String
                    val nullColumnHack = param.args[1] as String?
                    val initialValues = param.args[2] as ContentValues?
                    val conflictAlgorithm = param.args[3] as Int
                    XposedBridge.log("---------\nhook database-----\n")
                    /** 当前对象数据库对象*/
                    XposedBridge.log("thisObject$thisObject")
                    /** table表名--->message文字信息表*/
                    XposedBridge.log("table:$table")
                    /** nullColumHack列名*/
                    XposedBridge.log("nullColumnHack:$nullColumnHack")
                    XposedBridge.log("initialValues:${initialValues.toString()}")
                }
            })

    }
}


/** type=1 文字  数据库名称:ImgInfo2 */