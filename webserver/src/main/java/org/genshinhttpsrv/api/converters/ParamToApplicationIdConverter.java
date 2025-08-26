package org.genshinhttpsrv.api.converters;

// Imports
import org.genshinhttpsrv.api.enums.ApplicationId;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class ParamToApplicationIdConverter implements Converter<String, ApplicationId> {
    /**
     * Converts a string to application id.
     * @param source The given numeric string.
     * @return An application id if exist or else APP_UNKNOWN.
     */
    @Override
    public ApplicationId convert(@NotNull String source) {
        try {
            int code = Integer.parseInt(source);
            for (ApplicationId ct : ApplicationId.values()) {
                if (ct.getValue() == code) return ct;
            }
        } catch (NumberFormatException ignored) {

        }
        return ApplicationId.APP_UNKNOWN;
    }
}