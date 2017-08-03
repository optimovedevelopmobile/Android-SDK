package com.optimove.sdk.optimovemobilesdk;

import android.app.Application;

import com.optimove.sdk.optimove_sdk.main.TenantToken;
import com.optimove.sdk.optimove_sdk.main.Optimove;

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        TenantToken tenantToken = new TenantToken(1, "", "");
        Optimove.configure(this, tenantToken, null);
    }
}
