
#import "RNTUmengPush.h"

#import <UMCommon/UMConfigure.h>
#import <UMPush/UMessage.h>
#import <React/RCTConvert.h>

RNTUmengPush *umengPushInstance;
NSDictionary *umengLaunchOptions;

// 获取自定义键值对
NSDictionary* getUmengCustom(NSDictionary *userInfo) {

    NSMutableDictionary *custom = [[NSMutableDictionary alloc] init];

    for (NSString *key in userInfo) {
        // d p aps 这三个是所有通知都带的字段
        if (![key isEqual: @"d"] && ![key isEqual: @"p"] && ![key isEqual: @"aps"]) {
            custom[key] = userInfo[key];
        }
    }

    return custom;

}

// 获取推送消息
NSMutableDictionary* getUmengNotification(NSDictionary *userInfo) {

    NSDictionary *custom = getUmengCustom(userInfo);

    NSMutableDictionary *resultDict = [[NSMutableDictionary alloc] init];
    resultDict[@"custom"] = custom;

    NSDictionary *alertDict = userInfo[@"aps"][@"alert"];
    if (alertDict) {
        resultDict[@"notification"] = @{
                              @"title": alertDict[@"title"] ?: @"",
                              @"subTitle": alertDict[@"subtitle"] ?: @"",
                              @"content": alertDict[@"body"] ?: @""
                          };
    }
    else {
        NSString *alertStr = userInfo[@"aps"][@"alert"];
        resultDict[@"notification"] = @{
                              @"title": alertStr ?: @"",
                              @"subTitle": @"",
                              @"content": @""
                          };
    }

    return resultDict;

}

@implementation RNTUmengPush

RCT_EXPORT_MODULE(RNTUmengPush);

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (instancetype)init {
    self = [super init];
    umengPushInstance = self;
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    umengPushInstance = nil;
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
      @"register",
      @"localNotification",
      @"remoteNotification",
  ];
}

+ (void)init:(NSDictionary *)launchOptions appKey:(NSString *)appKey channel:(NSString *)channel debug:(BOOL)debug {
    
    umengLaunchOptions = launchOptions;
    
    [UMConfigure initWithAppkey:appKey channel:channel];
    [UMConfigure setLogEnabled:debug];
    
}

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {

    if (![deviceToken isKindOfClass:[NSData class]]) {
        return;
    }

    const unsigned *tokenBytes = (const unsigned *)[deviceToken bytes];
    NSString *hexToken = [NSString stringWithFormat:@"%08x%08x%08x%08x%08x%08x%08x%08x",
                          ntohl(tokenBytes[0]), ntohl(tokenBytes[1]), ntohl(tokenBytes[2]),
                          ntohl(tokenBytes[3]), ntohl(tokenBytes[4]), ntohl(tokenBytes[5]),
                          ntohl(tokenBytes[6]), ntohl(tokenBytes[7])];

    NSMutableDictionary *body;
    
    if ([umengLaunchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]) {
        NSDictionary *userInfo = [umengLaunchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
        body = getUmengNotification(userInfo);
    }
    else {
        body = [[NSMutableDictionary alloc] init];
    }
    
    body[@"deviceToken"] = hexToken;
    
    if (umengPushInstance != nil) {
        [umengPushInstance sendEventWithName:@"register" body:body];
    }

}

+ (void)didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {

    if ([[[UIDevice currentDevice] systemVersion]intValue] < 10) {
        [UMessage didReceiveRemoteNotification:userInfo];
        if (umengPushInstance != nil) {
            [umengPushInstance sendEventWithName:@"remoteNotification" body:getUmengNotification(userInfo)];
        }
    }

    completionHandler(UIBackgroundFetchResultNewData);

}

+ (void)willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler API_AVAILABLE(ios(10.0)) {

    NSDictionary *userInfo = notification.request.content.userInfo;

    if ([notification.request.trigger isKindOfClass:[UNPushNotificationTrigger class]]) {
        // 应用处于前台时的远程推送
        [UMessage didReceiveRemoteNotification:userInfo];
        if (umengPushInstance != nil) {
            NSMutableDictionary *body = getUmengNotification(userInfo);
            body[@"presented"] = @YES;
            [umengPushInstance sendEventWithName:@"remoteNotification" body:body];
        }
    }
    else {
        // 应用处于前台时的本地推送接受
    }

    completionHandler(UNNotificationPresentationOptionSound|UNNotificationPresentationOptionBadge|UNNotificationPresentationOptionAlert);

}

+ (void)didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler API_AVAILABLE(ios(10.0)) {

    NSDictionary *userInfo = response.notification.request.content.userInfo;

    if ([response.notification.request.trigger isKindOfClass:[UNPushNotificationTrigger class]]) {
        // 应用处于后台时的远程推送
        [UMessage didReceiveRemoteNotification:userInfo];
        if (umengPushInstance != nil) {
            NSMutableDictionary *body = getUmengNotification(userInfo);
            body[@"clicked"] = @YES;
            [umengPushInstance sendEventWithName:@"remoteNotification" body:body];
        }
    }
    else {
        // 应用处于后台时的本地推送接受
    }

}





// 获取 device token
RCT_EXPORT_METHOD(start) {

    // Push 组件基本功能配置
    UMessageRegisterEntity * entity = [[UMessageRegisterEntity alloc] init];

    // type 是对推送的几个参数的选择，可以选择一个或者多个
    // 默认是三个全部打开，即：声音，弹窗，角标
    entity.types = UMessageAuthorizationOptionBadge|UMessageAuthorizationOptionSound|UMessageAuthorizationOptionAlert;

    // 如果你期望使用交互式(只有iOS 8.0及以上有)的通知，请参考下面注释部分的初始化代码
    if (([[[UIDevice currentDevice] systemVersion]intValue] >= 8) && ([[[UIDevice currentDevice] systemVersion]intValue] < 10)) {

        UIMutableUserNotificationAction *action1 = [[UIMutableUserNotificationAction alloc] init];
        action1.identifier = @"action1_identifier";
        action1.title = @"打开应用";
        // 当点击的时候启动程序
        action1.activationMode = UIUserNotificationActivationModeForeground;

        UIMutableUserNotificationAction *action2 = [[UIMutableUserNotificationAction alloc] init];
        action2.identifier = @"action2_identifier";
        action2.title = @"忽略";
        // 当点击的时候不启动程序，在后台处理
        action2.activationMode = UIUserNotificationActivationModeBackground;
        // 需要解锁才能处理，如果action.activationMode = UIUserNotificationActivationModeForeground; 则这个属性被忽略
        action2.authenticationRequired = YES;
        // 是否显示为消极按钮
        action2.destructive = YES;

        UIMutableUserNotificationCategory *category1 = [[UIMutableUserNotificationCategory alloc] init];
        // 这组动作的唯一标示
        category1.identifier = @"category1";
        [category1 setActions:@[action1, action2] forContext:(UIUserNotificationActionContextDefault)];

        NSSet *categories = [NSSet setWithObjects:category1, nil];
        entity.categories = categories;

    }
    // 如果要在 iOS10 显示交互式的通知，必须注意实现以下代码
    else if (@available(iOS 10.0, *)) {

        UNNotificationAction *action1 = [UNNotificationAction actionWithIdentifier:@"action1_identifier" title:@"打开应用" options:UNNotificationActionOptionForeground];

        UNNotificationAction *action2 = [UNNotificationAction actionWithIdentifier:@"action2_identifier" title:@"忽略" options:UNNotificationActionOptionForeground];

        // UNNotificationCategoryOptionNone
        // UNNotificationCategoryOptionCustomDismissAction  清除通知被触发会走通知的代理方法
        // UNNotificationCategoryOptionAllowInCarPlay       适用于行车模式
        UNNotificationCategory *category1 = [UNNotificationCategory categoryWithIdentifier:@"category1" actions:@[action1, action2]   intentIdentifiers:@[] options:UNNotificationCategoryOptionCustomDismissAction];

        NSSet *categories = [NSSet setWithObjects:category1, nil];
        entity.categories=categories;

    }

    dispatch_async(dispatch_get_main_queue(), ^{
       
        [UMessage registerForRemoteNotificationsWithLaunchOptions:umengLaunchOptions Entity:entity completionHandler:^(BOOL granted, NSError * _Nullable error) {

            if (!granted) {
                [self sendEventWithName:@"register" body:@{
                    @"error": @"permissions is not granted."
                }];
            }
            else if (error != nil) {
                [self sendEventWithName:@"register" body:@{
                    @"error": error.localizedDescription
                }];
            }

        }];
        
    });
    
}

// 获取所有标签
RCT_EXPORT_METHOD(getTags:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {

    [UMessage getTags:^(NSSet * _Nonnull responseTags, NSInteger remain, NSError * _Nonnull error) {
        if (error) {
            NSString *msg = [self getErrorMessage:error];
            reject([NSString stringWithFormat:@"%ld", (long)error.code], msg, nil);
        }
        else {
            if ([responseTags isKindOfClass:[NSSet class]]) {
                NSArray *tags = responseTags.allObjects;
                resolve(@{
                    @"tags": tags,
                });
                return;
            }
            reject(@"-1", @"error", nil);
        }
    }];

}

// 添加标签
RCT_EXPORT_METHOD(addTags:(NSArray *)tags
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {

    [UMessage addTags:tags response:^(id  _Nonnull responseObject, NSInteger remain, NSError * _Nonnull error) {

        if (error) {
            NSString *msg = [self getErrorMessage:error];
            reject([NSString stringWithFormat:@"%ld", (long)error.code], msg, nil);
        }
        else {
            if ([responseObject isKindOfClass:[NSDictionary class]]) {
                NSDictionary *dict = responseObject;
                if ([dict[@"success"] isEqualToString:@"ok"]) {
                    resolve(@{
                        @"remain": @(remain),
                    });
                    return;
                }
            }
            reject(@"-1", @"error", nil);
        }

    }];

}

// 删除标签
RCT_EXPORT_METHOD(removeTags:(NSArray *)tags
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {

    [UMessage deleteTags:tags response:^(id  _Nonnull responseObject, NSInteger remain, NSError * _Nonnull error) {

        if (error) {
            NSString *msg = [self getErrorMessage:error];
            reject([NSString stringWithFormat:@"%ld", (long)error.code], msg, nil);
        }
        else {
            if ([responseObject isKindOfClass:[NSDictionary class]]) {
                NSDictionary *dict = responseObject;
                if ([dict[@"success"] isEqualToString:@"ok"]) {
                    resolve(@{
                        @"remain": @(remain),
                    });
                    return;
                }
            }
            reject(@"-1", @"error", nil);
        }

    }];

}

// 重置别名
RCT_EXPORT_METHOD(setAlias:(NSString *)alias
                  type:(NSString *)type
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {

    NSString *innerType = [self getAliasType:type];

    [UMessage setAlias:alias type:innerType response:^(id  _Nonnull responseObject, NSError * _Nonnull error) {

        if (error) {
            NSString *msg = [self getErrorMessage:error];
            reject([NSString stringWithFormat:@"%ld", (long)error.code], msg, nil);
        }
        else {
            if ([responseObject isKindOfClass:[NSDictionary class]]) {
                NSDictionary *dict = responseObject;
                if ([dict[@"success"] isEqualToString:@"ok"]) {
                    resolve(@{});
                    return;
                }
            }
            reject(@"-1", @"error", nil);
        }

    }];

}

// 绑定别名
RCT_EXPORT_METHOD(addAlias:(NSString *)alias
                  type:(NSString *)type
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {

    NSString *innerType = [self getAliasType:type];

    [UMessage addAlias:alias type:innerType response:^(id  _Nonnull responseObject, NSError * _Nonnull error) {

        if (error) {
            NSString *msg = [self getErrorMessage:error];
            reject([NSString stringWithFormat:@"%ld", (long)error.code], msg, nil);
        }
        else {
            if ([responseObject isKindOfClass:[NSDictionary class]]) {
                NSDictionary *dict = responseObject;
                if ([dict[@"success"] isEqualToString:@"ok"]) {
                    resolve(@{});
                    return;
                }
            }
            reject(@"-1", @"error", nil);
        }

    }];

}

// 移除别名
RCT_EXPORT_METHOD(removeAlias:(NSString *)alias
                  type:(NSString *)type
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {

    NSString *innerType = [self getAliasType:type];

    [UMessage removeAlias:alias type:innerType response:^(id  _Nonnull responseObject, NSError * _Nonnull error) {

        if (error) {
            NSString *msg = [self getErrorMessage:error];
            reject([NSString stringWithFormat:@"%ld", (long)error.code], msg, nil);
        }
        else {
            if ([responseObject isKindOfClass:[NSDictionary class]]) {
                NSDictionary *dict = responseObject;
                if ([dict[@"success"] isEqualToString:@"ok"]) {
                    resolve(@{});
                    return;
                }
            }
            reject(@"-1", @"error", nil);
        }

    }];

}

// 高级设置
RCT_EXPORT_METHOD(setAdvanced:(NSDictionary*)options) {

    // 设置是否允许 SDK 自动清空角标，默认自动角标清零
    if ([options objectForKey:@"badgeClear"]) {
        [UMessage setBadgeClear:[RCTConvert BOOL:options[@"badgeClear"]]];
    }

    // 设置是否允许 SDK 当应用在前台运行收到 Push 时弹出 Alert 框
    if ([options objectForKey:@"autoAlert"]) {
        [UMessage setAutoAlert:[RCTConvert BOOL:options[@"autoAlert"]]];
    }

}

- (NSString *)getAliasType:(NSString *)type {

    NSString *aliasType = @"custom";

    // 新浪微博
    if ([type isEqualToString:@"sina"]) {
        aliasType = kUMessageAliasTypeSina;
    }
    // 腾讯微博
    else if ([type isEqualToString:@"tencent"]) {
        aliasType = kUMessageAliasTypeTencent;
    }
    // QQ
    else if ([type isEqualToString:@"qq"]) {
        aliasType = kUMessageAliasTypeQQ;
    }
    // 微信
    else if ([type isEqualToString:@"weixin"]) {
        aliasType = kUMessageAliasTypeWeiXin;
    }
    // 百度
    else if ([type isEqualToString:@"baidu"]) {
        aliasType = kUMessageAliasTypeBaidu;
    }
    // 人人网
    else if ([type isEqualToString:@"renren"]) {
        aliasType = kUMessageAliasTypeRenRen;
    }
    // 开心网
    else if ([type isEqualToString:@"kaixin"]) {
        aliasType = kUMessageAliasTypeKaixin;
    }
    // 豆瓣
    else if ([type isEqualToString:@"douban"]) {
        aliasType = kUMessageAliasTypeDouban;
    }
    // facebook
    else if ([type isEqualToString:@"facebook"]) {
        aliasType = kUMessageAliasTypeFacebook;
    }
    // twitter
    else if ([type isEqualToString:@"twitter"]) {
        aliasType = kUMessageAliasTypeTwitter;
    }

    return aliasType;

}

- (NSString *)getErrorMessage:(NSError *)error {
    switch (error.code) {
        case kUMessageErrorUnknown:
            return @"未知错误";
            break;
        case kUMessageErrorResponseErr:
            return @"响应出错";
            break;
        case kUMessageErrorOperateErr:
            return @"操作失败";
            break;
        case kUMessageErrorParamErr:
            return @"参数非法";
            break;
        case kUMessageErrorDependsErr:
            return @"条件不足(如：还未获取device_token，添加tag是不成功的)";
            break;
        case kUMessageErrorServerSetErr:
            return @"服务器限定操作";
            break;
        default:
            break;
    }
    return error.localizedDescription;
}

@end
