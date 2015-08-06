package com.hitherejoe.watchtower.ui.adapter;

import android.view.View;
import android.widget.TextView;

import com.hitherejoe.watchtower.R;
import com.hitherejoe.watchtower.data.model.Attachment;
import com.hitherejoe.watchtower.data.model.Diagnostics;
import com.hitherejoe.watchtower.util.DataUtils;

import uk.co.ribot.easyadapter.ItemViewHolder;
import uk.co.ribot.easyadapter.PositionInfo;
import uk.co.ribot.easyadapter.annotations.LayoutId;
import uk.co.ribot.easyadapter.annotations.ViewId;

@LayoutId(R.layout.item_alert)
public class AlertHolder extends ItemViewHolder<Diagnostics.Alert> {

    @ViewId(R.id.text_alert)
    TextView mAlertText;

    public AlertHolder(View view) {
        super(view);
    }

    @Override
    public void onSetValues(Diagnostics.Alert alert, PositionInfo positionInfo) {
        mAlertText.setText(alert.toString());
    }


}