package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerWorldSceneInfoListNotifyOuterClass.PlayerWorldSceneInfoListNotify;

public class SendPlayerWorldSceneInfoListNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerWorldSceneInfoListNotify(Player player) {
        var proto =
            PlayerWorldSceneInfoListNotify.newBuilder()
                .addInfoList(PlayerWorldSceneInfoListNotify.PlayerWorldSceneInfo.newBuilder().setSceneId(1).setIsLocked(false).build());

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerWorldSceneInfoListNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}

/// TODO: FINISH