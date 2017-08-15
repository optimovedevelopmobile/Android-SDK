package com.optimove.mobile.optitester;

import android.app.Application;

import com.optimove.sdk.optimove_sdk.main.OptimoveConfigurationListener;
import com.optimove.sdk.optimove_sdk.main.TenantToken;

public class OptiTesterFactory {

    private OptiTester optiTester;

    private Application application;
    private TenantToken tenantToken;
    private OptimoveConfigurationListener configurationListener;
    private OptimoveClient optimoveClient;

    OptiTesterFactory(OptiTester instance) {
        optiTester = instance;
    }

    public OptiTesterFactory setApplication(Application application) {

        this.application = application;
        return this;
    }

    public OptiTesterFactory setTenantToken(TenantToken tenantToken) {

        this.tenantToken = tenantToken;
        return this;
    }

    public OptiTesterFactory setConfigurationListener(OptimoveConfigurationListener configurationListener) {

        this.configurationListener = configurationListener;
        return this;
    }

    public OptiTesterFactory setOptimoveClient(OptimoveClient optimoveClient) {

        this.optimoveClient = optimoveClient;
        return this;
    }

    public void build() {

        TestEnvironmentManager.SdkInitDataHolder initDataHolder = new TestEnvironmentManager.SdkInitDataHolder(application, tenantToken, configurationListener);
        TestEnvironmentManager testEnvironmentManager = new TestEnvironmentManager(initDataHolder);
        TestDispatcher testDispatcher = new RtdbTestDispatcher();
        optiTester = new OptiTester(testEnvironmentManager, testDispatcher, optimoveClient);
    }

    private class TesterConfigListener implements OptimoveConfigurationListener {

        @Override
        public void onConfigurationFinished() {

            configurationListener.onConfigurationFinished();
            OptiTester.current().performTestAction();
        }
    }
}
