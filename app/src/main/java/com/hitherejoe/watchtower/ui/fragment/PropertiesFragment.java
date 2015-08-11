package com.hitherejoe.watchtower.ui.fragment;

import android.app.ProgressDialog;
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

    @Bind(R.id.spinner_status)
    Spinner mBeaconStatusSpinner;

    @Bind(R.id.spinner_type)
    Spinner mBeaconTypeSpinner;

    @Bind(R.id.spinner_stability)
    Spinner mBeaconStabilitySpinner;

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

    @Bind(R.id.text_latitude_error_message)
    TextView mLatitudeErrorMessage;

    @Bind(R.id.text_longitude_error_message)
    TextView mLongitudeErrorMessage;

    private static final String EXTRA_MODE = "EXTRA_MODE";
    private static final String EXTRA_BEACON = "EXTRA_BEACON";
    private Beacon mBeacon;
    private CompositeSubscription mSubscriptions;
    private DataManager mDataManager;
    private Mode mPropertiesMode;
    private ProgressDialog mProgressDialog;

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
        if (mPropertiesMode == null) {
            throw new IllegalArgumentException("Properties fragment requires a mode!");
        }
        mBeacon = getArguments().getParcelable(EXTRA_BEACON);
        if (mPropertiesMode == Mode.UPDATE && mBeacon == null) {
            throw new IllegalArgumentException("Properties fragment requires a beacon!");
        }
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get(getActivity()).getComponent().dataManager();
        if (mPropertiesMode == Mode.VIEW) {
            WatchTowerApplication.get(getActivity()).getComponent().eventBus().register(this);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_properties, container, false);
        ButterKnife.bind(this, fragmentView);
        disableFormFields();
        if (mPropertiesMode != Mode.REGISTER) populateBeaconForm();
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPropertiesMode == Mode.VIEW) {
            WatchTowerApplication.get(getActivity()).getComponent().eventBus().unregister(this);
        }
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
        populateBeaconForm();
    }

    private void disableFormFields() {
        if (mPropertiesMode == Mode.VIEW) {
            displayTextFieldAsReadOnly(mBeaconNameEditText);
            displayTextFieldAsReadOnly(mAdvertisedIdEditText);
            displayTextFieldAsReadOnly(mDescriptionEditText);
            displayTextFieldAsReadOnly(mLatitudeEditText);
            displayTextFieldAsReadOnly(mLongitudeEditText);
            displayTextFieldAsReadOnly(mPlaceIdEditText);
            mBeaconStatusSpinner.setEnabled(false);
            mBeaconTypeSpinner.setEnabled(false);
            mBeaconStabilitySpinner.setEnabled(false);
            mAdvertisedIdErrorMessage.setVisibility(View.GONE);
            mStatusErrorMessage.setVisibility(View.GONE);
        }
        if (mPropertiesMode == Mode.REGISTER) {
            mBeaconNameText.setVisibility(View.GONE);
            mBeaconNameEditText.setVisibility(View.GONE);
        } else if (mPropertiesMode == Mode.UPDATE) {
            displayTextFieldAsReadOnly(mBeaconNameEditText);
            displayTextFieldAsReadOnly(mAdvertisedIdEditText);
        }
    }

    private void displayTextFieldAsReadOnly(TextView textField) {
        textField.setInputType(0);
        textField.setFocusable(false);
        textField.setBackground(null);
    }

    private void populateBeaconForm() {
        mBeaconNameEditText.setText(mBeacon.beaconName);
        String id = DataUtils.base64DecodeToString(mBeacon.advertisedId.id);
        if (id != null) mAdvertisedIdEditText.setText(id);
        // If properties are not defined then we hide the corresponding views
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
            } else if (mPropertiesMode == Mode.VIEW) {
                mLatitudeEditText.setVisibility(View.GONE);
                mLatitudeErrorMessage.setVisibility(View.GONE);
            }
            if (mBeacon.latLng.longitude != null) {
                mLongitudeEditText.setText(String.valueOf(mBeacon.latLng.longitude));
            } else if (mPropertiesMode == Mode.VIEW){
                mLongitudeEditText.setVisibility(View.GONE);
                mLongitudeErrorMessage.setVisibility(View.GONE);
            }
            if (mLatitudeEditText.getVisibility() == View.GONE
                    && mLongitudeEditText.getVisibility() == View.GONE) {
                mBeaconLocation.setVisibility(View.GONE);
            }
        } else if (mPropertiesMode == Mode.VIEW) {
            mBeaconLocation.setVisibility(View.GONE);
            mLatitudeEditText.setVisibility(View.GONE);
            mLatitudeErrorMessage.setVisibility(View.GONE);
            mLongitudeEditText.setVisibility(View.GONE);
            mLongitudeErrorMessage.setVisibility(View.GONE);
        }

        ArrayList<String> statuses =
                new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.statuses)));
        mBeaconStatusSpinner.setSelection(statuses.indexOf(mBeacon.status.getString()));
        if (mBeacon.advertisedId.type != null) {
            ArrayList<String> types =
                    new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.types)));
            mBeaconTypeSpinner.setSelection(types.indexOf(mBeacon.advertisedId.type.getString()));
        } else if (mPropertiesMode == Mode.VIEW) {
            mBeaconType.setVisibility(View.GONE);
            mBeaconTypeSpinner.setVisibility(View.GONE);
        }
        if (mBeacon.expectedStability != null) {
            ArrayList<String> types =
                    new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.stabilities)));
            mBeaconStabilitySpinner.setSelection(types.indexOf(mBeacon.expectedStability.getString()));
        } else if (mPropertiesMode == Mode.VIEW) {
            mBeaconStability.setVisibility(View.GONE);
            mBeaconStabilitySpinner.setVisibility(View.GONE);
        }
    }

    private void validateBeaconData() {
        boolean isValid = mAdvertisedIdEditText.getText().length() > 0
                && mBeaconStatusSpinner.getSelectedItemPosition() > 0;
        mAdvertisedIdErrorMessage.setVisibility(mAdvertisedIdEditText.getText().length() > 0
                ? View.INVISIBLE : View.VISIBLE);
        mStatusErrorMessage.setVisibility(mBeaconStatusSpinner.getSelectedItemPosition() > 0
                ? View.INVISIBLE : View.VISIBLE);
        String latitude = mLatitudeEditText.getText().toString();
        String longitude = mLongitudeEditText.getText().toString();
        if (latitude.length() > 0) {
            if(!DataUtils.isStringDoubleValue(latitude)) {
                isValid = false;
                mLatitudeErrorMessage.setVisibility(View.VISIBLE);
            } else {
                mLatitudeErrorMessage.setVisibility(View.INVISIBLE);
            }
        } else {
            mLatitudeErrorMessage.setVisibility(View.INVISIBLE);
        }
        if (longitude.length() > 0){
            if (!DataUtils.isStringDoubleValue(longitude)) {
                isValid = false;
                mLongitudeErrorMessage.setVisibility(View.VISIBLE);
            } else {
                mLongitudeErrorMessage.setVisibility(View.INVISIBLE);
            }
        } else {
            mLongitudeErrorMessage.setVisibility(View.INVISIBLE);
        }
        if (isValid) saveBeacon(buildBeaconObject());
    }

    private Beacon buildBeaconObject() {
        AdvertisedId advertisedId = new AdvertisedId(DataUtils.base64Encode(mAdvertisedIdEditText.getText().toString().getBytes()),
                AdvertisedId.Type.fromString(mBeaconTypeSpinner.getSelectedItem().toString()));
        String latitude = mLatitudeEditText.getText().toString();
        String longitude = mLatitudeEditText.getText().toString();
        LatLng latLng = new LatLng();
        if (latitude.length() > 0) latLng.latitude = Double.valueOf(latitude);
        if (longitude.length() > 0) latLng.longitude = Double.valueOf(latitude);

        return new Beacon.BeaconBuilder(advertisedId)
                .status(mBeaconStatusSpinner.getSelectedItemPosition() > 0
                        ? Status.fromString(mBeaconStatusSpinner.getSelectedItem().toString())
                        : null)
                .stability(mBeaconStabilitySpinner.getSelectedItemPosition() > 0
                        ? Stability.fromString(mBeaconStabilitySpinner.getSelectedItem().toString())
                        : null)
                .description(mDescriptionEditText.getText().toString())
                .placeId(mPlaceIdEditText.getText().toString())
                .latLng(latLng)
                .build();
    }

    private void saveBeacon(Beacon beacon) {
        showProgressDialog();
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
                        mProgressDialog.dismiss();
                        WatchTowerApplication.get(
                                getActivity()).getComponent().eventBus().post(new BusEvent.BeaconListAmended());
                        getActivity().finish();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mProgressDialog.dismiss();
                        Timber.d("There was an error saving the beacon : " + error.getMessage());
                        if (error instanceof RetrofitError) {
                            DialogFactory.createRetrofitErrorDialog(
                                    getActivity(), (RetrofitError) error).show();
                        } else {
                            DialogFactory.createSimpleErrorDialog(getActivity()).show();
                        }
                    }

                    @Override
                    public void onNext(Beacon beacon) {
                        if (mPropertiesMode == Mode.UPDATE) {
                            WatchTowerApplication.get(getActivity())
                                    .getComponent().eventBus().post(new BusEvent.BeaconUpdated(beacon));
                        }
                    }
                }));
    }

    private void showProgressDialog() {
        mProgressDialog = DialogFactory.createProgressDialog(getActivity(), R.string.progress_dialog_saving_beacon);
        mProgressDialog.show();
    }

}
