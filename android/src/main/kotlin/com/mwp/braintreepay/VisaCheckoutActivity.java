package com.mwp.braintreepay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.VisaCheckout;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.VisaCheckoutAddress;
import com.braintreepayments.api.models.VisaCheckoutNonce;
import com.visa.checkout.CheckoutButton;
import com.visa.checkout.Profile.ProfileBuilder;
import com.visa.checkout.PurchaseInfo;
import com.visa.checkout.PurchaseInfo.PurchaseInfoBuilder;
import com.visa.checkout.VisaCheckoutSdk;
import com.visa.checkout.VisaPaymentSummary;

import java.math.BigDecimal;

public class VisaCheckoutActivity extends BaseActivity
        implements BraintreeResponseListener<ProfileBuilder> {

    private CheckoutButton mVisaPaymentButton;

    @Override
    protected int layoutId() {
        return R.layout.visa_checkout_activity;
    }

    @Override
    protected void init(Bundle onSaveInstanceState) {
        setUpAsBack();
        String deposit = getIntent().getStringExtra(BaseActivity.EXTRA_DEPOSIT);
        String hAmount = getIntent().getStringExtra(BaseActivity.EXTRA_HOURLY_AMOUNT);
        TextView tvPaymentPrompt = findViewById(R.id.tv_payment_prompt);
        tvPaymentPrompt.setText(String.format(getString(R.string.payment_prompt),
                deposit, hAmount));
        mVisaPaymentButton = findViewById(R.id.visa_checkout_button);
        requestAuthorization();
    }

    @Override
    protected void onAuthorizationFetched() {
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
        } catch (InvalidArgumentException e) {
            onError(e);
        }
        VisaCheckout.createProfileBuilder(mBraintreeFragment, this);
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        super.onPaymentMethodNonceCreated(paymentMethodNonce);
        Intent intent = new Intent().putExtra(MainActivity.EXTRA_PAYMENT_RESULT,
                paymentMethodNonce);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void reset() {

    }

    @Override
    public void onResponse(ProfileBuilder profileBuilder) {
        PurchaseInfoBuilder purchaseInfo = new PurchaseInfoBuilder(new BigDecimal("1.00"),
                PurchaseInfo.Currency.USD).setDescription("Description");
        dismissLoadingDialog();
        mVisaPaymentButton.init(VisaCheckoutActivity.this, profileBuilder.build(),
                purchaseInfo.build(), new VisaCheckoutSdk.VisaCheckoutResultListener() {
                    @Override
                    public void onButtonClick(LaunchReadyHandler launchReadyHandler) {
                        launchReadyHandler.launch();
                    }

                    @Override
                    public void onResult(VisaPaymentSummary visaPaymentSummary) {
                        VisaCheckout.tokenize(mBraintreeFragment, visaPaymentSummary);
                    }
                });
    }

    public static String getDisplayString(VisaCheckoutNonce nonce) {
        return "User data\n" + "First name: " + nonce.getUserData().getUserFirstName() + "\n" +
                "Last name: " + nonce.getUserData().getUserLastName() + "\n" +
                "Full name: " + nonce.getUserData().getUserFullName() + "\n" +
                "User name: " + nonce.getUserData().getUsername() + "\n" +
                "Email: " + nonce.getUserData().getUserEmail() + "\n" +
                "Billing Address: " + formatAddress(nonce.getBillingAddress()) + "\n" +
                "Shipping Address: " + formatAddress(nonce.getShippingAddress()) + "\n" +
                getDisplayString(nonce.getBinData());
    }

    private static String formatAddress(VisaCheckoutAddress address) {
        return address.getFirstName() + " " +
                address.getLastName() + " " +
                address.getStreetAddress() + " " +
                address.getExtendedAddress() + " " +
                address.getLocality() + " " +
                address.getPostalCode() + " " +
                address.getRegion() + " " +
                address.getCountryCode() + " " +
                address.getPhoneNumber();
    }
}
