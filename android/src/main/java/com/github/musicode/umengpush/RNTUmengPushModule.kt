package com.github.musicode.umengpush

import android.app.Activity
import android.app.Application
import android.content.Context
import com.facebook.react.bridge.*
import android.content.Intent
import android.util.Log
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.umeng.commonsdk.UMConfigure
import android.app.Notification
import com.umeng.message.*
import com.umeng.message.common.inter.ITagManager
import com.umeng.message.entity.UMessage
import com.umeng.message.tag.TagManager
import org.android.agoo.huawei.HuaWeiRegister

class RNTUmengPushModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener, LifecycleEventListener {

    companion object {
        fun init(app: Application, appKey: String, appSecret: String, channel: String) {
            UMConfigure.init(app, appKey, channel, UMConfigure.DEVICE_TYPE_PHONE, appSecret)
            HuaWeiRegister.register(app)
        }
    }

    private var pushAgent: PushAgent

    init {

        // 日活统计及多维度推送的必调用方法
        PushAgent.getInstance(reactContext).onAppStart()

        pushAgent = PushAgent.getInstance(reactContext)

        // 自定义通知栏打开动作，让 js 去处理
        pushAgent.notificationClickHandler = object : UmengNotificationClickHandler() {
            override fun dealWithCustomAction(context: Context?, msg: UMessage) {
                Log.d("umeng_push_action", msg.custom)
            }
        }
        
        pushAgent.messageHandler = object : UmengMessageHandler() {
            override fun getNotification(context: Context?, msg: UMessage?): Notification {

                msg?.let {

                    val map = Arguments.createMap()

                    for (entry in it.extra.entries) {

                        val key = entry.key
                        val value = entry.value
                    }

                    sendEvent("message", map)

                }
                return super.getNotification(context, msg)
            }
        }

        reactContext.addActivityEventListener(this)
        reactContext.addLifecycleEventListener(this)

    }

    override fun getName(): String {
        return "RNTUmengPush"
    }

    @ReactMethod
    fun start(accessKey: String, promise: Promise) {



        pushAgent.register(object : IUmengRegisterCallback {
            override fun onSuccess(deviceToken: String) {

                val body = Arguments.createMap()
                body.putString("deviceToken", deviceToken)

                sendEvent("register", body)

            }

            override fun onFailure(s: String, s1: String) {

                val body = Arguments.createMap()
                body.putString("error", "$s $1")

                sendEvent("register", body)

            }
        })

        promise.resolve(Arguments.createMap())

    }

    @ReactMethod
    fun stop() {

        pushAgent.disable(object : IUmengCallback {
            override fun onSuccess() {}
            override fun onFailure(s: String, s1: String) {}
        })

    }

    @ReactMethod
    fun getTags(promise: Promise) {

        pushAgent.tagManager.getTags { isSuccess, result ->
            if (isSuccess) {
                val map = Arguments.createMap()
                val list = Arguments.createArray()
                for (tag in result) {
                    list.pushString(tag)
                }
                map.putArray("tags", list)
                promise.resolve(result)
            }
            else {
                promise.reject("-1", "error")
            }
        }

    }

    @ReactMethod
    fun addTags(tags: ReadableArray, promise: Promise) {

        val list = ArrayList<String>()

        for (i in 0 until tags.size()) {
            tags.getString(i)?.let {
                list.add(it)
            }
        }

        pushAgent.tagManager.addTags(
            object: TagManager.TCallBack {
                override fun onMessage(isSuccess: Boolean, result: ITagManager.Result?) {
                    if (isSuccess) {
                        val map = Arguments.createMap()
                        result?.let {
                            map.putInt("remain", it.remain)
                        }
                        promise.resolve(map)
                    }
                    else {
                        promise.reject("-1", "error")
                    }
                }
            },
            *list.toTypedArray()
        )

    }

    @ReactMethod
    fun removeTags(tags: ReadableArray, promise: Promise) {

        val list = ArrayList<String>()

        for (i in 0 until tags.size()) {
            tags.getString(i)?.let {
                list.add(it)
            }
        }

        pushAgent.tagManager.deleteTags(
                object: TagManager.TCallBack {
                    override fun onMessage(isSuccess: Boolean, result: ITagManager.Result?) {
                        if (isSuccess) {
                            val map = Arguments.createMap()
                            result?.let {
                                map.putInt("remain", it.remain)
                            }
                            promise.resolve(map)
                        }
                        else {
                            promise.reject("-1", "error")
                        }
                    }
                },
                *list.toTypedArray()
        )

    }

    @ReactMethod
    fun addAlias(alias: String, type: String, promise: Promise) {

        pushAgent.addAlias(alias, type) { isSuccess, _ ->
            if (isSuccess) {
                val map = Arguments.createMap()
                promise.resolve(map)
            }
            else {
                promise.reject("-1", "error")
            }
        }

    }

    @ReactMethod
    fun setAlias(alias: String, type: String, promise: Promise) {

        pushAgent.setAlias(alias, type) { isSuccess, _ ->
            if (isSuccess) {
                val map = Arguments.createMap()
                promise.resolve(map)
            }
            else {
                promise.reject("-1", "error")
            }
        }

    }

    @ReactMethod
    fun removeAlias(alias: String, type: String, promise: Promise) {

        pushAgent.deleteAlias(alias, type) { isSuccess, _ ->
            if (isSuccess) {
                val map = Arguments.createMap()
                promise.resolve(map)
            }
            else {
                promise.reject("-1", "error")
            }
        }

    }

    @ReactMethod
    fun setAdvanced(options: ReadableMap) {

        // 自定义资源包名
        if (options.hasKey("resourcePackageName")) {
            pushAgent.resourcePackageName = options.getString("resourcePackageName")
        }

        // 为了便于开发者更好的集成配置文件，我们提供了对于AndroidManifest配置文件的检查工具，可以自行检查开发者的配置问题
        if (options.hasKey("pushCheck")) {
            pushAgent.isPushCheck = options.getBoolean("pushCheck")
        }

        // app 在前台时是否显示推送
        if (options.hasKey("notificaitonOnForeground")) {
            // 在 pushAgent.register 方法之前调用
            pushAgent.setNotificaitonOnForeground(
                options.getBoolean("notificaitonOnForeground")
            )
        }

        // 通知栏可以设置最多显示通知的条数
        // 当通知栏显示数目大于设置值，此时再有新通知到达时，会把旧的一条通知隐藏
        // 参数可以设置为0~10之间任意整数，当参数为 0 时，表示不合并通知
        if (options.hasKey("displayNotificationNumber")) {
            pushAgent.displayNotificationNumber = options.getInt("displayNotificationNumber")
        }

        // 默认情况下，同一台设备在1分钟内收到同一个应用的多条通知时，不会重复提醒，同时在通知栏里新的通知会替换掉旧的通知
        // 可以通过如下方法来设置冷却时间
        if (options.hasKey("muteDurationSeconds")) {
            pushAgent.muteDurationSeconds = options.getInt("muteDurationSeconds")
        }

        // 为免过度打扰用户，SDK默认在“23:00”到“7:00”之间收到通知消息时不响铃，不振动，不闪灯
        // 如果需要改变默认的静音时间，可以使用以下接口：
        if (options.hasKey("noDisturbStartHour")
            && options.hasKey("noDisturbStartMinute")
            && options.hasKey("noDisturbEndHour")
            && options.hasKey("noDisturbEndMinute")
        ) {
            pushAgent.setNoDisturbMode(
                options.getInt("noDisturbStartHour"),
                options.getInt("noDisturbStartMinute"),
                options.getInt("noDisturbEndHour"),
                options.getInt("noDisturbEndMinute")
            )
        }

        // 0: MsgConstant.NOTIFICATION_PLAY_SERVER
        // 1: MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE
        // 2: MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE

        // 是否响铃
        if (options.hasKey("notificationPlaySound")) {
            pushAgent.notificationPlaySound = options.getInt("notificationPlaySound")
        }

        // 是否点亮呼吸灯
        if (options.hasKey("notificationPlayLights")) {
            pushAgent.notificationPlayLights = options.getInt("notificationPlayLights")
        }

        // 是否振动
        if (options.hasKey("notificationPlayVibrate")) {
            pushAgent.notificationPlayVibrate = options.getInt("notificationPlayVibrate")
        }

    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
    }

    override fun onNewIntent(intent: Intent?) {
        currentActivity?.intent = intent
    }

    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {

    }

    override fun onHostResume() {

    }

    override fun onHostPause() {

    }

    override fun onHostDestroy() {

    }
}