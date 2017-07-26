package com.optimove.optimove_sdk.optitrack;

import org.json.JSONException;
import org.json.JSONObject;
import org.piwik.sdk.TrackerConfig;

public class OptiTrackKeys {

    private String siteUrl;
    private int siteId;

    public OptiTrackKeys(String siteUrl, int siteId) {
        this.siteUrl = siteUrl;
        this.siteId = siteId;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public int getSiteId() {
        return siteId;
    }

    TrackerConfig toTrackerConfig() {

        return TrackerConfig.createDefault(siteUrl, siteId);
    }

    static OptiTrackKeys fromJson(JSONObject initData) throws JSONException {

        String siteUrl = initData.getString(OptitrackConstants.InitDataKeys.OPTITRACK_ENDPOINT);
        int siteId = initData.getInt(OptitrackConstants.InitDataKeys.SITE_ID);
        return new OptiTrackKeys(siteUrl, siteId);
    }
}
