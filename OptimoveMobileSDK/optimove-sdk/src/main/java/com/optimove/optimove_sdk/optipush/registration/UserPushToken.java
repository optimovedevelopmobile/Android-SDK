package com.optimove.optimove_sdk.optipush.registration;

import android.provider.Settings;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

public class UserPushToken {

    private String id;
    private String email;
    private String tenantId;
    private Type type;

    public UserPushToken(String id, String email, String tenantId, Type type) {
        this.id = id;
        this.email = email;
        this.tenantId = tenantId;
        this.type = type;
    }

    public JSONObject toJson() {

        return new JSONObject();
    }

    public enum Type {
        VISITOR, CUSTOMER
    }
}
