package com.optimove.sdk.optimove_sdk.optipush.registration;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.OptiPushManager;

import org.json.JSONException;
import org.json.JSONObject;

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

        SharedPreferences.Editor editor = registrationPreferences.edit();
        boolean shouldUnregister = shouldUnregister(lastToken, newToken);
        Optimove optimove = Optimove.getInstance();
        UserPushToken.Builder userPushTokenBuilder = new UserPushToken.Builder()
                .setIsRegistration(!shouldUnregister)
                .setUserInfo(optimove.getUserInfo())
                .setTenantId(optimove.getTenantToken().getTenantId());

        storeFailedOperationDataAndFlags(editor, newToken, userPushTokenBuilder.setIsRegistration(true).build(), true);
        if (shouldUnregister)
            storeFailedOperationDataAndFlags(editor, lastToken, userPushTokenBuilder.setIsRegistration(false).build(), false);
        editor.apply();
    }

    private void storeFailedOperationDataAndFlags(SharedPreferences.Editor editor, String token, UserPushToken userPushToken, boolean isRegister) {

        editor.putString(isRegister ? LAST_FAILED_REG_TOKEN_KEY : LAST_FAILED_UNREG_TOKEN_KEY, token);
        JSONObject data;
        try {
            data = userPushToken.toJson();
        } catch (JSONException e) {
            OptiLogger.e(this, e);
            return;
        }
        Context context = Optimove.getInstance().getContext();
        FileUtils.write(context, data)
                .to(isRegister ? LAST_FAILED_REG_DATA_FILE_NAME : LAST_FAILED_UNREG_DATA_FILE_NAME)
                .in(FileUtils.SourceDir.INTERNAL)
                .now();
    }

    private boolean shouldUnregister(String lastToken, String newToken) {
        return lastToken != null && !lastToken.equals(newToken);
    }

    @SuppressWarnings("ConstantConditions")
    private void callOperations(String lastToken, String newToken) {

        OptiPushManager optiPushManager = Optimove.getInstance().getOptiPushManager();
        ClientRegistrar clientRegistrar = optiPushManager.getClientRegistrar();
        if (shouldUnregister(lastToken, newToken))
            clientRegistrar.unregisterUserFromPush(lastToken);
        clientRegistrar.registerUserForPush(newToken);
    }

}
