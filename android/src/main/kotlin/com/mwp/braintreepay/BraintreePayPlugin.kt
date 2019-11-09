package com.mwp.braintreepay

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import com.braintreepayments.api.models.*
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

class BraintreePayPlugin(val activity: Activity) : MethodCallHandler,
        PluginRegistry.ActivityResultListener {

    var result: Result? = null

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.mwp.braintreepay")
            if (registrar.activity() != null) {
                val plugin = BraintreePayPlugin(registrar.activity())
                channel.setMethodCallHandler(plugin)
                registrar.addActivityResultListener(plugin)
            }
        }
    }

    override fun onActivityResult(code: Int, resultCode: Int, data: Intent?): Boolean {
//        if (code == 100) {
//            if (resultCode == Activity.RESULT_OK) {
//                val barcode = data?.getStringExtra("SCAN_RESULT")
//                barcode?.let { this.result?.success(barcode) }
//            } else {
//                val errorCode = data?.getStringExtra("ERROR_CODE")
//                this.result?.error(errorCode, null, null)
//            }
//            return true
//        }
        if (resultCode == Activity.RESULT_OK) {
            if (code == BaseActivity.DROP_IN_REQUEST) {
//                if (data != null) {
//                    val result = data.getParcelableExtra<DropInResult>(
//                            DropInResult.EXTRA_DROP_IN_RESULT)
//                    if (result != null && result.deviceData != null) {
//                        val p = result.paymentMethodNonce
//                        displayNonce(p, result.deviceData!!)
//                    }
//             }
            } else {
                if (data != null) {
                    val returnedData = data.getParcelableExtra<Parcelable>(
                            BaseActivity.EXTRA_PAYMENT_RESULT)
                    val deviceData = data.getStringExtra(BaseActivity.EXTRA_DEVICE_DATA)
                    if (returnedData != null && returnedData is PaymentMethodNonce) {
                        displayNonce(returnedData, deviceData)
                    }
                }
            }
        } else if (resultCode != Activity.RESULT_CANCELED) {
//            showDialog((data.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception)
//                    .message)
            this.result?.error("", "Payment Cancel", null)
        }
        return false
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when {
            call.method == "platform" -> {
                this.result = result
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            call.method == "startCreditCard" -> {
                this.result = result
                result.success("ok")
                launchCards()
            }
            call.method == "startGooglePay" -> {
                this.result = result
                result.success("ok")
                launchGooglePayment()
            }
            call.method == "startVenMo" -> {
                this.result = result
                result.success("ok")
                launchVenmo()
            }
            call.method == "startPaypal" -> {
                this.result = result
                result.success("ok")
                launchPayPal()
            }
            call.method == "startVisaCheckOut" -> {
                this.result = result
                result.success("ok")
                launchVisaCheckout()
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun launchGooglePayment() {
        val intent = Intent(activity, GooglePaymentActivity::class.java)
        activity.startActivityForResult(intent, BaseActivity.GOOGLE_PAYMENT_REQUEST)
    }

    private fun launchCards() {
        val intent = Intent(activity, CardActivity::class.java)
        intent.putExtra(BaseActivity.EXTRA_COLLECT_DEVICE_DATA,
                Settings.shouldCollectDeviceData(activity))
        activity.startActivityForResult(intent, BaseActivity.CARDS_REQUEST)
    }

    private fun launchPayPal() {
        val intent = Intent(activity, PayPalActivity::class.java)
        intent.putExtra(BaseActivity.EXTRA_COLLECT_DEVICE_DATA,
                Settings.shouldCollectDeviceData(activity))
        activity.startActivityForResult(intent, BaseActivity.PAYPAL_REQUEST)
    }

    private fun launchVenmo() {
        val intent = Intent(activity, VenmoActivity::class.java)
        activity.startActivityForResult(intent, BaseActivity.VENMO_REQUEST)
    }

    private fun launchVisaCheckout() {
        val intent = Intent(activity, VisaCheckoutActivity::class.java)
        activity.startActivityForResult(intent, BaseActivity.VISA_CHECKOUT_REQUEST)
    }

    /**private fun launchLocalPayments() {
    val intent = Intent(activity, LocalPaymentsActivity::class.java)
    activity.startActivityForResult(intent, BaseActivity.LOCAL_PAYMENTS_REQUEST)
    }**/

    private fun displayNonce(nonce: PaymentMethodNonce, deviceData: String?) {
        var details = ""
        when (nonce) {
            is CardNonce -> {
                details = CardActivity.getDisplayString(nonce)
            }
            is PayPalAccountNonce -> {
                details = PayPalActivity.getDisplayString(nonce)
            }
            is GooglePaymentCardNonce -> {
                details = GooglePaymentActivity.getDisplayString(nonce)
            }
            is VisaCheckoutNonce -> {
                details = VisaCheckoutActivity.getDisplayString(nonce)
            }
            is VenmoAccountNonce -> {
                details = VenmoActivity.getDisplayString(nonce)
            }
            is LocalPaymentResult -> {
                details = LocalPaymentsActivity.getDisplayString(nonce)
            }
        }
        Log.i("XIAOMAGE", "nonce = ${nonce.nonce}\r\n,details = $details,\r\n" +
                "deviceData = ${activity.getString(R.string.device_data_placeholder,
                        deviceData)}")
    }
}