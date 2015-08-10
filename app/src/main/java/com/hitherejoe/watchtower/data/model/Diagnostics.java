package com.hitherejoe.watchtower.data.model;

public class Diagnostics {

    public enum Alert {WRONG_LOCATION, LOW_BATTERY}

    public String beaconName;
    public BeaconDate estimatedLowBatteryDate;
    public Alert[] alerts;

    public static class BeaconDate {
        public int year;
        public int month;
        public int day;

        public String buildDate() {
            return String.format("%d/%d/%d", this.day, this.month, this.year);
        }
    }
}
