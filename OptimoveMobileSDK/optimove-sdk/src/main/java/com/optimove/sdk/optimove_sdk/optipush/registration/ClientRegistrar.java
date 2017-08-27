package com.optimove.sdk.optimove_sdk.optipush.registration;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.TenantToken;
import com.optimove.sdk.optimove_sdk.main.tools.BackendInteractor;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.optimove.sdk.optimove_sdk.optipush.registration.ClientRegistrar.Constants.*;

public class ClientRegistrar {

    private String registrationEndPoint;
    private SharedPreferences registrationPreferences;
    private boolean shouldRegisterClientToken;

    public ClientRegistrar(String registrationEndPoint, boolean shouldRegisterClientToken) {

        Context context = Optimove.getInstance().getContext();
        this.registrationEndPoint = registrationEndPoint;
        this.shouldRegisterClientToken = shouldRegisterClientToken;
        this.registrationPreferences = context.getSharedPreferences(REGISTRATION_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void registerUserForPush(String newToken) {

        @SuppressWarnings("ConstantConditions")
        OptimoveFirebaseInteractor firebaseInteractor = Optimove.getInstance().getOptiPushManager().getFirebaseInteractor();
        boolean clientHaveFirebase = firebaseInteractor.doesClientHaveFirebase();
        final String appControllerSenderId = firebaseInteractor.getAppControllerSenderId();

        if (!clientHaveFirebase || shouldRegisterClientToken) {
            new OperationDispatcher(newToken, true).dispatch();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String token = FirebaseInstanceId.getInstance().getToken(appControllerSenderId, "FCM");
                        new OperationDispatcher(token, true).dispatch();
                    } catch (IOException e) {
                        OptiLogger.e(this, e);
                    }
                }
            }).start();
        }
    }

    public void unregisterUserFromPush(String token) {

        new OperationDispatcher(token, false).dispatch();
    }

    public void registerToTopics() {

        Context context = Optimove.getInstance().getContext();
        FirebaseMessaging.getInstance().subscribeToTopic(OPTIPUSH_GENERAL_TOPIC);
        FirebaseMessaging.getInstance().subscribeToTopic(ANDROID_TOPIC);
        FirebaseMessaging.getInstance().subscribeToTopic(context.getPackageName());
    }

    public void unregisterFromTopics() {

        Context context = Optimove.getInstance().getContext();
        FirebaseMessaging.getInstance().unsubscribeFromTopic(OPTIPUSH_GENERAL_TOPIC);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(ANDROID_TOPIC);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(context.getPackageName());
    }

    public void completeLastOperationIfFailed() {

        String lastFailedRegToken = registrationPreferences.getString(LAST_FAILED_REG_TOKEN_KEY, null);
        String lastFailedUnregToken = registrationPreferences.getString(LAST_FAILED_UNREG_TOKEN_KEY, null);
        if (lastFailedRegToken != null)
            new OperationDispatcher(lastFailedRegToken, true).dispatchFromStorage();
        if (lastFailedUnregToken != null)
            new OperationDispatcher(lastFailedUnregToken, false).dispatchFromStorage();
    }

    public boolean checkAndActIfUserChangedNotificationPermission() {

        boolean currentlyEnabled = NotificationManagerCompat.from(Optimove.getInstance().getContext()).areNotificationsEnabled();
        String lastToken = registrationPreferences.getString(LAST_TOKEN, null);
        if (lastToken == null)
            return currentlyEnabled;
        if (registrationPreferences.contains(LAST_NOTIFICATION_PERMISSION_STATUS)) {
            boolean lastEnabled = registrationPreferences.getBoolean(LAST_NOTIFICATION_PERMISSION_STATUS, true);
            if (currentlyEnabled != lastEnabled)
                new OperationDispatcher(lastToken, true).dispatch();
        } else {
            new OperationDispatcher(lastToken, true).dispatch();
        }
        return currentlyEnabled;
    }

    public boolean wasUserAlreadyRegistered() {

        int conversionRawVal = registrationPreferences.getInt(FIRST_CONVERSION_REGISTRATION_STATUS_KEY, FIRST_CONVERSION_NA);
        return conversionRawVal != FIRST_CONVERSION_NA;
    }

    public void registerNewUser() {

        registrationPreferences.edit().putInt(FIRST_CONVERSION_REGISTRATION_STATUS_KEY, FIRST_CONVERSION_PENDING).apply();
        String lastToken = registrationPreferences.getString(LAST_TOKEN, null);
        if (lastToken != null)
            new OperationDispatcher(lastToken, true).dispatch();
    }

    private class OperationDispatcher implements Response.Listener<JSONObject>, Response.ErrorListener {

        private String token;
        private boolean isRegistration;
        private JSONObject pendingData;

        public OperationDispatcher(String token, boolean isRegistration) {

            this.token = token;
            this.isRegistration = isRegistration;
        }

        public void dispatch() {

            Context context = Optimove.getInstance().getContext();
            try {
                this.pendingData = generateUserPushToken().toJson();
                new BackendInteractor(context, registrationEndPoint).postJson(pendingData)
                        .successListener(this).errorListener(this).destination("registration")
                        .send();
            } catch (JSONException e) {
                OptiLogger.e(this, e);
            }
        }

        public void dispatchFromStorage() {

            Context context = Optimove.getInstance().getContext();
            this.pendingData = FileUtils.readFile(context).named(currentFileNameForOperation()).from(FileUtils.SourceDir.INTERNAL).asJson();
            if (pendingData == null) {
                OptiLogger.w(this, "Attempt to operate on empty file named %s", currentFileNameForOperation());
                return;
            }
            new BackendInteractor(context, registrationEndPoint).postJson(pendingData)
                    .successListener(this).errorListener(this).destination("registration")
                    .send();
        }

        private UserPushToken generateUserPushToken() {

            Optimove optimove = Optimove.getInstance();
            TenantToken tenantToken = optimove.getTenantToken();
            return new UserPushToken.Builder()
                    .setTenantId(tenantToken.getTenantId())
                    .setToken(token)
                    .setUserInfo(optimove.getUserInfo())
                    .setIsRegistration(isRegistration)
                    .build();
        }

        @Override
        public void onResponse(JSONObject response) {

            OptiLogger.d(this, response.toString());
            SharedPreferences.Editor editor = registrationPreferences.edit();
            updateOperationFlagsForSuccess(editor);
            clearCachedFailureData(editor);
            editor.apply();
        }

        private void updateOperationFlagsForSuccess(SharedPreferences.Editor editor) {

            int conversionStat = registrationPreferences.getInt(FIRST_CONVERSION_REGISTRATION_STATUS_KEY, 0);
            if (conversionStat == FIRST_CONVERSION_PENDING)
                editor.putInt(FIRST_CONVERSION_REGISTRATION_STATUS_KEY, FIRST_CONVERSION_DONE);
            editor.putString(LAST_TOKEN, token);
            try {
                editor.putBoolean(LAST_NOTIFICATION_PERMISSION_STATUS, pendingData.getBoolean(UserPushToken.UserPushTokenJsonKeys.OPT_IN));
            } catch (JSONException e) {
                OptiLogger.e(this, e);
            }
        }

        private void clearCachedFailureData(SharedPreferences.Editor editor) {

            Context context = Optimove.getInstance().getContext();
            FileUtils.deleteFile(context).named(currentFileNameForOperation()).from(FileUtils.SourceDir.INTERNAL).now();
            editor.remove(currentTokenForOperation());
        }

        @Override
        public void onErrorResponse(VolleyError error) {

            OptiLogger.w(this, "Failed destination register user set:\n%s", error.getMessage());
            registrationPreferences.edit().putString(currentTokenForOperation(), token).apply();
            Context context = Optimove.getInstance().getContext();
            FileUtils.write(context, pendingData).to(currentFileNameForOperation()).in(FileUtils.SourceDir.INTERNAL).now();
        }

        @NonNull
        private String currentTokenForOperation() {
            return isRegistration ? LAST_FAILED_REG_TOKEN_KEY : LAST_FAILED_UNREG_TOKEN_KEY;
        }

        @NonNull
        private String currentFileNameForOperation() {
            return isRegistration ? LAST_FAILED_REG_DATA_FILE_NAME : LAST_FAILED_UNREG_DATA_FILE_NAME;
        }
    }

    interface Constants {

        String REGISTRATION_PREFERENCES_NAME = "registration_preferences";

        String LAST_TOKEN = "lastToken";
        String LAST_NOTIFICATION_PERMISSION_STATUS = "lastNotificationPermissionStatus";

        String FIRST_CONVERSION_REGISTRATION_STATUS_KEY = "firstConversionStatus";
        int FIRST_CONVERSION_NA = 0;
        int FIRST_CONVERSION_PENDING = 1;
        int FIRST_CONVERSION_DONE = 2;

        String LAST_FAILED_REG_TOKEN_KEY = "lastRegistrationToken";
        String LAST_FAILED_REG_DATA_FILE_NAME = "lastRegistrationRequestData.json";
        String LAST_FAILED_UNREG_TOKEN_KEY = "lastUnregistrationToken";
        String LAST_FAILED_UNREG_DATA_FILE_NAME = "lastUnregistrationRequestData.json";

        String ANDROID_TOPIC = "Android";
        String OPTIPUSH_GENERAL_TOPIC = "OptipushGeneral";
    }
}
