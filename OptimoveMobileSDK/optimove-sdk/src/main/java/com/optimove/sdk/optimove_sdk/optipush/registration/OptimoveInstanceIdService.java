package com.optimove.sdk.optimove_sdk.optipush.registration;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.OptiPushManager;

import static com.optimove.sdk.optimove_sdk.optipush.registration.ClientRegistrar.Constants.*;

public class OptimoveInstanceIdService extends FirebaseInstanceIdService {

    private SharedPreferences registrationPreferences;

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
        OptiLogger.d("REFRESHED_TOKEN", token == null ? "null" : token);
        if (token == null)
            return;
        registrationPreferences = getSharedPreferences(REGISTRATION_PREFERENCES_NAME, MODE_PRIVATE);
        handleNewToken(token);
    }

    private void handleNewToken(String newToken) {

        String lastToken = registrationPreferences.getString(LAST_TOKEN, null);
        boolean shouldDoNothing = lastToken != null && lastToken.equals(newToken);
        if (!shouldDoNothing)
            tryPerformOperations(lastToken, newToken);
    }

    private void tryPerformOperations(String lastToken, String newToken) {

        OptiPushManager optiPushManager = Optimove.getInstance().getOptiPushManager();
        if (optiPushManager == null)
            postponeOperations(lastToken, newToken);
        else
            callOperations(lastToken, newToken);
    }

    private void postponeOperations(String lastToken, String newToken) {

        SharedPreferences.Editor editor = registrationPreferences.edit().putString(LAST_FAILED_REG_TOKEN_KEY, newToken);
        if (shouldUnregister(lastToken, newToken))
            editor.putString(LAST_FAILED_UNREG_TOKEN_KEY, lastToken);
        editor.apply();
    }

    @SuppressWarnings("ConstantConditions")
    private void callOperations(String lastToken, String newToken) {

        OptiPushManager optiPushManager = Optimove.getInstance().getOptiPushManager();
        ClientRegistrar clientRegistrar = optiPushManager.getClientRegistrar();
        if (shouldUnregister(lastToken, newToken))
            clientRegistrar.unregisterUserFromPush(lastToken);
        clientRegistrar.registerUserForPush(newToken);
    }

    private boolean shouldUnregister(String lastToken, String newToken) {
        return lastToken != null && !lastToken.equals(newToken);
    }

}
