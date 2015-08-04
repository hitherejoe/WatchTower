package com.hitherejoe.proximityapidemo.android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hitherejoe.proximityapidemo.android.ProximityApiApplication;
import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.BusEvent;
import com.hitherejoe.proximityapidemo.android.data.DataManager;
import com.hitherejoe.proximityapidemo.android.data.model.AdvertisedId;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.data.model.LatLng;
import com.hitherejoe.proximityapidemo.android.ui.activity.DetailActivity;
import com.hitherejoe.proximityapidemo.android.util.DataUtils;
import com.hitherejoe.proximityapidemo.android.util.DialogFactory;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PropertiesFragment extends Fragment {

    @InjectView(R.id.spinner_status)
    Spinner beaconStatusSpinner;

    @InjectView(R.id.spinner_type)
    Spinner beaconTypeSpinner;

    @InjectView(R.id.spinner_stability)
    Spinner beaconStabilitySpinner;

    @InjectView(R.id.edit_text_beacon_name)
    EditText mBeaconNameEditText;

    @InjectView(R.id.edit_text_advertised_id)
    EditText mAdvertisedIdEditText;

    @InjectView(R.id.edit_text_description)
    EditText mDescriptionEditText;

    @InjectView(R.id.edit_text_latitude)
    EditText mLatitudeEditText;

    @InjectView(R.id.edit_text_longitude)
    EditText mLongitudeEditText;

    @InjectView(R.id.edit_text_place_id)
    EditText mPlaceIdEditText;

    @InjectView(R.id.text_title_beacon_name)
    TextView mBeaconNameText;

    @InjectView(R.id.text_title_place_id)
    TextView mBeaconPlaceId;

    @InjectView(R.id.text_title_description)
    TextView mBeaconDescription;

    @InjectView(R.id.text_title_location)
    TextView mBeaconLocation;

    @InjectView(R.id.text_title_type)
    TextView mBeaconType;

    @InjectView(R.id.text_title_stability)
    TextView mBeaconStability;

    @InjectView(R.id.text_advertised_id_error_message)
    TextView mAdvertisedIdErrorMessage;

    @InjectView(R.id.text_status_error_message)
    TextView mStatusErrorMessage;

    private static final String EXTRA_MODE = "EXTRA_MODE";
    private static final String EXTRA_BEACON = "EXTRA_BEACON";
    private static final String TAG = "PropertiesFragment";
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
        mDataManager = ProximityApiApplication.get().getDataManager();
        if (mPropertiesMode == Mode.VIEW) ProximityApiApplication.get().getBus().register(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_properties, container, false);
        ButterKnife.inject(this, fragmentView);
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
        if (mPropertiesMode == Mode.VIEW) ProximityApiApplication.get().getBus().unregister(this);
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
        if (isValid) registerBeacon();
    }

    private void registerBeacon() {
        Beacon beacon = new Beacon();
        AdvertisedId id = new AdvertisedId();
        boolean hasStatusChanged = false;
        if (mPropertiesMode == Mode.UPDATE) hasStatusChanged = beacon.status != mBeacon.status;
        String advertisedId = mAdvertisedIdEditText.getText().toString();
        id.id = DataUtils.base64Encode(advertisedId.getBytes());
        if (beaconTypeSpinner.getSelectedItemPosition() > 0) id.type = AdvertisedId.Type.fromString(beaconTypeSpinner.getSelectedItem().toString());
        beacon.advertisedId = id;
        LatLng latLng = new LatLng();
        if (DataUtils.isStringDoubleValue(mLatitudeEditText.getText().toString())) {
            latLng.latitude = Double.valueOf(mLatitudeEditText.getText().toString());
        }
        if (DataUtils.isStringDoubleValue(mLongitudeEditText.getText().toString())) {
            latLng.longitude = Double.valueOf(mLongitudeEditText.getText().toString());
        }
        beacon.latLng = latLng;
        if (beaconStatusSpinner.getSelectedItemPosition() > 0) {
            beacon.status = Beacon.Status.fromString(beaconStatusSpinner.getSelectedItem().toString());
        }
        if (beaconStabilitySpinner.getSelectedItemPosition() > 0) {
            beacon.expectedStability = Beacon.Stability.fromString(beaconStabilitySpinner.getSelectedItem().toString());
        }
        beacon.placeId = mPlaceIdEditText.getText().toString();
        Observable<Beacon> observable = mPropertiesMode == Mode.UPDATE ? mDataManager.updateBeacon(mBeacon.beaconName, beacon, hasStatusChanged, beacon.status) : mDataManager.registerBeacon(beacon);
        mSubscriptions.add(AppObservable.bindFragment(this,
                observable)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Beacon>() {
                    @Override
                    public void onCompleted() {
                        ProximityApiApplication.get().getBus().post(new BusEvent.BeaconListAmended());
                        getActivity().finish();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.d(TAG, "There was an error : " + error.getMessage());
                        if (error instanceof RetrofitError) {
                            DialogFactory.createRetrofitErrorDialog(getActivity(), (RetrofitError) error);
                        } else {
                            DialogFactory.createSimpleOkErrorDialog(
                                    getActivity(),
                                    getString(R.string.dialog_error_title),
                                    getString(R.string.dialog_general_error_Message)).show();
                        }
                    }

                    @Override
                    public void onNext(Beacon beacon) {
                        if (mPropertiesMode == Mode.UPDATE) {
                            ProximityApiApplication.get().getBus().post(new BusEvent.BeaconUpdated(beacon));
                        }
                    }
                }));
    }

}
