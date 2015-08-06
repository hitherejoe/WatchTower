package com.hitherejoe.watchtower.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Beacon implements Parcelable {

    public AdvertisedId advertisedId;
    public String beaconName;
    public Status status;
    public Stability expectedStability;
    public LatLng latLng;
    public String description;
    public String placeId;

    public enum Status {
        STATUS_UNSPECIFIED("Unspecified"),
        ACTIVE("Active"),
        DECOMMISSIONED("Decommissioned"),
        INACTIVE("Inactive");

        private String string;

        Status(String string) {
            this.string = string;
        }

        public static Status fromString(String string) {
            if (string != null) {
                for (Status status : Status.values()) {
                    if (string.equalsIgnoreCase(status.string)) {
                        return status;
                    }
                }
            }
            return null;
        }

        public String getString() {
            return string;
        }

        public String getDisplayName() {
            return string.replaceAll("_", " ");
        }
    }

    public enum Stability {
        STATUS_UNSPECIFIED("Unspecified"),
        STABLE("Stable"),
        PORTABLE("Portable"),
        MOBILE("Mobile"),
        ROVING("Roving");

        private String string;

        Stability(String string) {
            this.string = string;
        }

        public static Stability fromString(String string) {
            if (string != null) {
                for (Stability status : Stability.values()) {
                    if (string.equalsIgnoreCase(status.string)) {
                        return status;
                    }
                }
            }
            return null;
        }

        public String getString() {
            return string;
        }

        public String getDisplayName() {
            return string.replaceAll("_", " ");
        }
    }

    private Beacon (BeaconBuilder builder) {
        this.advertisedId  = builder.advertisedId;
        this.beaconName = builder.beaconName;
        this.status = builder.status;
        this.expectedStability = builder.expectedStability;
        this.latLng = builder.latLng;
        this.description = builder.description;
        this.placeId = builder.placeId;
    }

    public static class BeaconBuilder {
        private AdvertisedId advertisedId;
        private String beaconName;
        private Status status;
        private Stability expectedStability;
        private LatLng latLng;
        private String description;
        private String placeId;

        public BeaconBuilder(AdvertisedId advertisedId) {
            this.advertisedId = advertisedId;
        }

        public BeaconBuilder beaconName(String beaconName) {
            this.beaconName = beaconName;
            return this;
        }

        public BeaconBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public BeaconBuilder stability(Stability stability) {
            this.expectedStability = stability;
            return this;
        }

        public BeaconBuilder latLng(LatLng latLng) {
            this.latLng = latLng;
            return this;
        }

        public BeaconBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BeaconBuilder placeId(String placeId) {
            this.placeId = placeId;
            return this;
        }

        public Beacon build() {
            return new Beacon(this);
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.advertisedId, 0);
        dest.writeString(this.beaconName);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeInt(this.expectedStability == null ? -1 : this.expectedStability.ordinal());
        dest.writeParcelable(this.latLng, 0);
        dest.writeString(this.description);
        dest.writeString(this.placeId);
    }

    public Beacon() { }

    protected Beacon(Parcel in) {
        this.advertisedId = in.readParcelable(AdvertisedId.class.getClassLoader());
        this.beaconName = in.readString();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : Status.values()[tmpStatus];
        int tmpStability = in.readInt();
        this.expectedStability = tmpStability == -1 ? null : Stability.values()[tmpStability];
        this.latLng = in.readParcelable(LatLng.class.getClassLoader());
        this.description = in.readString();
        this.placeId = in.readString();
    }

    public static final Parcelable.Creator<Beacon> CREATOR = new Parcelable.Creator<Beacon>() {
        public Beacon createFromParcel(Parcel source) {
            return new Beacon(source);
        }

        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };
}
