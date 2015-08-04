package com.hitherejoe.proximityapidemo.android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hitherejoe.proximityapidemo.android.ProximityApiApplication;
import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.DataManager;
import com.hitherejoe.proximityapidemo.android.data.model.Attachment;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.data.model.Diagnostics;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AlertsFragment extends Fragment {

    @InjectView(R.id.recycler_alerts)
    RecyclerView mAlertsRecycler;

    @InjectView(R.id.text_no_alerts)
    TextView mNoAttachmentsText;

    @InjectView(R.id.progress_indicator)
    ProgressBar mProgressBar;

    private static final String EXTRA_BEACON = "EXTRA_BEACON";
    private static final String TAG = "AlertsFragment";
    private Beacon mBeacon;
    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;
    private Diagnostics mDiagnostics;

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
        if (mBeacon == null) throw new IllegalArgumentException("Properties fragment requires a beacon!");
        mSubscriptions = new CompositeSubscription();
        mDataManager = ProximityApiApplication.get().getDataManager();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_alerts, container, false);
        ButterKnife.inject(this, fragmentView);
        getDiagnostics();
        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    private void getDiagnostics() {
        mSubscriptions.add(AppObservable.bindFragment(this,
                mDataManager.getDiagnostics(mBeacon.beaconName))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Diagnostics>() {
                    @Override
                    public void onCompleted() {
                        mProgressBar.setVisibility(View.GONE);
                        if (mDiagnostics != null
                                && mDiagnostics.alerts.length > 0) {
                            mNoAttachmentsText.setVisibility(View.GONE);
                        } else {

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.e(TAG, "There was an error retrieving the beacon diagnostics " + e);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Diagnostics diagnostics) {
                        Log.d("DIAGNOSTICS", diagnostics.beaconName);
                        mDiagnostics = diagnostics;
                    }
                }));
    }

}
