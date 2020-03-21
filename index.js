
import { NativeEventEmitter, NativeModules } from 'react-native'

const { RNTUmengPush } = NativeModules

const eventEmitter = new NativeEventEmitter(RNTUmengPush)

export default {

  start() {
    RNTUmengPush.start()
  },

  getTags() {
    return RNTUmengPush.getTags()
  },

  addTags(tags) {
    return RNTUmengPush.addTags(tags)
  },

  removeTags(tags) {
    return RNTUmengPush.removeTags(tags)
  },

  setAlias(alias, type) {
    return RNTUmengPush.setAlias(alias, type)
  },

  addAlias(alias, type) {
    return RNTUmengPush.addAlias(alias, type)
  },

  removeAlias(alias, type) {
    return RNTUmengPush.removeAlias(alias, type)
  },

  setAdvanced(options) {
    RNTUmengPush.setAdvanced(options)
  },

  addListener(type, listener) {
    return eventEmitter.addListener(type, listener)
  },

}
