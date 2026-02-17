package org.genshinimpact.webserver.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

public enum AppId {
    APP_UNKNOWN(0),
    APP_HONKAI3(1), // 崩坏3
    APP_TEARSOFTHEMIS(2), // 未定事件簿
    APP_GENSHIN(4), // 原神
    APP_PLATFORM(5), // 平台应用
    APP_HONKAI2(7), // 崩坏学园2
    APP_HONKAIRPG(8), // 崩坏:星穹铁道
    APP_CLOUDPLATFORM(9), // 云游戏
    APP_3NNN(10),
    APP_PJSH(11),
    APP_ZENLESS(12), // 绝区零
    APP_STARRYSKY(13), // 星布谷地
    APP_STARRAILSCLOUD(14), // 云星穹铁道
    APP_ZENLESSCLOUD(15), // 云·绝区零
    APP_HONKAINEXUS(16); // 崩坏：因缘精灵

    @JsonValue @Getter private final int value;
    private static final Map<Integer, AppId> MAP = Arrays.stream(values()).collect(Collectors.toMap(AppId::getValue, e -> e));
    AppId(int value) {
        this.value = value;
    }

    /**
     * Returns the AppId corresponding to the given string value.
     *
     * @param value The string representation of the application id value.
     * @return The matching AppId, or APP_UNKNOWN if not found or invalid.
     */
    public static AppId fromValue(String value) {
        if(value == null || value.isEmpty()) {
            return APP_UNKNOWN;
        }

        try {
            return MAP.getOrDefault(Integer.parseInt(value), APP_UNKNOWN);
        } catch (NumberFormatException ignored) {
            return APP_UNKNOWN;
        }
    }
}