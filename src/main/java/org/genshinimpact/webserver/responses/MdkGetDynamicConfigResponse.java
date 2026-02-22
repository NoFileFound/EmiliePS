package org.genshinimpact.webserver.responses;

public class MdkGetDynamicConfigResponse {
    public Boolean enable_consent_banner;
    public String region_code;

    public MdkGetDynamicConfigResponse(Boolean enable_consent_banner, String region_code) {
        this.enable_consent_banner = enable_consent_banner;
        this.region_code = region_code;
    }
}