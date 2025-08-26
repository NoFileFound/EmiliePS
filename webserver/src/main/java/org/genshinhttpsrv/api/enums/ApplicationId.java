package org.genshinhttpsrv.api.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationId {
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

    private final int value;
    ApplicationId(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}