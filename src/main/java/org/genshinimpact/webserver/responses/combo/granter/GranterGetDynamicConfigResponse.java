package org.genshinimpact.webserver.responses.combo.granter;

public class GranterGetDynamicConfigResponse {
    public Boolean enable_consent_banner;
    public String region_code;

    public GranterGetDynamicConfigResponse(Boolean enable_consent_banner, String region_code) {
        this.enable_consent_banner = enable_consent_banner;
        this.region_code = region_code;
    }
}