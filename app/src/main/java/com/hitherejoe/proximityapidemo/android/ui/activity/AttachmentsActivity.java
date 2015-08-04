package com.hitherejoe.proximityapidemo.android.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hitherejoe.proximityapidemo.android.ProximityApiApplication;
import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.BusEvent;
import com.hitherejoe.proximityapidemo.android.data.DataManager;
import com.hitherejoe.proximityapidemo.android.data.model.Attachment;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.data.remote.ProximityApiService;
import com.hitherejoe.proximityapidemo.android.ui.adapter.AttachmentHolder;
import com.hitherejoe.proximityapidemo.android.util.DialogFactory;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;

public class AttachmentsActivity extends BaseActivity {

    @InjectView(R.id.text_no_attachments)
    TextView mNoAttachmentsText;

    @InjectView(R.id.recycler_attachments)
    RecyclerView mAttachmentsRecycler;

    @InjectView(R.id.progress_indicator)
    ProgressBar mProgressBar;

    @InjectView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    private static final String TAG = "AttachmentsActivity";
    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;
    private static final String EXTRA_BEACON = "com.hitherejoe.proximityapidemo.android.ui.activity.UpdateActivity.EXTRA_BEACON";
    private Beacon mBeacon;
    private EasyRecyclerAdapter<Attachment> mEasyRecycleAdapter;
    private List<Attachment> mAttachments;
    private ProgressDialog mProgressDialog;

    public static Intent getStartIntent(Context context, Beacon beacon) {
        Intent intent = new Intent(context, AttachmentsActivity.class);
        intent.putExtra(EXTRA_BEACON, beacon);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachments);
        ButterKnife.inject(this);
        mBeacon = getIntent().getParcelableExtra(EXTRA_BEACON);
        if (mBeacon == null) throw new IllegalArgumentException("Beacon is required!");
        mSubscriptions = new CompositeSubscription();
        mDataManager = ProximityApiApplication.get().getDataManager();
        mAttachments = new ArrayList<>();
        mEasyRecycleAdapter = new EasyRecyclerAdapter<>(this, AttachmentHolder.class, mAttachments, mAttachmentListener);
        ProximityApiApplication.get().getBus().register(this);
        setupViews();
        setupActionBar();
        retrieveAttachments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
        ProximityApiApplication.get().getBus().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attachments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteAttachmentByType();
                return true;
            case R.id.action_delete_all:
                deleteAttachments(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onAttachmentAdded(BusEvent.AttachmentAdded event) {
        retrieveAttachments();
    }

    @OnClick(R.id.fab_add)
    public void onFabAddClick() {
        startActivity(AddAttachmentActivity.getStartIntent(this, mBeacon));
    }

    private void setupViews() {
        mAttachmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAttachmentsRecycler.setAdapter(mEasyRecycleAdapter);
        mSwipeRefresh.setColorSchemeResources(R.color.primary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrieveAttachments();
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void deleteAttachmentByType() {
        DialogFactory.createInputDialog(this, getString(R.string.dialog_title_delete), getString(R.string.dialog_message_delete), new DialogFactory.DialogInputCallback() {
            @Override
            public String onInputSubmitted(String input) {
                deleteAttachments(input);
                return null;
            }
        }).show();
    }

    private void deleteAttachments(String type) {
        mProgressDialog = DialogFactory.createProgressDialog(this,
                type == null ? getString(R.string.progress_dialog_deleting_all_attachments)
                        : getString(R.string.progress_dialog_deleting_type_attachments, type));
        mProgressDialog.show();
        mSubscriptions.add(mDataManager.deleteBatchAttachments(mBeacon.beaconName, type)
                .subscribeOn(mDataManager.getScheduler())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ProximityApiService.AttachmentResponse>() {
                    @Override
                    public void onCompleted() {
                        mProgressDialog.dismiss();
                        handleViewVisibility();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "There was an error deleting all attachments " + error);
                        mProgressDialog.dismiss();
                        displayErrorDialog(error);
                    }

                    @Override
                    public void onNext(ProximityApiService.AttachmentResponse attachmentResponse) {
                        mAttachments.clear();
                        if (attachmentResponse.attachments != null) {
                            mAttachments.addAll(attachmentResponse.attachments);
                        }
                    }
                }));
    }

    private void retrieveAttachments() {
        mSubscriptions.add(mDataManager.getAttachments(mBeacon.beaconName, null)
                .subscribeOn(mDataManager.getScheduler())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ProximityApiService.AttachmentResponse>() {
                    @Override
                    public void onCompleted() {
                        mProgressBar.setVisibility(View.GONE);
                        mSwipeRefresh.setRefreshing(false);
                        handleViewVisibility();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "There was an error retrieving the namespaces " + error);
                        mProgressBar.setVisibility(View.GONE);
                        mSwipeRefresh.setRefreshing(false);
                        displayErrorDialog(error);
                    }

                    @Override
                    public void onNext(ProximityApiService.AttachmentResponse attachmentResponse) {
                        if (attachmentResponse.attachments != null) {
                            mAttachments.addAll(attachmentResponse.attachments);
                        }
                    }
                }));
    }

    private void deleteAttachment(final Attachment attachment) {
        if (mProgressDialog == null) {
            mProgressDialog = DialogFactory.createProgressDialog(this, R.string.progress_dialog_deleting_attachment);
        }
        mProgressDialog.show();
        mSubscriptions.add(mDataManager.deleteAttachment(attachment.attachmentName)
                .subscribeOn(mDataManager.getScheduler())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        mProgressDialog.dismiss();
                        mAttachments.remove(attachment);
                        mEasyRecycleAdapter.notifyDataSetChanged();
                        handleViewVisibility();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "There was an error deleting the attachment " + error);
                        mProgressDialog.dismiss();
                        displayErrorDialog(error);
                    }

                    @Override
                    public void onNext(Void aVoid) { }
                }));
    }

    private void handleViewVisibility() {
        if (mEasyRecycleAdapter.getItemCount() > 0) {
            mAttachmentsRecycler.setVisibility(View.VISIBLE);
            mNoAttachmentsText.setVisibility(View.GONE);
        } else {
            mAttachmentsRecycler.setVisibility(View.GONE);
            mNoAttachmentsText.setVisibility(View.VISIBLE);
        }
    }

    private void displayErrorDialog(Throwable error) {
        if (error instanceof RetrofitError) {
            DialogFactory.createRetrofitErrorDialog(
                    AttachmentsActivity.this,
                    (RetrofitError) error).show();
        } else {
            DialogFactory.createSimpleOkErrorDialog(
                    AttachmentsActivity.this,
                    getString(R.string.dialog_error_title),
                    getString(R.string.dialog_general_error_Message)).show();
        }
    }

    private AttachmentHolder.AttachmentListener mAttachmentListener = new AttachmentHolder.AttachmentListener() {
        @Override
        public void onDeleteClicked(Attachment attachment) {
            deleteAttachment(attachment);
        }
    };

}