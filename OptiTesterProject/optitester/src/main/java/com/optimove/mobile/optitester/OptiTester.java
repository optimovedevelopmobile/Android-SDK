package com.optimove.mobile.optitester;

import com.optimove.mobile.optitester.test_cases.OptimoveTestCaseStrategy;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class OptiTester {

    private static OptiTester instance;

    public static OptiTester current() {

        return instance;
    }

    public static OptiTesterFactory instantiate() {

        return new OptiTesterFactory(instance);
    }

    private TestEnvironmentManager testEnvironmentManager;
    private OptimoveClient client;
    private TestDispatcher testDispatcher;
    private JSONObject testPayload;

    OptiTester(TestEnvironmentManager testEnvironmentManager, TestDispatcher testDispatcher, OptimoveClient client) {

        this.testEnvironmentManager = testEnvironmentManager;
        this.testDispatcher = testDispatcher;
        this.client = client;
    }

    public void startTest(JSONObject jsonObject) {

        testPayload = jsonObject;
        testEnvironmentManager.start();
    }

    public void performTestAction() {

        try {
            String action = testPayload.getString("action");
            Map<String, Object> mappedTestData = new HashMap<>();
            JSONObject testData = testPayload.getJSONObject("testData");
            Iterator<String> testDataKeys = testData.keys();
            while (testDataKeys.hasNext()) {
                String key = testDataKeys.next();
                mappedTestData.put(key, testData.get(key));
            }
            OptimoveTestCaseStrategy.test(action, mappedTestData);
        } catch (JSONException e) {
            OptiLogger.e(this, e);
        }
    }

    public OptimoveClient getClient() {
        return client;
    }

    public TestDispatcher getTestDispatcher() {
        return testDispatcher;
    }
}
