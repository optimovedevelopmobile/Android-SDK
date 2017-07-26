package com.optimove.optimove_sdk.main;

public class InitToken {

    private String tenantToken;
    private String configVersion;

    public InitToken(String tenantToken, String configVersion) {
        this.tenantToken = tenantToken;
        this.configVersion = configVersion;
    }

    String getTenantToken() {
        return tenantToken;
    }

    String getConfigVersion() {
        return configVersion;
    }
}
