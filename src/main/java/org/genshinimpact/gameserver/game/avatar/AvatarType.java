package org.genshinimpact.gameserver.game.avatar;

public enum AvatarType {
    NORMAL,
    TRIAL;

    public int getValue() {
        return this.ordinal() + 1;
    }
}