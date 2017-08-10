package com.optimove.sdk.optimove_sdk.tests;

import org.json.JSONObject;

public interface TestExecutor {

    void reportToServer(String id, boolean success, long timestamp);

    void startTest(JSONObject jsonObject);
}
