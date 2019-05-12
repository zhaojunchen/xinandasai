package com.rliget.wechathook

import android.app.Activity
import android.content.ContentValues
import android.widget.Toast
import com.gh0u1l5.wechatmagician.spellbook.SpellBook
import com.gh0u1l5.wechatmagician.spellbook.SpellBook.isImportantWechatProcess
import com.gh0u1l5.wechatmagician.spellbook.base.Operation
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IDatabaseHook
import com.gh0u1l5.wechatmagician.spellbook.util.BasicUtil.tryVerbosely
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class WechatHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            if (lpparam.packageName == "com.rliget.wechathook") {
                XposedHelpers.findAndHookMethod("com.rliget.wechathook.MainActivity", lpparam.classLoader, "isModuleActive",
                    XC_MethodReplacement.returnConstant(true))
            }
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
        tryVerbosely {
            if (isImportantWechatProcess(lpparam)) {
                SpellBook.startup(lpparam, listOf(Message))
            }
        }
    }
}

//活动监控
object Alert : IActivityHook {
    override fun onActivityStarting(activity: Activity) {
        Toast.makeText(activity, "hello wechat", Toast.LENGTH_SHORT).show()
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
            if (initialValues?.get("isSend") == 0) {
                initialValues.remove("content")
                initialValues.put("content", "这是接收的消息")
            } else {
                initialValues?.remove("content")
                initialValues?.put("content", "这是发送的消息")
            }
        }
        return super.onDatabaseInserting(thisObject, table, nullColumnHack, initialValues, conflictAlgorithm)
    }
}