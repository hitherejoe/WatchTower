package com.hitherejoe.proximityapidemo.android.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hitherejoe.proximityapidemo.android.R;
import com.hitherejoe.proximityapidemo.android.data.model.Attachment;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.util.DataUtils;

import uk.co.ribot.easyadapter.ItemViewHolder;
import uk.co.ribot.easyadapter.PositionInfo;
import uk.co.ribot.easyadapter.annotations.LayoutId;
import uk.co.ribot.easyadapter.annotations.ViewId;

@LayoutId(R.layout.item_attachment)
public class AttachmentHolder extends ItemViewHolder<Attachment> {

    @ViewId(R.id.text_name)
    TextView mAttachmentNameText;

    @ViewId(R.id.text_data)
    TextView mAttachmentDataText;

    @ViewId(R.id.text_delete)
    TextView mAttachmentDeleteText;

    public AttachmentHolder(View view) {
        super(view);
    }

    @Override
    public void onSetValues(Attachment attachment, PositionInfo positionInfo) {
        mAttachmentNameText.setText(attachment.attachmentName);
        mAttachmentDataText.setText(DataUtils.base64Encode(DataUtils.base64Decode(attachment.data)));
    }

    @Override
    public void onSetListeners() {
        mAttachmentDeleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttachmentListener attachmentListener = getListener(AttachmentListener.class);
                if (attachmentListener != null) attachmentListener.onDeleteClicked(getItem());
            }
        });
    }

    public interface AttachmentListener {
        void onDeleteClicked(Attachment attachment);
    }

}