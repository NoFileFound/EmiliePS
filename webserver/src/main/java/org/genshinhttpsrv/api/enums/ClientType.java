package org.genshinhttpsrv.api.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClientType {
    PLATFORM_UNKNOWN(0),
    PLATFORM_IOS(1),
    PLATFORM_ANDROID(2),
    PLATFORM_PC(3),
    PLATFORM_WEB(4),
    PLATFORM_WAP(5),
    PLATFORM_PS4(6),
    PLATFORM_NINTENDO(7),
    PLATFORM_ANDROIDCLOUD(8),
    PLATFORM_PCCLOUD(9),
    PLATFORM_IOSCLOUD(10),
    PLATFORM_PS5(11),
    PLATFORM_MACOS(12),
    PLATFORM_MACOSCLOUD(13),
    PLATFORM_CX(26),
    PLATFORM_DOUYIN_IOSCLOUD(27),
    PLATFORM_DOUYIN_ANDROIDCLOUD(28),
    PLATFORM_HARMONYOSNEXT(50),
    PLATFORM_HOSTCLOUD(100);

    private final int value;
    ClientType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}