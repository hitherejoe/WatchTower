package com.hitherejoe.watchtower.data;

import com.hitherejoe.watchtower.data.model.Beacon;

public class BusEvent {
    public static class BeaconListAmended { }
    public static class BeaconUpdated {
        public Beacon beacon;

        public BeaconUpdated(Beacon beacon) {
            this.beacon = beacon;
        }

    }
    public static class AttachmentAdded { }
    public static class AuthenticationError { }
}
