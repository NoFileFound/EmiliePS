package org.genshinimpact.gameserver.enums;

// Imports
import lombok.Getter;

public enum EntityIdType {
    AVATAR(1),
    MONSTER(2),
    NPC(3),
    GADGET(4),
    REGION(5),
    WEAPON(6),
    TEAM(9),
    MPLEVEL(11);

    @Getter private final int value;
    EntityIdType(int value) {
        this.value = value;
    }
}