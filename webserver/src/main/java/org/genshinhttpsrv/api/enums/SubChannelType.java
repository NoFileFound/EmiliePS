package org.genshinhttpsrv.api.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;

public enum SubChannelType {
    SUB_CHANNEL_UNKNOWN(-1),
    SUB_CHANNEL_DEFAULT(0),
    SUB_CHANNEL_OFFICIAL(1),
    SUB_CHANNEL_TAPTAP(2),
    SUB_CHANNEL_EPIC(3),
    SUB_CHANNEL_SAMSUNG(4),
    SUB_CHANNEL_STEAM(5),
    SUB_CHANNEL_GOOGLE(6),
    SUB_CHANNEL_MAX(7);

    private final int value;
    SubChannelType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}