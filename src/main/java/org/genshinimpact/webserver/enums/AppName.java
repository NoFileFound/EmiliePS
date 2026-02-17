package org.genshinimpact.webserver.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

public enum AppName {
    APP_UNKNOWN(""),
    APP_GENSHIN_OVERSEAS("hk4e_global"),
    APP_GENSHIN("hk4e_cn");

    @JsonValue @Getter private final String value;
    private static final Map<String, AppName> MAP = Arrays.stream(values()).collect(Collectors.toMap(AppName::getValue, e -> e));
    AppName(String value) {
        this.value = value;
    }

    /**
     * Returns the AppName corresponding to the given string value.
     *
     * @param value The string representation of the application name value.
     * @return The matching AppName, or APP_UNKNOWN if not found or invalid.
     */
    public static AppName fromValue(String value) {
        if(value == null || value.isEmpty()) {
            return APP_UNKNOWN;
        }

        try {
            return MAP.getOrDefault(value, APP_UNKNOWN);
        } catch (NumberFormatException ignored) {
            return APP_UNKNOWN;
        }
    }
}