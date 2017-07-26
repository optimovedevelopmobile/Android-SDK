package com.optimove.sdk.optimove_sdk.optipush.registration;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

import org.json.JSONObject;

import static com.optimove.sdk.optimove_sdk.optipush.registration.OptiPushClientRegistrar.RegistrarConstants.*;

public class OptiPushClientRegistrar {

    private SharedPreferences logSp;
    private Context context;

    public OptiPushClientRegistrar(Context context) {

        this.logSp = context.getApplicationContext().getSharedPreferences(OPERATIONS_LOG_SP, Context.MODE_PRIVATE);
        this.context = context;
    }

    public void completeLastRegistrationIfFailed() {

        if (logSp.getBoolean(LAST_REG_OPERATION_SUCCESS_KEY, true))
            return;
        if (logSp.getString(LAST_REG_OPERATION_TOKEN_KEY, null) == null)
            unregisterUserFromPush();
        else
            registerUserForPush(FirebaseInstanceId.getInstance(Optimove.getInstance().getSdkFa()).getToken());
    }

     public void unregisterUserFromPush() {

        UserPushToken userPushToken = generateUserPushToken(null);
//        BackendInteractor backendInteractor = new BackendInteractor(context, "");
        // TODO: 7/6/2017 Unregister at the MBAAS
    }

    public void registerUserForPush(String token) {

        UserPushToken userPushToken = generateUserPushToken(token);
//        BackendInteractor backendInteractor = new BackendInteractor(context, "");
        // TODO: 7/6/2017 Register at the MBAAS
    }

    public void unregisterFromTopics() {

        FirebaseMessaging.getInstance().unsubscribeFromTopic(OPTIPUSH_GENERAL_TOPIC);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(ANDROID_TOPIC);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(context.getPackageName());
    }

    public void registerToTopics() {

        FirebaseMessaging.getInstance().subscribeToTopic(OPTIPUSH_GENERAL_TOPIC);
        FirebaseMessaging.getInstance().subscribeToTopic(ANDROID_TOPIC);
        FirebaseMessaging.getInstance().subscribeToTopic(context.getPackageName());
    }

    public boolean isFirstConversion() {

        int conversionRawVal = logSp.getInt(FIRST_CONVERSION_STATUS_KEY, 0);
        return FirstConversionStatus.valueOf(conversionRawVal) == FirstConversionStatus.NA;
    }

    public void registerNewUser(String userId, String email, String visitorId) {

        logSp.edit().putInt(FIRST_CONVERSION_STATUS_KEY, FirstConversionStatus.PENDING.getRawValue()).apply();
        generateUserPushToken(userId, email, visitorId);
//        BackendInteractor backendInteractor = new BackendInteractor(context, "");
        // TODO: 7/6/2017 Register at the MBAAS
    }

    private UserPushToken generateUserPushToken(String token) {

        return null;
    }

    private UserPushToken generateUserPushToken(String userId, String email, String visitorId) {

        return null;
    }

    private class RegistrationResponder implements Response.Listener<JSONObject>, Response.ErrorListener {

        private String token;

        public RegistrationResponder(String token) {

            this.token = token;
        }

        @Override
        public void onResponse(JSONObject response) {

            SharedPreferences.Editor editor = logSp.edit().putBoolean(LAST_REG_OPERATION_SUCCESS_KEY, true);
            int conversionStatRaw = logSp.getInt(FIRST_CONVERSION_STATUS_KEY, 0);
            if (FirstConversionStatus.valueOf(conversionStatRaw) == FirstConversionStatus.PENDING)
                editor.putInt(FIRST_CONVERSION_STATUS_KEY, FirstConversionStatus.DONE.getRawValue());
            editor.apply();
        }

        @Override
        public void onErrorResponse(VolleyError error) {

            OptiLogger.w(this, "Failed destination register user set:\n%s", error.getMessage());
            logSp.edit().putBoolean(LAST_REG_OPERATION_SUCCESS_KEY, false)
                    .putString(LAST_REG_OPERATION_TOKEN_KEY, token).apply();
        }
    }

    interface RegistrarConstants {

        String OPERATIONS_LOG_SP = "operations_log";
        String LAST_REG_OPERATION_SUCCESS_KEY = "lastOperationSuccess";
        String LAST_REG_OPERATION_TOKEN_KEY = "lastOperationToken";
        String FIRST_CONVERSION_STATUS_KEY = "firstConversionStatus";

        String ANDROID_TOPIC = "Android";
        String OPTIPUSH_GENERAL_TOPIC = "OptipushGeneral";
    }
}
