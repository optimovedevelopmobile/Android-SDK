package com.optimove.mobile.optitester;

import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;

public class OptimoveClient {

    public void setUserId(String userId, @Nullable String email) {

        if (email == null)
            Optimove.getInstance().updateUserSignIn(userId);
        else
            Optimove.getInstance().updateUserSignIn(userId, email);
    }

    public void reportEvent(OptimoveEvent event) {

    }
}
