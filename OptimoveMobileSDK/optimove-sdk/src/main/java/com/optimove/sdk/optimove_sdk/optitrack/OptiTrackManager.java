package com.optimove.sdk.optimove_sdk.optitrack;

import android.content.Context;
import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptimoveComponentSetupListener;
import com.optimove.sdk.optimove_sdk.main.OptimoveComponentType;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

import org.json.JSONException;
import org.json.JSONObject;
import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;
import org.piwik.sdk.extra.TrackHelper;

import static com.optimove.sdk.optimove_sdk.optitrack.OptiTrackManager.Constants.INIT_DATA_FILE_NAME;

public class OptiTrackManager {

    private Tracker mainTracker;
    private OptimoveEventsValidator eventsValidator;
    private OptiTrackMetadata optiTrackMetadata;

    private OptiTrackManager() {

        eventsValidator = new OptimoveEventsValidator();
        optiTrackMetadata = new OptiTrackMetadata();
    }

    public static OptiTrackManager newInstanceFromLocalStorage(@Nullable OptimoveComponentSetupListener setupListener) {

        Context context = Optimove.getInstance().getContext();
        JSONObject initData = FileUtils.readFile(context, INIT_DATA_FILE_NAME).from(FileUtils.SourceDir.INTERNAL).asJson();
        if (initData == null) {
            OptiLogger.w(OptiTrackManager.class, "Attempt to initialize from local data failed");
            return null;
        }
        return newInstance(initData, setupListener);
    }

    public static OptiTrackManager newInstance(JSONObject initData, @Nullable OptimoveComponentSetupListener setupListener) {

        OptiTrackManager optiTrackManager = new OptiTrackManager();
        optiTrackManager.backupInitData(initData);
        boolean success = optiTrackManager.executeInit(initData);
        if (setupListener != null)
            setupListener.onSetupFinished(OptimoveComponentType.OPTITRACK, success);
        return optiTrackManager;
    }

    public void userIdWasUpdated(String userId) {

        mainTracker.setUserId(userId);
    }

    public void dispatchAllEvents() {

        mainTracker.dispatch();
    }

    public void reportEvent(OptimoveEvent event, @Nullable OptimoveEventSentListener listener) {

        if (!eventsValidator.getEventConfig(event.getName()).isSupportedOnOptitrack())
            return;
        EventSentResult error = eventsValidator.lookForError(event);
        if (error == null) {
            sendEvent(event);
            if (listener != null) listener.onResponse(EventSentResult.SUCCESS);
        } else {
            if (listener != null) listener.onResponse(error);
        }
    }

    private void sendEvent(OptimoveEvent event) {

        EventConfig eventConfig = eventsValidator.getEventConfig(event.getName());
        TrackHelper.Dimension trackHelper = TrackHelper.track()
                .dimension(optiTrackMetadata.eventIdCustomDimensionId, String.valueOf(eventConfig.getId()))
                .dimension(optiTrackMetadata.eventNameCustomDimensionId, event.getName());
        for (String paramName : event.getParameters().keySet()) {
            ParameterConfig parameterConfig = eventConfig.getParameterConfigs().get(paramName);
            Object paramValue = event.getParameters().get(paramName);
            trackHelper.dimension(parameterConfig.dimensionId, paramValue.toString());
        }
        trackHelper.event(optiTrackMetadata.eventCategoryName, event.getName()).with(mainTracker);
    }

    private void backupInitData(JSONObject data) {

        Context context = Optimove.getInstance().getContext();
        FileUtils.write(context, data).to(FileUtils.SourceDir.INTERNAL).named(INIT_DATA_FILE_NAME).now();
    }

    private boolean executeInit(JSONObject initData) {

        try {
            JSONObject optitrackMetadataJson = initData.getJSONObject(Optimove.InitDataJsonKeys.OPTITRACK_META_DATA);
            optiTrackMetadata = OptiTrackMetadata.fromJson(optitrackMetadataJson);
            setupMainTracker(optitrackMetadataJson);
            eventsValidator.loadEventsConfigs(initData.getJSONObject(Optimove.InitDataJsonKeys.EVENTS));
        } catch (JSONException e) {
            OptiLogger.e(this, e);
            return false;
        }
        setupTrackerUserIds();
        return true;
    }

    private void setupMainTracker(JSONObject optitrackMetadata) throws JSONException {

        OptiTrackKeys optiTrackKeys = OptiTrackKeys.fromJson(optitrackMetadata);
        Piwik piwik = Piwik.getInstance(Optimove.getInstance().getContext());
        mainTracker = piwik.newTracker(optiTrackKeys.toTrackerConfig());
    }

    private void setupTrackerUserIds() {

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        if (userInfo.getVisitorId() == null)
            userInfo.setVisitorId(mainTracker.getVisitorId());
        else
            mainTracker.setVisitorId(userInfo.getVisitorId());
        mainTracker.setUserId(userInfo.getUserId());
    }

    interface Constants {

        String INIT_DATA_FILE_NAME = "initData.json";
        String SITE_ID = "siteId";
        String OPTITRACK_ENDPOINT = "optitrackEndpoint";
    }
}
