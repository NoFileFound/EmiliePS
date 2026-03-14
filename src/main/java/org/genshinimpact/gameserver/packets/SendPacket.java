package org.genshinimpact.gameserver.packets;

public interface SendPacket {
    int getCode();
    byte[] getPacket();
}