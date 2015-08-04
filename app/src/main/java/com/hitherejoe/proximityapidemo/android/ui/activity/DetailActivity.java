package com.hitherejoe.proximityapidemo.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.hitherejoe.proximityapidemo.android.ProximityApiApplication;
import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.BusEvent;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.ui.fragment.AlertsFragment;
import com.hitherejoe.proximityapidemo.android.ui.fragment.PropertiesFragment;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

public class DetailActivity extends BaseActivity {

    @InjectView(R.id.pager_beacon_detail)
    ViewPager mBeaconDetailViewPager;

    @InjectView(R.id.sliding_tabs)
    TabLayout mTabLayout;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    private CompositeSubscription mSubscriptions;
    private static final String EXTRA_BEACON = "con.hitherejoe.proximityapidemo.android.ui.activity.UpdateActivity.EXTRA_BEACON";
    private Beacon mBeacon;

    public static Intent getStartIntent(Context context, Beacon beacon) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_BEACON, beacon);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.inject(this);
        mBeacon = getIntent().getParcelableExtra(EXTRA_BEACON);
        if (mBeacon == null) throw new IllegalArgumentException("Beacon is required!");
        mSubscriptions = new CompositeSubscription();
        setupToolbar();
        setupViewPager();
        ProximityApiApplication.get().getBus().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProximityApiApplication.get().getBus().unregister(this);
        mSubscriptions.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                startActivity(PropertiesActivity.getStartIntent(DetailActivity.this, mBeacon, PropertiesFragment.Mode.UPDATE));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onBeaconUpdated(BusEvent.BeaconUpdated event) {
        mBeacon = event.beacon;
    }

    private void setupToolbar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewPager() {
        mBeaconDetailViewPager.setOffscreenPageLimit(2);
        mBeaconDetailViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return position == 0 ? PropertiesFragment.newInstance(mBeacon, PropertiesFragment.Mode.VIEW) : AlertsFragment.newInstance(mBeacon);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return position == 0 ? getString(R.string.fragment_title_properties) : getString(R.string.fragment_title_alerts);
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        mTabLayout.setupWithViewPager(mBeaconDetailViewPager);
    }
}