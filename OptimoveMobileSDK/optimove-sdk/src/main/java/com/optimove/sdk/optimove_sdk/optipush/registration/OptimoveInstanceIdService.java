package com.optimove.sdk.optimove_sdk.optipush.registration;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

public class OptimoveInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null)
            OptiLogger.d("REFRESHED_TOKEN", token);
        OptiPushClientRegistrar clientRegistrar = new OptiPushClientRegistrar(this);
        registerByToken(clientRegistrar, token != null);
        updateTopicsSubscription(clientRegistrar, token != null);
    }

    private void registerByToken(OptiPushClientRegistrar clientRegistrar, boolean shouldRegister) {
        if (shouldRegister)
            clientRegistrar.registerUserForPush();
        else
            clientRegistrar.unregisterUserFromPush();
    }

    private void updateTopicsSubscription(OptiPushClientRegistrar clientRegistrar, boolean shouldSubscribe) {
        if (shouldSubscribe)
            clientRegistrar.registerToTopics();
        else
            clientRegistrar.unregisterFromTopics();
    }
}
