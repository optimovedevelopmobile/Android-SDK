package com.optimove.sdk.optimove_sdk.optitrack;


public interface OptitrackConstants {

    String INIT_DATA_FILE_NAME = "initData.json";

    interface InitDataKeys {

        String EVENTS = "events";
        String OPTITRACK_META_DATA = "optitrackMetaData";
        String SITE_ID = "siteId";
        String OPTITRACK_ENDPOINT = "optitrackEndpoint";
        String EVENT_ID_CUSTOM_DIMENSION_ID = "eventIdCustomDimensionId";
        String EVENT_NAME_CUSTOM_DIMENSION_ID = "eventNameCustomDimensionId";
        String VISIT_CUSTOM_DIMENSIONS_START_ID = "visitCustomDimensionsStartId";
        String MAX_VISIT_CUSTOM_DIMENSIONS = "maxVisitCustomDimensions";
        String ACTION_CUSTOM_DIMENSIONS_START_ID = "actionCustomDimensionsStartId";
        String MAX_ACTION_CUSTOM_DIMENSIONS = "maxActionCustomDimensions";
    }
}
