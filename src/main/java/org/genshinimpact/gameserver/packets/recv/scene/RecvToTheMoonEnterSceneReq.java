package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.player.PlayerAntiCheat;
import org.genshinimpact.gameserver.game.world.SceneLoadState;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.scene.SendToTheMoonEnterSceneRsp;

// Protocol buffers
import org.generated.protobuf.ToTheMoonEnterSceneReqOuterClass.ToTheMoonEnterSceneReq;

public final class RecvToTheMoonEnterSceneReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = ToTheMoonEnterSceneReq.parseFrom(data);
        if(req.getVersion() != PlayerAntiCheat.antiCheatVersion || !player.getAntiCheatInfo().checkToTheMoonEnterScene(req.getSceneId())) {
            player.sendPacket(new SendToTheMoonEnterSceneRsp(Retcode.RET_TOTHEMOON_PLAYER_NOT_EXIST));
            return;
        }

        if(player.getSceneLoadState() != SceneLoadState.LOADING) {
            player.sendPacket(new SendToTheMoonEnterSceneRsp(Retcode.RET_TOTHEMOON_ERROR_SCENE));
            return;
        }

        player.getAntiCheatInfo().setACStatus(PlayerAntiCheat.AntiCheatStatus.PASSED_TO_THE_MOON_ENTER_SCENE);
        player.sendPacket(new SendToTheMoonEnterSceneRsp(Retcode.RET_SUCC));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.ToTheMoonEnterSceneReq;
    }
}