package com.hitherejoe.watchtower.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.BusEvent;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.ErrorResponse;
import com.hitherejoe.watchtower.ui.adapter.BeaconHolder;
import com.hitherejoe.watchtower.ui.fragment.PropertiesFragment;
import com.hitherejoe.watchtower.util.AccountUtils;
import com.hitherejoe.watchtower.util.DataUtils;
import com.hitherejoe.watchtower.util.DialogFactory;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;

public class MainActivity extends BaseActivity {

    @Bind(R.id.progress_indicator)
    ProgressBar mProgressBar;

    @Bind(R.id.recycler_beacons)
    RecyclerView mBeaconsRecycler;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    @Bind(R.id.text_no_beacons)
    TextView mNoBeaconsText;

    private static final int REQUEST_CODE_AUTHORIZATION = 1234;
    private static final int REQUEST_CODE_PLAY_SERVICES = 1235;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1236;
    private static final int REQUEST_CODE_REGISTER_BEACON = 1237;
    private static final String ACCOUNT_TYPE = "com.google";
    private static final String REQUEST_SCOPE =
            "oauth2:https://www.googleapis.com/auth/userlocation.beacon.registry";

    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;
    private EasyRecyclerAdapter<Beacon> mEasyRecycleAdapter;
    private AccountManager mAccountManager;
    private boolean mHasTriedNewAuthToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mHasTriedNewAuthToken = false;
        mAccountManager = AccountManager.get(this);
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get(this).getComponent().dataManager();
        mEasyRecycleAdapter = new EasyRecyclerAdapter<>(this, BeaconHolder.class, mBeaconListener);
        setupLayoutViews();

        if (AccountUtils.isUserAuthenticated(this)) {
            getBeacons();
        } else {
            if (checkPlayServices()) chooseAccount();
        }

        WatchTowerApplication.get(this).getComponent().eventBus().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WatchTowerApplication.get(this).getComponent().eventBus().unregister(this);
        mSubscriptions.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_medium:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.medium.com")));
                return true;
            case R.id.action_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hitherejoe/WatchTower")));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                mProgressBar.setVisibility(View.GONE);
                DialogFactory.createSimpleErrorDialog(this).show();
            }
        }
    }

    @Subscribe
    public void onBeaconListAmended(BusEvent.BeaconListAmended event) {
        getBeacons();
    }

    @OnClick(R.id.fab_add)
    public void onFabAddClick() {
        Intent intent = PropertiesActivity.getStartIntent(this, PropertiesFragment.Mode.REGISTER);
        startActivityForResult(intent, REQUEST_CODE_REGISTER_BEACON);
    }

    private void setupLayoutViews() {
        mBeaconsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mBeaconsRecycler.setAdapter(mEasyRecycleAdapter);
        mSwipeRefresh.setColorSchemeResources(R.color.primary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getBeacons();
            }
        });
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        REQUEST_CODE_PLAY_SERVICES).show();
            } else {
                Dialog playServicesDialog = DialogFactory.createSimpleOkErrorDialog(
                        this,
                        getString(R.string.dialog_error_title),
                        getString(R.string.error_message_play_services));
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

    private void getBeacons() {
        mEasyRecycleAdapter.setItems(new ArrayList<Beacon>());
        mSubscriptions.add(mDataManager.getBeacons()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getScheduler())
                .subscribe(new Subscriber<Beacon>() {
                    @Override
                    public void onCompleted() {
                        mProgressBar.setVisibility(View.GONE);
                        mSwipeRefresh.setRefreshing(false);
                        if (mEasyRecycleAdapter.getItemCount() > 0) {
                            mBeaconsRecycler.setVisibility(View.VISIBLE);
                            mNoBeaconsText.setVisibility(View.GONE);
                        } else {
                            mBeaconsRecycler.setVisibility(View.GONE);
                            mNoBeaconsText.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e("There was an error retrieving the beacons " + error);
                        mProgressBar.setVisibility(View.GONE);
                        mSwipeRefresh.setRefreshing(false);
                        if (error instanceof RetrofitError) {
                            ErrorResponse errorResponse = DataUtils.parseRetrofitError(error);
                            int errorCode = errorResponse.error.code;
                            if (!mHasTriedNewAuthToken && (errorCode == 401 || errorCode == 403)) {
                                mHasTriedNewAuthToken = true;
                                AccountUtils.invalidateToken(MainActivity.this);
                                requestToken();
                            } else {
                                DialogFactory.createRetrofitErrorDialog(MainActivity.this, (RetrofitError) error);
                            }
                        } else {
                            DialogFactory.createSimpleErrorDialog(MainActivity.this).show();
                        }
                    }

                    @Override
                    public void onNext(Beacon beacon) {
                        mEasyRecycleAdapter.addItem(beacon);
                    }
                }));
    }

    private BeaconHolder.BeaconListener mBeaconListener = new BeaconHolder.BeaconListener() {
        @Override
        public void onAttachmentsClicked(Beacon beacon) {
            startActivity(AttachmentsActivity.getStartIntent(MainActivity.this, beacon));
        }

        @Override
        public void onViewClicked(Beacon beacon) {
            startActivity(DetailActivity.getStartIntent(MainActivity.this, beacon));
        }
    };

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
                    getBeacons();
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
