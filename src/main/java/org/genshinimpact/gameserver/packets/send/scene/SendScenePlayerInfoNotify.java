package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.world.World;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.ScenePlayerInfoNotifyOuterClass.ScenePlayerInfoNotify;

public class SendScenePlayerInfoNotify implements SendPacket {
    private final byte[] data;

    public SendScenePlayerInfoNotify(World world) {
        var proto = ScenePlayerInfoNotify.newBuilder();
        for(int i = 0; i < world.getPlayers().size(); i++) {
            Player player = world.getPlayers().get(i);
            proto.addPlayerInfoList(
                ScenePlayerInfoNotify.ScenePlayerInfo.newBuilder()
                    .setUid(player.getPlayerIdentity().getId().intValue())
                    .setPeerId(player.getPeerId())
                    .setName(player.getPlayerIdentity().getUsername())
                    .setSceneId(player.getScene().getSceneId())
                    /// TODO: .setOnlinePlayerInfo(p.getOnlinePlayerInfo())
                    .build()
            );
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.ScenePlayerInfoNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}