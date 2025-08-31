package org.emilieps.bootspring.converters;

// Imports
import org.emilieps.data.enums.SubChannelType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class ParamToSubChannelIdConverter implements Converter<String, SubChannelType> {
    /**
     * Converts a string to client sub channel id.
     * @param source The given numeric string.
     * @return A sub channel id if exist or else SUB_CHANNEL_UNKNOWN.
     */
    @Override
    public SubChannelType convert(@NotNull String source) {
        try {
            int code = Integer.parseInt(source);
            for (SubChannelType ct : SubChannelType.values()) {
                if (ct.getValue() == code) return ct;
            }
        } catch (NumberFormatException ignored) {

        }
        return SubChannelType.SUB_CHANNEL_UNKNOWN;
    }
}