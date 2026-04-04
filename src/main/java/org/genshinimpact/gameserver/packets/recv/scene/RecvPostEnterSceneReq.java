package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.world.SceneLoadState;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.scene.SendEnterSceneReadyRsp;
import org.genshinimpact.gameserver.packets.send.scene.SendPostEnterSceneRep;

// Protocol buffers
import org.generated.protobuf.PostEnterSceneReqOuterClass.PostEnterSceneReq;

public final class RecvPostEnterSceneReq implements RecvPacket {
    @Override
    public void handle(Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        int enterSceneToken = PostEnterSceneReq.parseFrom(data).getEnterSceneToken();
        if(enterSceneToken != player.getSceneEnterToken()) {
            player.sendPacket(new SendPostEnterSceneRep(Retcode.RET_ENTER_SCENE_TOKEN_INVALID, enterSceneToken));
            return;
        }

        if(player.getSceneLoadState() != SceneLoadState.LOADED) {
            player.sendPacket(new SendEnterSceneReadyRsp(Retcode.RET_ENTER_SCENE_FAIL, enterSceneToken));
            return;
        }

        ///  TODO: PacketGroupSuiteNotify
        ///  TODO: QUEST/ DUNGEON MANAGER.
        player.sendPacket(new SendPostEnterSceneRep(Retcode.RET_SUCC, enterSceneToken));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.PostEnterSceneReq;
    }
}