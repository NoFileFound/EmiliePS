package org.emilieps.bootspring.converters;

// Imports
import org.emilieps.data.enums.RegionType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class ParamToRegionTypeConverter implements Converter<String, RegionType> {
    /**
     * Converts a string to region type.
     * @param source The given numeric string.
     * @return A region type if exist or else REGION_UNKNOWN.
     */
    @Override
    public RegionType convert(@NotNull String source) {
        return switch (source) {
            case "hk4e_cn" -> RegionType.REGION_CHINA;
            case "hk4e_global" -> RegionType.REGION_OVERSEAS;
            default -> RegionType.REGION_UNKNOWN;
        };
    }
}