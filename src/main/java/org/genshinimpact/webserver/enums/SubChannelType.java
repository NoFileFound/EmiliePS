package org.genshinimpact.webserver.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

public enum SubChannelType {
    SUB_CHANNEL_UNKNOWN(-1),
    SUB_CHANNEL_DEFAULT(0),
    SUB_CHANNEL_OFFICIAL(1),
    SUB_CHANNEL_TAPTAP(2),
    SUB_CHANNEL_EPICGAMES(3),
    SUB_CHANNEL_SAMSUNG(4),
    SUB_CHANNEL_STEAM(5),
    SUB_CHANNEL_GOOGLE(6);

    @JsonValue @Getter private final int value;
    private static final Map<Integer, SubChannelType> MAP = Arrays.stream(values()).collect(Collectors.toMap(SubChannelType::getValue, e -> e));
    SubChannelType(int value) {
        this.value = value;
    }

    /**
     * Returns the SubChannelType corresponding to the given string value.
     *
     * @param value The string representation of the channel value.
     * @return The matching SubChannelType, or SUB_CHANNEL_DEFAULT if not found or invalid.
     */
    public static SubChannelType fromValue(String value) {
        if(value == null || value.isEmpty()) {
            return SUB_CHANNEL_UNKNOWN;
        }

        try {
            return MAP.getOrDefault(Integer.parseInt(value), SUB_CHANNEL_UNKNOWN);
        } catch (NumberFormatException ignored) {
            return SUB_CHANNEL_UNKNOWN;
        }
    }
}