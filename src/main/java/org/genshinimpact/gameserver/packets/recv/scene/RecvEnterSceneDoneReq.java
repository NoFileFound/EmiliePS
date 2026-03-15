package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.send.scene.SendEnterSceneDoneRsp;

// Protocol buffers
import org.generated.protobuf.EnterSceneDoneReqOuterClass.EnterSceneDoneReq;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneEntityAppearNotify;

public class RecvEnterSceneDoneReq implements RecvPacket {
    @Override
    public void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        int enterSceneToken = EnterSceneDoneReq.parseFrom(data).getEnterSceneToken();
        var player = session.getPlayer();
        if(player == null) {
            session.sendPacket(new SendEnterSceneDoneRsp(Retcode.RET_ENTER_SCENE_FAIL, enterSceneToken));
            return;
        }

        if(enterSceneToken != session.getPlayer().getScene().getEnterSceneToken()) {
            session.sendPacket(new SendEnterSceneDoneRsp(Retcode.RET_ENTER_SCENE_TOKEN_INVALID, enterSceneToken));
            return;
        }

        var info = player.getPlayerIdentity().getTeamList().get(player.getPlayerIdentity().getCurrentTeamId());
        var info2 = player.getPlayerIdentity().getAvatars().get(info.getAvatars().get(0));
        session.sendPacket(new SendSceneEntityAppearNotify(info2.getAvatarId(), player.getPlayerIdentity().getId().intValue(), player.getPeerId(), info2.getGuid()));

        session.sendPacket(new SendEnterSceneDoneRsp(Retcode.RET_SUCC, enterSceneToken));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.EnterSceneDoneReq;
    }
}

/// todo: finish