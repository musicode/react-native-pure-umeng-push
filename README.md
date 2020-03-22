# react-native-pure-dimension

友盟推送烂的一笔，集成完收不到推送，不用往下看了。

This is a module which help you get screen dimension info.

## Installation

```
npm i react-native-pure-dimension
// link below 0.60
react-native link react-native-pure-dimension
```

## Setup

### iOS

modify `AppDelegate.m`

```oc
#import <RNTDimension.h>

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  ...
  // add this line
  [RNTDimension bind:rootView];
  return YES;
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

配置厂商通道请先阅读[官方文档](https://developer.umeng.com/docs/66632/detail/98589)，主要是获取各个通道的 `appId`、`appKey`、`appSecret` 等数据，并保存到友盟后台的应用信息里。

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


```
