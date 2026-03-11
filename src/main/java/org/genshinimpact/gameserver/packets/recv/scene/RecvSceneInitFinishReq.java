package org.genshinimpact.gameserver.packets.recv.scene;

import com.google.protobuf.InvalidProtocolBufferException;
import org.generated.protobuf.SceneInitFinishReqOuterClass.SceneInitFinishReq;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.packets.InboundPacket;
import org.genshinimpact.gameserver.packets.PacketHandler;
import org.genshinimpact.gameserver.packets.send.SendSyncTeamEntityNotify;
import org.genshinimpact.gameserver.packets.send.player.SendHostPlayerNotify;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerGameTimeNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendEnterSceneDoneRsp;
import org.genshinimpact.gameserver.packets.send.scene.SendPlayerEnterSceneInfoNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendPostEnterSceneRsp;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneDataNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneInitFinishRsp;
import org.genshinimpact.gameserver.packets.send.scene.SendScenePlayerInfoNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneTeamUpdateNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneTimeNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSyncScenePlayTeamEntityNotify;
import org.genshinimpact.gameserver.packets.send.world.SendWorldDataNotify;
import org.genshinimpact.gameserver.packets.send.world.SendWorldPlayerInfoNotify;

public class RecvSceneInitFinishReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        SceneInitFinishReq req = SceneInitFinishReq.parseFrom(packet.getData());

        System.out.println("REACHED SceneInitFinishReq");
        session.sendPacket(new SendWorldDataNotify());
        session.sendPacket(new SendWorldPlayerInfoNotify());
        session.sendPacket(new SendScenePlayerInfoNotify());
        session.sendPacket(new SendPlayerEnterSceneInfoNotify(req.getEnterSceneToken()));
        session.sendPacket(new SendPlayerGameTimeNotify());
        session.sendPacket(new SendSceneTimeNotify());
        session.sendPacket(new SendSceneDataNotify());
        session.sendPacket(new SendHostPlayerNotify());
        session.sendPacket(new SendSceneTeamUpdateNotify());
        session.sendPacket(new SendSyncTeamEntityNotify());
        session.sendPacket(new SendSyncScenePlayTeamEntityNotify());
        session.sendPacket(new SendSceneInitFinishRsp(req.getEnterSceneToken()));
        session.sendPacket(new SendEnterSceneDoneRsp(req.getEnterSceneToken()));
    }

    @Override
    public int getCode() {
        return 235;
    }
}