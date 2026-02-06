package org.genshinimpact.webserver.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

public enum ChannelType {
    CHANNEL_UNKNOWN(0),
    CHANNEL_DEFAULT(1),
    CHANNEL_XIAOMI(11),
    CHANNEL_COOLPAD(12),
    CHANNEL_YYB(13),
    CHANNEL_BILI(14),
    CHANNEL_HUAWEI(15),
    CHANNEL_MEIZU(16),
    CHANNEL_360(17),
    CHANNEL_OPPO(18),
    CHANNEL_VIVO(19),
    CHANNEL_UC(20),
    CHANNEL_LENOVO(22),
    CHANNEL_JINLI(23),
    CHANNEL_BAIDU(25),
    CHANNEL_DANGLE(26),
    CHANNEL_WEGAME(27);

    @JsonValue @Getter private final int value;
    private static final Map<Integer, ChannelType> MAP = Arrays.stream(values()).collect(Collectors.toMap(ChannelType::getValue, e -> e));
    ChannelType(int value) {
        this.value = value;
    }

    /**
     * Returns the ChannelType corresponding to the given string value.
     *
     * @param value The string representation of the channel value.
     * @return The matching ChannelType, or CHANNEL_UNKNOWN if not found or invalid.
     */
    public static ChannelType fromValue(String value) {
        if(value == null || value.isEmpty()) {
            return CHANNEL_UNKNOWN;
        }

        try {
            return MAP.getOrDefault(Integer.parseInt(value), CHANNEL_UNKNOWN);
        } catch (NumberFormatException ignored) {
            return CHANNEL_UNKNOWN;
        }
    }
}