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

nothing to do.

## Usage

```js
import dimension from 'react-native-pure-dimension'

dimension.getStatusBarHeight().then(data => {
  data.height
})

dimension.getNavigationBarInfo().then(data => {
  data.height
  data.visible
})

dimension.getScreenSize().then(data => {
  data.width
  data.height
})

dimension.getSafeArea().then(data => {
  data.top
  data.right
  data.bottom
  data.left
})

```
