package org.emilieps.data.enums.webserver;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;

public enum CaptchaAction {
    ACTION_NONE(0),
    ACTION_GEETEST(1);

    private final int value;
    CaptchaAction(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}