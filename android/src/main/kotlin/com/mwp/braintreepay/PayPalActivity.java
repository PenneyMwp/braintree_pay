package com.mwp.braintreepay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.DataCollector;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.PostalAddress;
import com.paypal.android.sdk.onetouch.core.PayPalOneTouchCore;

import java.util.Arrays;
import java.util.List;

public class PayPalActivity extends BaseActivity implements ConfigurationListener,
        PaymentMethodNonceCreatedListener, BraintreeErrorListener {

    private String mDeviceData;
    @SuppressWarnings("FieldCanBeLocal")
    private TextView mPayPalAppIndicator;
    private Button mBillingAgreementButton;
    private Button mSinglePaymentButton;

    @Override
    protected int layoutId() {
        return R.layout.paypal_activity;
    }

    @Override
    protected void init(Bundle onSaveInstanceState) {
        setUpAsBack();
        TextView tvPaymentPrompt = findViewById(R.id.tv_payment_prompt);
        tvPaymentPrompt.setText(String.format(getString(R.string.payment_prompt), "99", "1"));
        mPayPalAppIndicator = findViewById(R.id.paypal_wallet_app_indicator);
        mBillingAgreementButton = findViewById(R.id.paypal_billing_agreement_button);
        mSinglePaymentButton = findViewById(R.id.paypal_single_payment_button);
        mPayPalAppIndicator.setText(getString(R.string.paypal_wallet_available,
                PayPalOneTouchCore.isWalletAppInstalled(this)));
        mBillingAgreementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBillingAgreement();
                showLoadingDialog();
            }
        });
        mSinglePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSinglePayment();
                showLoadingDialog();
            }
        });
        requestAuthorization();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissLoadingDialog();
    }

    @Override
    protected void reset() {
        enableButtons(false);
    }

    @Override
    protected void onAuthorizationFetched() {
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
        } catch (InvalidArgumentException e) {
            onError(e);
        }
        enableButtons(true);
        dismissLoadingDialog();
    }

    private void enableButtons(boolean enabled) {
        mBillingAgreementButton.setEnabled(enabled);
        mSinglePaymentButton.setEnabled(enabled);
    }

    public void launchSinglePayment() {
        setProgressBarIndeterminateVisibility(true);
        PayPal.requestOneTimePayment(mBraintreeFragment, getPayPalRequest("1.00"));
    }

    public void launchBillingAgreement() {
        setProgressBarIndeterminateVisibility(true);
        PayPal.requestBillingAgreement(mBraintreeFragment, getPayPalRequest(null));
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        if (getIntent().getBooleanExtra(MainActivity.EXTRA_COLLECT_DEVICE_DATA,
                false)) {
            DataCollector.collectDeviceData(mBraintreeFragment, new BraintreeResponseListener<String>() {
                @Override
                public void onResponse(String deviceData) {
                    mDeviceData = deviceData;
                }
            });
        }
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        super.onPaymentMethodNonceCreated(paymentMethodNonce);
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_PAYMENT_RESULT, paymentMethodNonce);
        intent.putExtra(MainActivity.EXTRA_DEVICE_DATA, mDeviceData);
        setResult(RESULT_OK, intent);
        finish();
    }

    private PayPalRequest getPayPalRequest(@Nullable String amount) {
        PayPalRequest request = new PayPalRequest(amount);
        request.displayName(Settings.getPayPalDisplayName(this));
        String landingPageType = Settings.getPayPalLandingPageType(this);
        if (getString(R.string.paypal_landing_page_type_billing).equals(landingPageType)) {
            request.landingPageType(PayPalRequest.LANDING_PAGE_TYPE_BILLING);
        } else if (getString(R.string.paypal_landing_page_type_login).equals(landingPageType)) {
            request.landingPageType(PayPalRequest.LANDING_PAGE_TYPE_LOGIN);
        }
        String intentType = Settings.getPayPalIntentType(this);
        if (intentType.equals(getString(R.string.paypal_intent_authorize))) {
            request.intent(PayPalRequest.INTENT_AUTHORIZE);
        } else if (intentType.equals(getString(R.string.paypal_intent_order))) {
            request.intent(PayPalRequest.INTENT_ORDER);
        } else if (intentType.equals(getString(R.string.paypal_intent_sale))) {
            request.intent(PayPalRequest.INTENT_SALE);
        }

        if (Settings.isPayPalUseractionCommitEnabled(this)) {
            request.userAction(PayPalRequest.USER_ACTION_COMMIT);
        }

        if (Settings.isPayPalCreditOffered(this)) {
            request.offerCredit(true);
        }

        if (Settings.usePayPalAddressOverride(this)) {
            request.shippingAddressOverride(new PostalAddress()
                    .recipientName("Brian Tree")
                    .streetAddress("123 Fake Street")
                    .extendedAddress("Floor A")
                    .locality("San Francisco")
                    .region("CA")
                    .countryCodeAlpha2("US")
            );
        }
        return request;
    }

    public static String getDisplayString(PayPalAccountNonce nonce) {
        return "First name: " + nonce.getFirstName() + "\n" +
                "Last name: " + nonce.getLastName() + "\n" +
                "Email: " + nonce.getEmail() + "\n" +
                "Phone: " + nonce.getPhone() + "\n" +
                "Payer id: " + nonce.getPayerId() + "\n" +
                "Client metadata id: " + nonce.getClientMetadataId() + "\n" +
                "Billing address: " + formatAddress(nonce.getBillingAddress()) + "\n" +
                "Shipping address: " + formatAddress(nonce.getShippingAddress());
    }

    private static String formatAddress(PostalAddress address) {
        StringBuilder addressString = new StringBuilder();
        List<String> addresses = Arrays.asList(
                address.getRecipientName(),
                address.getStreetAddress(),
                address.getExtendedAddress(),
                address.getLocality(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountryCodeAlpha2()
        );
        for (String line : addresses) {
            if (line == null) {
                addressString.append("null");
            } else {
                addressString.append(line);
            }
            addressString.append(" ");
        }
        return addressString.toString();
    }
}
