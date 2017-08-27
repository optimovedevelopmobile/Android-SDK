package com.optimove.sdk.optimove_sdk.main;

public interface OptimoveConfigurationListener {

    void onConfigurationSuccess();
    void onConfigurationError(Error... errors);

    enum Error {
        ERROR, DISCONNECTED_FROM_NETWORK, GOOGLE_PLAY_SERVICES_MISSING, NOTIFICATIONS_NOT_GRANTED_BY_USER
    }
}
