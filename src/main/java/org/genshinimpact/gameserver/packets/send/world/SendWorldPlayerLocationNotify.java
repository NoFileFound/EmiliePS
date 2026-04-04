package org.genshinimpact.gameserver.packets.send.world;

// Imports
import org.genshinimpact.gameserver.game.world.World;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.WorldPlayerLocationNotifyOuterClass.WorldPlayerLocationNotify;
import org.generated.protobuf.PlayerLocationInfoOuterClass.PlayerLocationInfo;

public final class SendWorldPlayerLocationNotify implements SendPacket {
    private final byte[] data;

    public SendWorldPlayerLocationNotify(World world) {
        var proto = WorldPlayerLocationNotify.newBuilder();
        for(var playerEntry : world.getPlayers()) {
            proto.addPlayerWorldLocList(WorldPlayerLocationNotify.PlayerWorldLocationInfo.newBuilder().setSceneId(playerEntry.getSceneId())
                .setPlayerLoc(PlayerLocationInfo.newBuilder()
                    .setUid(playerEntry.getAccount().getId().intValue())
                    .setPos(playerEntry.getAccount().getPlayerPosition().toProto())
                    .setRot(playerEntry.getAccount().getPlayerRotation().toProto())
                    .build()).build());
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.WorldPlayerLocationNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}