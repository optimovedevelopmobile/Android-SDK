package com.optimove.sdk.optimovemobilesdk;

import android.app.Application;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptimoveConfigurationListener;
import com.optimove.sdk.optimove_sdk.main.TenantToken;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        final long start = System.currentTimeMillis();
        TenantToken tenantToken = new TenantToken(5, "586b34667efff2d96c1d4062e5652835", "1.0.0");
        Optimove.configure(this, tenantToken, new OptimoveConfigurationListener() {

            @Override
            public void onConfigurationSuccess() {
                long end = System.currentTimeMillis();
                OptiLogger.d("DONE_CONFIGURE", "Loading took %d millis", (end - start));
            }

            @Override
            public void onConfigurationError(Error... errors) {

                StringBuilder builder = new StringBuilder(errors.length);
                for (Error error : errors) {
                    builder.append(error.name());
                    if (error != errors[errors.length - 1])
                        builder.append(", ");
                }
                OptiLogger.w("CONFIGURATION_ERRORS", builder.toString());
            }
        });
    }
}
