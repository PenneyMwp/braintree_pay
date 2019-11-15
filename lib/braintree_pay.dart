import 'dart:async';

import 'package:flutter/services.dart';

class BraintreePay {
  static const MethodChannel _channel =
      const MethodChannel('com.mwp.braintreepay');
  static const String KEY_DEPOSIT = "deposit";
  static const String KEY_HOURLY_AMOUNT = "hAmount";

  static Future<String> get platform async {
    final String version = await _channel.invokeMethod('platform');
    return version;
  }

  static Future<Map> startCreditCard(String deposit, String hAmount) async {
    return await _channel.invokeMethod('startCreditCard', {
      KEY_DEPOSIT: deposit,
      KEY_HOURLY_AMOUNT: hAmount,
    });
  }

  static Future<Map> startGooglePay(String deposit, String hAmount) async {
    return await _channel.invokeMethod('startGooglePay', {
      KEY_DEPOSIT: deposit,
      KEY_HOURLY_AMOUNT: hAmount,
    });
  }

  static Future<Map> startVenMo(String deposit, String hAmount) async {
    return await _channel.invokeMethod('startVenMo', {
      KEY_DEPOSIT: deposit,
      KEY_HOURLY_AMOUNT: hAmount,
    });
  }

  static Future<Map> startPaypal(String deposit, String hAmount) async {
    return await _channel.invokeMethod('startPayPal', {
      KEY_DEPOSIT: deposit,
      KEY_HOURLY_AMOUNT: hAmount,
    });
  }

  static Future<Map> startVisaCheckOut(String deposit, String hAmount) async {
    return await _channel.invokeMethod('startVisaCheckOut', {
      KEY_DEPOSIT: deposit,
      KEY_HOURLY_AMOUNT: hAmount,
    });
  }
}
