package com.optimove.mobile.optitester;

public interface TestDispatcher {

    void reportToServer(String id, boolean success, long timestamp);
}
