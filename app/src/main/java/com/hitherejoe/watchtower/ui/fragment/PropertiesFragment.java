package com.hitherejoe.watchtower.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.BusEvent;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.model.AdvertisedId;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.Beacon.Status;
import com.hitherejoe.watchtower.data.model.Beacon.Stability;
import com.hitherejoe.watchtower.data.model.LatLng;
import com.hitherejoe.watchtower.ui.activity.DetailActivity;
import com.hitherejoe.watchtower.util.DataUtils;
import com.hitherejoe.watchtower.util.DialogFactory;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PropertiesFragment extends Fragment {

    @Bind(R.id.spinner_status)
    Spinner beaconStatusSpinner;

    @Bind(R.id.spinner_type)
    Spinner beaconTypeSpinner;

    @Bind(R.id.spinner_stability)
    Spinner beaconStabilitySpinner;

    @Bind(R.id.edit_text_beacon_name)
    EditText mBeaconNameEditText;

    @Bind(R.id.edit_text_advertised_id)
    EditText mAdvertisedIdEditText;

    @Bind(R.id.edit_text_description)
    EditText mDescriptionEditText;

    @Bind(R.id.edit_text_latitude)
    EditText mLatitudeEditText;

    @Bind(R.id.edit_text_longitude)
    EditText mLongitudeEditText;

    @Bind(R.id.edit_text_place_id)
    EditText mPlaceIdEditText;

    @Bind(R.id.text_title_beacon_name)
    TextView mBeaconNameText;

    @Bind(R.id.text_title_place_id)
    TextView mBeaconPlaceId;

    @Bind(R.id.text_title_description)
    TextView mBeaconDescription;

    @Bind(R.id.text_title_location)
    TextView mBeaconLocation;

    @Bind(R.id.text_title_type)
    TextView mBeaconType;

    @Bind(R.id.text_title_stability)
    TextView mBeaconStability;

    @Bind(R.id.text_advertised_id_error_message)
    TextView mAdvertisedIdErrorMessage;

    @Bind(R.id.text_status_error_message)
    TextView mStatusErrorMessage;

    private static final String EXTRA_MODE = "EXTRA_MODE";
    private static final String EXTRA_BEACON = "EXTRA_BEACON";
    private Mode mPropertiesMode;
    private Beacon mBeacon;
    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;

    public enum Mode { VIEW, REGISTER, UPDATE }

    public static PropertiesFragment newInstance(Beacon beacon, Mode mode) {
        PropertiesFragment propertiesFragment = new PropertiesFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_BEACON, beacon);
        args.putSerializable(EXTRA_MODE, mode);
        propertiesFragment.setArguments(args);
        return propertiesFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPropertiesMode = (Mode) getArguments().getSerializable(EXTRA_MODE);
        if (mPropertiesMode == null) throw new IllegalArgumentException("Properties fragment requires a mode!");
        mBeacon = getArguments().getParcelable(EXTRA_BEACON);
        if (mPropertiesMode == Mode.UPDATE && mBeacon == null) throw new IllegalArgumentException("Properties fragment requires a beacon!");
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get(getActivity()).getDataManager();
        if (mPropertiesMode == Mode.VIEW) WatchTowerApplication.get(getActivity()).getBus().register(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_properties, container, false);
        ButterKnife.bind(this, fragmentView);
        setupFragment();
        if (mPropertiesMode == Mode.UPDATE || mPropertiesMode == Mode.VIEW) {
            disableViewInteration();
            setupBeaconForm();
        }
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPropertiesMode == Mode.VIEW) WatchTowerApplication.get(getActivity()).getBus().unregister(this);
        mSubscriptions.unsubscribe();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!(getActivity() instanceof DetailActivity)) inflater.inflate(R.menu.register, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                validateBeaconData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onBeaconUpdated(BusEvent.BeaconUpdated event) {
        mBeacon = event.beacon;
        setupBeaconForm();
    }

    private void setupFragment() {
        if (mPropertiesMode == Mode.REGISTER) {
            mBeaconNameText.setVisibility(View.GONE);
            mBeaconNameEditText.setVisibility(View.GONE);
        }
    }

    private void disableViewInteration() {
        mBeaconNameEditText.setInputType(0);
        mBeaconNameEditText.setFocusable(false);
        mBeaconNameEditText.setBackground(null);
        mAdvertisedIdEditText.setInputType(0);
        mAdvertisedIdEditText.setFocusable(false);
        mAdvertisedIdEditText.setBackground(null);
        if (mPropertiesMode == Mode.VIEW) {
            mDescriptionEditText.setInputType(0);
            mDescriptionEditText.setFocusable(false);
            mDescriptionEditText.setBackground(null);
            mLatitudeEditText.setInputType(0);
            mLatitudeEditText.setFocusable(false);
            mLatitudeEditText.setBackground(null);
            mLongitudeEditText.setInputType(0);
            mLongitudeEditText.setFocusable(false);
            mLongitudeEditText.setBackground(null);
            mPlaceIdEditText.setInputType(0);
            mPlaceIdEditText.setFocusable(false);
            mPlaceIdEditText.setBackground(null);
            beaconStatusSpinner.setEnabled(false);
            beaconTypeSpinner.setEnabled(false);
            beaconStabilitySpinner.setEnabled(false);
            mAdvertisedIdErrorMessage.setVisibility(View.GONE);
            mStatusErrorMessage.setVisibility(View.GONE);
        }
    }

    private void setupBeaconForm() {
        mBeaconNameEditText.setText(mBeacon.beaconName);
        mAdvertisedIdEditText.setText(mBeacon.advertisedId.id);
        if (mBeacon.placeId != null) {
            mPlaceIdEditText.setText(mBeacon.placeId);
        } else if (mPropertiesMode == Mode.VIEW) {
            mBeaconPlaceId.setVisibility(View.GONE);
            mPlaceIdEditText.setVisibility(View.GONE);
        }
        if (mBeacon.description != null) {
            mDescriptionEditText.setText(mBeacon.description);
        } else if (mPropertiesMode == Mode.VIEW) {
            mBeaconDescription.setVisibility(View.GONE);
            mDescriptionEditText.setVisibility(View.GONE);
        }
        if (mBeacon.latLng != null) {
            if (mBeacon.latLng.latitude != null) {
                mLatitudeEditText.setText(String.valueOf(mBeacon.latLng.latitude));
            } else {
                mLatitudeEditText.setVisibility(View.GONE);
            }
            if (mBeacon.latLng.longitude != null) {
                mLongitudeEditText.setText(String.valueOf(mBeacon.latLng.longitude));
            } else {
                mLongitudeEditText.setVisibility(View.GONE);
            }
            if (mLatitudeEditText.getVisibility() == View.GONE
                    && mLongitudeEditText.getVisibility() == View.GONE) {
                mBeaconLocation.setVisibility(View.GONE);
            }
        } else if (mPropertiesMode == Mode.VIEW) {
            mBeaconLocation.setVisibility(View.GONE);
            mLatitudeEditText.setVisibility(View.GONE);
            mLongitudeEditText.setVisibility(View.GONE);
        }

        ArrayList<String> statuses = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.statuses)));
        beaconStatusSpinner.setSelection(statuses.indexOf(mBeacon.status.getString()));
        if (mBeacon.advertisedId.type != null) {
            ArrayList<String> types = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.types)));
            beaconTypeSpinner.setSelection(types.indexOf(mBeacon.advertisedId.type.getString()));
        } else if (mPropertiesMode == Mode.VIEW) {
            mBeaconType.setVisibility(View.GONE);
            beaconTypeSpinner.setVisibility(View.GONE);
        }
        if (mBeacon.expectedStability != null) {
            ArrayList<String> types = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.stabilities)));
            beaconStabilitySpinner.setSelection(types.indexOf(mBeacon.expectedStability.getString()));
        } else if (mPropertiesMode == Mode.VIEW) {
            mBeaconStability.setVisibility(View.GONE);
            beaconStabilitySpinner.setVisibility(View.GONE);
        }
    }

    private void validateBeaconData() {
        boolean isValid = mAdvertisedIdEditText.getText().length() > 0 && beaconStatusSpinner.getSelectedItemPosition() > 0;
        mAdvertisedIdErrorMessage.setVisibility(mAdvertisedIdEditText.getText().length() > 0 ? View.INVISIBLE : View.VISIBLE);
        mStatusErrorMessage.setVisibility(beaconStatusSpinner.getSelectedItemPosition() > 0 ? View.INVISIBLE : View.VISIBLE);
        if (!DataUtils.isStringDoubleValue(mLatitudeEditText.getText().toString())) isValid = false;
        if (!DataUtils.isStringDoubleValue(mLongitudeEditText.getText().toString())) isValid = false;
        if (isValid) registerBeacon();
    }

    private Beacon buildBeaconObject() {
        AdvertisedId advertisedId = new AdvertisedId(mAdvertisedIdEditText.getText().toString(),
                AdvertisedId.Type.fromString(beaconTypeSpinner.getSelectedItem().toString()));

        LatLng latLng = new LatLng(Double.valueOf(mLatitudeEditText.getText().toString()),
                Double.valueOf(mLongitudeEditText.getText().toString()));

        return new Beacon.BeaconBuilder(advertisedId)
                .status(beaconStatusSpinner.getSelectedItemPosition() > 0 ? Status.fromString(beaconStatusSpinner.getSelectedItem().toString()) : null)
                .stability(beaconStabilitySpinner.getSelectedItemPosition() > 0 ? Stability.fromString(beaconStabilitySpinner.getSelectedItem().toString()) : null)
                .description(mDescriptionEditText.getText().toString())
                .placeId(mPlaceIdEditText.getText().toString())
                .latLng(latLng)
                .build();
    }

    private void registerBeacon() {
        Beacon beacon = buildBeaconObject();
        boolean hasStatusChanged = false;
        if (mPropertiesMode == Mode.UPDATE) hasStatusChanged = beacon.status != mBeacon.status;

        Observable<Beacon> beaconObservable =  mPropertiesMode == Mode.UPDATE
                ? mDataManager.updateBeacon(mBeacon.beaconName, beacon, hasStatusChanged, beacon.status)
                : mDataManager.registerBeacon(beacon);

        mSubscriptions.add(beaconObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getScheduler())
                .subscribe(new Subscriber<Beacon>() {
                    @Override
                    public void onCompleted() {
                        WatchTowerApplication.get(getActivity()).getBus().post(new BusEvent.BeaconListAmended());
                        getActivity().finish();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.d("There was an error : " + error.getMessage());
                        if (error instanceof RetrofitError) {
                            DialogFactory.createRetrofitErrorDialog(getActivity(), (RetrofitError) error);
                        } else {
                            DialogFactory.createSimpleErrorDialog(getActivity()).show();
                        }
                    }

                    @Override
                    public void onNext(Beacon beacon) {
                        if (mPropertiesMode == Mode.UPDATE) {
                            WatchTowerApplication.get(getActivity()).getBus().post(new BusEvent.BeaconUpdated(beacon));
                        }
                    }
                }));
    }

}
