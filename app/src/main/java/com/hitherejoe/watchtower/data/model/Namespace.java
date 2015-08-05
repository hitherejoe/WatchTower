package com.hitherejoe.watchtower.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Namespace implements Parcelable {
    public String namespaceName;
    public Visibility servingVisibility;

    public enum Visibility {
        VISIBILITY_UNSPECIFIED("Unspecified"),
        UNLISTED("Unlisted"),
        PUBLIC("Public");

        private String string;

        Visibility(String string) {
            this.string = string;
        }

        public static Visibility fromString(String string) {
            if (string != null) {
                for (Visibility visibility : Visibility.values()) {
                    if (string.equalsIgnoreCase(visibility.string)) {
                        return visibility;
                    }
                }
            }
            return null;
        }

        public String getString() {
            return string;
        }

    }

    public Namespace() { }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.namespaceName);
        dest.writeInt(this.servingVisibility == null ? -1 : this.servingVisibility.ordinal());
    }

    protected Namespace(Parcel in) {
        this.namespaceName = in.readString();
        int tmpServingVisibility = in.readInt();
        this.servingVisibility = tmpServingVisibility == -1 ? null : Visibility.values()[tmpServingVisibility];
    }

    public static final Parcelable.Creator<Namespace> CREATOR = new Parcelable.Creator<Namespace>() {
        public Namespace createFromParcel(Parcel source) {
            return new Namespace(source);
        }

        public Namespace[] newArray(int size) {
            return new Namespace[size];
        }
    };
}
