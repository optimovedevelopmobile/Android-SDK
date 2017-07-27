package com.optimove.sdk.optimovemobileclientoptitrack;

import android.app.Application;

import com.optimove.sdk.optimove_sdk.main.ConfigurationFinishedListener;
import com.optimove.sdk.optimove_sdk.main.InitToken;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        InitToken initToken = new InitToken("c0458fb79ea03ef7be4589549a969ede", "1.0.0");
        final long start = System.currentTimeMillis();
        Optimove.configure(this, initToken, new ConfigurationFinishedListener() {

            @Override
            public void onConfigurationFinished() {
                long end = System.currentTimeMillis();
                OptiLogger.d("DONE_CONFIGURE", "Loading took %d millis", (end - start));
            }
        });
    }
}
