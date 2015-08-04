package com.hitherejoe.proximityapidemo.android.data;

import com.hitherejoe.proximityapidemo.android.data.model.Beacon;

public class BusEvent {
    public static class BeaconListAmended { }
    public static class BeaconUpdated {
        public Beacon beacon;

        public BeaconUpdated(Beacon beacon) {
            this.beacon = beacon;
        }

    }
    public static class AttachmentAdded { }
}
