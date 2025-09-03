package org.emilieps.config.webserver.converters;

// Imports
import org.emilieps.data.enums.AccountType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class ParamToAccountTypeConverter implements Converter<String, AccountType> {
    /**
     * Converts a string to account type.
     * @param source The given numeric string.
     * @return An account type if exist or else ACCOUNT_UNKNOWN.
     */
    @Override
    public AccountType convert(@NotNull String source) {
        try {
            int code = Integer.parseInt(source);
            for (AccountType ct : AccountType.values()) {
                if (ct.getValue() == code) return ct;
            }
        } catch (NumberFormatException ignored) {

        }
        return AccountType.ACCOUNT_UNKNOWN;
    }
}