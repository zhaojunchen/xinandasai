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


import static com.zhao.database.Debug.e;

import com.zhao.database.Database;

import com.zhao.database.Enc;

/**
 * 始终都只会有一个对象来hook这些函数
 */
public class Hooker implements IXposedHookLoadPackage {
    private static AbstractCoder cipher = EncryptionManager.getCipher(EncryptionManager.Model.SM4);
    SQLiteDatabase db = null;
    SQLiteDatabase db_readonly = null;
    private static Context launch_context = null;
    private static String socket = null;
    static User[] talkers = new User[5];
    static User current_talker;
    static long couner = 0;
    public static String key = Enc.getKey();
    private String prefix = "-------@";

    static {
        for (int i = 0; i < 5; i++) {
            talkers[i] = new User();
        }
    }


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("当前包名" + lpparam.packageName);
        if (!lpparam.packageName.equals("com.tencent.mm"))
            return;

        /**此部分初始化新用户的数据库*/
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.tencent.mm.ui.LauncherUI", lpparam.classLoader), "onCreate", Bundle.class
                , new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("进入启动界面");
                        SharedPreferences sharedPreferences = ((Activity) param.thisObject).getSharedPreferences("com.tencent.mm_preferences", Context.MODE_PRIVATE);
                        /** 设置全局微信ID*/
                        String login_weixin_username = sharedPreferences.getString("login_weixin_username", "null");
                        MyApplication.setCurrentUser(login_weixin_username);
                        /** 设置全局微信用户名*/
                        String last_login_nick_name = sharedPreferences.getString("last_login_nick_name", "null");
                        MyApplication.setCurrentNickName(last_login_nick_name);

                        /**获取上下文*/
                        if (last_login_nick_name == null) {
                            return;
                        }
                        /** 防止无用的空的函数来获取数据的对象*/

                        Context context = ((Activity) param.thisObject).getApplicationContext();
                        launch_context = context;
                        XposedBridge.log("这个DB是谁啊");
                        db = new MyDatabaseHelp(context, "MyDb", null, MyApplication.getDbversion()).getWritableDatabase();
                        db_readonly = new MyDatabaseHelp(context, "MyDb", null, MyApplication.getDbversion()).getReadableDatabase();
                        Cursor cursor = null;
                        /** 开启数据库事物*/
                        if (db == null) {
                            e("数据打开失败");
                        }
                        db.beginTransaction();
//                        cursor = db.rawQuery("select id from HOSTS where WXID =?;", new String[]{MyApplication.getCurrentUser()});
                        cursor = db.rawQuery("select * from HOSTS where WXID =?;", new String[]{MyApplication.getCurrentUser()});
                        if (cursor.getCount() != 0) {
                            /** 数据库不为空  当前用户已经存在
                             * 设置全局的数据库用户ID*/
                            cursor.moveToFirst();
                            do {
                                XposedBridge.log("读取数据库文件");
                                int id = cursor.getInt(cursor.getColumnIndex("id"));
                                MyApplication.setCurrentUserId(id);
                                String WXID = cursor.getString(cursor.getColumnIndex("WXID"));
                                String private_key = cursor.getString(cursor.getColumnIndex("private_key"));
                                String public_key = cursor.getString(cursor.getColumnIndex("public_key"));
                                XposedBridge.log("ID:" + String.valueOf(id) + "  微信ID" + WXID + "  私钥" + private_key + "  公钥" + public_key);
                            } while (cursor.moveToNext());


                            XposedBridge.log("当前用户不为空");
                            e();

                            e("答应当前ID" + MyApplication.getCurrentUser());
                            e("答应当前ID" + String.valueOf(MyApplication.getCurrentUserId()));
                            e("打印ID");
                        } else {
                            /** 为找到数据库的用户名 在表里插入数据*/

                            /** sm2加密方式 设置非对称秘钥并存入数据库*/
                            AbstractCoder cipher_sm2 = EncryptionManager.getCipher(EncryptionManager.Model.SM2);
                            SM2KeyHelper.KeyPair keyPair = SM2KeyHelper.generateKeyPair((Sm2Kit) cipher_sm2);
                            String privateKeyHex = keyPair.getPrivateKey();
                            e(privateKeyHex);
                            String publicKeyHex = keyPair.getPublicKey();
                            e(publicKeyHex);

                            db.execSQL("insert into HOSTS(WXID,NICKNAME,private_key,public_key) values(?,?,?,?);", new String[]{login_weixin_username, last_login_nick_name, privateKeyHex, publicKeyHex});
                            cursor = db.rawQuery("select id from HOSTS where WXID =?;", new String[]{MyApplication.getCurrentUser()});
                            cursor.moveToFirst();
                            MyApplication.setCurrentUserId(cursor.getInt(cursor.getColumnIndex("id")));
                            e(String.valueOf(MyApplication.getCurrentUserId()));
                            e(MyApplication.getCurrentNickName());
                            e(MyApplication.getCurrentUser());
                            XposedBridge.log("设置的打印ID");
                            e(String.valueOf(MyApplication.getCurrentUserId()));
                        }
                        db.setTransactionSuccessful();


                        if (cursor != null) {
                            cursor.close();
                        }
                        /** 提交事务*/
                        db.endTransaction();

                        XposedBridge.log("当前用户" + MyApplication.getCurrentUser());
                        XposedBridge.log("当前用户" + MyApplication.getCurrentNickName());
                        XposedBridge.log("\n开始遍历所有内容\n");
                        /** 调试测试*/
                        Map<String, ?> allContent = sharedPreferences.getAll();
                        for (
                                Map.Entry<String, ?> entry : allContent.entrySet()) {
                            XposedBridge.log(entry.getKey() + entry.getValue().toString());
                        }
                    }
                });

        /**数据库hook*/

        /** 程序不退出 我就不死*/
        XposedHelpers.findAndHookMethod("com.tencent.wcdb.database.SQLiteDatabase", lpparam.classLoader,
                "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class, new

                        XC_MethodHook() {
                            private int live = Enc.getRandom();
                            private Cursor cursor = null;

                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                /** 打印数据库*/

                                if (db != null) {
                                    XposedBridge.log(db.toString());
                                    Database.show(db);
                                } else {
                                    XposedBridge.log("这个db是个残废");
                                }
                                /** 生命周期测试*/
                                XposedBridge.log("live:" + live);

                                XposedBridge.log("当前用户WXID:" + MyApplication.getCurrentUser());
                                Object object = param.thisObject;
                                String table = (String) param.args[0];
                                String nullColumnHack = (String) param.args[1];
                                ContentValues initialValues = (ContentValues) param.args[2];

                                XposedBridge.log("para class:" + param.getClass().toString());
                                XposedBridge.log("当前对象:" + object.toString());
                                XposedBridge.log("数据库名:" + table);
                                XposedBridge.log("列名字:" + nullColumnHack);
                                XposedBridge.log("当前Content\n");
                                XposedBridge.log(initialValues.toString());
                                XposedBridge.log("\n-----------------------\n\n");
                                if (table == "message") {
                                    /** 获取上下文*/

                                    if (initialValues == null) {
                                        return;
                                    }
                                    //获取用户名
                                    String talker = initialValues.getAsString("talker");

                                    /******************************
                                     * 作用对象只是个人微信号 否则退出
                                     * ****************************/
                                    if (!talker.startsWith("wxid")) {
                                        return;
                                    }
                                    //获取消息
                                    String content = initialValues.getAsString("content");

                                    /******************************
                                     * 在此分支建立加密的对话
                                     * ****************************/
                                    if (content.startsWith("建立链接")) {
                                        /** 通过socket==null？来控制content的消息
                                         *  从而控制对话机建立二人之间的加密连接秘钥*/
                                        socket = challenge(content, talker, db);
                                        return;
                                    }

                                    /*****************************
                                     * table ="message"过滤了其他的表
                                     * db !=null  在逻辑上一定是成立的
                                     * 可以在不判断的db！=null条件下使用
                                     * ***************************/
                                    if (socket != null) {
                                        initialValues.remove("content");
                                        initialValues.put("content", socket);
                                        socket = null;      //销毁socket防止正常消息被过滤
                                        return;             //直接替换消息 发送连接语言
                                    }

                                    cursor = db.rawQuery("select public_key from FriendTable where wxid =? and BELONG = ?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                                    if (!cursor.moveToNext()) {
                                        /** 未查询到数据 退出此模块*/
                                        return;
                                    }
                                    //查询返回的SM4秘钥
                                    String sm4key = cursor.getString(0);

                                    initialValues.remove("content");
                                    initialValues.put("content", cipher.simpleEnCode(content, key));

                                }


                            }
                        });

        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.a.a",
                lpparam.classLoader,
                "notifyDataSetChanged", new

                        XC_MethodHook() {
                            private Cursor cursor = null;   //只读数据库操作游标实例
                            private String key = null;      //对称解密秘钥串

                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                BaseAdapter baseAdapter = (BaseAdapter) param.thisObject;
                                if (baseAdapter == null) {
                                    return;
                                }
                                /**查看适配器的使用*/
                                String random = Enc.getKey();
                                XposedBridge.log("live:" + random);
                                XposedBridge.log("\n----在这里使用一个适配器-----\n");
                                for (int i = 0; i < baseAdapter.getCount(); i++) {
                                    XposedBridge.log("内部执行了第" + i + "循环");
                                    Object item = baseAdapter.getItem(i);

                                    String wxid = (String) XposedHelpers.getObjectField(item, "field_content");
                                    if (XposedHelpers.getIntField(item, "field_type") == 1 && XposedHelpers.getIntField(item, "field_isSend") == 1 && wxid.startsWith("wxid")) {

                                        String content = (String) XposedHelpers.getObjectField(item, "field_content");

                                        if (!content.startsWith(prefix)) {
                                            XposedBridge.log("此字段不是解密模块");
                                            return;
                                        }

                                        if (db_readonly == null) {
                                            /****************
                                             * 判断数据库是否打开
                                             * 根据实际情况可能不
                                             * 需要这个判断条件..
                                             * **************/
                                            XposedBridge.log("只读数据库为空");
                                            return;
                                        }
                                        /***************************
                                         * 适配器解密模块实现模块
                                         * *************************/

                                        cursor = db.rawQuery("select public_key from FriendTable where wxid =?;", new String[]{wxid});
                                        if (cursor.moveToNext()) {
                                            key = cursor.getColumnName(0);
                                            XposedBridge.log("解密秘钥获取成功" + key);
                                            /** 更改为解密消息
                                             *  在加密的基础性上添加前缀*/
                                            if (content.startsWith(prefix)) {
                                                XposedHelpers.setObjectField(item, "field_content", cipher.simpleDeCode(content, key));
                                            } else {
                                                XposedBridge.log("我不对非加密字符负责");
                                                return;
                                            }


                                        } else {
                                            XposedBridge.log("未存储好友,不做任何操作");
                                            return;
                                        }


                                    }
                                }
                            }
                        });

    }

    private String challenge(String content, String talker, SQLiteDatabase db) {

        String[] split_content = content.split("@");
        String public_key;
        AbstractCoder cipher_sm2 = EncryptionManager.getCipher(EncryptionManager.Model.SM2);
        Cursor cursor = null;
        Cursor cursor1 = null;
        /****************************
         * 处理来自自己的链接请求
         * **************************/
        if (split_content.length == 1) {
            //查询好友的公钥表  查看公钥是否在库
            //在库则建立已经建立公钥链接但可能没建立通信连接
            cursor = db.rawQuery("SELECT public_key FROM linktable where wxid =? and belong = ?", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
            /** 公钥在库*/
            if (cursor.moveToNext()) {
                /** 链接已建立*/
                Cursor mycursor = db.rawQuery("select * from FRIEND_TABEL where wxid = ? and BELONG=?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                if (mycursor.moveToNext()) {
                    return "连接已经建立";
                }
                //链接不在库的库的话  再次尝试建立连接 组织挑战语言

                public_key = cursor.getString(0);

                /**随机生成公钥*/
                String key = Enc.getKey();
                /** sm2加密方式 非对称加密秘钥*/

                String key_by_sm2 = cipher_sm2.simpleEnCode(key, public_key);

                /** 插入数据库*/
                db.execSQL("insert into FRIEND_TABEL (wxid,note,public_key,isEnc,isDec,BELONG) values(?,?,?,?,?,?);", new String[]{
                        talker, talker, key, String.valueOf(1), String.valueOf(1), String.valueOf(MyApplication.getCurrentUserId())
                });

                //组织挑战的返回语句

                return "建立连接@SM2KEY" + key_by_sm2;

                //查看是否建立了连接
            } else {
                //无公钥信息保存 将自己的公钥发送给好友
                cursor1 = db.rawQuery("SELECT public_key FROM HOSTS where WXID=?;", new String[]{MyApplication.getCurrentUser()});

                /*private static String KEY_TABLE = "CREATE TABLE IF NOT EXISTS" +
                        " HOSTS(id INTEGER PRIMARY KEY AUTOINCREMENT,WXID TEXT,NICKNAME TEXT,private_key " +
                        "TEXT,public_key TEXT);";*/
                public_key = cursor1.getString(0);
                return "建立连接@PK" + public_key;

            }
        }
        if (split_content.length == 2) {
            String link = split_content[1];
            if (link.startsWith("PK")) {
                link = link.substring(2);
                //判断公钥是否在库
                cursor = db.rawQuery("SELECT public_key FROM linktable where wxid =? and belong = ?", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});

                /** 公钥不在库 则添加到库*/
                if (!cursor.moveToNext()) {
                    db.execSQL("insert into linktable (wxid,public_key,belong) values(?,?,?)", new String[]{talker, link, String.valueOf(MyApplication.getCurrentUserId())});
                }
                /** 组织发送的链接语言(已知对方公钥)*/

                String key = Enc.getKey();
                /** sm2加密方式 非对称加密秘钥*/

                String key_by_sm2 = cipher_sm2.simpleEnCode(key, link);

                /** 插入数据库*/
                db.execSQL("insert into FRIEND_TABEL (wxid,note,public_key,isEnc,isDec,BELONG) values(?,?,?,?,?,?);", new String[]{
                        talker, talker, key, String.valueOf(1), String.valueOf(1), String.valueOf(MyApplication.getCurrentUserId())
                });
                return "建立连接@SM2KEY" + key_by_sm2;

            } else {
                if (link.startsWith("SM2KEY")) {
                    link = link.substring(6);
                    /** 此时接收到来自对方的加密秘钥*/
                    /** 解密并添加到自己的数据库*/
                    cursor = db.rawQuery("select private_key from HOSTS where id = ?", new String[]{String.valueOf(MyApplication.getCurrentUserId())});
                    String private_key = null;
                    if (cursor.moveToNext()) {
                        private_key = cursor.getString(0);

                    } else {
                        XposedBridge.log("Fuck you! Don't be crash!!!");
                    }
                    String decode_key = cipher_sm2.simpleDeCode(link, private_key);
                    /** 添加到数据库*/
                    /** 插入数据库*/
                    db.execSQL("insert into FRIEND_TABEL (wxid,note,public_key,isEnc,isDec,BELONG) values(?,?,?,?,?,?);", new String[]{
                            talker, talker, decode_key, String.valueOf(1), String.valueOf(1), String.valueOf(MyApplication.getCurrentUserId())
                    });
                    return "链接建立成功";
                    /* private static String KEY_TABLE = "CREATE TABLE IF NOT EXISTS" +
                        " HOSTS(id INTEGER PRIMARY KEY AUTOINCREMENT,WXID TEXT,NICKNAME TEXT,private_key " +
                        "TEXT,public_key TEXT);";

                private static String FRIEND_TABEL = "CREATE TABLE IF NOT EXISTS" +
                        " FriendTable(wxid TEXT,id INTEGER PRIMARY KEY" +
                        " , note TEXT, public_key TEXT, isEnc INTEGER, " +
                        "isDec INTEGER, BELONG INTEGER);";

                private static String INDEX = "CREATE INDEX index_name on FriendTable (wxid);";

                private static String link_table = "CREATE TABLE IF NOT EXISTS linktable(" +
            "wxid TEXT ,public_key TEXT,belong INTEGER,primary key(wxid,belong))";*/

                }
            }
        }
        return null;


    }
}
