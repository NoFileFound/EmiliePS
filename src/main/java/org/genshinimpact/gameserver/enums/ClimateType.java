package org.genshinimpact.gameserver.enums;

public enum ClimateType {
    NONE,
    SUNNY,
    CLOUDY,
    RAINY,
    STORMY,
    SNOW,
    MIST,
    DESERT;

    public int getValue() {
        return this.ordinal();
    }
}