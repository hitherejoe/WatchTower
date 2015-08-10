package com.hitherejoe.watchtower.data.model;

public class Namespace {
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

        public String getString() {
            return string;
        }

    }

    public Namespace() { }
}
