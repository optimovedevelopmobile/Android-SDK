package com.optimove.sdk.optimove_sdk.optipush;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptimoveComponentSetupListener;
import com.optimove.sdk.optimove_sdk.main.OptimoveComponentType;
import com.optimove.sdk.optimove_sdk.main.OptimoveConfigurationListener;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor;
import com.optimove.sdk.optimove_sdk.optipush.registration.ClientRegistrar;

import org.json.JSONException;
import org.json.JSONObject;

public class OptiPushManager {

    private OptimoveFirebaseInteractor firebaseInteractor;
    private ClientRegistrar clientRegistrar;

    private OptiPushManager(String registrationEndPoint, boolean shouldRegisterClientToken, boolean hasClientFa) {

        this.firebaseInteractor = new OptimoveFirebaseInteractor(hasClientFa);
        this.clientRegistrar = new ClientRegistrar(registrationEndPoint, shouldRegisterClientToken);
    }

    public static OptiPushManager newInstance(JSONObject initData, boolean hasClientFa, OptimoveComponentSetupListener setupListener) {

        if (!validateGooglePlayServicesAvailable(setupListener))
            return null;
        OptiPushManager optiPushManager = createOptiPushManager(initData, hasClientFa, setupListener);
        if (optiPushManager != null) {
            setupListener.onSetupFinished(OptimoveComponentType.OPTIPUSH, true);
            optiPushManager.clientRegistrar.completeLastOperationIfFailed();
        }
        return optiPushManager;
    }

    private static boolean validateGooglePlayServicesAvailable(OptimoveComponentSetupListener setupListener) {

        Context context = Optimove.getInstance().getContext();
        final int servicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (servicesAvailable == ConnectionResult.SUCCESS)
            return true;
        setupListener.onSetupFinished(OptimoveComponentType.OPTIPUSH, false, OptimoveConfigurationListener.Error.GOOGLE_PLAY_SERVICES_MISSING);
        return false;
    }

    @Nullable
    private static OptiPushManager createOptiPushManager(JSONObject initData, boolean hasClientFa, OptimoveComponentSetupListener setupListener) {

        OptiPushManager optiPushManager = null;
        try {
            String optipushEndpoint = initData.getString(Constants.OPTIPUSH_ENDPOINT);
            boolean shouldRegisterClientToken = initData.getBoolean(Constants.SHOULD_REGISTER_CLIENT_TOKEN);
            optiPushManager = new OptiPushManager(optipushEndpoint, shouldRegisterClientToken, hasClientFa);
            JSONObject appControllerKeys = initData.getJSONObject(Constants.FIREBASE_PROJECT_KEYS);
            optiPushManager.firebaseInteractor.setup(appControllerKeys);
        } catch (JSONException e) {
            OptiLogger.e(OptiPushManager.class, e);
            setupListener.onSetupFinished(OptimoveComponentType.OPTIPUSH, false, OptimoveConfigurationListener.Error.ERROR);
        }
        return optiPushManager;
    }

    public ClientRegistrar getClientRegistrar() {
        return clientRegistrar;
    }

    public OptimoveFirebaseInteractor getFirebaseInteractor() {
        return firebaseInteractor;
    }

    public void registerNewUserIfNeeded() {

        if (clientRegistrar.wasUserAlreadyRegistered()) {
            clientRegistrar.registerNewUser();
        }
    }

    interface Constants {

        String FIREBASE_PROJECT_KEYS = "firebaseProjectKeys";
        String OPTIPUSH_ENDPOINT = "optipushEndpoint";
        String SHOULD_REGISTER_CLIENT_TOKEN = "shouldRegisterClientToken";
    }
}