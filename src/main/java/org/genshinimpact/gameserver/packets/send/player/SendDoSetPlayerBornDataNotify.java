package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

public final class SendDoSetPlayerBornDataNotify implements SendPacket {
    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.DoSetPlayerBornDataNotify;
    }

    @Override
    public byte[] getPacket() {
        return new byte[0];
    }
}