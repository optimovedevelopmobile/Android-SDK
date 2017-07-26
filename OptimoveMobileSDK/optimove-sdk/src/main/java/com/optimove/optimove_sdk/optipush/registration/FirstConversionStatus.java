package com.optimove.optimove_sdk.optipush.registration;

public enum FirstConversionStatus {
    NA(0), PENDING(1), DONE(2);

    private int rawValue;

    FirstConversionStatus(int rawValue) {
        this.rawValue = rawValue;
    }

    public int getRawValue() {
        return rawValue;
    }

    public static FirstConversionStatus valueOf(int rawValue) {

        switch (rawValue) {
            case 0:
                return NA;
            case 1:
                return PENDING;
            case 2:
                return DONE;
        }
        return null;
    }
}
