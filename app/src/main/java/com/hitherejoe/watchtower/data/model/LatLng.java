package com.hitherejoe.watchtower.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LatLng implements Parcelable {
    public Double latitude;
    public Double longitude;

    public LatLng() { }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.latitude);
        dest.writeValue(this.longitude);
    }

    protected LatLng(Parcel in) {
        this.latitude = (Double) in.readValue(Double.class.getClassLoader());
        this.longitude = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Creator<LatLng> CREATOR = new Creator<LatLng>() {
        public LatLng createFromParcel(Parcel source) {
            return new LatLng(source);
        }

        public LatLng[] newArray(int size) {
            return new LatLng[size];
        }
    };
}