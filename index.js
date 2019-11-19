
import { NativeEventEmitter, NativeModules } from 'react-native'

const { RNTUmengPush } = NativeModules

const eventEmitter = new NativeEventEmitter(RNTUmengPush)

eventEmitter.addListener('deviceToken', function (data) {
  console.log('deviceToken', data)
})

eventEmitter.addListener('localNotification', function (data) {
  console.log('localNotification', data)
})

eventEmitter.addListener('remoteNotification', function (data) {
  console.log('remoteNotification', data)
})

export default {

  start(appKey) {
    return RNTUmengPush.start(appKey)
  },

}
