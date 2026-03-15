package org.genshinimpact.gameserver.packets.recv.scene;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.send.player.SendHostPlayerNotify;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerGameTimeNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendPlayerEnterSceneInfoNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneInitFinishRsp;
import org.genshinimpact.gameserver.packets.send.scene.SendScenePlayerInfoNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneTeamUpdateNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneTimeNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSyncScenePlayTeamEntityNotify;
import org.genshinimpact.gameserver.packets.send.team.SendSyncTeamEntityNotify;
import org.genshinimpact.gameserver.packets.send.world.SendWorldDataNotify;

// Protocol buffers
import org.generated.protobuf.SceneInitFinishReqOuterClass.SceneInitFinishReq;

public class RecvSceneInitFinishReq implements RecvPacket {
    @Override
    public void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var enterSceneToken = SceneInitFinishReq.parseFrom(data).getEnterSceneToken();
        var player = session.getPlayer();
        if(player == null) {
            session.sendPacket(new SendSceneInitFinishRsp(Retcode.RET_ENTER_SCENE_FAIL, enterSceneToken));
            return;
        }

        if(enterSceneToken != session.getPlayer().getScene().getEnterSceneToken()) {
            session.sendPacket(new SendSceneInitFinishRsp(Retcode.RET_ENTER_SCENE_TOKEN_INVALID, enterSceneToken));
            return;
        }

        //session.sendPacket(new SendServerTimeNotify());
        //session.sendPacket(new SendWorldPlayerInfoNotify(world));
        session.sendPacket(new SendWorldDataNotify(player.getWorld().getWorldLevel(), player.getWorld().getPlayers().size() > 1));
        //session.sendPacket(new SendPlayerWorldSceneInfoListNotify(player));
        // SceneForceUnlockNotify
        session.sendPacket(new SendHostPlayerNotify(player.getWorld().getWorldHost().getPlayerIdentity().getId(), player.getWorld().getWorldHost().getPeerId()));
        session.sendPacket(new SendSceneTimeNotify(player.getScene()));
        session.sendPacket(new SendPlayerGameTimeNotify(player.getPlayerIdentity().getId(), player.getPlayerGameTime()));
        session.sendPacket(new SendPlayerEnterSceneInfoNotify(player, enterSceneToken));
        //session.sendPacket(new SendSceneAreaWeatherNotify(player));
        session.sendPacket(new SendScenePlayerInfoNotify(player.getWorld()));
        session.sendPacket(new SendSceneTeamUpdateNotify(player));
        session.sendPacket(new SendSyncTeamEntityNotify(player));
        session.sendPacket(new SendSyncScenePlayTeamEntityNotify(player.getScene().getSceneId()));
        session.sendPacket(new SendSceneInitFinishRsp(Retcode.RET_SUCC, enterSceneToken));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SceneInitFinishReq;
    }
}


/// todo: finish