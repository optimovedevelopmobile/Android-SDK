package com.optimove.optimove_sdk.main.entities;

import java.util.Map;

public interface OptimoveEvent {

    String getName();

    Map<String, Object> getParameters();
}
