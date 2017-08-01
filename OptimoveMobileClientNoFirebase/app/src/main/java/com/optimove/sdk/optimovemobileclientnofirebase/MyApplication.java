package com.optimove.sdk.optimovemobileclientnofirebase;

import android.app.Application;

import com.optimove.sdk.optimove_sdk.main.OptimoveConfigurationListener;
import com.optimove.sdk.optimove_sdk.main.InitToken;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        InitToken initToken = new InitToken("8ec0b468286ccccdfaf75eb656c573a2", "1.0.0");
        final long start = System.currentTimeMillis();
        Optimove.configure(this, initToken, new OptimoveConfigurationListener() {

            @Override
            public void onConfigurationFinished() {
                long end = System.currentTimeMillis();
                OptiLogger.d("DONE_CONFIGURE", "Loading took %d millis", (end - start));
            }
        });
    }
}
