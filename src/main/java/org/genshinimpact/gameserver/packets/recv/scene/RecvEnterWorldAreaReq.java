package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.scene.SendEnterWorldAreaRsp;

// Protocol buffers
import org.generated.protobuf.EnterWorldAreaReqOuterClass.EnterWorldAreaReq;

public final class RecvEnterWorldAreaReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = EnterWorldAreaReq.parseFrom(data);
        int areaId = req.getAreaId();
        int areaType = req.getAreaType();

        ///  TODO: RET_AREA_LOCKED
        ///  TODO: RET_INVALID_AREA_ID
        ///  TODO: RET_WEATHER_AREA_NOT_FOUND

        player.setAreaInfo(areaId, areaType);
        player.sendPacket(new SendEnterWorldAreaRsp(areaId, areaType));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.EnterWorldAreaReq;
    }
}