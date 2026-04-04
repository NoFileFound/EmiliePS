package org.genshinimpact.gameserver.enums;

public enum LifeState {
    LIFE_NONE,
    LIFE_ALIVE,
    LIFE_DEAD,
    LIFE_REVIVE;

    public int getValue() {
        return this.ordinal();
    }
}