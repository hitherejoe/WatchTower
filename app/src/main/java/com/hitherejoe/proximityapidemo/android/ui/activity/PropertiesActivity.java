package com.hitherejoe.proximityapidemo.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.ui.fragment.PropertiesFragment;
import com.hitherejoe.proximityapidemo.android.ui.fragment.PropertiesFragment.Mode;

import butterknife.ButterKnife;

public class PropertiesActivity extends BaseActivity {

    private static final String TAG = "PropertiesActivity";
    private static final String EXTRA_BEACON =
            "com.hitherejoe.proximityapidemo.android.ui.activity.PropertiesActivity.EXTRA_BEACON";
    private static final String EXTRA_MODE =
            "com.hitherejoe.proximityapidemo.android.ui.activity.PropertiesActivity.EXTRA_MODE";

    public static Intent getStartIntent(Context context, Beacon beacon, PropertiesFragment.Mode mode) {
        Intent intent = new Intent(context, PropertiesActivity.class);
        intent.putExtra(EXTRA_BEACON, beacon);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }

    public static Intent getStartIntent(Context context, PropertiesFragment.Mode mode) {
        Intent intent = new Intent(context, PropertiesActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        ButterKnife.inject(this);
        Mode mode = (PropertiesFragment.Mode) getIntent().getSerializableExtra(EXTRA_MODE);
        if (mode == null) throw new IllegalArgumentException(TAG + ": Beacon is required!");
        Beacon beacon = getIntent().getParcelableExtra(EXTRA_BEACON);
        if (mode == PropertiesFragment.Mode.UPDATE && beacon == null) throw new IllegalArgumentException(TAG + ": Beacon is required!");
        setupActionBar();
        addFragment(beacon, mode);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void addFragment(Beacon beacon, Mode mode) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container_fragment, PropertiesFragment.newInstance(beacon, mode))
                .commit();
    }

}