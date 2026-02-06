package org.genshinimpact.webserver.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@SuppressWarnings("unused")
public enum Retcode {
    RETCODE_SEND_PHONE_CODE_FREQUENTLY(-3101),
    RETCODE_NEED_GUARDIAN(-119),
    RETCODE_NEED_REALNAME(-118),
    RETCODE_REQUEST_FAILED(-106),
    RETCODE_PARAMETER_ERROR(-102),
    RETCODE_SYSTEM_ERROR(-101),
    RETCODE_FAIL(-1),
    RETCODE_SUCC(0),
    RETCODE_COMBO_INVALID_MODULE(4),
    RETCODE_COMBO_INVALID_KEY(6),
    RETCODE_COMBO_NO_CONFIG(7);

    @JsonValue @Getter private final int value;
    Retcode(int value) {
        this.value = value;
    }
}