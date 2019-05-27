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
    public static String key = Enc.getKey();
    private String prefix = "-------@";


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        /**过滤微信以外的包名*/
        if (!lpparam.packageName.equals("com.tencent.mm"))
            return;


        /*******************************************************
         * hook拦截微信的启动界面,初始化系统的数据库
         * 以及大部分的变量初始值----wxid等关键信息
         * *****************************************************/
        XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.tencent.mm.ui.LauncherUI", lpparam.classLoader), "onCreate", Bundle.class
                , new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("进入启动界面");
                        /** 获取sharedPreferences存储的文本信息*/
                        SharedPreferences sharedPreferences = ((Activity) param.thisObject).getSharedPreferences("com.tencent.mm_preferences", Context.MODE_PRIVATE);
                        String login_weixin_username = sharedPreferences.getString("login_weixin_username", "null");
                        /** 设置全局微信用户的WXID*/
                        MyApplication.setCurrentUser(login_weixin_username);
                        /** 设置全局微信用户名*/
                        String last_login_nick_name = sharedPreferences.getString("last_login_nick_name", "null");
                        MyApplication.setCurrentNickName(last_login_nick_name);

                        /**获取上下文  过滤*/
                        if (last_login_nick_name == null) {
                            return;
                        }

                        /** 获取微信上下文
                         *  操作数据库context*/
                        Context context = ((Activity) param.thisObject).getApplicationContext();
                        launch_context = context;
                        XposedBridge.log("这个DB是谁啊");
                        /** 打开读写数据库 用于challenge和hookdatabase模块*/
                        db = new MyDatabaseHelp(context, "MyDb", null, MyApplication.getDbversion()).getWritableDatabase();
                        /**打开只读数据库  用于刷新操作*/
                        db_readonly = new MyDatabaseHelp(context, "MyDb", null, MyApplication.getDbversion()).getReadableDatabase();

                        Cursor cursor = null;
                        /** 开启数据库事物*/
                        if (db == null) {
                            XposedBridge.log("数据打开失败");
                        }

                        cursor = db.rawQuery("select id from HOSTS where WXID =?;", new String[]{MyApplication.getCurrentUser()});
                        if (cursor.moveToNext()) {
                            XposedBridge.log("用户在库");
                            /** 设置全局微信ID*/
                            MyApplication.setCurrentUserId(cursor.getInt(0));

                        } else {

                            /** 添加并取id设置全局微信ID的值*/
                            XposedBridge.log("添加用户入库");


                            /** sm2加密方式 设置非对称秘钥并存入数据库*/
                            AbstractCoder cipher_sm2 = EncryptionManager.getCipher(EncryptionManager.Model.SM2);
                            SM2KeyHelper.KeyPair keyPair = SM2KeyHelper.generateKeyPair((Sm2Kit) cipher_sm2);

                            String privateKeyHex = keyPair.getPrivateKey();
                            XposedBridge.log(privateKeyHex);
                            String publicKeyHex = keyPair.getPublicKey();
                            XposedBridge.log(publicKeyHex);

                            db.execSQL("insert into HOSTS(WXID,NICKNAME,private_key,public_key) values(?,?,?,?);", new String[]{login_weixin_username, login_weixin_username.substring(5), privateKeyHex, publicKeyHex});
                            cursor = db.rawQuery("select id from HOSTS where WXID =?;", new String[]{MyApplication.getCurrentUser()});
                            if (!cursor.moveToNext()) {
                                XposedBridge.log("致命错误");
                                return;
                            }

                            MyApplication.setCurrentUserId(cursor.getInt(0));
                            XposedBridge.log("设置ID");
                            XposedBridge.log(String.valueOf(MyApplication.getCurrentUserId()));
                            XposedBridge.log(String.valueOf(MyApplication.getCurrentUserId()));
                            XposedBridge.log(MyApplication.getCurrentNickName());
                            XposedBridge.log(MyApplication.getCurrentUser());

                        }

                        if (cursor != null) {
                            cursor.close();
                        }
                        /** 复查审核错误状况*/
                        if (MyApplication.getCurrentUser() == null) {
                            XposedBridge.log("致命错误");
                            return;
                        }
                        XposedBridge.log("当前用户WXID" + MyApplication.getCurrentUser());
                        XposedBridge.log("当前用户ID号" + String.valueOf(MyApplication.getCurrentUserId()));
                        XposedBridge.log("\n开始遍历所有内容\n");
                        /** 调试测试*/
                        Map<String, ?> allContent = sharedPreferences.getAll();
                        for (
                                Map.Entry<String, ?> entry : allContent.entrySet()) {
                            XposedBridge.log(entry.getKey() + entry.getValue().toString());
                        }
                    }
                });


        /*******************************************************
         * 数据库hook拦截所有插入数据库的消息内容
         * 本代码只是使用到拦截微信文本消息的模块
         * *****************************************************/
        XposedHelpers.findAndHookMethod("com.tencent.wcdb.database.SQLiteDatabase", lpparam.classLoader,
                "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class, new

                        XC_MethodHook() {
                            /**
                             * 随机值测试生命周期
                             */
                            private int live = Enc.getRandom();
                            /** 优化数据库查询语句   null是代表某些参数被修改*/
                            private String lasttalker = "lasttalker";
                            private String sm4key = null;

                            private Cursor cursor = null;

                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                /** 打印数据库*/

                                if (db != null) {
                                    /** debug数据库*/
                                    Database.show(db);
                                } else {
                                    XposedBridge.log("这个DB还未被初始化");
                                    return;
                                }
                                /** 生命周期测试*/
                                XposedBridge.log("live:" + live);

                                XposedBridge.log("微信号拥有者" + MyApplication.getCurrentUser() + "\nID" + String.valueOf(MyApplication.getCurrentUserId()));

                                Object object = param.thisObject;
                                String table = (String) param.args[0];
                                String nullColumnHack = (String) param.args[1];
                                ContentValues initialValues = (ContentValues) param.args[2];

                                //XposedBridge.log("para class:" + param.getClass().toString());
                                XposedBridge.log("当前对象:" + object.toString());
                                XposedBridge.log("数据库名:" + table);
                                XposedBridge.log("列名字:" + nullColumnHack);
                                XposedBridge.log("当前Content\n\n");
                                XposedBridge.log(initialValues.toString() + "\n\n\n");
                                if (table == "message") {
                                    /** 获取上下文*/

                                    if (initialValues == null) {
                                        return;
                                    }
                                    //获取用户名
                                    String talker = initialValues.getAsString("talker");
                                    XposedBridge.log("talker:" + talker);


                                    /******************************
                                     * 作用对象只是个人微信号 否则退出
                                     * ****************************/
                                    if (!talker.startsWith("wxid")) {
                                        XposedBridge.log("room exit");
                                        return;
                                    }
                                    //获取消息
                                    String content = initialValues.getAsString("content");
                                    XposedBridge.log("content" + content);

                                    /******************************
                                     * 在此分支建立加密的对话
                                     * ****************************/
                                    if (content.startsWith("建立连接")) {
                                        /** 通过socket==null？来控制content的消息
                                         *  从而控制对话机建立二人之间的加密连接秘钥*/
                                        XposedBridge.log("发起一个挑战");
                                        socket = challenge(content, talker, db);
                                        XposedBridge.log("待发送的socket?" + socket);
                                        /** 将通信的socket发送到别人手机上*/
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

                                    if (!lasttalker.equals(talker)) {
                                        cursor = db.rawQuery("select public_key from FriendTable where wxid =? and BELONG = ?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                                        if (!cursor.moveToNext()) {
                                            /** 未查询到数据 退出此模块*/
                                            XposedBridge.log("用户尚未加密认证");
                                            return;
                                        }
                                        sm4key = cursor.getString(0);
                                    }

                                    //查询返回的SM4秘钥

                                    lasttalker = talker;            //这儿貌似没有漏洞
                                    initialValues.remove("content");
                                    /** 替换内容为加密之后的密文
                                     * 并添加前缀区分明密文明文*/
                                    initialValues.put("content", prefix + cipher.simpleEnCode(content, key));

                                }


                            }
                        });

        /*****************************************************************
         * 对象说明:wechat获取到com.tencent.mm.ui.chatting.a.a的包名时候，注册对象
         * 这个对象的生命周期wechat的生命周期一致, note:通过随机数的直来检对象的生命周期
         * ****************************************************************/
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.a.a",
                lpparam.classLoader,
                "notifyDataSetChanged", new

                        XC_MethodHook() {
                            private Cursor cursor = null;   //只读数据库操作游标实例

                            /**************************************
                             * 函数说明:当wechat调用适配器刷新界面就会调用
                             * 通过参数的判定来解决密文的显示问题,每个好友的
                             * 的聊天列表会有一个或者多个的消息适配器函数调用
                             * 到那时一个关于效率的关键性因素是没切换一个用户
                             * 的聊天界面就一定会切换适配器函数
                             * ************************************/

                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                BaseAdapter baseAdapter = (BaseAdapter) param.thisObject;
                                if (baseAdapter == null || db_readonly == null) {
                                    return;
                                }

                                /**检测适配器的这个函数的生命周期*/
                                String random = Enc.getKey();
                                String key = null;
                                String wxid = null;
                                String content;

                                XposedBridge.log("live:" + random);

                                if (baseAdapter.getCount() == 0) {
                                    return;
                                } else {
                                    Object item = baseAdapter.getItem(0);
                                    wxid = (String) XposedHelpers.getObjectField(item, "field_talker");
                                }
                                /** 过滤聊天室*/
                                if (!wxid.startsWith("wxid")) {
                                    return;
                                }
                                /** 查询好友列表 获取公钥信息 当然还可拓展是否加密和是否解密*/
                                cursor = db_readonly.rawQuery("select public_key from FriendTable where wxid =? and BELONG = ?;", new String[]{wxid, String.valueOf(MyApplication.getCurrentUserId())});
                                if (!cursor.moveToNext()) {
                                    /** 未找到加密记录
                                     * 直接退出函数*/
                                    return;
                                }
                                //公钥的解密秘钥
                                key = cursor.getString(0);

                                XposedBridge.log("\n----在这里使用一个适配器-----\n");

                                /** 同一个函数调用生命周期内必定对应同一个聊天用户的界面
                                 *  即可以获取 同一个解密的秘钥,减少数据库的查询次数 */
                                for (int i = 0; i < baseAdapter.getCount(); i++) {
                                    XposedBridge.log("内部执行了第" + i + "循环");
                                    Object item = baseAdapter.getItem(i);

                                    /** 获取消息类型 1 代表文本消息*/
                                    if (XposedHelpers.getIntField(item, "field_type") == 1) {

                                        content = (String) XposedHelpers.getObjectField(item, "field_content");

                                        if (!content.startsWith(prefix)) {
                                            XposedBridge.log("不对非加密内容负责");
                                        } else {
                                            XposedHelpers.setObjectField(item, "field_content", cipher.simpleDeCode(content.substring(8), key));
                                        }

                                    }
                                }
                            }
                        });
    }

    private String challenge(String content, String talker, SQLiteDatabase db) {

        XposedBridge.log("进入挑战函数");
        String[] split_content = content.split("@");
        String public_key;
        AbstractCoder cipher_sm2 = EncryptionManager.getCipher(EncryptionManager.Model.SM2);
        Cursor cursor = null;
        Cursor cursor1 = null;
        XposedBridge.log("让我们来看一下这个数据库");
        Database.show(db);
        /****************************
         * 处理来自自己的链接请求
         * **************************/
        if (split_content.length == 1) {
            //查询好友的公钥表  查看公钥是否在库
            //在库则建立已经建立公钥链接但可能没建立通信连接
            XposedBridge.log("断言:" + split_content.length);
            cursor = db.rawQuery("select public_key from linktable where wxid =? and belong = ?", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
            /** 公钥在库*/
            if (cursor.moveToNext()) {
                /** 链接已建立*/
                Cursor mycursor = db.rawQuery("select * from FriendTable where wxid = ? and BELONG=?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                if (mycursor.moveToNext()) {
                    mycursor.close();
                    cursor.close();
                    return "建立连接@连接已建立";
                }
                //链接不在库的库的话  再次尝试建立连接 组织挑战语言

                public_key = cursor.getString(0);

                /**随机生成公钥*/
                String key = Enc.getKey();
                /** sm2加密方式 非对称加密秘钥*/

                String key_by_sm2 = cipher_sm2.simpleEnCode(key, public_key);

                /** 插入数据库*/
                db.execSQL("insert into FriendTable (wxid,note,public_key,isEnc,isDec,BELONG) values(?,?,?,?,?,?);", new String[]{
                        talker, talker, key, String.valueOf(1), String.valueOf(1), String.valueOf(MyApplication.getCurrentUserId())
                });

                cursor = db.rawQuery("select public_key from linktable where wxid =? and belong = ?", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                if (!cursor.moveToNext()) {
                    return "添加错误";
                }

                //组织挑战的返回语句

                return "建立连接@SM2KEY" + key_by_sm2;

                //查看是否建立了连接
            } else {
                //无公钥信息保存 将自己的公钥发送给好友
                cursor1 = db.rawQuery("SELECT public_key FROM HOSTS where WXID=?;", new String[]{MyApplication.getCurrentUser()});

                /*private static String KEY_TABLE = "CREATE TABLE IF NOT EXISTS" +
                        " HOSTS(id INTEGER PRIMARY KEY AUTOINCREMENT,WXID TEXT,NICKNAME TEXT,private_key " +
                        "TEXT,public_key TEXT);";*/
                if (!cursor1.moveToNext()) {
                    XposedBridge.log("自己的公钥表建立错误");
                    return "自己的公钥表建立错误";
                }
                public_key = cursor1.getString(0);
                XposedBridge.log("public_key" + public_key);
                return "建立连接@PK" + public_key;
            }
        }

        if (split_content.length >= 2) {
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
                db.execSQL("insert into FriendTable (wxid,note,public_key,isEnc,isDec,BELONG) values(?,?,?,?,?,?);", new String[]{
                        talker, talker.substring(5), key, String.valueOf(1), String.valueOf(1), String.valueOf(MyApplication.getCurrentUserId())
                });
                return "建立连接@SM2KEY" + key_by_sm2;

            } else if (link.startsWith("SM2KEY")) {
                link = link.substring(6);
                /** 此时接收到来自对方的加密秘钥*/
                /** 解密并添加到自己的数据库*/
                cursor = db.rawQuery("select * from FriendTable where wxid = ? and BELONG=?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                if (cursor.moveToNext()) {
                    return "建立连接@连接已建立";
                }

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
                return "建立连接@连接已建立@"+link;
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

            } else if (link.equals("连接已建立")) {

                cursor = db.rawQuery("select * from FriendTable where wxid = ? and BELONG=?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                if (cursor.moveToNext()) {
                    return "建立连接@我也是!!!成功,我们开始加密通信吧";
                } else {
                    return "程序有bug. 哇我不想调试啊. 亲爱的!";
                }
            }

        }
        return null;


    }
}
