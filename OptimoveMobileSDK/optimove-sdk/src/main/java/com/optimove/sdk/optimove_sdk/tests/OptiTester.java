package com.optimove.sdk.optimove_sdk.tests;

import org.json.JSONObject;

public final class OptiTester {

    private static OptiTester instance;

    public static OptiTester getInstance() {

        if (instance == null) {
            synchronized (OptiTester.class) {
                if (instance == null)
                    instance = new OptiTester();
            }
        }
        return instance;
    }

    private TestExecutor testExecutor;

    private OptiTester() {

        testExecutor = new StubTestExecutor();
    }

    public void logTestEvent(String id, boolean success, long timestamp) {

        testExecutor.reportToServer(id, success, timestamp);
    }

    public void startTest(JSONObject jsonObject) {

        testExecutor.startTest(jsonObject);
    }

    private class StubTestExecutor implements TestExecutor {

        @Override
        public void reportToServer(String id, boolean success, long timestamp) {

        }

        @Override
        public void startTest(JSONObject jsonObject) {

        }
    }
}
