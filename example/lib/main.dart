import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:braintree_pay/braintree_pay.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platform = 'Get Platform';

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> getPlatform() async {
    String platform;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platform = await BraintreePay.platform;
    } on PlatformException {
      platform = 'Failed to get platform version.';
    }
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
    setState(() {
      _platform = platform;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        home: Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: <Widget>[
          Padding(
              padding: EdgeInsets.all(10),
              child: MaterialButton(
                height: 50,
                minWidth: 400,
                color: Colors.red,
                child: Text(
                  _platform,
                  style: TextStyle(fontSize: 20),
                ),
                onPressed: () {
                  getPlatform();
                },
              )),
          Padding(
              padding: EdgeInsets.all(10),
              child: MaterialButton(
                height: 50,
                minWidth: 400,
                color: Colors.red,
                child: Text(
                  'Start Credit Card',
                  style: TextStyle(fontSize: 20),
                ),
                onPressed: () {
                  BraintreePay.startCreditCard();
                },
              )),
          Padding(
              padding: EdgeInsets.all(10),
              child: MaterialButton(
                height: 50,
                minWidth: 400,
                color: Colors.red,
                child: Text(
                  'Start Google Pay',
                  style: TextStyle(fontSize: 20),
                ),
                onPressed: () {
                  BraintreePay.startGooglePay();
                },
              )),
          Padding(
              padding: EdgeInsets.all(10),
              child: MaterialButton(
                height: 50,
                minWidth: 400,
                color: Colors.red,
                child: Text(
                  'Start Venmo',
                  style: TextStyle(fontSize: 20),
                ),
                onPressed: () {
                  BraintreePay.startVenMo();
                },
              )),
          Padding(
              padding: EdgeInsets.all(10),
              child: MaterialButton(
                height: 50,
                minWidth: 400,
                color: Colors.red,
                child: Text(
                  'Start Paypal',
                  style: TextStyle(fontSize: 20),
                ),
                onPressed: () {
                  BraintreePay.startPaypal();
                },
              )),
          Padding(
              padding: EdgeInsets.all(10),
              child: MaterialButton(
                height: 50,
                minWidth: 400,
                color: Colors.red,
                child: Text(
                  'Start Visa CheckOut',
                  style: TextStyle(fontSize: 20),
                ),
                onPressed: () {
                  BraintreePay.startVisaCheckOut();
                },
              )),
        ],
      ),
    ));
  }
}
