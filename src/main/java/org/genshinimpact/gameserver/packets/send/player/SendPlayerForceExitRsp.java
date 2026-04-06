package org.genshinimpact.gameserver.packets.send.player;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerForceExitRspOuterClass.PlayerForceExitRsp;

public final class SendPlayerForceExitRsp implements SendPacket {
    private final byte[] data;

    public SendPlayerForceExitRsp() {
        this.data = PlayerForceExitRsp.newBuilder().setRetcode(RET_SUCC.getValue()).build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerForceExitRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}