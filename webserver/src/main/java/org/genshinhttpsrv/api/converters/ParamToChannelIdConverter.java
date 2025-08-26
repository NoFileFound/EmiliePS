package org.genshinhttpsrv.api.converters;

// Imports
import org.genshinhttpsrv.api.enums.ChannelType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class ParamToChannelIdConverter implements Converter<String, ChannelType> {
    /**
     * Converts a string to channel id.
     * @param source The given numeric string.
     * @return A channel id if exist or else CHANNEL_UNKNOWN.
     */
    @Override
    public ChannelType convert(@NotNull String source) {
        try {
            int code = Integer.parseInt(source);
            for (ChannelType ct : ChannelType.values()) {
                if (ct.getValue() == code) return ct;
            }
        } catch (NumberFormatException ignored) {

        }
        return ChannelType.CHANNEL_UNKNOWN;
    }
}