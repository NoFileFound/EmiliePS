package org.emilieps.data.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;

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

    private final int value;
    ChannelType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}