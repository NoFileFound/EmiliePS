package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SyncScenePlayTeamEntityNotifyOuterClass.SyncScenePlayTeamEntityNotify;

public class SendSyncScenePlayTeamEntityNotify implements SendPacket {
    private final byte[] data;

    public SendSyncScenePlayTeamEntityNotify(int sceneId) {
        var proto = SyncScenePlayTeamEntityNotify.newBuilder().setSceneId(sceneId).build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SyncScenePlayTeamEntityNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}

///  todo: finish
