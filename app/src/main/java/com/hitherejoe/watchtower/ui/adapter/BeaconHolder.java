package com.hitherejoe.watchtower.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.data.model.Beacon;

import uk.co.ribot.easyadapter.ItemViewHolder;
import uk.co.ribot.easyadapter.PositionInfo;
import uk.co.ribot.easyadapter.annotations.LayoutId;
import uk.co.ribot.easyadapter.annotations.ViewId;

@LayoutId(R.layout.item_beacon)
public class BeaconHolder extends ItemViewHolder<Beacon> {

    @ViewId(R.id.image_status)
    ImageView mStatusImage;

    @ViewId(R.id.text_identifier)
    TextView mIdentifierText;

    @ViewId(R.id.text_type)
    TextView mTypeText;

    @ViewId(R.id.text_attachments)
    TextView mAttachmentsText;

    @ViewId(R.id.text_view)
    TextView mViewText;

    public BeaconHolder(View view) {
        super(view);
    }

    @Override
    public void onSetValues(Beacon beacon, PositionInfo positionInfo) {
        setBeaconStatusResource(beacon);
        mTypeText.setText(beacon.advertisedId.type.getString());
        mIdentifierText.setText(beacon.beaconName);
    }

    @Override
    public void onSetListeners() {
        getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeaconListener beaconListener = getListener(BeaconListener.class);
                if (beaconListener != null) beaconListener.onViewClicked(getItem());
            }
        });
        mAttachmentsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeaconListener beaconListener = getListener(BeaconListener.class);
                if (beaconListener != null) beaconListener.onAttachmentsClicked(getItem());
            }
        });
        mViewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeaconListener beaconListener = getListener(BeaconListener.class);
                if (beaconListener != null) beaconListener.onViewClicked(getItem());
            }
        });
    }

    private void setBeaconStatusResource(Beacon beacon) {
        Beacon.Status status = beacon.status;
        int resource;
        switch (status) {
            case STATUS_UNSPECIFIED:
                resource = R.drawable.ic_unspecified;
                break;
            case ACTIVE:
                resource = R.drawable.ic_active;
                break;
            case INACTIVE:
                resource = R.drawable.ic_inactive;
                break;
            case DECOMMISSIONED:
                resource = R.drawable.ic_decommissioned;
                break;
            default:
                resource = R.drawable.ic_unspecified;
                break;
        }
        mStatusImage.setBackgroundResource(resource);
    }

    public interface BeaconListener {
        void onAttachmentsClicked(Beacon beacon);
        void onViewClicked(Beacon beacon);
    }
}