package com.optimove.mobile.optitester;

import android.content.Context;

import com.optimove.mobile.optitester.TestDispatcher;
import com.optimove.sdk.optimove_sdk.main.Optimove;

public class RtdbTestDispatcher implements TestDispatcher {

    @Override
    public void reportToServer(String id, boolean success, long timestamp) {

        Context context = Optimove.getInstance().getContext();
    }
}
