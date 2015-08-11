package com.hitherejoe.watchtower.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.hitherejoe.watchtower.data.model.Diagnostics.Alert;
import com.hitherejoe.watchtower.ui.adapter.AlertHolder;
import com.hitherejoe.watchtower.util.DataUtils;
import com.hitherejoe.watchtower.util.DialogFactory;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;

public class AlertsFragment extends Fragment {

    @Bind(R.id.recycler_alerts)
    RecyclerView mAlertsRecycler;

    @Bind(R.id.text_no_alerts)
    TextView mNoAttachmentsText;

    @Bind(R.id.progress_indicator)
    ProgressBar mProgressBar;

    @Bind(R.id.text_battery_date)
    TextView mBatteryDateText;

    private static final String EXTRA_BEACON = "EXTRA_BEACON";
    private Beacon mBeacon;
    private CompositeSubscription mSubscriptions;
    private DataManager mDataManager;
    private Diagnostics mDiagnostics;
    private EasyRecyclerAdapter<Alert> mEasyRecycleAdapter;

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
        if (mBeacon == null) {
            throw new IllegalArgumentException("Alerts fragment requires a beacon instance!");
        }
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get(getActivity()).getComponent().dataManager();
        mEasyRecycleAdapter = new EasyRecyclerAdapter<>(getActivity(), AlertHolder.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_alerts, container, false);
        ButterKnife.bind(this, fragmentView);
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
        if (DataUtils.isNetworkAvailable(getActivity())) {
            mSubscriptions.add(mDataManager.getDiagnostics(mBeacon.beaconName)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(mDataManager.getScheduler())
                    .subscribe(new Subscriber<Diagnostics>() {
                        @Override
                        public void onCompleted() {
                            mProgressBar.setVisibility(View.GONE);
                            if (mDiagnostics != null) {
                                if (mDiagnostics.estimatedLowBatteryDate != null) {
                                    mBatteryDateText.setText(mDiagnostics.estimatedLowBatteryDate.buildDate());
                                } else {
                                    mBatteryDateText.setText(getString(R.string.text_battery_unknown));
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
                        public void onError(Throwable error) {
                            mProgressBar.setVisibility(View.GONE);
                            Timber.e("There was an error retrieving beacon diagnostics " + error);
                            if (error instanceof RetrofitError) {
                                DialogFactory.createRetrofitErrorDialog(getActivity(), (RetrofitError) error).show();
                            } else {
                                DialogFactory.createSimpleErrorDialog(getActivity()).show();
                            }
                        }

                        @Override
                        public void onNext(Diagnostics diagnostics) {
                            mDiagnostics = diagnostics;
                        }
                    }));
        } else {
            mProgressBar.setVisibility(View.GONE);
            DialogFactory.createSimpleOkErrorDialog(
                    getActivity(),
                    getString(R.string.dialog_error_title),
                    getString(R.string.dialog_error_no_connection)
            ).show();
        }
    }

}
