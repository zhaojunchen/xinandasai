package com.whu.annoywechat;

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

/**
 * 始终都只会有一个对象来hook这些函数
 */
public class Hooker implements IXposedHookLoadPackage {
    public static SQLiteDatabase db = null;
    private static SQLiteDatabase db_readonly = null;
    private static Context launch_context = null;
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
                            AbstractCoder cipher = EncryptionManager.getCipher(EncryptionManager.Model.SM4);
                            /** 优化数据库查询语句   null是代表某些参数被修改*/
                            private String lasttalker = "lasttalker";
                            private String talker = null;
                            private String content = null;
                            private int isSend;

                            private Object object = null;
                            private String table = null;
                            private String nullColumnHack = null;
                            private ContentValues initialValues = null;
                            private String sm4key = null;
                            private Cursor cursor = null;
                            private String socket = null;

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

                                object = param.thisObject;
                                table = (String) param.args[0];
                                nullColumnHack = (String) param.args[1];
                                initialValues = (ContentValues) param.args[2];

//                                XposedBridge.log("para class:" + param.getClass().toString());
//                                XposedBridge.log("当前对象:" + object.toString());
//                                XposedBridge.log("数据库名:" + table);
//                                XposedBridge.log("列名字:" + nullColumnHack);
//                                XposedBridge.log("当前Content\n\n");
//                                XposedBridge.log(initialValues.toString() + "\n\n\n");
                                if (table == "message") {
                                    /** 获取上下文*/

                                    if (initialValues == null) {
                                        return;
                                    }
                                    //获取用户名
                                    talker = initialValues.getAsString("talker");
                                    XposedBridge.log("talker:" + talker);


                                    /******************************
                                     * 作用对象只是个人微信号 否则退出
                                     * ****************************/
                                    if (!talker.startsWith("wxid")) {
                                        XposedBridge.log("room exit");
                                        return;
                                    }
                                    //获取消息
                                    content = initialValues.getAsString("content");
                                    isSend = initialValues.getAsInteger("isSend");
                                    XposedBridge.log("原文content" + content);

                                    /******************************
                                     * 在此分支建立加密的对话
                                     * ****************************/
                                    if (content.startsWith("建立连接")) {
                                        /** 通过socket==null？来控制content的消息
                                         *  从而控制对话机建立二人之间的加密连接秘钥*/
                                        XposedBridge.log("发起一个挑战");
                                        if (content.equals("建立连接")) {
                                            socket = challenge(talker, db);
                                        } else if (content.startsWith("建立连接@PK")) {
                                            socket = challenge_3(content, talker, db);
                                        } else if (content.startsWith("建立连接@一方连接已建立")) {
                                            /** 发送者*/
                                            if (isSend == 0) {
                                                socket = challenge_2(content, talker, db);
                                            } else {
                                                //对发送者不做任何的修改
                                                return;
                                            }

                                        } else {
                                            socket = null;
                                        }
                                        if (socket != null) {
                                            XposedBridge.log("待发送的socket?" + socket);
                                        } else {
                                            XposedBridge.log("socket is null");
                                        }

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
                                    if (isSend == 0) {
                                        return;
                                    }

                                    /** 针对发送消息的加密模块*/


                                    /** lasttalker 基于时间空间
                                     * 局部性优化数据库查询*/

                                    if (!lasttalker.equals(talker)) {
                                        cursor = db.rawQuery("select public_key from FriendTable where wxid =? and BELONG = ?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                                        if (!cursor.moveToNext()) {
                                            /** 未查询到数据 退出此模块*/
                                            XposedBridge.log("用户尚未加密认证");
                                            return;
                                        }
                                        sm4key = cursor.getString(0);
                                        XposedBridge.log("网络传输加密的秘钥" + sm4key);
                                    }

                                    /** 加密只是是针对发送的时候加密
                                     * 对于别人发送的消息我们是不加密
                                     * 否则在解密的是造成二次加密一次解密*/
                                    //查询返回的SM4秘钥

                                    XposedBridge.log("使用上次的key");
                                    lasttalker = talker;            //这儿貌似没有漏洞
                                    initialValues.remove("content");
                                    /** 替换内容为加密之后的密文
                                     * 并添加前缀区分明密文明文*/
                                    XposedBridge.log("明文原文:" + content);
                                    initialValues.put("content", prefix + cipher.simpleEnCode(content, sm4key));

                                }


                            }
                        });

        /******************************************************************
         * 对象说明:wechat获取到com.tencent.mm.ui.chatting.a.a的包名时候，注册对象
         * 这个对象的生命周期wechat的生命周期一致, note:通过随机数的直来检对象的生命周期
         * ****************************************************************/
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.a.a",
                lpparam.classLoader,
                "notifyDataSetChanged", new

                        XC_MethodHook() {
                            private Cursor cursor = null;   //只读数据库操作游标实例
                            AbstractCoder cipher = EncryptionManager.getCipher(EncryptionManager.Model.SM4);

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
                                    XposedBridge.log("未建立加密连接");
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
                                        XposedBridge.log("显示原生" + content);

                                        if (!content.startsWith(prefix)) {
                                            XposedBridge.log("不对非加密内容负责");
                                        } else {
//                                            XposedBridge.log("content" + content);
//                                            XposedBridge.log("解密显示的秘钥:" + key);
                                            content = content.substring(8);
//                                            XposedBridge.log("刷新之前" + content);
//                                            XposedBridge.log("长度" + content.length());
                                            String decode = cipher.simpleDeCode(content, key);
                                            XposedHelpers.setObjectField(item, "field_content", decode);
                                            XposedBridge.log((String) XposedHelpers.getObjectField(item, "field_content"));
                                        }

                                    }
                                }
                            }
                        });
    }


    /**
     * 当content = "建立连接"
     */
    private String challenge(String talker, SQLiteDatabase db) {
//        XposedBridge.log("进入challenge");
//        XposedBridge.log("数据库");
//        Database.show(db);

        AbstractCoder cipher_sm2 = EncryptionManager.getCipher(EncryptionManager.Model.SM2);

        Cursor cursor = null;
        //无公钥信息保存 将自己的公钥发送给好友
        cursor = db.rawQuery("SELECT public_key FROM HOSTS where WXID=?;", new String[]{MyApplication.getCurrentUser()});
        cursor.moveToNext();
        String public_ley = cursor.getString(0);


        cursor = db.rawQuery("select public_key from linktable where wxid =? and belong = ?", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
        /** 公钥在库*/
        if (cursor.moveToNext()) {
            if (cursor.moveToNext()) {
                /** 链接已建立*/
                Cursor mycursor = db.rawQuery("select public_key from FriendTable where wxid = ? and BELONG=?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
                if (mycursor.moveToNext()) {
                    String PK = mycursor.getString(0);
                    String sm2_key = cipher_sm2.simpleEnCode(cursor.getString(0), mycursor.getString(0));
                    return "建立连接@一方连接已建立" + sm2_key;
                    /**直接发送sm2加密的公钥去匹配*/
                }
                return "尝试让对方发送建立连接关键字";
            }
        } else {
            return "建立连接@PK" + public_ley;
        }
        /** 不在库*/
        return "尝试让对方发送建立连接关键字";
    }

    /**
     * PK操作
     */
    private String challenge_3(String content, String talker, SQLiteDatabase db) {
//        XposedBridge.log("进入challenge_3");
//        XposedBridge.log("数据库");
//        Database.show(db);

        AbstractCoder cipher_sm2 = EncryptionManager.getCipher(EncryptionManager.Model.SM2);
        String pk = content.substring(7);

        Cursor cursor = null;
        //无公钥信息保存 将自己的公钥发送给好友
        cursor = db.rawQuery("SELECT public_key FROM HOSTS where WXID=?;", new String[]{MyApplication.getCurrentUser()});
        cursor.moveToNext();
        String public_ley = cursor.getString(0);


        cursor = db.rawQuery("select public_key from FriendTable where wxid = ? and BELONG=?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
        if (cursor.moveToNext()) {

            String sm2_key = cipher_sm2.simpleEnCode(cursor.getString(0), pk);
            return "建立连接@一方连接已建立" + sm2_key;
        } else {
            /** 添加用户的公钥库*/
            db.execSQL("insert into linktable (wxid,public_key,belong) values(?,?,?)", new String[]{talker, pk, String.valueOf(MyApplication.getCurrentUserId())});
            String key = Enc.getKey();
            db.execSQL("insert into FriendTable (wxid,note,public_key,isEnc,isDec,BELONG) values(?,?,?,?,?,?);", new String[]{
                    talker, talker.substring(5), key, String.valueOf(1), String.valueOf(1), String.valueOf(MyApplication.getCurrentUserId())
            });

            String sm2_key = cipher_sm2.simpleEnCode(key, pk);
            return "建立连接@一方连接已建立" + sm2_key;
        }
    }

    private String challenge_2(String content, String talker, SQLiteDatabase db) {
        XposedBridge.log("进入chllenge3函数,这是我作为挑战者的反馈信号");
        Cursor cursor = db.rawQuery("select public_key from FriendTable where wxid = ? and BELONG=?;", new String[]{talker, String.valueOf(MyApplication.getCurrentUserId())});
        /** friendtable已经配置*/
        if (cursor.moveToNext()) {
            return "基于Xposed的加密通信已经建立成功";
        }

        cursor = db.rawQuery("SELECT private_key FROM HOSTS where WXID=?;", new String[]{MyApplication.getCurrentUser()});
        if (!cursor.moveToNext()) {
            XposedBridge.log("出现很奇怪的错误");
        }
        String private_ley = cursor.getString(0);
        AbstractCoder cipher_sm2 = EncryptionManager.getCipher(EncryptionManager.Model.SM2);
        String key = cipher_sm2.simpleDeCode(content.substring(12), private_ley);

        db.execSQL("insert into FriendTable (wxid,note,public_key,isEnc,isDec,BELONG) values(?,?,?,?,?,?);", new String[]{
                talker, talker.substring(5), key, String.valueOf(1), String.valueOf(1), String.valueOf(MyApplication.getCurrentUserId())
        });

        return "基于Xposed的加密通信已经建立成功";
    }

}
