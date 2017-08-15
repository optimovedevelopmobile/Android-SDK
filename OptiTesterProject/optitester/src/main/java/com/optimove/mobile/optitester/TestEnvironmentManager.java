package com.optimove.mobile.optitester;

import android.app.Application;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptimoveConfigurationListener;
import com.optimove.sdk.optimove_sdk.main.TenantToken;

public class TestEnvironmentManager {

    private SdkInitDataHolder sdkInitDataHolder;

    public TestEnvironmentManager(SdkInitDataHolder sdkInitDataHolder) {
        this.sdkInitDataHolder = sdkInitDataHolder;
    }

    void start() {

        Application application = sdkInitDataHolder.application;
        Optimove.configure(application, sdkInitDataHolder.tenantToken, sdkInitDataHolder.configurationListener);
    }

    public static class SdkInitDataHolder {

        private Application application;
        private TenantToken tenantToken;
        private OptimoveConfigurationListener configurationListener;

        public SdkInitDataHolder(Application application, TenantToken tenantToken, OptimoveConfigurationListener configurationListener) {
            this.application = application;
            this.tenantToken = tenantToken;
            this.configurationListener = configurationListener;
        }
    }
}
