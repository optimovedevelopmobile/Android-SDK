package com.optimove.sdk.optimove_sdk.optitrack;

import android.support.annotation.Nullable;

import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class OptimoveEventsValidator {

    private Map<String, EventConfig> eventsConfigs;

    public OptimoveEventsValidator() {

        eventsConfigs = new HashMap<>();
    }

    void loadEventsConfigs(JSONObject eventsData) throws JSONException {

        Iterator<String> eventsNamesIterator = eventsData.keys();
        while (eventsNamesIterator.hasNext()) {
            String eventName = eventsNamesIterator.next();
            JSONObject eventsDataJson = eventsData.getJSONObject(eventName);
            EventConfig eventConfig = EventConfig.fromJson(eventsDataJson);
            eventsConfigs.put(eventName, eventConfig);
        }
    }

    public EventSentResult lookForError(OptimoveEvent event) {

        EventConfig eventConfig = eventsConfigs.get(event.getName());
        Map<String, Object> parameters = event.getParameters();
        if (isUnknownEventName(eventConfig))
            return EventSentResult.UNKNOWN_EVENT_NAME_ERROR;
        if (isMissingMandatoryParameter(eventConfig, parameters))
            return EventSentResult.MISSING_MANDATORY_PARAMETER_ERROR;
        return lookInsideEventParameters(eventConfig, parameters);
    }

    public EventConfig getEventConfig(String name) {
        return eventsConfigs.get(name);
    }

    @Nullable
    private EventSentResult lookInsideEventParameters(EventConfig eventConfig, Map<String, Object> parameters) {

        for (String key : parameters.keySet()) {
            ParameterConfig parameterConfig = eventConfig.getParameterConfigs().get(key);
            if (isInvalidParameterName(parameterConfig))
                return EventSentResult.INVALID_PARAMETER_NAME_ERROR;
            Object value = parameters.get(key);
            if (isIncorrectParameterValueType(parameterConfig, value))
                return EventSentResult.INCORRECT_VALUE_TYPE_ERROR;
            if (isValueTooLarge(value))
                return EventSentResult.VALUE_TOO_LARGE_ERROR;
        }
        return null;
    }

    private boolean isUnknownEventName(EventConfig eventConfig) {
        return eventConfig == null;
    }

    private boolean isMissingMandatoryParameter(EventConfig eventConfig, Map<String, Object> parameters) {
        for (ParameterConfig parameterConfig : eventConfig.getParameterConfigs().values()) {
            if (!parameterConfig.isOptional && !parameters.containsKey(parameterConfig.name))
                return true;
        }
        return false;
    }

    private boolean isInvalidParameterName(ParameterConfig parameterConfig) {
        return parameterConfig == null;
    }

    @Nullable
    private boolean isIncorrectParameterValueType(ParameterConfig parameterConfig, Object value) {
        switch (parameterConfig.type) {
            case ParametersDefinitions.STRING_TYPE:
                return !(value instanceof String);
            case ParametersDefinitions.NUMBER_TYPE:
                return !(value instanceof Number);
            default:
                return true;
        }
    }

    private boolean isValueTooLarge(Object value) {
        return value.toString().length() > ParametersDefinitions.VALUE_MAX_LENGTH;
    }

    interface ParametersDefinitions {

        String STRING_TYPE = "String";
        String NUMBER_TYPE = "Number";
        int VALUE_MAX_LENGTH = 255;
    }
}
