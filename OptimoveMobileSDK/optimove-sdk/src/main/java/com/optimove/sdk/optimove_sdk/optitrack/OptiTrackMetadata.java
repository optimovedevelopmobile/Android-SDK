package com.optimove.sdk.optimove_sdk.optitrack;

import org.json.JSONException;
import org.json.JSONObject;

public class OptiTrackMetadata {

    boolean sendUserAgentHeader;
    boolean enableHeartBeatTimer;
    int heartBeatTimer;
    int eventIdCustomDimensionId;
    int eventNameCustomDimensionId;
    String eventCategoryName;
    int visitCustomDimensionsStartId;
    int maxVisitCustomDimensions;
    int actionCustomDimensionsStartId;
    int maxActionCustomDimensions;

    public OptiTrackMetadata() {

        sendUserAgentHeader = false;
        enableHeartBeatTimer = false;
        heartBeatTimer = -1;
        eventIdCustomDimensionId = -1;
        eventNameCustomDimensionId = -1;
        eventCategoryName = null;
        visitCustomDimensionsStartId = -1;
        maxVisitCustomDimensions = -1;
        actionCustomDimensionsStartId = -1;
        maxActionCustomDimensions = -1;
    }

    static OptiTrackMetadata fromJson(JSONObject jsonObject) throws JSONException {

        OptiTrackMetadata metadata = new OptiTrackMetadata();
        metadata.sendUserAgentHeader = jsonObject.getBoolean("sendUserAgentHeader");
        metadata.enableHeartBeatTimer = jsonObject.getBoolean("enableHeartBeatTimer");
        metadata.heartBeatTimer = jsonObject.getInt("heartBeatTimer");
        metadata.eventIdCustomDimensionId = jsonObject.getInt("eventIdCustomDimensionId");
        metadata.eventNameCustomDimensionId = jsonObject.getInt("eventNameCustomDimensionId");
        metadata.eventCategoryName = jsonObject.getString("eventCategoryName");
        metadata.visitCustomDimensionsStartId = jsonObject.getInt("visitCustomDimensionsStartId");
        metadata.maxVisitCustomDimensions = jsonObject.getInt("maxVisitCustomDimensions");
        metadata.actionCustomDimensionsStartId = jsonObject.getInt("actionCustomDimensionsStartId");
        metadata.maxActionCustomDimensions = jsonObject.getInt("maxActionCustomDimensions");
        return metadata;
    }
}
