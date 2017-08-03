package com.optimove.sdk.optimove_sdk.main;

public class TenantToken {

    private int tenantId;
    private String token;
    private String configVersion;

    public TenantToken(int tenantId, String token, String configVersion) {
        this.tenantId = tenantId;
        this.token = token;
        this.configVersion = configVersion;
    }

    public int getTenantId() {
        return tenantId;
    }

    String getToken() {
        return token;
    }

    String getConfigVersion() {
        return configVersion;
    }
}
