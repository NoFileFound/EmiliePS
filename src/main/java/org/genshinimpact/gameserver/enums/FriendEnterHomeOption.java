package org.genshinimpact.gameserver.enums;

public enum FriendEnterHomeOption {
    FRIEND_ENTER_HOME_OPTION_NEED_CONFIRM,
    FRIEND_ENTER_HOME_OPTION_REFUSE,
    FRIEND_ENTER_HOME_OPTION_DIRECT;

    public int getValue() {
        return this.ordinal();
    }
}