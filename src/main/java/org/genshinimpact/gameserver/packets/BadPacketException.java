package org.genshinimpact.gameserver.packets;

public class BadPacketException extends Exception {
    public BadPacketException(String message) {
        super(message);
    }
}