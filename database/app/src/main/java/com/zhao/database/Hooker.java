package com.zhao.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.BaseAdapter;

import com.uopen.cryptionkit.EncryptionManager;
import com.uopen.cryptionkit.core.AbstractCoder;
import com.uopen.cryptionkit.core.sm2.SM2KeyHelper;
import com.uopen.cryptionkit.core.sm2.Sm2Kit;

import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


import static com.zhao.database.Enc.getKey;


/**
 * 始终都只会有一个对象来hook这些函数
 */
public class Hooker implements IXposedHookLoadPackage {
    public static MyDatabaseHelp dbhelper = new MyDatabaseHelp(MyApplication.getContext(), "MyDb", null, MyApplication.getDbversion());
    public static SQLiteDatabase db;
    private static AbstractCoder cipher = EncryptionManager.getCipher(EncryptionManager.Model.SM4);
    public String key = getKey();


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("当前包名" + lpparam.packageName);
        if (!lpparam.packageName.equals("com.tencent.mm"))
            return;
        /**此部分初始化新用户的数据库*/
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.tencent.mm.ui.LauncherUI", lpparam.classLoader), "onCreate", Bundle.class
                , new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        SharedPreferences sharedPreferences = ((Activity) param.thisObject).getSharedPreferences("com.tencent.mm_preferences", Context.MODE_PRIVATE);
                        /** 设置全局微信ID*/
                        String login_weixin_username = sharedPreferences.getString("login_weixin_username", "null");
                        MyApplication.setCurrentUser(login_weixin_username);
                        /** 设置全局微信用户名*/
                        String last_login_nick_name = sharedPreferences.getString("last_login_nick_name", "null");
                        MyApplication.setCurrentNickName(last_login_nick_name);
                        Cursor cursor = null;
                        /** 开启数据库事物*/
                        try {

                            db.beginTransaction();
                            db = dbhelper.getWritableDatabase();
                            cursor = db.rawQuery("select id from HOSTS where WXID =?;", new String[]{MyApplication.getCurrentUser()});
                            if (cursor.getCount() != 0) {
                                /** 数据库不为空  当前用户已经存在
                                 * 设置全局的数据库用户ID*/
                                MyApplication.setCurrentUserId(cursor.getInt(cursor.getColumnIndex("id")));
                            } else {
                                /** 为找到数据库的用户名 在表里插入数据*/

                                /** sm2加密方式 设置非对称秘钥并存入数据库*/
                                AbstractCoder cipher_sm2 = EncryptionManager.getCipher(EncryptionManager.Model.SM2);
                                SM2KeyHelper.KeyPair keyPair = SM2KeyHelper.generateKeyPair((Sm2Kit) cipher_sm2);
                                String privateKeyHex = keyPair.getPrivateKey();
                                String publicKeyHex = keyPair.getPublicKey();

                                db.execSQL("insert into HOSTS(WXID,NICKNAME,private_key,public_key) values(?,?,?,?);", new String[]{login_weixin_username, last_login_nick_name, privateKeyHex, publicKeyHex});
                                cursor = db.rawQuery("select id from HOSTS where WXID =?;", new String[]{MyApplication.getCurrentUser()});
                                MyApplication.setCurrentUserId(cursor.getInt(cursor.getColumnIndex("id")));
                            }
                            db.setTransactionSuccessful();
                        } catch (Exception e) {
                            e.printStackTrace();
                            XposedBridge.log("数据库出错");
                            return;
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                            /** 提交事务*/
                            db.endTransaction();
                            if (db != null) {
                                db.close();
                            }
                        }

                        XposedBridge.log("当前用户" + MyApplication.getCurrentUser());
                        XposedBridge.log("当前用户" + MyApplication.getCurrentNickName());
                        XposedBridge.log("\n开始遍历所有内容\n");
                        /** 调试测试*/
                        Map<String, ?> allContent = sharedPreferences.getAll();
                        for (Map.Entry<String, ?> entry : allContent.entrySet()) {
                            XposedBridge.log(entry.getKey() + entry.getValue().toString());
                        }
                    }
                });

        /**数据库hook*/
        XposedHelpers.findAndHookMethod("com.tencent.wcdb.database.SQLiteDatabase", lpparam.classLoader,
                "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("当前数据库名字" + MyApplication.getCurrentUser());
                        Object object = param.thisObject;
                        String table = (String) param.args[0];
                        String nullColumnHack = (String) param.args[1];
                        ContentValues initialValues = (ContentValues) param.args[2];
                        XposedBridge.log("\n-----------当前数据库为" + MyApplication.getCurrentUser() + "------------");
                        XposedBridge.log("para class:" + param.getClass().toString());
                        XposedBridge.log("当前对象:" + object.toString());
                        XposedBridge.log("数据库名:" + table);
                        XposedBridge.log("列名字:" + nullColumnHack);
                        XposedBridge.log("当前Content\n");
                        XposedBridge.log(initialValues.toString());
                        XposedBridge.log("\n-----------------------\n\n");
                        if (table == "message") {
                            if (initialValues != null) {
                                String content = initialValues.getAsString("content");
                                if ((initialValues.getAsInteger("isSend") == 1)) {
                                    initialValues.remove("content");
                                    initialValues.put("content", cipher.simpleEnCode(content, key));
                                }
                            }

                        }
                    }
                });
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.a.a",
                lpparam.classLoader,
                "notifyDataSetChanged", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        BaseAdapter baseAdapter = (BaseAdapter) param.thisObject;
                        if (baseAdapter == null) {
                            return;
                        }
                        for (int i = 0; i < baseAdapter.getCount(); i++) {
                            Object item = baseAdapter.getItem(i);
                            /** field_type 发送的内容的类型*/
                            if (XposedHelpers.getIntField(item, "field_type") == 1 && XposedHelpers.getIntField(item, "field_isSend") == 1) {

                                String content = (String) XposedHelpers.getObjectField(item, "field_content");
                                XposedHelpers.setObjectField(item, "field_content", cipher.simpleDeCode(content, key));
                            }
                        }
                    }
                });

    }

}


///**
// *  /**这个模块hook这个UI气泡改变时候调用的函数,
// *          * 首先获得适配器的,在一次获得适配适配器的item
// *          * 根据item里面的属性(issend、content等属性
// *          * 来实现我们的功能*/
// *XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.a.a",
//         *lpparam.classLoader,
//         *"notifyDataSetChanged",
//         *object:XC_MethodHook(){
//         *             /**在适配器对UI更新前,获取里面的参数并修改内容*/
//         *override fun beforeHookedMethod(param:MethodHookParam?){
//         *                 /**获取适配器对象*/
//         *val baseAdapter=param!!.thisObject as BaseAdapter
//         *                 /**遍历适配器,获取item
//         *for(i in 0until baseAdapter.count){
//         *val item=baseAdapter.getItem(i)
//         *XposedBridge.log("item有那些属性::${item.toString()}")
//         *if(XposedHelpers.getIntField(item,"field_type")==1&&(XposedHelpers.getIntField(
//         *item,
//         *"field_isSend"
//         *)==1)
//         *
//                      *//**经过验证有以下字段的结论:
// * field_msgId type:long 貌似是每条信息的存储ID
//  *                          * field_content type:string 是从数据库取回来信息
//  *                          * field_talker  type:string 对话者的WXID_ID
//  *                          * field_transContent : type暂时未知
//         *val longField=XposedHelpers.getLongField(item,"field_msgId")
//         *val objectField=XposedHelpers.getObjectField(item,"field_transContent")
//         *var str=XposedHelpers.getObjectField(item,"field_content")as String
//         *val str2=XposedHelpers.getObjectField(item,"field_talker")as String
//         * //                            val str1 = XposedHelpers.getObjectField(item, "field_transContent") as String
//         *XposedBridge.log("str::filed_content是什么$str")
//         * //                            XposedBridge.log("str1::field_transContent$str1")
//         *XposedBridge.log("str2::filed_talker是什么$str2")
//         *if((if(objectField==null)null
//         *else objectField as String)==null){
//         *XposedHelpers.setObjectField(item,"field_transContent","测试")
//         *XposedHelpers.setObjectField(
//         *item,
//         *"field_content",
//         *"$str+$str"
//         *)
//         *XposedBridge.log("刷新str为双倍的str")
//         *}
//         *}else{
//         *XposedBridge.log("error")
//         *}
//         *}
//         *}
//         *}
//         *)
//         */