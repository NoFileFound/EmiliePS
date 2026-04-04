package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.world.SceneLoadState;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.scene.SendEnterScenePeerNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendEnterSceneReadyRsp;

// Protocol buffers
import org.generated.protobuf.EnterSceneReadyReqOuterClass.EnterSceneReadyReq;

public final class RecvEnterSceneReadyReq implements RecvPacket {
    @Override
    public void handle(Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        int enterSceneToken = EnterSceneReadyReq.parseFrom(data).getEnterSceneToken();
        if(enterSceneToken != player.getSceneEnterToken()) {
            player.sendPacket(new SendEnterSceneReadyRsp(Retcode.RET_ENTER_SCENE_TOKEN_INVALID, enterSceneToken));
            return;
        }

        if(player.getSceneLoadState() != SceneLoadState.INIT) {
            player.sendPacket(new SendEnterSceneReadyRsp(Retcode.RET_ENTER_SCENE_FAIL, enterSceneToken));
            return;
        }

        player.setSceneLoadState(SceneLoadState.LOADING);
        player.sendPacket(new SendEnterScenePeerNotify(enterSceneToken, player.getSceneId(), player.getPeerId(), player.getWorld().getWorldHost().getPeerId()));
        player.sendPacket(new SendEnterSceneReadyRsp(Retcode.RET_SUCC, enterSceneToken));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.EnterSceneReadyReq;
    }
}