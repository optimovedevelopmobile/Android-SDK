package com.optimove.mobile.optitester.test_cases;

import java.util.Map;

public class OptimoveTestCaseStrategy {

    public static void test(String action, Map<String, Object> testData) {

        switch (action) {
            case TestActions.SET_USER_ID:
                new OptiTrackTestCase(testData).testSetNewUserId().execute();
        }
    }

    private interface TestActions {

        String SET_USER_ID = "setUserId";
    }
}
