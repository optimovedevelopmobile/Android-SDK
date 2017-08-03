package com.optimove.sdk.optimove_sdk.optipush.registration;

import android.content.ContentResolver;
import android.os.Build;
import android.provider.Settings;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import static com.optimove.sdk.optimove_sdk.optipush.registration.UserPushToken.UserPushTokenJsonKeys.*;

public class UserPushToken {

    private int tenantId;
    private String deviceId;
    private String osVersion;
    private String token;
    private String publicCustomerId;
    private boolean isCustomer;
    private String email;

    private UserPushToken() {

        this.tenantId = -1;
        ContentResolver contentResolver = Optimove.getInstance().getContext().getContentResolver();
        // TODO: 8/3/2017 Maybe user InstanceID.getId() ?????
        this.deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        this.osVersion = String.format("Android %s; %s", Build.VERSION.RELEASE, Build.MODEL);
        this.token = null;
        this.publicCustomerId = null;
        this.email = null;
    }

    public JSONObject toJson() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TENANT_ID, tenantId);
        jsonObject.put(DEVICE_ID, deviceId);
        jsonObject.put(OS_VERSION, osVersion);
        jsonObject.put(TOKEN, token);
        jsonObject.put(PUBLIC_CUSTOMER_ID, publicCustomerId);
        jsonObject.put(IS_CUSTOMER, isCustomer);
        jsonObject.put(EMAIL, email);
        return jsonObject;
    }

    public static class Builder {

        private UserPushToken userPushToken;

        public Builder() {
            userPushToken = new UserPushToken();
        }

        public Builder setTenantId(int tenantId) {

            userPushToken.tenantId = tenantId;
            return this;
        }

        public Builder setToken(String token) {

            userPushToken.token = token;
            return this;
        }

        public Builder setUserInfo(UserInfo userInfo) {

            userPushToken.isCustomer = userInfo.getUserId() == null;
            userPushToken.publicCustomerId = userPushToken.isCustomer ? userInfo.getVisitorId() : userInfo.getUserId();
            userPushToken.email = userInfo.getEmail();
            return this;
        }

        public UserPushToken build() {
            return userPushToken;
        }
    }

    interface UserPushTokenJsonKeys {

        String TENANT_ID = "tenantId";
        String DEVICE_ID = "deviceId";
        String OS_VERSION = "osVersion";
        String TOKEN = "token";
        String PUBLIC_CUSTOMER_ID = "publicCustomerId";
        String IS_CUSTOMER = "isCustomer";
        String EMAIL = "email";
    }
}
