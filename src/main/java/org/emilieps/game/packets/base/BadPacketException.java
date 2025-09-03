package org.emilieps.game.packets.base;

public class BadPacketException extends Exception {
    public BadPacketException(String message) {
        super(message);
    }
}