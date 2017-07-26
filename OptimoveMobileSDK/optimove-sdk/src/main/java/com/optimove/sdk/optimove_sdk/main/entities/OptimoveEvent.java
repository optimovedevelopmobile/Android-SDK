package com.optimove.sdk.optimove_sdk.main.entities;

import java.util.Map;

public interface OptimoveEvent {

    String getName();

    Map<String, Object> getParameters();
}
