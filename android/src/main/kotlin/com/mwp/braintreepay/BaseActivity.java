package com.mwp.braintreepay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreePaymentResultListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.BinData;
import com.braintreepayments.api.models.BraintreePaymentResult;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.GooglePaymentCardNonce;
import com.braintreepayments.api.models.LocalPaymentResult;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.VenmoAccountNonce;
import com.braintreepayments.api.models.VisaCheckoutNonce;
import com.mwp.braintreepay.api.internal.SignatureVerificationOverrides;
import com.mwp.braintreepay.models.ClientToken;
import com.paypal.android.sdk.onetouch.core.PayPalOneTouchCore;

import org.jetbrains.annotations.NotNull;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@SuppressWarnings("deprecation")
public abstract class BaseActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        PaymentMethodNonceCreatedListener, BraintreeCancelListener,
        BraintreeErrorListener, BraintreePaymentResultListener,
        ActionBar.OnNavigationListener {

    public static final String EXTRA_PAYMENT_TYPE = "payment_type";
    public static final String EXTRA_PAYMENT_RESULT = "payment_result";
    public static final String EXTRA_DEVICE_DATA = "device_data";
    public static final String EXTRA_COLLECT_DEVICE_DATA = "collect_device_data";
    public static final String EXTRA_DEPOSIT = "com.mwp.braintreepay.EXTRA_DEPOSIT";
    public static final String EXTRA_HOURLY_AMOUNT = "com.mwp.braintreepay.EXTRA_HOURLY_AMOUNT";
    public static final int DROP_IN_REQUEST = 1;
    public static final int GOOGLE_PAYMENT_REQUEST = 2;
    public static final int CARDS_REQUEST = 3;
    public static final int PAYPAL_REQUEST = 4;
    public static final int VENMO_REQUEST = 5;
    public static final int VISA_CHECKOUT_REQUEST = 6;
    public static final int LOCAL_PAYMENTS_REQUEST = 7;
    private static final String EXTRA_AUTHORIZATION = "com.mwp.braintreepay.EXTRA_AUTHORIZATION";
    private static final String EXTRA_CUSTOMER_ID = "com.mwp.braintreepay.EXTRA_CUSTOMER_ID";

    static final String KEY_NONCE = "nonce";
    PaymentMethodNonce mNonce;

    protected String mAuthorization;
    protected String mCustomerId;
    protected BraintreeFragment mBraintreeFragment;
    private boolean mActionBarSetup;
    private MaterialDialog mLoadingDialog;

    protected abstract void reset();

    protected abstract void onAuthorizationFetched();

    protected abstract int layoutId();

    protected abstract void init(Bundle onSaveInstanceState);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        if (layoutId() != 0) {
            setContentView(layoutId());
        }
        if (savedInstanceState != null) {
            mAuthorization = savedInstanceState.getString(EXTRA_AUTHORIZATION);
            mCustomerId = savedInstanceState.getString(EXTRA_CUSTOMER_ID);
        }
        init(savedInstanceState);
        if (!mActionBarSetup) {
            setupActionBar();
            mActionBarSetup = true;
        }
        SignatureVerificationOverrides.disableAppSwitchSignatureVerification(
                Settings.isPayPalSignatureVerificationDisabled(this));
        PayPalOneTouchCore.useHardcodedConfig(this,
                Settings.useHardcodedPayPalConfiguration(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                           @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handleAuthorizationState();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == DROP_IN_REQUEST) {
//                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
//                displayNonce(result.getPaymentMethodNonce(), result.getDeviceData());
//            } else {
//                Parcelable returnedData = data.getParcelableExtra(EXTRA_PAYMENT_RESULT);
//                String deviceData = data.getStringExtra(EXTRA_DEVICE_DATA);
//                if (returnedData instanceof PaymentMethodNonce) {
//                    displayNonce((PaymentMethodNonce) returnedData, deviceData);
//                }
//                //mCreateTransactionButton.setEnabled(true);
//            }
//        } else if (resultCode != RESULT_CANCELED) {
//            showDialog(((Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR))
//                    .getMessage());
//        }
//    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAuthorization != null) {
            outState.putString(EXTRA_AUTHORIZATION, mAuthorization);
            outState.putString(EXTRA_CUSTOMER_ID, mCustomerId);
        }
    }

    @CallSuper
    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        setProgressBarIndeterminateVisibility(true);
        Log.d(getClass().getSimpleName(),
                "Payment Method Nonce received: " + paymentMethodNonce.getTypeLabel());
    }

    @CallSuper
    @Override
    public void onBraintreePaymentResult(BraintreePaymentResult result) {
        setProgressBarIndeterminateVisibility(true);
        Log.d(getClass().getSimpleName(),
                "Braintree Payment Result received: " + result.getClass().getSimpleName());
    }

    @CallSuper
    @Override
    public void onCancel(int requestCode) {
        setProgressBarIndeterminateVisibility(false);
        Log.d(getClass().getSimpleName(), "Cancel received: " + requestCode);
    }

    @CallSuper
    @Override
    public void onError(Exception error) {
        setProgressBarIndeterminateVisibility(false);
        Log.d(getClass().getSimpleName(),
                "Error received (" + error.getClass() + "): " + error.getMessage());
        Log.d(getClass().getSimpleName(), error.toString());
        showDialog("An error occurred (" + error.getClass() + "): " + error.getMessage(), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
    }

    protected void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new MaterialDialog.Builder(this).title("Please wait")
                    .content("Payment init...").progress(true, 0)
                    .canceledOnTouchOutside(false).build();
        }
        if (!isFinishing() && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    protected void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    protected void requestAuthorization() {
        if (BuildConfig.DEBUG && ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            handleAuthorizationState();
        }
    }

    private void handleAuthorizationState() {
        showLoadingDialog();
        if (mAuthorization == null || (Settings.useTokenizationKey(this)
                && !mAuthorization.equals(Settings.getEnvironmentTokenizationKey(this)))
                || !TextUtils.equals(mCustomerId, Settings.getCustomerId(this))) {
            performReset();
        } else {
            onAuthorizationFetched();
        }
    }

    private void performReset() {
        setProgressBarIndeterminateVisibility(true);
        mAuthorization = null;
        mCustomerId = Settings.getCustomerId(this);
        if (mBraintreeFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mBraintreeFragment).commit();
            mBraintreeFragment = null;
        }
        reset();
        fetchAuthorization();
    }

    protected void fetchAuthorization() {
        if (mAuthorization != null) {
            setProgressBarIndeterminateVisibility(false);
            onAuthorizationFetched();
        } else if (Settings.useTokenizationKey(this)) {
            mAuthorization = Settings.getEnvironmentTokenizationKey(this);
            setProgressBarIndeterminateVisibility(false);
            onAuthorizationFetched();
        } else {
            App.getApiClient(this).getClientToken(Settings.getCustomerId(this),
                    Settings.getMerchantAccountId(this), new Callback<ClientToken>() {
                        @Override
                        public void success(ClientToken clientToken, Response response) {
                            setProgressBarIndeterminateVisibility(false);
                            if (TextUtils.isEmpty(clientToken.getClientToken())) {
                                showDialog("Client token was empty", new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                            } else {
                                mAuthorization = clientToken.getClientToken();
                                onAuthorizationFetched();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            setProgressBarIndeterminateVisibility(false);
                            if (error.getResponse() == null) {
                                showDialog(error.getCause().getMessage(), new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                            } else {
                                showDialog("Unable to get a client token. Response Code: " +
                                        error.getResponse().getStatus() + " Response body: " +
                                        error.getResponse().getBody(), new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                            }
                            dismissLoadingDialog();
                        }
                    });
        }
    }

    protected void showDialog(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(android.R.string.ok, listener)
                .setCancelable(false)
                .show();
    }

    protected void setUpAsBack() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.environments,
                        android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setSelectedNavigationItem(Settings.getEnvironment(this));
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (Settings.getEnvironment(this) != itemPosition) {
            Settings.setEnvironment(this, itemPosition);
            performReset();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }/* else if (itemId == R.id.reset) {
            performReset();
            return true;
        } else if (itemId == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }*/
        return false;
    }

    public static String getDisplayString(BinData binData) {
        return "Bin Data: \n" +
                "         - Prepaid: " + binData.getHealthcare() + "\n" +
                "         - Healthcare: " + binData.getHealthcare() + "\n" +
                "         - Debit: " + binData.getDebit() + "\n" +
                "         - Durbin Regulated: " + binData.getDurbinRegulated() + "\n" +
                "         - Commercial: " + binData.getCommercial() + "\n" +
                "         - Payroll: " + binData.getPayroll() + "\n" +
                "         - Issuing Bank: " + binData.getIssuingBank() + "\n" +
                "         - Country of Issuance: " + binData.getCountryOfIssuance() + "\n" +
                "         - Product Id: " + binData.getProductId();
    }

    private void displayNonce(PaymentMethodNonce paymentMethodNonce, String deviceData) {
        mNonce = paymentMethodNonce;
        String details = "";
        if (mNonce instanceof CardNonce) {
            details = CardActivity.getDisplayString((CardNonce) mNonce);
        } else if (mNonce instanceof PayPalAccountNonce) {
            details = PayPalActivity.getDisplayString((PayPalAccountNonce) mNonce);
        } else if (mNonce instanceof GooglePaymentCardNonce) {
            details = GooglePaymentActivity.getDisplayString((GooglePaymentCardNonce) mNonce);
        } else if (mNonce instanceof VisaCheckoutNonce) {
            details = VisaCheckoutActivity.getDisplayString((VisaCheckoutNonce) mNonce);
        } else if (mNonce instanceof VenmoAccountNonce) {
            details = VenmoActivity.getDisplayString((VenmoAccountNonce) mNonce);
        } else if (mNonce instanceof LocalPaymentResult) {
            details = LocalPaymentsActivity.getDisplayString((LocalPaymentResult) mNonce);
        }
        Log.i("mwp", "details = " + details + ",deviceData = "
                + getString(R.string.device_data_placeholder, deviceData));
//        mNonceDetails.setText(details);
//        mNonceDetails.setVisibility(VISIBLE);
//        mDeviceData.setText(getString(R.string.device_data_placeholder, deviceData));
//        mDeviceData.setVisibility(VISIBLE);
//        mCreateTransactionButton.setEnabled(true);
    }
}
