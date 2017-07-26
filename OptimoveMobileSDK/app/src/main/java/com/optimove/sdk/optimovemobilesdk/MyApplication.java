package com.optimove.sdk.optimovemobilesdk;

import android.app.Application;

import com.optimove.optimove_sdk.main.InitToken;
import com.optimove.optimove_sdk.main.Optimove;

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        InitToken initToken = new InitToken("", "");
        Optimove.configure(this, initToken, null);
    }
}
