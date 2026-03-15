package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.send.scene.SendEnterScenePeerNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendEnterSceneReadyRsp;

// Protocol buffers
import org.generated.protobuf.EnterSceneReadyReqOuterClass.EnterSceneReadyReq;

public final class RecvEnterSceneReadyReq implements RecvPacket {
    @Override
    public void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        int enterSceneToken = EnterSceneReadyReq.parseFrom(data).getEnterSceneToken();
        var player = session.getPlayer();
        if(player == null) {
            session.sendPacket(new SendEnterSceneReadyRsp(Retcode.RET_ENTER_SCENE_FAIL, enterSceneToken));
            return;
        }

        if(enterSceneToken != session.getPlayer().getScene().getEnterSceneToken()) {
            session.sendPacket(new SendEnterSceneReadyRsp(Retcode.RET_ENTER_SCENE_TOKEN_INVALID, enterSceneToken));
            return;
        }

        session.sendPacket(new SendEnterScenePeerNotify(enterSceneToken, player.getScene().getSceneId(), player.getPeerId(), player.getWorld().getWorldHost().getPeerId()));
        session.sendPacket(new SendEnterSceneReadyRsp(Retcode.RET_SUCC, enterSceneToken));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.EnterSceneReadyReq;
    }
}