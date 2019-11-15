package com.github.musicode.umengpush

import android.os.Build
import com.facebook.react.bridge.*
import android.util.DisplayMetrics
import android.view.Display
import android.content.Context.WINDOW_SERVICE
import android.view.WindowManager
import java.lang.Exception

class RNTUmengPushModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "RNTUmengPush"
    }

    @ReactMethod
    fun getStatusBarHeight(promise: Promise) {

        val resources = reactApplicationContext.resources
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")

        val height = if (resId > 0) {
            (resources.getDimensionPixelSize(resId) / resources.displayMetrics.density).toInt()
        }
        else {
            0
        }

        val map = Arguments.createMap()
        map.putInt("height", height)

        promise.resolve(map)

    }

    @ReactMethod
    fun getNavigationBarInfo(promise: Promise) {

        val resources = reactApplicationContext.resources

        val resId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val height = if (resId > 0) {
            (resources.getDimensionPixelSize(resId) / resources.displayMetrics.density).toInt()
        }
        else {
            0
        }

        val map = Arguments.createMap()
        map.putInt("height", height)
        map.putBoolean("visible", getNavigationBarVisible())

        promise.resolve(map)

    }

    @ReactMethod
    fun getScreenSize(promise: Promise) {

        // 跟 ios 保持一致，获取的是物理屏尺寸
        promise.resolve(getRealScreenSize())

    }

    @ReactMethod
    fun getSafeArea(promise: Promise) {

        val map = Arguments.createMap()
        map.putInt("top", 0)
        map.putInt("bottom", 0)
        map.putInt("left", 0)
        map.putInt("right", 0)

        val activity = currentActivity

        if (activity == null) {
            promise.resolve(map)
            return
        }

        // P 之前的版本都是厂商私有实现，懒得折腾了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val displayCutout = activity.window.decorView.rootWindowInsets?.displayCutout
            if (displayCutout != null) {
                val metrics = reactApplicationContext.resources.displayMetrics
                map.putInt("top", (displayCutout.safeInsetTop / metrics.density).toInt())
                map.putInt("bottom", (displayCutout.safeInsetBottom / metrics.density).toInt())
                map.putInt("left", (displayCutout.safeInsetLeft / metrics.density).toInt())
                map.putInt("right", (displayCutout.safeInsetRight / metrics.density).toInt())
            }
        }
        promise.resolve(map)

    }

    private fun getNavigationBarVisible(): Boolean {

        val realScreenSize = getRealScreenSize()
        val screenSize = getScreenSize()

        return realScreenSize.getInt("width") > screenSize.getInt("width")
                || realScreenSize.getInt("height") > screenSize.getInt("height")

    }

    private fun getScreenSize(): WritableMap {

        val metrics = reactApplicationContext.resources.displayMetrics

        val map = Arguments.createMap()
        map.putInt("width", (metrics.widthPixels / metrics.density).toInt())
        map.putInt("height", (metrics.heightPixels / metrics.density).toInt())

        return map

    }

    private fun getRealScreenSize(): WritableMap {

        val metrics = reactApplicationContext.resources.displayMetrics

        // See: http://developer.android.com/reference/android/view/Display.html#getRealMetrics(android.util.DisplayMetrics)
        if (Build.VERSION.SDK_INT >= 17) {
            val display = (reactContext.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
            try {
                Display::class.java.getMethod("getRealMetrics", DisplayMetrics::class.java).invoke(display, metrics)
            } catch (e: Exception) {
            }
        }

        val map = Arguments.createMap()
        map.putInt("width", (metrics.widthPixels / metrics.density).toInt())
        map.putInt("height", (metrics.heightPixels / metrics.density).toInt())

        return map

    }

}