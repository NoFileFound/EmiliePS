package org.genshinimpact.webserver.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

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

    @JsonValue @Getter private final int value;
    private static final Map<Integer, ClientType> MAP = Arrays.stream(values()).collect(Collectors.toMap(ClientType::getValue, e -> e));
    ClientType(int value) {
        this.value = value;
    }

    /**
     * Returns The ClientType corresponding to the given string value.
     *
     * @param value The string representation of the channel value.
     * @return The matching ClientType, or PLATFORM_UNKNOWN if not found or invalid.
     */
    public static ClientType fromValue(String value) {
        try {
            return MAP.getOrDefault(Integer.parseInt(value), PLATFORM_UNKNOWN);
        } catch (NumberFormatException e) {
            return PLATFORM_UNKNOWN;
        }
    }
}