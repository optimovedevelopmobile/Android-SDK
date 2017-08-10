package com.optimove.sdk.optimove_sdk.main;

public interface OptimoveConfigurationListener {

    void onConfigurationFinished();
    void googlePlayServicesFailed(int errorCode);
}
