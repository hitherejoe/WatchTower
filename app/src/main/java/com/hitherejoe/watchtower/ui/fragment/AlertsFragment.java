package com.hitherejoe.watchtower.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.Diagnostics;
import com.hitherejoe.watchtower.ui.adapter.AlertHolder;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;

public class AlertsFragment extends Fragment {

    @InjectView(R.id.recycler_alerts)
    RecyclerView mAlertsRecycler;

    @InjectView(R.id.text_no_alerts)
    TextView mNoAttachmentsText;

    @InjectView(R.id.progress_indicator)
    ProgressBar mProgressBar;

    @InjectView(R.id.text_battery_date)
    TextView mBatteryDateText;

    @InjectView(R.id.text_battery_title)
    TextView mBatteryTitleText;

    private static final String EXTRA_BEACON = "EXTRA_BEACON";
    private static final String TAG = "AlertsFragment";
    private Beacon mBeacon;
    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;
    private Diagnostics mDiagnostics;
    private Diagnostics.Alert[] mAlerts;
    private EasyRecyclerAdapter<Diagnostics.Alert> mEasyRecycleAdapter;

    public static AlertsFragment newInstance(Beacon beacon) {
        AlertsFragment propertiesFragment = new AlertsFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_BEACON, beacon);
        propertiesFragment.setArguments(args);
        return propertiesFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBeacon = getArguments().getParcelable(EXTRA_BEACON);
        if (mBeacon == null) throw new IllegalArgumentException("Alerts fragment requires a beacon instance!");
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get().getDataManager();
        mEasyRecycleAdapter = new EasyRecyclerAdapter<>(getActivity(), AlertHolder.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_alerts, container, false);
        ButterKnife.inject(this, fragmentView);
        setupRecyclerView();
        getDiagnostics();
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSubscriptions.unsubscribe();
    }

    private void setupRecyclerView() {
        mAlertsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAlertsRecycler.setAdapter(mEasyRecycleAdapter);
    }

    private void getDiagnostics() {
        mSubscriptions.add(AppObservable.bindFragment(this,
                mDataManager.getDiagnostics(mBeacon.beaconName))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Diagnostics>() {
                    @Override
                    public void onCompleted() {
                        mProgressBar.setVisibility(View.GONE);
                        if (mDiagnostics != null) {
                            if (mDiagnostics.estimatedLowBatteryDate != null) {
                                mBatteryTitleText.setVisibility(View.VISIBLE);
                                mBatteryDateText.setText(mDiagnostics.estimatedLowBatteryDate.buildDate());
                            } else {
                                mBatteryDateText.setVisibility(View.GONE);
                                mBatteryTitleText.setVisibility(View.GONE);
                            }
                            if (mDiagnostics.alerts != null
                                    && mDiagnostics.alerts.length > 0) {
                                mEasyRecycleAdapter.addItems(Arrays.asList(mDiagnostics.alerts));
                                mNoAttachmentsText.setVisibility(View.GONE);
                                mAlertsRecycler.setVisibility(View.VISIBLE);
                            } else {
                                mNoAttachmentsText.setVisibility(View.VISIBLE);
                                mAlertsRecycler.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.e(TAG, "There was an error retrieving the beacon diagnostics " + e);
                    }

                    @Override
                    public void onNext(Diagnostics diagnostics) {
                        mDiagnostics = diagnostics;
                    }
                }));
    }

}
