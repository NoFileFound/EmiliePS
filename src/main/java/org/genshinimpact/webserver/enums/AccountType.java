package org.genshinimpact.webserver.enums;

// Imports
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum AccountType {
    ACCOUNT_UNKNOWN(-1),
    ACCOUNT_GUEST(0),
    ACCOUNT_NORMAL(1),
    ACCOUNT_XIAOMI(11),
    ACCOUNT_COOLPAD(12),
    ACCOUNT_YYB(13),
    ACCOUNT_BILI(14),
    ACCOUNT_HUAWEI(15),
    ACCOUNT_MEIZU(16),
    ACCOUNT_360(170),
    ACCOUNT_OPPO(18),
    ACCOUNT_VIVO(19),
    ACCOUNT_UC (20),
    ACCOUNT_WANDOJIA(21),
    ACCOUNT_LENOVO(22),
    ACCOUNT_JINLI(23),
    ACCOUNT_BAIDU(25),
    ACCOUNT_DANGLE(26);

    @JsonValue @Getter private final int value;
    private static final Map<Integer, AccountType> MAP = Arrays.stream(values()).collect(Collectors.toMap(AccountType::getValue, e -> e));
    AccountType(int value) {
        this.value = value;
    }

    /**
     * Returns the AccountType corresponding to the given string value.
     *
     * @param value The string representation of the account type value.
     * @return The matching AccountType, or ACCOUNT_UNKNOWN if not found or invalid.
     */
    public static AccountType fromValue(String value) {
        if(value == null || value.isEmpty()) {
            return ACCOUNT_UNKNOWN;
        }

        try {
            return MAP.getOrDefault(Integer.parseInt(value), ACCOUNT_UNKNOWN);
        } catch (NumberFormatException ignored) {
            return ACCOUNT_UNKNOWN;
        }
    }
}