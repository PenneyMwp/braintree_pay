#import "BraintreePayPlugin.h"
#import <braintree_pay/braintree_pay-Swift.h>

@implementation BraintreePayPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBraintreePayPlugin registerWithRegistrar:registrar];
}
@end
