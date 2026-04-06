package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerEnterSceneNotifyOuterClass.PlayerEnterSceneNotify;

public final class SendPlayerEnterSceneNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerEnterSceneNotify(Player player) {
        var proto =
            PlayerEnterSceneNotify.newBuilder()
                .setEnterReason(PlayerEnterSceneNotify.EnterReason.ENTER_REASON_LOGIN)
                .setEnterSceneToken(player.getSceneEnterToken())
                .setIsFirstLoginEnterScene(player.isActive())
                .setIsSkipUi(false)
                .setPos(player.getAccount().getPlayerPosition().toProto())
                .setSceneBeginTime(System.currentTimeMillis())
                .setSceneId(player.getSceneId())
                .setSceneTransaction(String.format("3-%d-%d-%d", player.getAccount().getId().intValue(), (int)(System.currentTimeMillis() / 1000), 18402))
                .setTargetUid(player.getAccount().getId().intValue())
                .setType(PlayerEnterSceneNotify.EnterType.ENTER_SELF)
                .setWorldLevel(player.getWorld().getWorldLevel())
                .setWorldType(player.getWorld().getWorldType())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerEnterSceneNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}