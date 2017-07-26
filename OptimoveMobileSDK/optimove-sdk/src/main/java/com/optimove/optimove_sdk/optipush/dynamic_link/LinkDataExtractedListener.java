package com.optimove.optimove_sdk.optipush.dynamic_link;

import java.util.Map;

public interface LinkDataExtractedListener {

    void onDataExtracted(Map<String, String> data);
    void onErrorOccurred(LinkDataError error);
}
