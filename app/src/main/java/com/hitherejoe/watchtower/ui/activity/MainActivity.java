package com.hitherejoe.watchtower.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.BusEvent;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.ui.adapter.BeaconHolder;
import com.hitherejoe.watchtower.ui.fragment.PropertiesFragment;
import com.hitherejoe.watchtower.util.DialogFactory;
import com.squareup.otto.Subscribe;

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

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @Bind(R.id.text_no_beacons)
    TextView mNoBeaconsText;

    @Bind(R.id.recycler_beacons)
    RecyclerView mBeaconsRecycler;

    @Bind(R.id.progress_indicator)
    ProgressBar mProgressBar;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 2000;
    private static final int REQUEST_CODE_PLAY_SERVICES = 1238;
    private static final int REQUEST_CODE_REGISTER_BEACON = 1538;

    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;
    private EasyRecyclerAdapter<Beacon> mEasyRecycleAdapter;
    //private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get(this).getDataManager();
        mEasyRecycleAdapter = new EasyRecyclerAdapter<>(this, BeaconHolder.class, mBeaconListener);
        setupLayoutViews();
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        final boolean connected = accounts != null && accounts.length > 0;
       // if (!connected) {
            pickUserAccount();
       // } else {
         //   getBeacons();
       // }
        WatchTowerApplication.get(this).getBus().register(this);

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

    private void getBeacons() {
        mEasyRecycleAdapter.setItems(new ArrayList<Beacon>());
        mSubscriptions.add(mDataManager.getBeacons()
                .subscribeOn(mDataManager.getScheduler())
                .observeOn(AndroidSchedulers.mainThread())
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
                            DialogFactory.createRetrofitErrorDialog(MainActivity.this, (RetrofitError) error);
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

    @Subscribe
    public void onBeaconChanged(BusEvent.BeaconListAmended event) {
        getBeacons();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        REQUEST_CODE_PLAY_SERVICES).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        retrieveAuthToken();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        WatchTowerApplication.get(this).getBus().unregister(this);
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
            case R.id.action_about:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.fab_add)
    public void onFabAddClick() {
        startActivityForResult(PropertiesActivity.getStartIntent(this, PropertiesFragment.Mode.REGISTER), REQUEST_CODE_REGISTER_BEACON);
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    String mEmail; // Received from newChooseAccountIntent(); passed to getToken()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                //mGoogleApiClient.connect();
                // With the account name acquired, go get the auth token
                retrieveAuthToken();


            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR) {
            Log.d("ERROR", "ERRRRRRRR");
        } else if (requestCode == REQUEST_CODE_REGISTER_BEACON) {

        }
    }

    private void retrieveAuthToken() {

        String auth = "oauth2:https://www.googleapis.com/auth/userlocation.beacon.registry";
        mSubscriptions.add(mDataManager.getAccessToken(MainActivity.this, mEmail, auth, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getScheduler())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e("Error getting auth token: " + error);
                        if (error instanceof UserRecoverableAuthException) {
                            Timber.w("UserRecoverableAuthException has happen. Opening intent to resolve it");
                            Intent recover = ((UserRecoverableAuthException) error).getIntent();
                            startActivityForResult(recover, 1000);
                        } else if (error instanceof RetrofitError) {
                            DialogFactory.createRetrofitErrorDialog(MainActivity.this, (RetrofitError) error);
                        } else {
                            DialogFactory.createSimpleErrorDialog(MainActivity.this).show();
                        }
                    }

                    @Override
                    public void onNext(String s) {
                        WatchTowerApplication.get(MainActivity.this).getDataManager().getPreferencesHelper().saveToken(s);
                        getBeacons();
                    }
                }));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            tryToResolveConnectionError(connectionResult);
        } else {
            Timber.d("FAIL: " + connectionResult.toString());
        }
    }

    private void tryToResolveConnectionError(ConnectionResult connectionResult) {
        try {
            startIntentSenderForResult(connectionResult.getResolution().getIntentSender(), REQUEST_CODE_PICK_ACCOUNT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Timber.e("Error starting intent to resolve connection issue. " + e.getMessage());
        }
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
}
