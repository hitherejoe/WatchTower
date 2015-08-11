package com.hitherejoe.watchtower.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.util.AccountUtils;
import com.hitherejoe.watchtower.util.DialogFactory;

import java.io.IOException;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class AuthActivity extends BaseActivity {


    public static final String EXTRA_SHOULD_SHOW_AUTH_MESSAGE =
            "com.hitherejoe.ui.activity.AuthActivity.EXTRA_SHOULD_SHOW_AUTH_MESSAGE";
    private static final String REQUEST_SCOPE =
            "oauth2:https://www.googleapis.com/auth/userlocation.beacon.registry";
    private static final String ACCOUNT_TYPE = "com.google";

    private static final int REQUEST_CODE_AUTHORIZATION = 1234;
    private static final int REQUEST_CODE_PLAY_SERVICES = 1235;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1236;


    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;
    private AccountManager mAccountManager;

    public static Intent getStartIntent(Context context, boolean shouldShowAuthMessage) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(AuthActivity.EXTRA_SHOULD_SHOW_AUTH_MESSAGE, shouldShowAuthMessage);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountManager = AccountManager.get(this);
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get(this).getComponent().dataManager();

        if (checkPlayServices()) {
            if (getIntent().getBooleanExtra(EXTRA_SHOULD_SHOW_AUTH_MESSAGE, false)) {
                showErrorDialog(getString(R.string.dialog_error_unauthorised_response));
            } else {
                chooseAccount();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_AUTHORIZATION) {
                requestToken();
            } else if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                mDataManager.getPreferencesHelper().setUser(accountName);
                AccountUtils.invalidateToken(this);
                requestToken();
            } else {
                showErrorDialog(getString(R.string.dialog_error_account_chooser));
            }
        } else {
            showErrorDialog(getString(R.string.dialog_error_account_chooser));
        }
    }

    private void showErrorDialog(String message) {
        Dialog dialog = DialogFactory.createAuthErrorDialog(this,
                message,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chooseAccount();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(
                        resultCode, this, REQUEST_CODE_PLAY_SERVICES).show();
            } else {
                Dialog playServicesDialog = DialogFactory.createSimpleOkErrorDialog(
                        this,
                        getString(R.string.dialog_error_title),
                        getString(R.string.error_message_play_services)
                );
                playServicesDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
                playServicesDialog.show();
            }
            return false;
        }
        return true;
    }

    private void chooseAccount() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{ACCOUNT_TYPE}, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private void requestToken() {
        Account userAccount = null;
        String user = mDataManager.getPreferencesHelper().getUser();
        for (Account account : mAccountManager.getAccountsByType(ACCOUNT_TYPE)) {
            if (account.name.equals(user)) {
                userAccount = account;
                break;
            }
        }
        mAccountManager.getAuthToken(userAccount, REQUEST_SCOPE, null, this,
                new OnTokenReceivedCallback(), null);
    }

    private void startMainActivity() {
        startActivity(MainActivity.getStartIntent(this));
        finish();
    }

    private class OnTokenReceivedCallback implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();

                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, REQUEST_CODE_AUTHORIZATION);
                } else {
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    mDataManager.getPreferencesHelper().saveToken(token);
                    startMainActivity();
                }
            } catch (AuthenticatorException e) {
                Timber.e("There was an Authenticator error: " + e);
            } catch (OperationCanceledException e) {
                Timber.e("There was an Operation error: " + e);
            } catch (IOException e) {
                Timber.e("There was an IO Exception: " + e);
            }
        }
    }
}
