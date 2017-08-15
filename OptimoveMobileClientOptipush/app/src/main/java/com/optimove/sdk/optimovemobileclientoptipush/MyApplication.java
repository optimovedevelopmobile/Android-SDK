package com.optimove.sdk.optimovemobileclientoptipush;

import android.app.Application;

import com.google.firebase.iid.FirebaseInstanceId;
import com.optimove.sdk.optimove_sdk.main.OptimoveConfigurationListener;
import com.optimove.sdk.optimove_sdk.main.TenantToken;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TenantToken initToken = new TenantToken(4, "de9e1f28cce9e92106d237d437b69f05", "1.0.0");
        final long start = System.currentTimeMillis();
        Optimove.configure(this, initToken, new OptimoveConfigurationListener() {

            @Override
            public void onConfigurationFinished() {
                long end = System.currentTimeMillis();
                OptiLogger.d("DONE_CONFIGURE", "Loading took %d millis", (end - start));
                String token = FirebaseInstanceId.getInstance().getToken();
                if (token != null)
                    OptiLogger.d("TOKEN_TOKEN", token);
            }
        });
    }
}
