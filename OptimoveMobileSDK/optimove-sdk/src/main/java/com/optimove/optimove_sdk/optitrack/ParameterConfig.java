package com.optimove.optimove_sdk.optitrack;

import org.json.JSONException;
import org.json.JSONObject;

import static com.optimove.optimove_sdk.optitrack.ParameterConfig.ParameterConfigJsonKeys.*;

public class ParameterConfig {

    int id;
    String name;
    String type;
    int dimensionId;
    boolean isOptional;

    public ParameterConfig(int id, String name, String type, int dimensionId, boolean isOptional) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.dimensionId = dimensionId;
        this.isOptional = isOptional;
    }

    public static ParameterConfig fromJson(JSONObject parameterConfigJson) throws JSONException {

        int id = parameterConfigJson.getInt(ID);
        String name = parameterConfigJson.getString(NAME);
        String type = parameterConfigJson.getString(TYPE);
        int optiTrackDimensionId = parameterConfigJson.getInt(OPTI_TRACK_DIMENSION_ID);
        boolean optional = parameterConfigJson.getBoolean(OPTIONAL);
        return new ParameterConfig(id, name, type, optiTrackDimensionId, optional);
    }

    interface ParameterConfigJsonKeys {

        String ID = "id";
        String NAME = "name";
        String TYPE = "type";
        String OPTI_TRACK_DIMENSION_ID = "optiTrackDimensionId";
        String OPTIONAL = "optional";
    }
}
