package com.hitherejoe.watchtower.ui.activity;

import android.app.Dialog;
import android.content.Context;
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
import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.BusEvent;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.ui.adapter.BeaconHolder;
import com.hitherejoe.watchtower.ui.fragment.PropertiesFragment;
import com.hitherejoe.watchtower.util.DataUtils;
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

public class MainActivity extends BaseActivity {

    @Bind(R.id.progress_indicator)
    ProgressBar mProgressBar;

    @Bind(R.id.recycler_beacons)
    RecyclerView mBeaconsRecycler;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    @Bind(R.id.text_no_beacons)
    TextView mNoBeaconsText;

    private static final String URL_MEDIUM_ARTICLE = "http://www.medium.com";
    private static final String URL_GITHUB_REPOSITORY = "https://github.com/hitherejoe/WatchTower";
    private static final int REQUEST_CODE_REGISTER_BEACON = 1237;

    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;
    private EasyRecyclerAdapter<Beacon> mEasyRecycleAdapter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get(this).getComponent().dataManager();
        mEasyRecycleAdapter = new EasyRecyclerAdapter<>(this, BeaconHolder.class, mBeaconListener);
        setupLayoutViews();
        getBeacons();
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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_MEDIUM_ARTICLE)));
                return true;
            case R.id.action_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_GITHUB_REPOSITORY)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private void getBeacons() {
        if (DataUtils.isNetworkAvailable(this)) {
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
                                DialogFactory.createRetrofitErrorDialog(MainActivity.this, (RetrofitError) error).show();
                            } else {
                                DialogFactory.createSimpleErrorDialog(MainActivity.this).show();
                            }
                        }

                        @Override
                        public void onNext(Beacon beacon) {
                            mEasyRecycleAdapter.addItem(beacon);
                        }
                    }));
        } else {
            mProgressBar.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            DialogFactory.createSimpleOkErrorDialog(
                    this,
                    getString(R.string.dialog_error_title),
                    getString(R.string.dialog_error_no_connection)
            ).show();
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
