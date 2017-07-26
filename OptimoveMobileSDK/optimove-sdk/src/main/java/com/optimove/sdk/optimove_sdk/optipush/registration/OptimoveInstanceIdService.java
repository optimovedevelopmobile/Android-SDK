package com.optimove.sdk.optimove_sdk.optipush.registration;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

public class OptimoveInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance(Optimove.getInstance().getSdkFa()).getToken();
        OptiLogger.d("NOTICE_ME_SENPAI", token);
        OptiPushClientRegistrar clientRegistrar = new OptiPushClientRegistrar(this);
        registerByToken(clientRegistrar, token);
        updateTopicsSubscription(clientRegistrar, token != null);
    }

    private void registerByToken(OptiPushClientRegistrar clientRegistrar, String token) {
        if (token == null)
            clientRegistrar.unregisterUserFromPush();
        else
            clientRegistrar.registerUserForPush(token);
    }

    private void updateTopicsSubscription(OptiPushClientRegistrar clientRegistrar, boolean shouldSubscribe) {
        if (shouldSubscribe)
            clientRegistrar.registerToTopics();
        else
            clientRegistrar.unregisterFromTopics();
    }
}
