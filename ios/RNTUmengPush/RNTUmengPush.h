#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>
#import <UserNotifications/UserNotifications.h>

@interface RNTUmengPush : RCTEventEmitter <RCTBridgeModule, UNUserNotificationCenterDelegate>

+ (void)init:(NSString *)appKey launchOptions:(NSDictionary *)launchOptions debug:(BOOL)debug;

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
         
+ (void)didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler;

@end
