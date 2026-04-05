package org.genshinimpact.gameserver.packets.send.team;

// Imports
import java.util.List;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.DelTeamEntityNotifyOuterClass.DelTeamEntityNotify;

public final class SendDelTeamEntityNotify implements SendPacket {
    private final byte[] data;

    public SendDelTeamEntityNotify(int sceneId, List<Integer> teamEntityIds) {
        var proto =
            DelTeamEntityNotify.newBuilder()
                .setSceneId(sceneId)
                .addAllDelEntityIdList(teamEntityIds)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.DelTeamEntityNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}