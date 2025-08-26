package org.genshinhttpsrv.api.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;

public enum RegionType {
    REGION_UNKNOWN(""),
    REGION_CHINA("hk4e_cn"),
    REGION_OVERSEAS("hk4e_global"),
    REGION_DEV("takumi");

    private final String value;
    RegionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}