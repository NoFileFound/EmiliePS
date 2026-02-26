package org.genshinimpact.webserver.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ClientApiActionType {
    CLIENT_API_ACTION_TYPE_UNKNOWN(""),
    CLIENT_API_ACTION_TYPE_LOGIN("login"),
    CLIENT_API_ACTION_TYPE_DEVICE_GRANT("device_grant"),
    CLIENT_API_ACTION_TYPE_BIND_EMAIL("bind_email"),
    CLIENT_API_ACTION_TYPE_BIND_REALNAME("bind_realname"),
    CLIENT_API_ACTION_TYPE_MODIFY_REALNAME("modify_realname"),
    CLIENT_API_ACTION_TYPE_BIND_MOBILE("bind_mobile");

    @JsonValue
    @Getter
    private final String value;
    private static final Map<String, ClientApiActionType> MAP = Arrays.stream(values()).collect(Collectors.toMap(ClientApiActionType::getValue, e -> e));
    ClientApiActionType(String value) {
        this.value = value;
    }

    /**
     * Returns the ClientApiActionType corresponding to the given string value.
     *
     * @param value The string representation of the channel value.
     * @return The matching ClientApiActionType, or CLIENT_API_ACTION_TYPE_UNKNOWN if not found or invalid.
     */
    public static ClientApiActionType fromValue(String value) {
        if(value == null || value.isEmpty()) {
            return CLIENT_API_ACTION_TYPE_UNKNOWN;
        }

        try {
            return MAP.getOrDefault(value, CLIENT_API_ACTION_TYPE_UNKNOWN);
        } catch (NumberFormatException ignored) {
            return CLIENT_API_ACTION_TYPE_UNKNOWN;
        }
    }
}