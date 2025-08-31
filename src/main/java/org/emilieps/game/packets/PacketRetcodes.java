package org.emilieps.game.packets;

public enum PacketRetcodes {
    RETCODE_FAIL(-1),
    RETCODE_SUCC(0);

    private final int value;
    PacketRetcodes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}