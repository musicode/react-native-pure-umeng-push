
import { NativeModules } from 'react-native'

const { RNTDimension } = NativeModules

export default {

  getStatusBarHeight() {
    return RNTDimension.getStatusBarHeight()
  },

  getNavigationBarInfo() {
    return RNTDimension.getNavigationBarInfo()
  },

  getScreenSize() {
    return RNTDimension.getScreenSize()
  },

  getSafeArea() {
    return RNTDimension.getSafeArea()
  },

}
