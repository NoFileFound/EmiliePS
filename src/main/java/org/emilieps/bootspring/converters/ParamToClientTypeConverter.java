package org.emilieps.bootspring.converters;

// Imports
import org.emilieps.data.enums.ClientType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class ParamToClientTypeConverter implements Converter<String, ClientType> {
    /**
     * Converts a string to client platform type.
     * @param source The given numeric string.
     * @return A client type if exist or else PLATFORM_UNKNOWN.
     */
    @Override
    public ClientType convert(@NotNull String source) {
        try {
            int code = Integer.parseInt(source);
            for (ClientType ct : ClientType.values()) {
                if (ct.getValue() == code) return ct;
            }
        } catch (NumberFormatException ignored) {

        }
        return ClientType.PLATFORM_UNKNOWN;
    }
}