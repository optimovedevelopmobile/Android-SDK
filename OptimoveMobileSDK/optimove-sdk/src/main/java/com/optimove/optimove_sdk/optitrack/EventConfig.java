package com.optimove.optimove_sdk.optitrack;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.optimove.optimove_sdk.optitrack.EventConfig.EventConfigJsonKeys.*;

public class EventConfig {

    private int id;
    private boolean supportedOnOptitrack;
    private boolean supportedOnRealTime;
    private Map<String, ParameterConfig> parameterConfigs;

    public EventConfig(int id, boolean supportedOnOptitrack, boolean supportedOnRealTime, Map<String, ParameterConfig> parameterConfigs) {

        this.id = id;
        this.supportedOnOptitrack = supportedOnOptitrack;
        this.supportedOnRealTime = supportedOnRealTime;
        this.parameterConfigs = parameterConfigs;
    }

    static EventConfig fromJson(JSONObject eventConfigJson) throws JSONException {

        int eventId = eventConfigJson.getInt(ID);
        boolean supportedOnOptitrack = eventConfigJson.getBoolean(SUPPORTED_ON_OPTITRACK);
        boolean supportedOnRealTime = eventConfigJson.getBoolean(SUPPORTED_ON_REAL_TIME);
        Map<String, ParameterConfig> parameterConfigs = extractParameterConfigs(eventConfigJson);
        return new EventConfig(eventId, supportedOnOptitrack, supportedOnRealTime, parameterConfigs);
    }

    public int getId() {
        return id;
    }

    public boolean isSupportedOnOptitrack() {
        return supportedOnOptitrack;
    }

    public boolean isSupportedOnRealTime() {
        return supportedOnRealTime;
    }

    public Map<String, ParameterConfig> getParameterConfigs() {
        return parameterConfigs;
    }

    @NonNull
    private static Map<String, ParameterConfig> extractParameterConfigs(JSONObject eventConfigJson) throws JSONException {

        JSONObject parametersJson = eventConfigJson.getJSONObject(PARAMETERS);
        Map<String, ParameterConfig> parameterConfigs = new HashMap<>(parametersJson.length());
        Iterator<String> paramsNamesIterator = parametersJson.keys();
        while (paramsNamesIterator.hasNext()) {
            String paramName = paramsNamesIterator.next();
            JSONObject paramConfigJson = parametersJson.getJSONObject(paramName);
            ParameterConfig parameterConfig = ParameterConfig.fromJson(paramConfigJson);
            parameterConfigs.put(paramName, parameterConfig);
        }
        return parameterConfigs;
    }

    interface EventConfigJsonKeys {

        String ID = "id";
        String SUPPORTED_ON_OPTITRACK = "supportedOnOptitrack";
        String SUPPORTED_ON_REAL_TIME = "supportedOnRealTime";
        String PARAMETERS = "parameters";
    }
}
