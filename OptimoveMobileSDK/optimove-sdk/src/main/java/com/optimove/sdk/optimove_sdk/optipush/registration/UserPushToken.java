package com.optimove.sdk.optimove_sdk.optipush.registration;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

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
    private boolean isRegistration;
    private boolean optIn;
    private String osName;

    private UserPushToken(boolean isRegistration) {

        this.tenantId = -1;
        this.osVersion = String.format("Android %s; %s", Build.VERSION.RELEASE, Build.MODEL);
        this.osName = "Android";
        this.token = null;
        this.publicCustomerId = null;
        this.isRegistration = isRegistration;
        Context context = Optimove.getInstance().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        // TODO: 8/3/2017 Maybe user InstanceID.getId() ?????
        this.deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        this.optIn = NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public JSONObject toJson() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TENANT_ID, tenantId);
        jsonObject.put(DEVICE_ID, deviceId);
        jsonObject.put(OS_VERSION, osVersion);
        jsonObject.put(FCM_TOKEN, token);
        jsonObject.put(PUBLIC_CUSTOMER_ID, publicCustomerId);
        jsonObject.put(IS_CUSTOMER, isCustomer);
        jsonObject.put(OS_NAME, osName);
        jsonObject.put(IS_REGISTRATION, isRegistration);
        jsonObject.put(OPT_IN, optIn);
        return jsonObject;
    }

    public static class Builder {

        private UserPushToken userPushToken;

        public Builder(boolean isRegistration) {

            userPushToken = new UserPushToken(isRegistration);
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

            userPushToken.isCustomer = userInfo.getUserId() != null;
            userPushToken.publicCustomerId = userPushToken.isCustomer ? userInfo.getUserId() : userInfo.getVisitorId();
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
        String FCM_TOKEN = "fcmToken";
        String PUBLIC_CUSTOMER_ID = "publicCustomerId";
        String IS_CUSTOMER = "isCustomer";
        String OS_NAME = "osName";
        String OPT_IN = "optIn";
        String IS_REGISTRATION = "optIn";
    }
}
