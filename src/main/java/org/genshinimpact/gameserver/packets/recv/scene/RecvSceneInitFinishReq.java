package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.world.SceneLoadState;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.SendServerTimeNotify;
import org.genshinimpact.gameserver.packets.send.player.SendHostPlayerNotify;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerGameTimeNotify;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerTimeNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendPlayerEnterSceneInfoNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendPlayerWorldSceneInfoListNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneAreaWeatherNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneInitFinishRsp;
import org.genshinimpact.gameserver.packets.send.scene.SendScenePlayerInfoNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneTeamUpdateNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneTimeNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSyncScenePlayTeamEntityNotify;
import org.genshinimpact.gameserver.packets.send.team.SendSyncTeamEntityNotify;
import org.genshinimpact.gameserver.packets.send.world.SendWorldDataNotify;
import org.genshinimpact.gameserver.packets.send.world.SendWorldPlayerInfoNotify;

// Protocol buffers
import org.generated.protobuf.SceneInitFinishReqOuterClass.SceneInitFinishReq;

public final class RecvSceneInitFinishReq implements RecvPacket {
    @Override
    public void handle(Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var enterSceneToken = SceneInitFinishReq.parseFrom(data).getEnterSceneToken();
        if(enterSceneToken != player.getSceneEnterToken()) {
            player.sendPacket(new SendSceneInitFinishRsp(Retcode.RET_ENTER_SCENE_TOKEN_INVALID, enterSceneToken));
            return;
        }

        if(player.getSceneLoadState() != SceneLoadState.LOADING) {
            player.sendPacket(new SendSceneInitFinishRsp(Retcode.RET_ENTER_SCENE_FAIL, enterSceneToken));
            return;
        }

        player.sendPacket(new SendServerTimeNotify());
        player.sendPacket(new SendWorldPlayerInfoNotify(player.getWorld()));
        player.sendPacket(new SendWorldDataNotify(player.getWorld().getWorldLevel(), player.getWorld().isMultiplayer()));
        player.sendPacket(new SendPlayerWorldSceneInfoListNotify(player));
        player.sendPacket(new SendHostPlayerNotify(player.getWorld().getWorldHost().getAccount().getId(), player.getWorld().getWorldHost().getPeerId()));
        player.sendPacket(new SendSceneTimeNotify(player.getScene()));
        player.sendPacket(new SendPlayerGameTimeNotify(player.getAccount().getId(), player.getPlayerGameTime()));
        player.sendPacket(new SendPlayerEnterSceneInfoNotify(player, player.getSceneEnterToken()));
        player.sendPacket(new SendSceneAreaWeatherNotify(player));
        player.sendPacket(new SendScenePlayerInfoNotify(player.getWorld()));
        player.sendPacket(new SendSceneTeamUpdateNotify(player));
        player.sendPacket(new SendSyncTeamEntityNotify(player));
        player.sendPacket(new SendSyncScenePlayTeamEntityNotify(player));
        player.sendPacket(new SendPlayerTimeNotify(player));
        player.sendPacket(new SendSceneInitFinishRsp(Retcode.RET_SUCC, enterSceneToken));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SceneInitFinishReq;
    }
}