package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerEnterSceneNotifyOuterClass.PlayerEnterSceneNotify;

public class SendPlayerEnterSceneNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerEnterSceneNotify() {
        ///  TODO: FINISH
        this.data = new byte[0];
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerEnterSceneNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}