package com.hitherejoe.watchtower.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.WatchTowerApplication;
import com.hitherejoe.watchtower.data.BusEvent;
import com.hitherejoe.watchtower.data.DataManager;
import com.hitherejoe.watchtower.data.model.Attachment;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.remote.WatchTowerService;
import com.hitherejoe.watchtower.ui.adapter.AttachmentHolder;
import com.hitherejoe.watchtower.util.DialogFactory;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;

public class AttachmentsActivity extends BaseActivity {

    @Bind(R.id.text_no_attachments)
    TextView mNoAttachmentsText;

    @Bind(R.id.recycler_attachments)
    RecyclerView mAttachmentsRecycler;

    @Bind(R.id.progress_indicator)
    ProgressBar mProgressBar;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    private DataManager mDataManager;
    private CompositeSubscription mSubscriptions;
    private static final String EXTRA_BEACON =
            "com.hitherejoe.watchtower.ui.activity.UpdateActivity.EXTRA_BEACON";
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
        ButterKnife.bind(this);
        mBeacon = getIntent().getParcelableExtra(EXTRA_BEACON);
        if (mBeacon == null) throw new IllegalArgumentException("Beacon is required!");
        mSubscriptions = new CompositeSubscription();
        mDataManager = WatchTowerApplication.get(this).getComponent().dataManager();
        mAttachments = new ArrayList<>();
        mEasyRecycleAdapter = new EasyRecyclerAdapter<>(this, AttachmentHolder.class, mAttachments, mAttachmentListener);
        WatchTowerApplication.get(this).getComponent().eventBus().register(this);
        setupViews();
        setupActionBar();
        retrieveAttachments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
        WatchTowerApplication.get(this).getComponent().eventBus().unregister(this);
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
                if (mEasyRecycleAdapter.getItemCount() == 0) {
                    showEmptyAttachmentsDialog();
                } else {
                    deleteAttachmentByType();
                }
                return true;
            case R.id.action_delete_all:
                if (mEasyRecycleAdapter.getItemCount() == 0) {
                    showEmptyAttachmentsDialog();
                } else {
                    deleteAttachments(null);
                }
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
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
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
        showProgressDialog(type == null
                ? getString(R.string.progress_dialog_deleting_all_attachments)
                : getString(R.string.progress_dialog_deleting_type_attachments, type));
        mSubscriptions.add(mDataManager.deleteBatchAttachments(mBeacon.beaconName, type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getScheduler())
                .subscribe(new Subscriber<WatchTowerService.AttachmentResponse>() {
                    @Override
                    public void onCompleted() {
                        mProgressDialog.dismiss();
                        handleViewVisibility();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e("There was an error deleting all attachments " + error);
                        mProgressDialog.dismiss();
                        displayErrorDialog(error);
                    }

                    @Override
                    public void onNext(WatchTowerService.AttachmentResponse attachmentResponse) {
                        mAttachments.clear();
                        if (attachmentResponse.attachments != null) {
                            mAttachments.addAll(attachmentResponse.attachments);
                        }
                        mEasyRecycleAdapter.notifyDataSetChanged();
                    }
                }));
    }

    private void retrieveAttachments() {
        mSubscriptions.add(mDataManager.getAttachments(mBeacon.beaconName, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getScheduler())
                .subscribe(new Subscriber<WatchTowerService.AttachmentResponse>() {
                    @Override
                    public void onCompleted() {
                        mProgressBar.setVisibility(View.GONE);
                        mSwipeRefresh.setRefreshing(false);
                        handleViewVisibility();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e("There was an error retrieving the namespaces " + error);
                        mProgressBar.setVisibility(View.GONE);
                        mSwipeRefresh.setRefreshing(false);
                        displayErrorDialog(error);
                    }

                    @Override
                    public void onNext(WatchTowerService.AttachmentResponse attachmentResponse) {
                        mAttachments.clear();
                        if (attachmentResponse.attachments != null) {
                            mAttachments.addAll(attachmentResponse.attachments);
                        }
                        mEasyRecycleAdapter.notifyDataSetChanged();
                    }
                }));
    }

    private void deleteAttachment(final Attachment attachment) {
        showProgressDialog(getString(R.string.progress_dialog_deleting_attachment));
        mSubscriptions.add(mDataManager.deleteAttachment(attachment.attachmentName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getScheduler())
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
                        Timber.e("There was an error deleting the attachment " + error);
                        mProgressDialog.dismiss();
                        displayErrorDialog(error);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                    }
                }));
    }

    private void showProgressDialog(String message) {
        mProgressDialog = DialogFactory.createProgressDialog(this, message);
        mProgressDialog.show();
    }

    private void showEmptyAttachmentsDialog() {
        DialogFactory.createSimpleOkErrorDialog(
                this,
                getString(R.string.dialog_error_title),
                getString(R.string.dialog_error_no_attachments)
        ).show();
    }

    private void handleViewVisibility() {
        boolean hasItems = mEasyRecycleAdapter.getItemCount() > 0;
        mAttachmentsRecycler.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        mNoAttachmentsText.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void displayErrorDialog(Throwable error) {
        if (error instanceof RetrofitError) {
            DialogFactory.createRetrofitErrorDialog(
                    AttachmentsActivity.this,
                    (RetrofitError) error).show();
        } else {
            DialogFactory.createSimpleErrorDialog(AttachmentsActivity.this).show();
        }
    }

    private AttachmentHolder.AttachmentListener mAttachmentListener = new AttachmentHolder.AttachmentListener() {
        @Override
        public void onDeleteClicked(Attachment attachment) {
            deleteAttachment(attachment);
        }
    };

}