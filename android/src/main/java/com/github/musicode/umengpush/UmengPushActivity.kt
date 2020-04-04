package com.github.musicode.umengpush

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.umeng.message.UmengNotifyClickActivity
import org.android.agoo.common.AgooConstants

// com.github.musicode.umengpush.UmengPushActivity 要填写到友盟后台系统通道
class UmengPushActivity : UmengNotifyClickActivity() {

    companion object {
        lateinit var mainActivityClass: Class<*>
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.umeng_push_activity)
    }

    override fun onMessage(intent: Intent?) {

        // 统计 【打开数】【收到数】【忽略数】
        super.onMessage(intent)

        // 离线状态收到多条推送
        // 点击第一条会经此 activity 启动 app
        // 点击其他的依然会经此 activity，这里需要做区别
        val pushModule = RNTUmengPushModule.pushModule

        // 跳转到 main activity
        intent?.getStringExtra(AgooConstants.MESSAGE_BODY)?.let {
            if (it.isNotEmpty()) {
                if (pushModule == null) {
                    RNTUmengPushModule.launchMessage = it
                }
                else {
                    pushModule.onNotificationClicked(it)
                }
            }
        }

        val newIntent = Intent(this, mainActivityClass)
        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(newIntent)
        finish()

    }

}