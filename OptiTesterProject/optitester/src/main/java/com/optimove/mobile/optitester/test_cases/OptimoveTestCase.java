package com.optimove.mobile.optitester.test_cases;

import com.optimove.mobile.optitester.OptiTester;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class OptimoveTestCase {

    protected Map<String, Object> testData;

    public OptimoveTestCase(Map<String, Object> testData) {
        this.testData = testData;
    }

    protected void reportTestStep(String id, boolean success) {

        OptiTester.current().getTestDispatcher().reportToServer(id, success, System.currentTimeMillis());
    }

    protected class TestEvent implements OptimoveEvent {

        @Override
        public String getName() {
            return "Event1";
        }

        @Override
        public Map<String, Object> getParameters() {

            Map<String, Object> params = new HashMap<>(3);
            params.put("action_name", "Banana");
            params.put("action_value", 100);
            params.put("action_price", 200);
            return params;
        }
    }
}
