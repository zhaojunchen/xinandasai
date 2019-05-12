package com.inforse.annoywechat


import android.app.Activity
import android.content.ContentValues
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.gh0u1l5.wechatmagician.spellbook.SpellBook
import com.gh0u1l5.wechatmagician.spellbook.SpellBook.isImportantWechatProcess
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryVerbosely
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class WechatHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {

            if (lpparam.packageName.equals("com.inforse.annoywechat")) {
                XposedHelpers.findAndHookMethod(
                    "com.inforse.annoywechat.MainActivity", lpparam.classLoader, "isModuleActive",
                    XC_MethodReplacement.returnConstant(true)
                )
            }
            XposedBridge.log("success")
            if (SpellBook.isImportantWechatProcess(lpparam)) {
                XposedBridge.log("hello,wechat")
            }
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }

        /**这个模块hook这个UI气泡改变时候调用的函数,
         * 首先获得适配器的,在一次获得适配适配器的item
         * 根据item里面的属性(issend、content等属性
         * 来实现我们的功能*/
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.a.a",
            lpparam.classLoader,
            "notifyDataSetChanged",
            object : XC_MethodHook() {
                /**在适配器对UI更新前,获取里面的参数并修改内容*/
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    /**获取适配器对象*/
                    val baseAdapter = param!!.thisObject as BaseAdapter
                    /**遍历适配器,获取item */
                    for (i in 0 until baseAdapter.count) {
                        val item = baseAdapter.getItem(i)
                        XposedBridge.log("item有那些属性::${item.toString()}")
                        if (XposedHelpers.getIntField(item, "field_type") == 1 && (XposedHelpers.getIntField(
                                item,
                                "field_isSend"
                            ) == 1)

                        ) {
                            /**经过验证有以下字段的结论:
                             * field_msgId type:long 貌似是每条信息的存储ID
                             * field_content type:string 是从数据库取回来信息
                             * field_talker  type:string 对话者的WXID_ID
                             * field_transContent : type暂时未知*/
                            val longField = XposedHelpers.getLongField(item, "field_msgId")
                            val objectField = XposedHelpers.getObjectField(item, "field_transContent")
                            var str = XposedHelpers.getObjectField(item, "field_content") as String
                            val str2 = XposedHelpers.getObjectField(item, "field_talker") as String
//                            val str1 = XposedHelpers.getObjectField(item, "field_transContent") as String
//                            val str1 = XposedHelpers.getObjectField(item, "field_transContent") as String
                            XposedBridge.log("str::filed_content是什么$str")
//                            XposedBridge.log("str1::field_transContent$str1")
                            XposedBridge.log("str2::filed_talker是什么$str2")
                            if ((if (objectField == null) null else objectField as String) == null) {
                                XposedHelpers.setObjectField(item, "field_transContent", "测试")
                                XposedHelpers.setObjectField(
                                    item,
                                    "field_content",
                                    "$str+$str"
                                )
                                XposedBridge.log("刷新str为双倍的str")
                            }
                        } else {
                            XposedBridge.log("error")
                        }
                    }
                }
            }
        )

        tryVerbosely {
            if (isImportantWechatProcess(lpparam)) {
                SpellBook.startup(lpparam, listOf(Message, Alert))
            }
        }

    }
}

//消息监控
object Message : IDatabaseHook {
    override fun onDatabaseInserting(
        thisObject: Any,
        table: String,
        nullColumnHack: String?,
        initialValues: ContentValues?,
        conflictAlgorithm: Int
    ): Operation<Long> {
        if (table == "message") {
            //在这里进行修改
            val talker: String? = initialValues?.getAsString("talker")
            var message: String? = initialValues?.getAsString("content")

            /** 消息发送  只是针对单个用户的消息进行接收*/
            if (initialValues?.get("isSend") == 0) {
                tryVerbosely {
                    if (talker != null && talker.startsWith("wxid_")) {
                        initialValues?.remove("content")
                        initialValues.put("content", "wxid" + "$message")
                    }
                }
                /** 消息接收 只是针对单个用户的消息进行发送修改*/
            } else {
                if (talker != null && talker.startsWith("wxid_")) {
                    initialValues?.remove("content")
                    initialValues.put("content", "wxid" + "$message")
                }
            }

        }

        return super.onDatabaseInserting(thisObject, table, nullColumnHack, initialValues, conflictAlgorithm)
    }
}

//活动监控
object Alert : IActivityHook {
    override fun onActivityStarting(activity: Activity) {
        Toast.makeText(activity, "Welcome to our party", Toast.LENGTH_SHORT).show()
    }
}




