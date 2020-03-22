package com.github.musicode.umengpush

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.*
import com.umeng.message.entity.UMessage
import com.umeng.message.tag.TagManager
import org.android.agoo.huawei.HuaWeiRegister
import org.android.agoo.mezu.MeizuRegister
import org.android.agoo.oppo.OppoRegister
import org.android.agoo.vivo.VivoRegister
import org.android.agoo.xiaomi.MiPushRegistar
import org.json.JSONException
import org.json.JSONObject

class RNTUmengPushModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {

        var launchMessage = ""

        private var deviceToken = ""

        private var pushModule: RNTUmengPushModule? = null

        private fun isMainThread(): Boolean {
            return Looper.getMainLooper() == Looper.myLooper()
        }

        fun init(app: Application, appKey: String, appSecret: String, channel: String, debug: Boolean) {

            UMConfigure.setLogEnabled(debug)
            UMConfigure.init(app, appKey, channel, UMConfigure.DEVICE_TYPE_PHONE, appSecret)

            val pushAgent = PushAgent.getInstance(app)

            if (debug) {
                pushAgent.isPushCheck = true
            }

            // app 在前台时是否显示推送
            // 在 pushAgent.register 方法之前调用
            pushAgent.setNotificaitonOnForeground(true)

            pushAgent.messageHandler = object : UmengMessageHandler() {
                override fun dealWithNotificationMessage(context: Context?, msg: UMessage?) {
                    super.dealWithNotificationMessage(context, msg)
                    pushModule?.let {
                        if (msg != null) {
                            it.onNotificationPresented(msg)
                        }
                    }
                }

                override fun dealWithCustomMessage(context: Context?, msg: UMessage?) {
                    pushModule?.let {
                        if (msg != null) {
                            it.onMessage(msg)
                        }
                    }
                }
            }

            // 自定义通知栏打开动作，让 js 去处理
            pushAgent.notificationClickHandler = object : UmengNotificationClickHandler() {
                override fun launchApp(context: Context?, msg: UMessage?) {
                    msg?.let {
                        pushModule?.onNotificationClicked(it)
                    }
                }

                override fun openUrl(context: Context?, msg: UMessage?) {
                    msg?.let {
                        pushModule?.onNotificationClicked(it)
                    }
                }

                override fun openActivity(context: Context?, msg: UMessage?) {
                    msg?.let {
                        pushModule?.onNotificationClicked(it)
                    }
                }

                override fun dealWithCustomAction(context: Context?, msg: UMessage?) {
                    msg?.let {
                        pushModule?.onNotificationClicked(it)
                    }
                }
            }

            pushAgent.register(object : IUmengRegisterCallback {
                override fun onSuccess(token: String) {
                    deviceToken = token
                }
                override fun onFailure(code: String, msg: String) {}
            })

        }

        fun xiaomi(app: Application, appId: String, appKey: String) {
            if (isMainThread()) {
                MiPushRegistar.register(app, appId, appKey)
            }
        }

        fun huawei(app: Application) {
            if (isMainThread()) {
                HuaWeiRegister.register(app)
            }
        }

        fun meizu(app: Application, appId: String, appKey: String) {
            if (isMainThread()) {
                MeizuRegister.register(app, appId, appKey)
            }
        }

        fun oppo(app: Application, appId: String, appSecret: String) {
            if (isMainThread()) {
                OppoRegister.register(app, appId, appSecret)
            }
        }

        fun vivo(app: Application) {
            if (isMainThread()) {
                VivoRegister.register(app)
            }
        }

    }

    private var pushAgent = PushAgent.getInstance(reactContext)

    init {
        // 日活统计及多维度推送的必调用方法
        pushAgent.onAppStart()
        reactContext.addActivityEventListener(this)
    }

    override fun getName(): String {
        return "RNTUmengPush"
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        pushModule = null
    }

    override fun initialize() {
        super.initialize()
        pushModule = this
    }

    @ReactMethod
    fun start() {

        if (deviceToken.isEmpty()) {
            return
        }

        // 接收启动 app 的推送
        val map = Arguments.createMap()
        map.putString("deviceToken", deviceToken)

        // 启动参数
        if (launchMessage.isNotEmpty()) {
            val msg = UMessage(JSONObject(launchMessage))
            map.putMap("notification", formatNotification(msg))
            map.putMap("custom", formatCustom(msg))
            launchMessage = ""
        }

        sendEvent("register", map)

    }

    @ReactMethod
    fun enable() {

        pushAgent.enable(object : IUmengCallback {
            override fun onSuccess() {}
            override fun onFailure(s: String, s1: String) {}
        })

    }

    @ReactMethod
    fun disable() {

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
            TagManager.TCallBack { isSuccess, result ->
                if (isSuccess) {
                    val map = Arguments.createMap()
                    result?.let {
                        map.putInt("remain", it.remain)
                    }
                    promise.resolve(map)
                } else {
                    promise.reject("-1", "error")
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
            TagManager.TCallBack { isSuccess, result ->
                if (isSuccess) {
                    val map = Arguments.createMap()
                    result?.let {
                        map.putInt("remain", it.remain)
                    }
                    promise.resolve(map)
                } else {
                    promise.reject("-1", "error")
                }
            },
            *list.toTypedArray()
        )

    }

    @ReactMethod
    fun setAlias(alias: String, type: String, promise: Promise) {

        pushAgent.setAlias(alias, getAliasType(type)) { isSuccess, _ ->
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
    fun addAlias(alias: String, type: String, promise: Promise) {

        pushAgent.addAlias(alias, getAliasType(type)) { isSuccess, _ ->
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

        pushAgent.deleteAlias(alias, getAliasType(type)) { isSuccess, _ ->
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

    private fun getAliasType(type: String): String {
        return when (type) {
            "sina" -> {
               "weibo"
            }
            "tencent" -> {
                "tencent_weibo"
            }
            "qq" -> {
                "qq"
            }
            "weixin" -> {
                "weixin"
            }
            "baidu" -> {
                "baidu"
            }
            "renren" -> {
                "renren"
            }
            "kaixin" -> {
                "kaixin"
            }
            "douban" -> {
                "douban"
            }
            "facebook" -> {
                "facebook"
            }
            "twitter" -> {
                "twitter"
            }
            else -> {
                "custom"
            }
        }
    }

    private fun onNotificationPresented(message: UMessage) {

        val map = Arguments.createMap()
        map.putMap("notification", formatNotification(message))
        map.putMap("custom", formatCustom(message))
        map.putBoolean("presented", true)

        sendEvent("remoteNotification", map)

    }

    private fun onNotificationClicked(message: UMessage) {

        val map = Arguments.createMap()
        map.putMap("notification", formatNotification(message))
        map.putMap("custom", formatCustom(message))
        map.putBoolean("clicked", true)

        sendEvent("remoteNotification", map)

    }

    private fun onMessage(message: UMessage) {

        val map = Arguments.createMap()
        map.putMap("message", formatMessage(message))
        map.putMap("custom", formatCustom(message))

        sendEvent("message", map)

    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
    }

    private fun formatNotification(msg: UMessage): WritableMap {
        val body = Arguments.createMap()
        body.putString("title", msg.title)
        body.putString("subTitle", "")
        body.putString("content", msg.text)
        return body
    }

    private fun formatCustom(msg: UMessage): WritableMap {
        val custom = Arguments.createMap()
        msg.extra?.let {
            for ((key,value) in it){
                custom.putString(key, value)
            }
        }
        return custom
    }

    private fun formatMessage(msg: UMessage): WritableMap {

        val customMessage = Arguments.createMap()
        val json = msg.custom

        if (json.startsWith("{") && json.endsWith("}")) {
            try {
                val obj = JSONObject(json)
                val iterator = obj.keys()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    customMessage.putString(key, obj.getString(key))
                }
            }
            catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        return customMessage
    }

    override fun onNewIntent(intent: Intent?) {

    }

    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {

    }

}