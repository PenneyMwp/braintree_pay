import 'dart:async';

import 'package:flutter/services.dart';

class BraintreePay {
  static const MethodChannel _channel = const MethodChannel('com.mwp.braintreepay');

  static Future<String> get platform async {
    final String version = await _channel.invokeMethod('platform');
    return version;
  }

  static void startCreditCard() {
    _channel.invokeMethod('startCreditCard');
  }

  static void startGooglePay() {
    _channel.invokeMethod('startGooglePay');
  }

  static void startVenMo() {
    _channel.invokeMethod('startVenMo');
  }

  static void startPaypal() {
    _channel.invokeMethod('startPaypal');
  }

  static void startVisaCheckOut() {
    _channel.invokeMethod('startVisaCheckOut');
  }
}
