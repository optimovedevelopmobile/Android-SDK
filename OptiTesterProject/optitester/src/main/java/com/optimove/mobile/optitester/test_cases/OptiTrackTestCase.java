package com.optimove.mobile.optitester.test_cases;

import com.optimove.mobile.optitester.OptiTester;
import com.optimove.mobile.optitester.OptimoveClient;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.optitrack.EventSentResult;
import com.optimove.sdk.optimove_sdk.optitrack.OptimoveEventSentListener;

import java.util.Map;

public class OptiTrackTestCase extends OptimoveTestCase {

    public OptiTrackTestCase(Map<String, Object> testData) {
        super(testData);
    }

    public TestCaseCommand testSetNewUserId() {

        final OptimoveEventSentListener listener = new OptimoveEventSentListener() {
            @Override
            public void onResponse(EventSentResult result) {
                reportTestStep("uid-1", result == EventSentResult.SUCCESS);
            }
        };
        return new TestCaseCommand() {
            @Override
            public void execute() {
                OptimoveClient client = OptiTester.current().getClient();
                String userId = (String) testData.get("userId");
                String email = (String) testData.get("email");
                client.setUserId(userId, email);
                Optimove.getInstance().logEvent(new TestEvent(), listener);
            }
        };
    }
}
