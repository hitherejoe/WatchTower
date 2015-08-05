package com.hitherejoe.watchtower.data.model;

public class Diagnostics {

    public enum Alert {ALERT_UNSPECIFIED, WRONG_LOCATION, LOW_BATTERY}

    public String beaconName;
    public BeaconDate estimatedLowBatteryDate;
    public Alert[] alerts;

    public class BeaconDate {
        public int year;
        public int month;
        public int day;
    }
}
