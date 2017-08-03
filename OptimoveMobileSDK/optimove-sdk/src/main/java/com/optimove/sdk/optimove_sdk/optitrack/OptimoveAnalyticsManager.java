package com.optimove.sdk.optimove_sdk.optitrack;

import android.content.Context;
import android.support.annotation.NonNull;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptimoveComponentSetupListener;
import com.optimove.sdk.optimove_sdk.main.OptimoveComponentType;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptiPushClientRegistrar;

import org.json.JSONException;
import org.json.JSONObject;
import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;
import org.piwik.sdk.extra.TrackHelper;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.*;

public class OptimoveAnalyticsManager {

    private Tracker mainTracker;
    private OptimoveEventsValidator eventsValidator;
    private OptiTrackMetadata optiTrackMetadata;

    public OptimoveAnalyticsManager() {

        eventsValidator = new OptimoveEventsValidator();
        optiTrackMetadata = new OptiTrackMetadata();
    }

    public void setupFromLocalStorage() {

        Context context = Optimove.getInstance().getContext();
        JSONObject initData = FileUtils.readJsonFromFile(context, INIT_DATA_FILE_NAME);
        boolean initialized = mainTracker != null;
        if (initData != null && !initialized)
            executeInit(initData);
    }

    public void setup(JSONObject initData, @NonNull OptimoveComponentSetupListener setupListener) {

        backupInitData(initData);
        boolean initialized = mainTracker != null;
        if (!initialized)
            initialized = executeInit(initData);
        setupListener.onSetupFinished(OptimoveComponentType.OPTITRACK, initialized);

    }

    public void userIdWasUpdated(String userId) {

        mainTracker.setUserId(userId);
    }

    public void dispatchAllEvents() {

        mainTracker.dispatch();
    }

    public void logEvent(OptimoveEvent event, OptimoveEventSentListener listener) {

        if (!eventsValidator.getEventConfig(event.getName()).isSupportedOnOptitrack())
            return;
        EventSentResult error = eventsValidator.lookForError(event);
        if (error == null) {
            sendEvent(event);
            listener.onResponse(EventSentResult.SUCCESS);
        } else {
            listener.onResponse(error);
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
        FileUtils.writeJsonToFile(context, INIT_DATA_FILE_NAME, data);
    }

    private boolean executeInit(JSONObject initData) {

        try {
            JSONObject optitrackMetadataJson = initData.getJSONObject(InitDataKeys.OPTITRACK_META_DATA);
            optiTrackMetadata = OptiTrackMetadata.fromJson(optitrackMetadataJson);
            setupMainTracker(optitrackMetadataJson);
            eventsValidator.loadEventsConfigs(initData.getJSONObject(InitDataKeys.EVENTS));
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
        mainTracker.setDispatchInterval(100);
    }

    private void setupTrackerUserIds() {

        UserInfo userInfo = Optimove.getInstance().getUserInfo();
        if (userInfo.getVisitorId() == null)
            userInfo.setVisitorId(mainTracker.getVisitorId());
        else
            mainTracker.setVisitorId(userInfo.getVisitorId());
        mainTracker.setUserId(userInfo.getUserId());
    }

    interface ParametersDefinitions {
        String STRING_TYPE = "String";
        String NUMBER_TYPE = "Number";
        int VALUE_MAX_LENGTH = 255;
    }
}
