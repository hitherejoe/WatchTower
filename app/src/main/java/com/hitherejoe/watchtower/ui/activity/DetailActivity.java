package com.hitherejoe.watchtower.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.BusEvent;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.ui.fragment.AlertsFragment;
import com.hitherejoe.watchtower.ui.fragment.PropertiesFragment;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class DetailActivity extends BaseActivity {

    @Bind(R.id.sliding_tabs)
    TabLayout mTabLayout;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.pager_beacon_detail)
    ViewPager mBeaconDetailViewPager;

    private CompositeSubscription mSubscriptions;
    private static final String EXTRA_BEACON = "con.hitherejoe.watchtower.ui.activity.DetailActivity.EXTRA_BEACON";
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
        ButterKnife.bind(this);
        mBeacon = getIntent().getParcelableExtra(EXTRA_BEACON);
        if (mBeacon == null) throw new IllegalArgumentException("DetailActivity requires a Beacon object!");
        mSubscriptions = new CompositeSubscription();
        setupToolbar();
        setupViewPager();
        WatchTowerApplication.get(this).getBus().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WatchTowerApplication.get(this).getBus().unregister(this);
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
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewPager() {
        mBeaconDetailViewPager.setOffscreenPageLimit(2);
        mBeaconDetailViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            String[] titles = getResources().getStringArray(R.array.detail_fragment_titles);

            @Override
            public Fragment getItem(int position) {
                return position == 0
                        ? PropertiesFragment.newInstance(mBeacon, PropertiesFragment.Mode.VIEW)
                        : AlertsFragment.newInstance(mBeacon);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }

            @Override
            public int getCount() {
                return titles.length;
            }
        });
        mTabLayout.setupWithViewPager(mBeaconDetailViewPager);
    }
}