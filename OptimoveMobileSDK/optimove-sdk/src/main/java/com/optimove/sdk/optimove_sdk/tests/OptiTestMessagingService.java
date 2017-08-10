package com.optimove.sdk.optimove_sdk.tests;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

import org.json.JSONException;
import org.json.JSONObject;

public class OptiTestMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);
        if (!remoteMessage.getData().containsKey("isTestMessage"))
            return;
        executeTest(remoteMessage);
    }

    private void executeTest(RemoteMessage remoteMessage) {

        String testData = remoteMessage.getData().get("testData");
        OptiTester optiTester = OptiTester.getInstance();
        try {
            optiTester.startTest(new JSONObject(testData));
        } catch (JSONException e) {
            OptiLogger.e(this, "failed to start test due to corrupted data with error:\n%s", e);
            // TODO: 8/10/2017 report failed to start test?
        }
    }

}
