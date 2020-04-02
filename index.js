
import { NativeEventEmitter, NativeModules } from 'react-native'

const { RNTUmengPush } = NativeModules

const eventEmitter = new NativeEventEmitter(RNTUmengPush)

// 初始化时配置的渠道
export const CHANNEL = RNTUmengPush.CHANNEL

export const ALIAS_TYPE_SINA = 'sina'
export const ALIAS_TYPE_TENCENT = 'tencent'
export const ALIAS_TYPE_QQ = 'qq'
export const ALIAS_TYPE_WEIXIN = 'weixin'
export const ALIAS_TYPE_BAIDU = 'baidu'
export const ALIAS_TYPE_RENREN = 'renren'
export const ALIAS_TYPE_KAIXIN = 'kaixin'
export const ALIAS_TYPE_DOUBAN = 'douban'
export const ALIAS_TYPE_FACEBOOK = 'facebook'
export const ALIAS_TYPE_TWITTER = 'twitter'

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
