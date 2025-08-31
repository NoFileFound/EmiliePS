package org.emilieps.data.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountType {
    ACCOUNT_UNKNOWN(-1),
    ACCOUNT_GUEST(0),
    ACCOUNT_NORMAL(1);

    private final int value;
    AccountType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}