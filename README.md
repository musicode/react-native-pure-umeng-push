# react-native-pure-umeng-push

友盟推送

## Installation

```
npm i react-native-pure-umeng-push
// link below 0.60
react-native link react-native-pure-umeng-push
```

## Setup

### iOS

打开推送开关。

![](http://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/assets/pic/66734/UMDP_zh/1518004555215/pushswitch.png)

打开后台推送权限设置

![](http://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/assets/pic/66734/UMDP_zh/1518004619406/background.png)

修改 `AppDelegate.m`，如下

```oc
#import <RNTUmengPush.h>

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  ...
  [RNTUmengPush init:@"appKey" launchOptions:launchOptions debug:false];
  return YES;
}

- (void)application:(UIApplication *)application
didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
  [RNTUmengPush didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
  [RNTUmengPush didReceiveRemoteNotification:userInfo fetchCompletionHandler:completionHandler];
}
```

### Android

`android/build.gradle` add the umeng maven repo.

```
allprojects {
    repositories {
        // add this line
        maven { url 'https://dl.bintray.com/umsdk/release' }
    }
}
```

在 `MainApplication` 的 `onCreate` 方法进行初始化，如下：

```kotlin
override fun onCreate() {
    // 点击推送跳转到的 activity
    // 通常 react native 只有一个 main activity
    UmengPushActivity.mainActivityClass = MainActivity::class.java
    // 初始化
    RNTUmengPushModule.init(this, "appKey", "appSecret", "渠道名，如xiaomi/huawei", false)
    // 如果需要厂商通道，按需调用
    RNTUmengPushModule.huawei(this)
    RNTUmengPushModule.xiaomi(this, "appId", "appKey")
    RNTUmengPushModule.oppo(this, "appId", "appSecret")
    RNTUmengPushModule.vivo(this)
    RNTUmengPushModule.meizu(this, "appId", "appKey")
}
```

配置厂商通道请先阅读[官方文档](https://developer.umeng.com/docs/66632/detail/98589)，主要是获取各个通道的 `appId`、`appKey`、`appSecret` 等数据，并保存到友盟后台的应用信息里，此外还需要进行一些额外配置，如下。

### 配置华为

在 `AndroidManifest.xml` 中 `application` 标签下配置华为的 `appId`，如下：

```xml
<meta-data
    android:name="com.huawei.hms.client.appid"
    android:value="你的华为应用的 app id"
/>
```

### 配置 vivo

在 `AndroidManifest.xml` 中 `application` 标签下配置 vivo 的 `appId` 和 `appKey`，如下：

```xml
<meta-data
    android:name="com.vivo.push.app_id"
    android:value="你的 vivo 应用的 app id"
/>
<meta-data
    android:name="com.vivo.push.api_key"
    android:value="你的 vivo 应用的 app key"
/>
```

### 配置魅族

配置系统通知图标

请在 `drawable` 目录下添加一个图标，命名为 `stat_sys_third_app_notify.png`，建议尺寸 `64px * 64px`，图标四周留有透明。若不添加此图标，可能在部分魅族手机上无法弹出通知。

## Usage

```js
import umengPush from 'react-native-pure-umeng-push'

umengPush.addListener(
  'register',
  function (data) {
    data.deviceToken
    // 如果 app 未启动状态下，点击推送打开 app，会有两个新字段
    // 点击的推送
    data.notification
    // 推送的自定义参数
    data.custom
  }
)

umengPush.addListener(
  'remoteNotification',
  function (data) {
    // 如果点击了推送，data.clicked 是 true
    data.clicked
    // 如果推送送达并展示了，data.presented 是 true
    data.presented

    // 推送详情，如标题、内容
    data.notification
    // 推送的自定义参数
    data.custom
  }
)

// 启动
umengPush.start()
```
