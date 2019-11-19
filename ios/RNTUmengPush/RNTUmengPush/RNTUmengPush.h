#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>
#import <UserNotifications/UserNotifications.h>

@interface RNTUmengPush : RCTEventEmitter <RCTBridgeModule>

+ (void)didFinishLaunchingWithOptions:(NSDictionary *)launchOptions;

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;

+ (void)didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler;

// 下面两个是 ios10 以上专用
+ (void)willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler API_AVAILABLE(ios(10.0));

+ (void)didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler API_AVAILABLE(ios(10.0));
                                                                                                                                                
@end
