package com.optimove.sdk.optimove_sdk.optipush.registration;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.TenantToken;
import com.optimove.sdk.optimove_sdk.main.tools.BackendInteractor;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
        String token = logSp.getString(LAST_REG_OPERATION_TOKEN_KEY, null);
        if (token == null)
            unregisterUserFromPush();
        else
            new UserRegistrationCommand(token).execute();
    }

    public void unregisterUserFromPush() {

        new UserRegistrationCommand(null).execute();
    }

    public void registerUserForPush() {

        final Optimove optimove = Optimove.getInstance();
        if (optimove.clientHasFirebase()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String token = FirebaseInstanceId.getInstance().getToken(optimove.getSdkFaSenderId(), "FCM");
                        new UserRegistrationCommand(token).execute();
                    } catch (IOException e) {
                        OptiLogger.e(this, e);
                    }
                }
            }).start();
        } else {
            String token = FirebaseInstanceId.getInstance().getToken();
            new UserRegistrationCommand(token).execute();
        }
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

    public void registerNewUser() {

        logSp.edit().putInt(FIRST_CONVERSION_STATUS_KEY, FirstConversionStatus.PENDING.getRawValue()).apply();
        registerUserForPush();
    }

    private class RegistrationResponder implements Response.Listener<JSONObject>, Response.ErrorListener {

        private String token;

        public RegistrationResponder(String token) {

            this.token = token;
        }

        @Override
        public void onResponse(JSONObject response) {

            OptiLogger.d(this, response.toString());
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

    private class UserRegistrationCommand {

        private String token;

        public UserRegistrationCommand(String token) {
            this.token = token;
        }

        public void execute() {

            UserPushToken userPushToken = generateUserPushToken(token);
            RegistrationResponder responder = new RegistrationResponder(token);
            try {
                new BackendInteractor(context).post(userPushToken.toJson())
                .successListener(responder).errorListener(responder).destination("registration")
                .send();
            } catch (JSONException e) {
                OptiLogger.e(this, e);
            }
        }

        private UserPushToken generateUserPushToken(String token) {

            Optimove optimove = Optimove.getInstance();
            TenantToken tenantToken = optimove.getTenantToken();
            return new UserPushToken.Builder()
                    .setTenantId(tenantToken.getTenantId())
                    .setUserInfo(optimove.getUserInfo())
                    .setToken(token)
                    .build();
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
