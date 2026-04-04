package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.world.SceneLoadState;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.scene.SendEnterSceneDoneRsp;

// Protocol buffers
import org.generated.protobuf.EnterSceneDoneReqOuterClass.EnterSceneDoneReq;

public final class RecvEnterSceneDoneReq implements RecvPacket {
    @Override
    public void handle(Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        int enterSceneToken = EnterSceneDoneReq.parseFrom(data).getEnterSceneToken();
        if(enterSceneToken != player.getSceneEnterToken()) {
            player.sendPacket(new SendEnterSceneDoneRsp(Retcode.RET_ENTER_SCENE_TOKEN_INVALID, enterSceneToken));
            return;
        }

        player.setSceneLoadState(SceneLoadState.LOADED);
        player.getScene().sendSceneEntities(player.getAccount().getPlayerTeam().getCurrentAvatarEntity());
        player.sendUpdateLocation();
        player.sendPacket(new SendEnterSceneDoneRsp(Retcode.RET_SUCC, enterSceneToken));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.EnterSceneDoneReq;
    }
}